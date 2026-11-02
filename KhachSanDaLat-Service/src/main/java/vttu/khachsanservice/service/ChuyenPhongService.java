package vttu.khachsanservice.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.request.GiaPhongTheoNgayRequest;
import vttu.khachsanservice.dto.request.Log_ChuyenPhongRequest;
import vttu.khachsanservice.entity.*;
import vttu.khachsanservice.entity.log.Log_ChuyenPhong;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.ChuyenPhongMapper;
import vttu.khachsanservice.repository.*;
import vttu.khachsanservice.repository.log.Log_ChuyenPhongRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
// =====================================================================
// ChuyenPhongService.java (ban fix)
// Cach dung: copy toan bo class nay de vao file cu.
// GIU NGUYEN: dong `package`, cac import entity/repository/service,
// constructor (hoac @RequiredArgsConstructor) cua file cu.
// File nay khong dung them type nao ngoai nhung gi file cu da import.
// =====================================================================

public class ChuyenPhongService {

    private final XepPhongRepository xepPhongRepository;
    private final PhongRepository phongRepository;
    private final Log_ChuyenPhongRepository log_ChuyenPhongRepository;
    private final GiaPhongTheoNgayRepository giaPhongTheoNgayRepository;
    private final DanhSachPhongChiTietRepository danhSachPhongChiTietRepository;
    private final ChiTietBooKingRepository chiTietBooKingRepository;
    private final LoaiPhongRepository loaiPhongRepository;
    private final LoaiGiaRepository loaiGiaRepository;

    // (giu nguyen constructor cua file cu)

    @Transactional
    public boolean saveChuyenPhong(String maXepPhong, String maPhongCu, String maPhongMoi,
                                   String maloaiphong, String maLoaiPhongMoi,
                                   List<GiaPhongTheoNgayRequest> requests) {

        XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(maXepPhong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
        phongRepository.findById(maPhongMoi)
                .orElseThrow(() -> new AppException(ErrorCode.MA_PHONG_NOT_FOUND));
        phongRepository.findById(maPhongCu)
                .orElseThrow(() -> new AppException(ErrorCode.MA_PHONG_NOT_FOUND));

        if (maPhongCu.equals(maPhongMoi))
            throw new AppException(ErrorCode.ERROR_PHONG_CU_PHONG_MOI_GIONG_NHAU);

        if (maloaiphong == null || maloaiphong.isEmpty()) {
            throw new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND);
        }

        if (requests == null || requests.isEmpty()) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_NOT_FOUND);
        }

        int kiemTraPhong = xepPhongRepository.KiemTraPhongDaTraChua(maPhongMoi);
        if (kiemTraPhong > 0)
            throw new AppException(ErrorCode.ERROR_PHONG_CHUA_TRA, "Phòng: " + maPhongMoi);

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        Log_ChuyenPhong logChuyenPhong = new Log_ChuyenPhong();
        logChuyenPhong.setMaXepPhong(maXepPhong);
        // FIX: lyDo luu ly do that, khong nhet ma loai phong vao
        logChuyenPhong.setLyDo("Chuyen phong (" + maloaiphong + " -> " + maLoaiPhongMoi + "): " + maPhongCu + " -> " + maPhongMoi);
        logChuyenPhong.setNoiDung("Chuyển từ phòng: " + maPhongCu + " sang phòng: " + maPhongMoi);
        logChuyenPhong.setUserName(authentication.getName());
        log_ChuyenPhongRepository.save(logChuyenPhong);

        phongRepository.updatePhongWithCurrentStatus(true, maPhongCu);

        LocalDate ngayHienTai = LocalDate.now();

        // FIX: dung dung ErrorCode LOAI_PHONG_NOT_FOUND khi tim loai phong that bai
        LoaiPhong loaiPhongCu = loaiPhongRepository.findById(maloaiphong)
                .orElseThrow(() -> new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND));
        LoaiPhong loaiPhongMoi = loaiPhongRepository.findById(maLoaiPhongMoi)
                .orElseThrow(() -> new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND));

        ChiTietBooking chiTietCu = chiTietBooKingRepository
                .getChiTietBookingByMaChiTietBooking(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());

        if (chiTietCu.getNgayDen().isEqual(ngayHienTai)) {
            // Khach vua nhan phong hom nay: doi phong + doi gia tren cung 1 dong
            chuyenPhongNgayNhan(chiTietCu, maPhongCu, maPhongMoi, maXepPhong,
                    loaiPhongMoi, maLoaiPhongMoi, requests);
        } else {
            // Chuyen giua ky: tach doan da o / doan con lai
            chuyenPhongGiuaKy(chiTietCu, xepPhongBooKing, maXepPhong, maPhongCu, maPhongMoi,
                    loaiPhongCu, loaiPhongMoi, maLoaiPhongMoi, requests, ngayHienTai, authentication);
        }

        return true;
    }

    /**
     * Truong hop ngayDen == hom nay: giu nguyen 1 dong chi tiet,
     * xoa gia cu theo ngay roi them gia moi cua phong moi.
     */
    private void chuyenPhongNgayNhan(ChiTietBooking chiTietCu,
                                     String maPhongCu, String maPhongMoi, String maXepPhong,
                                     LoaiPhong loaiPhongMoi, String maLoaiPhongMoi,
                                     List<GiaPhongTheoNgayRequest> requests) {

        chiTietCu.setLoaiPhong(loaiPhongMoi);

        // FIX: xoa gia cu truoc khi them gia moi - truoc day dong nay bi comment
        // nen gia bi trung ngay, tinh tien double
        List<GiaPhongTheoNgay> giaCu =
                giaPhongTheoNgayRepository.findByMaChiTietBooking(chiTietCu.getMaChiTietBooking());
        if (!giaCu.isEmpty()) {
            giaPhongTheoNgayRepository.deleteAll(giaCu);
        }

        List<GiaPhongTheoNgay> listGiaMoi = new ArrayList<>();
        BigDecimal tongGiaPhong = BigDecimal.ZERO;
        for (GiaPhongTheoNgayRequest req : requests) {
            GiaPhongTheoNgay giaPhong = new GiaPhongTheoNgay();
            giaPhong.setChiTietBooking(chiTietCu);
            // FIX: luu MA LOAI PHONG moi, khong phai ma phong
            giaPhong.setMaLoaiPhong(maLoaiPhongMoi);
            giaPhong.setNgay(req.getNgay());
            giaPhong.setGia(req.getGia());
            tongGiaPhong = tongGiaPhong.add(nz(req.getGia()));
            listGiaMoi.add(giaPhong);
        }
        giaPhongTheoNgayRepository.saveAll(listGiaMoi);

        // FIX: tinh lai tien theo gia phong moi (truoc day giu nguyen tien cu)
        chiTietCu.setGia(tongGiaPhong.doubleValue());
        chiTietCu.setTienPhong(tinhTongTienPhong(tongGiaPhong, chiTietCu, nzInt(chiTietCu.getSoDem())));
        chiTietBooKingRepository.save(chiTietCu);

        DanhSachPhongChiTiet dsCt = danhSachPhongChiTietRepository
                .findByChiTietBooking_MaChiTietBookingAndMaPhong(chiTietCu.getMaChiTietBooking(), maPhongCu);
        if (dsCt == null) {
            throw new AppException(ErrorCode.DANH_SACH_PHONG_CHI_TIET_NOT_FOUND, maPhongCu);
        }
        dsCt.setMaPhong(maPhongMoi);
        danhSachPhongChiTietRepository.save(dsCt);

        int updatePhong = xepPhongRepository.UpdatePhong(maPhongMoi, maXepPhong, true);
        if (updatePhong == 0) {
            throw new AppException(ErrorCode.UPDATE_DA_XEP_PHONG);
        }
    }

    /**
     * Truong hop chuyen giua ky (ngayDen < hom nay):
     * - Doan da o: [ngayDen, hom nay), o phong cu
     * - Doan con lai: [hom nay, ngayDi), o phong moi
     * Ngay hom nay chi thuoc ve doan moi -> khong bi tinh trung 2 lan.
     */
    private void chuyenPhongGiuaKy(ChiTietBooking chiTietCu, XepPhongBooKing xepPhongBooKing,
                                   String maXepPhong, String maPhongCu, String maPhongMoi,
                                   LoaiPhong loaiPhongCu, LoaiPhong loaiPhongMoi, String maLoaiPhongMoi,
                                   List<GiaPhongTheoNgayRequest> requests, LocalDate ngayHienTai,
                                   Authentication authentication) {

        LocalDate ngayDenGoc = chiTietCu.getNgayDen();
        LocalDate ngayDiCu = chiTietCu.getNgayDi();
        BooKing booKing = chiTietCu.getBooKing();

        DanhSachPhongChiTiet dsPhongCu = danhSachPhongChiTietRepository
                .findByChiTietBooking_MaChiTietBookingAndMaPhong(chiTietCu.getMaChiTietBooking(), maPhongCu);
        if (dsPhongCu == null) {
            throw new AppException(ErrorCode.DANH_SACH_PHONG_CHI_TIET_NOT_FOUND, maPhongCu);
        }

        ChiTietBooking doanDaO; // doan [ngayDenGoc, hom nay), o phong cu

        if (nzInt(chiTietCu.getSoLuong()) > 1) {
            // --- Dong chung dang gop nhieu phong: tach rieng maPhongCu ra ---
            // FIX: KHONG xoa gia tuong lai cua chiTietCu o day - no van la dong
            // cua cac phong con lai (full ky). Truoc day xoa lam mat gia cac phong kia.
            chiTietCu.setSoLuong(nzInt(chiTietCu.getSoLuong()) - 1);
            chiTietBooKingRepository.save(chiTietCu);

            danhSachPhongChiTietRepository.delete(dsPhongCu);

            doanDaO = cloneChiTietBooking(chiTietCu, loaiPhongCu);
            doanDaO.setNgayDen(ngayDenGoc);
            doanDaO.setNgayDi(ngayHienTai);
            doanDaO.setLoaiPhong(loaiPhongCu);
            doanDaO.setSoLuong(1);

            // Copy gia cac dem DA O (ngay < hom nay) sang dong moi
            List<GiaPhongTheoNgay> giaDaO = new ArrayList<>();
            BigDecimal tongGiaDaO = BigDecimal.ZERO;
            for (GiaPhongTheoNgay g :
                    giaPhongTheoNgayRepository.findByMaChiTietBooking(chiTietCu.getMaChiTietBooking())) {
                if (g.getNgay().isBefore(ngayHienTai)) {
                    GiaPhongTheoNgay copy = new GiaPhongTheoNgay();
                    copy.setChiTietBooking(doanDaO);
                    copy.setMaLoaiPhong(g.getMaLoaiPhong());
                    copy.setNgay(g.getNgay());
                    copy.setGia(g.getGia());
                    tongGiaDaO = tongGiaDaO.add(nz(g.getGia()));
                    giaDaO.add(copy);
                }
            }
            giaPhongTheoNgayRepository.saveAll(giaDaO);

            // FIX: tinh lai soDem/gia/tienPhong cho dung doan da o
            // (truoc day clone nguyen soDem/gia/tienPhong cua ca ky goc)
            int soDemDaO = (int) ChronoUnit.DAYS.between(ngayDenGoc, ngayHienTai);
            doanDaO.setSoDem(soDemDaO);
            doanDaO.setGia(tongGiaDaO.doubleValue());
            doanDaO.setTienPhong(tinhTongTienPhong(tongGiaDaO, doanDaO, soDemDaO));
            chiTietBooKingRepository.save(doanDaO);

            DanhSachPhongChiTiet dsDoanDaO = new DanhSachPhongChiTiet();
            dsDoanDaO.setMaPhong(maPhongCu);
            dsDoanDaO.setChiTietBooking(doanDaO);
            dsDoanDaO.setSoGiuongThem(dsPhongCu.getSoGiuongThem());
            danhSachPhongChiTietRepository.save(dsDoanDaO);

            updateXepPhongBooKing(maXepPhong, doanDaO, booKing, authentication);
        } else {
            // --- chiTietCu von chi dai dien dung 1 phong: cat ngan ve hom nay ---
            // FIX: chi xoa gia tu hom nay tro di; doan da o chi tinh cac dem < hom nay
            List<GiaPhongTheoNgay> giaCanXoa = new ArrayList<>();
            BigDecimal tongGiaDaO = BigDecimal.ZERO;
            for (GiaPhongTheoNgay g :
                    giaPhongTheoNgayRepository.findByMaChiTietBooking(chiTietCu.getMaChiTietBooking())) {
                if (!g.getNgay().isBefore(ngayHienTai)) {
                    giaCanXoa.add(g);
                } else {
                    // FIX: cong don dung cach (truoc day: giaPhong = tongTien.add(gia)
                    // voi tongTien luon = 0 nen chi lay duoc gia cua ngay cuoi)
                    tongGiaDaO = tongGiaDaO.add(nz(g.getGia()));
                }
            }
            if (!giaCanXoa.isEmpty()) {
                giaPhongTheoNgayRepository.deleteAll(giaCanXoa);
            }

            chiTietCu.setNgayDi(ngayHienTai);
            int soDemDaO = (int) ChronoUnit.DAYS.between(chiTietCu.getNgayDen(), ngayHienTai);
            chiTietCu.setSoDem(soDemDaO);
            chiTietCu.setGia(tongGiaDaO.doubleValue());
            // FIX: gan dung ket qua cong don (truoc day goi .add() ma khong gan
            // lai nen tienPhong luon = 0)
            chiTietCu.setTienPhong(tinhTongTienPhong(tongGiaDaO, chiTietCu, soDemDaO));
            chiTietBooKingRepository.save(chiTietCu);

            updateXepPhongBooKing(maXepPhong, chiTietCu, booKing, authentication);

            doanDaO = chiTietCu;
        }

        // --- Doan CON LAI: [hom nay, ngayDiCu), o phong MOI ---
        ChiTietBooking chiTietMoi = cloneChiTietBooking(doanDaO, loaiPhongMoi);
        chiTietMoi.setNgayDen(ngayHienTai);
        chiTietMoi.setNgayDi(ngayDiCu);
        chiTietMoi.setLoaiPhong(loaiPhongMoi);
        chiTietMoi.setSoLuong(1);
        // FIX: tinh lai soDem cho doan moi (truoc day clone nguyen soDem ca ky goc)
        int soDemMoi = (int) ChronoUnit.DAYS.between(ngayHienTai, ngayDiCu);
        chiTietMoi.setSoDem(soDemMoi);

        List<GiaPhongTheoNgay> listGiaMoi = new ArrayList<>();
        BigDecimal tongGiaMoi = BigDecimal.ZERO;
        for (GiaPhongTheoNgayRequest req : requests) {
            LocalDate ngay = req.getNgay();
            if (!ngay.isBefore(ngayHienTai)) {
                GiaPhongTheoNgay giaPhong = new GiaPhongTheoNgay();
                giaPhong.setChiTietBooking(chiTietMoi);
                // FIX: luu MA LOAI PHONG moi, khong phai ma phong
                giaPhong.setMaLoaiPhong(maLoaiPhongMoi);
                giaPhong.setNgay(ngay);
                giaPhong.setGia(req.getGia());
                tongGiaMoi = tongGiaMoi.add(nz(req.getGia()));
                listGiaMoi.add(giaPhong);
            }
        }
        giaPhongTheoNgayRepository.saveAll(listGiaMoi);

        chiTietMoi.setGia(tongGiaMoi.doubleValue());
        chiTietMoi.setTienPhong(tinhTongTienPhong(tongGiaMoi, chiTietMoi, soDemMoi));
        chiTietBooKingRepository.save(chiTietMoi);

        DanhSachPhongChiTiet dsMoi = new DanhSachPhongChiTiet();
        dsMoi.setMaPhong(maPhongMoi);
        dsMoi.setChiTietBooking(chiTietMoi);
        // FIX: copy so giuong them tu phong cu, khong hardcode 1
        dsMoi.setSoGiuongThem(dsPhongCu.getSoGiuongThem());
        danhSachPhongChiTietRepository.save(dsMoi);

        String stt = xepPhongBooKing.getStt();
        // FIX: chong crash khi stt null/rong
        String kyTuCuoi = (stt != null && !stt.isEmpty()) ? stt.substring(stt.length() - 1) : "";

        createSaveXepPhongBooKing(chiTietMoi, booKing, maPhongMoi, kyTuCuoi,
                nzInt(chiTietMoi.getSoLuongExtraBed()), nz(chiTietMoi.getGiaExtraBed()),
                authentication, listGiaMoi);
    }

    private ChiTietBooking cloneChiTietBooking(ChiTietBooking source) {
        return cloneChiTietBooking(source, source.getLoaiPhong());
    }

    private ChiTietBooking cloneChiTietBooking(ChiTietBooking source, LoaiPhong loaiPhongChoTraGia) {

        ChiTietBooking target = new ChiTietBooking();

        target.setNgayDen(source.getNgayDen());
        target.setNgayDi(source.getNgayDi());
        target.setGioDen(source.getGioDen());
        target.setGioDi(source.getGioDi());
        target.setSoDem(source.getSoDem());
        target.setSoLuong(source.getSoLuong());
        target.setSoLuongDuBaoPhong(source.getSoLuongDuBaoPhong());
        target.setNguoiLon(source.getNguoiLon());
        target.setTreEm(source.getTreEm());
        target.setGia(source.getGia());
        target.setSoLuongExtraBed(source.getSoLuongExtraBed());
        target.setGiaExtraBed(source.getGiaExtraBed());
        target.setSoLuongPhuThuTreEm(source.getSoLuongPhuThuTreEm());
        target.setGiaPhuThuTreEm(source.getGiaPhuThuTreEm());
        target.setSoLuongPhuThuAnSang(source.getSoLuongPhuThuAnSang());
        target.setGiaPhuThuAnSang(source.getGiaPhuThuAnSang());
        target.setTienPhong(source.getTienPhong());
        target.setGhiChu(source.getGhiChu());
        target.setLoaiPhong(source.getLoaiPhong()); // mac dinh giu loai cua source, caller override sau
        target.setBooKing(source.getBooKing());
        target.setGiaPhong(source.getGiaPhong());

        List<ChiTietBooking> chiTiet =
                chiTietBooKingRepository.getChiTietBookingByMaBooking(
                        source.getBooKing().getMaBooking()
                );

        int maxIndex = 0;
        for (ChiTietBooking chiTietBooking : chiTiet) {
            int stt = chiTietBooking.getIndex();
            if (stt > maxIndex) {
                maxIndex = stt;
            }
        }
        target.setIndex(maxIndex + 1);

        // FIX: tra LoaiGia theo loai phong DUOC CHI DINH (khong phai luc nao cung
        // loai cua source), lay phan tu DAU TIEN va co check size.
        // Truoc day: getLoaiGiaByMaLoaiPhong(source...) + get(1) khong check size
        // -> IndexOutOfBoundsException lam rollback ca transaction.
        if (loaiPhongChoTraGia != null) {
            List<LoaiGia> loaiGia = loaiGiaRepository
                    .getLoaiGiaByMaLoaiPhong(loaiPhongChoTraGia.getMaLoaiPhong());
            if (!loaiGia.isEmpty()) {
                LoaiGia loaiGiaRef = new LoaiGia();
                loaiGiaRef.setMaLoaiGia(loaiGia.get(0).getMaLoaiGia());
                target.setLoaiGia(loaiGiaRef);
            }
        }

        return chiTietBooKingRepository.save(target);
    }

    @Transactional
    public void updateXepPhongBooKing(String maXepPhong, ChiTietBooking chiTietBooking,
                                      BooKing booKing, Authentication authentication) {

        XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(maXepPhong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND,
                        "Không tìm thấy xếp phòng với mã: " + maXepPhong));
        xepPhongBooKing.setNgayDen(chiTietBooking.getNgayDen());
        xepPhongBooKing.setGioDen(chiTietBooking.getGioDen());
        // FIX: xoa khoi if/else chet - truoc day setNgayDi 2 lan
        xepPhongBooKing.setNgayDi(chiTietBooking.getNgayDi());
        xepPhongBooKing.setGia(nz(chiTietBooking.getTienPhong()));
        xepPhongBooKing.setDaTraPhong(true);
        xepPhongBooKing.setThoiGianTra(LocalDateTime.now());
        xepPhongBooKing.setChiTietBooking(chiTietBooking);
        xepPhongBooKing.setBooKing(booKing);
        xepPhongBooKing.setSoDem(nzInt(chiTietBooking.getSoDem()));
        // FIX: thanhTien = tong tien doan da o (da gom moi phu thu trong tienPhong),
        // khong cong them giuong nua de tranh double-count. Dong thoi xoa khoi
        // BigDecimal chet tinh ra roi bo khong dung.
        xepPhongBooKing.setThanhTien(nz(chiTietBooking.getTienPhong()));
        xepPhongBooKing.setNguoiTao(authentication.getName());

        xepPhongRepository.save(xepPhongBooKing);
    }

    public void createSaveXepPhongBooKing(ChiTietBooking chiTietBooking, BooKing booKing,
                                          String maPhong, String count, int soGiuong,
                                          BigDecimal giaExtraBed, Authentication authentication,
                                          List<GiaPhongTheoNgay> giaPhongTheoNgays) {

        XepPhongBooKing xepPhongBooKing = new XepPhongBooKing();
        xepPhongBooKing.setNgayDen(chiTietBooking.getNgayDen());
        xepPhongBooKing.setGioDen(chiTietBooking.getGioDen());
        xepPhongBooKing.setNgayDi(chiTietBooking.getNgayDi());
        // FIX: truoc day lay nham getGioDen() cho gio di
        xepPhongBooKing.setGioDi(chiTietBooking.getGioDi());
        xepPhongBooKing.setLoaiGia(chiTietBooking.getLoaiGia());
        xepPhongBooKing.setGia(nz(chiTietBooking.getTienPhong()));
        xepPhongBooKing.setStt(
                chiTietBooking.getLoaiPhong().getMaLoaiPhong() + "-" + count
        );
        xepPhongBooKing.setNgayXep(LocalDate.now());
        xepPhongBooKing.setGioXep(LocalTime.now());
        xepPhongBooKing.setPhuThuTienGiuong(nz(giaExtraBed));
        xepPhongBooKing.setSoGiuong(soGiuong);
        xepPhongBooKing.setPhuThuTienTre(BigDecimal.ZERO);
        xepPhongBooKing.setSoTre(0);
        xepPhongBooKing.setPhuThuNguoiLon(BigDecimal.ZERO);
        xepPhongBooKing.setSoNguoiLon(0);
        xepPhongBooKing.setChiTietBooking(chiTietBooking);
        xepPhongBooKing.setBooKing(booKing);
        xepPhongBooKing.setSoDem(nzInt(chiTietBooking.getSoDem()));
        // FIX: thanhTien = tong doan moi (da gom phu thu), thong nhat voi updateXepPhongBooKing
        xepPhongBooKing.setThanhTien(nz(chiTietBooking.getTienPhong()));
        xepPhongBooKing.setGhiChu(chiTietBooking.getGhiChu());
        xepPhongBooKing.setGiaPhong(chiTietBooking.getGiaPhong());
        xepPhongBooKing.setThoiGianTao(LocalDateTime.now());
        Phong phongAuto = new Phong();
        phongAuto.setMaPhong(maPhong);
        xepPhongBooKing.setPhong(phongAuto);
        xepPhongBooKing.setNguoiTao(authentication.getName());
        xepPhongBooKing.setPhuThuCheckInSom(BigDecimal.ZERO);
        // FIX: xoa dong setPhuThuCheckInSom trung lap
        xepPhongBooKing.setPhuThuNangHangPhong(BigDecimal.ZERO);
        xepPhongBooKing.setSoLuongNangHangPhong(0);

        xepPhongRepository.save(xepPhongBooKing);
    }

    // FIX: BigDecimal bat bien - moi phep .add() phai gan lai ket qua.
    // Ham nay tinh: tong gia phong + phu thu giuong + phu thu tre em + phu thu an sang.
    private BigDecimal tinhTongTienPhong(BigDecimal tongGiaPhong, ChiTietBooking ct, int soDem) {
        BigDecimal tong = nz(tongGiaPhong);
        tong = tong.add(nz(ct.getGiaExtraBed())
                .multiply(BigDecimal.valueOf((long) nzInt(ct.getSoLuongExtraBed()) * soDem)));
        tong = tong.add(nz(ct.getGiaPhuThuTreEm())
                .multiply(BigDecimal.valueOf((long) nzInt(ct.getSoLuongPhuThuTreEm()) * soDem)));
        // FIX: truoc day dung nham getSoLuongPhuThuTreEm() cho phu thu an sang
        tong = tong.add(nz(ct.getGiaPhuThuAnSang())
                .multiply(BigDecimal.valueOf((long) nzInt(ct.getSoLuongPhuThuAnSang()) * soDem)));
        return tong;
    }

    // FIX: chong NPE khi field tien/soluong null
    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private int nzInt(Integer v) {
        return v == null ? 0 : v;
    }
}

//public class ChuyenPhongService {
//
//    private final XepPhongRepository xepPhongRepository;
//    private final PhongRepository phongRepository;
//    private final Log_ChuyenPhongRepository log_ChuyenPhongRepository;
//    private final GiaPhongTheoNgayRepository giaPhongTheoNgayRepository;
//    private final KhachHangPhongRepository khachHangPhongRepository;
//    private final DanhSachPhongChiTietRepository danhSachPhongChiTietRepository;
//
//    @Transactional
//    public boolean saveChuyenPhong(String maXepPhong, String maPhongCu, String maPhongMoi, String maloaiphong, List<GiaPhongTheoNgayRequest> requests) {
//
//        XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(maXepPhong)
//                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
//        phongRepository.findById(maPhongMoi)
//                .orElseThrow(() -> new AppException(ErrorCode.MA_PHONG_NOT_FOUND));
//        phongRepository.findById(maPhongCu)
//                .orElseThrow(() -> new AppException(ErrorCode.MA_PHONG_NOT_FOUND));
//
//        if(maPhongCu.equals(maPhongMoi))
//            throw new AppException(ErrorCode.ERROR_PHONG_CU_PHONG_MOI_GIONG_NHAU);
//
//        if(maloaiphong == null || maloaiphong.equals("")) {
//            throw new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND);
//        }
//
//        if(requests == null || requests.isEmpty()) {
//            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_NOT_FOUND);
//        }
//
//        int KiemTraPhong = xepPhongRepository.KiemTraPhongDaTraChua(maPhongMoi);
//        if(KiemTraPhong > 0)
//            throw new AppException(ErrorCode.ERROR_PHONG_CHUA_TRA, "Phòng: " + maPhongMoi);
//
//
//        var authentiaction = SecurityContextHolder.getContext().getAuthentication();
//
//        Log_ChuyenPhong log_chuyenPhong = new Log_ChuyenPhong();
//        log_chuyenPhong.setMaXepPhong(maXepPhong);
//        log_chuyenPhong.setLyDo(maloaiphong);
//        log_chuyenPhong.setNoiDung("Chuyển từ phòng: " + maPhongCu + " sang phòng: " + maPhongMoi);
//        log_chuyenPhong.setUserName(authentiaction.getName());
//
//        log_ChuyenPhongRepository.save(log_chuyenPhong);
//
//        phongRepository.updatePhongWithCurrentStatus(true, maPhongCu);
//
//        //// xóa toàn bộ giá phòng theo ngày của chi tiết booking hiện tại
//        giaPhongTheoNgayRepository.xoaGiaPhongTheoNgay(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());
//
//
//        //// tạo mới giá phòng theo ngày cho chi tiết booking
//        List<GiaPhongTheoNgay> listGiaPhongTheoNgays = new ArrayList<>();
//
//        for (GiaPhongTheoNgayRequest giaPhongTheoNgay : requests) {
//
//            GiaPhongTheoNgay giaPhong = new GiaPhongTheoNgay();
//            giaPhong.setChiTietBooking(xepPhongBooKing.getChiTietBooking());
//            giaPhong.setMaLoaiPhong(maPhongMoi);
//            giaPhong.setNgay(giaPhongTheoNgay.getNgay());
//            giaPhong.setGia(giaPhongTheoNgay.getGia());
//            listGiaPhongTheoNgays.add(giaPhong);
//        }
//
//        giaPhongTheoNgayRepository.saveAll(listGiaPhongTheoNgays);
//
//        //// cap nhat lai ma phong trong danh sach xep phong chi tiet
//        DanhSachPhongChiTiet danhSachPhongChiTiet = danhSachPhongChiTietRepository.findByChiTietBooking_MaChiTietBookingAndMaPhong(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking(), maPhongCu);
//        if(danhSachPhongChiTiet == null) {
//            throw new AppException(ErrorCode.DANH_SACH_PHONG_CHI_TIET_NOT_FOUND, maPhongCu);
//        }
//        danhSachPhongChiTiet.setMaPhong(maPhongMoi);
//        danhSachPhongChiTietRepository.save(danhSachPhongChiTiet);
//
//        int updatePhong = xepPhongRepository.UpdatePhong(maPhongMoi,maXepPhong,true);
//        if (updatePhong == 0) {
//
//            throw new AppException(ErrorCode.UPDATE_DA_XEP_PHONG);
//        }
//
//        return true;
//    }
//}

