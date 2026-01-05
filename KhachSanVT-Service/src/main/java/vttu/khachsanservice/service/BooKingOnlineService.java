package vttu.khachsanservice.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.PhongTrongThoiGian;
import vttu.khachsanservice.dto.request.*;
import vttu.khachsanservice.dto.response.BooKingResponse;
import vttu.khachsanservice.entity.*;
import vttu.khachsanservice.entity.log.Log_DanhSachPhongChiTiet;
import vttu.khachsanservice.entity.log.Log_GiaPhongTheoNgay;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.*;
import vttu.khachsanservice.repository.*;
import vttu.khachsanservice.repository.log.Log_DanhSachPhongChiTietRepositoty;
import vttu.khachsanservice.repository.log.Log_GiaPhongTheoNgayRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BooKingOnlineService {

        KhachHangBooKingRepository khachHangBooKingRepository;
        KhachHangBooKingMapper khachHangBooKingMapper;
        ThongTinLienHeRepository thongTinLienHeRepository;
        BooKingRepository booKingRepository;
        BooKingOnlineMapper booKingMapper;
        ChiTietBooKingRepository chiTietBooKingRepository;
        ChiTietBooKingOnlineMapper chiTietBooKingMapper;
        KhachHangBooKingService khachHangBooKingService;

        private final LoaiGiaRepository loaiGiaRepository;
        private final LoaiPhongRepository loaiPhongRepository;
        private final XepPhongRepository xepPhongRepository;
        private final PhongRepository phongRepository;
        private final GiaPhongRepository giaPhongRepository;

        Log_GiaPhongTheoNgayRepository logGiaPhongTheoNgayRepository;
        private final Log_DanhSachPhongChiTietRepositoty log_DanhSachPhongChiTietRepositoty;

        @Transactional
        public BooKingResponse createBooKing(BooKingOnlineRequest request) {

            DanhXung danhXung = new DanhXung();
            danhXung.setMaDanhXung("Guest");

            NhomKhachHang nhomKhachHang = new NhomKhachHang();
            nhomKhachHang.setMaNhomKhachHang("NKH2026010301");

            KhuVuc khuVuc = new KhuVuc();
            khuVuc.setMaKhuVuc(5); // Không xác định

            ThiTruong thiTruong = new ThiTruong();
            thiTruong.setMaThiTruong(7);  //WEB ONLINE GOLDEN ERA

            HinhThucThanhToan hinhThucThanhToan = new HinhThucThanhToan();
            hinhThucThanhToan.setMaHinhThucThanhToan(5); //VNPAY

            MucDichDen mucDichDen = new MucDichDen();
            mucDichDen.setMaMucDich(5);  //Không xác định

            NguonKhach nguonKhach = new NguonKhach();
            nguonKhach.setMaNguonKhach(3); //Online

            GiamGia giamGia = new GiamGia();
            giamGia.setMaGiamGia(1); //NONE PROMOTION


            TrangThaiBooKing trangThaiBooKing = new TrangThaiBooKing();
            trangThaiBooKing.setMaTrangThaiBooKing(6); //Online

            LoaiThe loaiThe = new LoaiThe();
            loaiThe.setMaLoaiThe(4); // UNKNOWN



            // Lưu KhachHangBooKing
            KhachHangBooKing khachHangBooKing = khachHangBooKingMapper.toCreateKhachHangBooKing(request.getKhachHangBooKing());
            khachHangBooKing.setMaKhachHangBooking(khachHangBooKingService.generateNewId());
            khachHangBooKing.setNgayTao(LocalDateTime.now());
            khachHangBooKing = khachHangBooKingRepository.save(khachHangBooKing);

            // Lưu ThongTinLienHeBooKing
            ThongTinLienHeBooKing thongTinLienHeBooKing = new ThongTinLienHeBooKing();
            thongTinLienHeBooKing.setTenThongTinLienHeBooKing("");
            thongTinLienHeBooKing.setEmailThongTinLienHeBooKing("");
            thongTinLienHeBooKing.setSdtThongTinLienHeBooKing("");
            thongTinLienHeBooKing.setFaxThongTinLienHeBooKing("");
            thongTinLienHeBooKing.setTourCode("");

            thongTinLienHeBooKing = thongTinLienHeRepository.save(thongTinLienHeBooKing);


            // Tạo và lưu BooKing chính
            BooKing booKing = booKingMapper.toCreateBooKing(request);
            booKing.setKhachHangBooKing(khachHangBooKing);
            booKing.setThongTinLienHeBooKing(thongTinLienHeBooKing);

            booKing.setDanhXung(danhXung);
            booKing.setNhomKhachHang(nhomKhachHang);
            booKing.setKhuVuc(khuVuc);
            booKing.setThiTruong(thiTruong);
            booKing.setHinhThucThanhToan(hinhThucThanhToan);
            booKing.setMucDichDen(mucDichDen);
            booKing.setNguonKhach(nguonKhach);
            booKing.setGiamGia(giamGia);
            booKing.setTrangThaiBooKing(trangThaiBooKing);
            booKing.setLoaiThe(loaiThe);
            booKing.setGioDen(LocalTime.parse("14:00:00"));
            booKing.setGioDi(LocalTime.parse("12:00:00"));

            booKing.setNguoiTao("online");
            booKing.setThoiGianTao(LocalDateTime.now());
            booKing.setLoaiNguonKhach("");
            booKing = booKingRepository.save(booKing);
            // Kiểm tra giá trị sau khi lưu
            log.info("After saving BooKing - maBooking: {}, tongTien: {}",
                    booKing.getMaBooking(), booKing.getTongTien());

            if(request.getChiTietBooKings() == null || request.getChiTietBooKings().isEmpty()) {
                throw new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY);
            }

            // Xử lý ChiTietBooking

            BooKing finalBooKing = booKing;
            List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList = new ArrayList<>();

            List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgay = new ArrayList<>();
            List<ChiTietBooking> chiTietBookings = request.getChiTietBooKings().stream()
                    .map(chiTietBooKingRequest -> {

                        // Tính số ngày thuê
                        LocalDate ngayDen = chiTietBooKingRequest.getNgayDen();
                        LocalDate ngayDi = chiTietBooKingRequest.getNgayDi();

                        if(chiTietBooKingRequest.getIndex() == 0) {
                            throw new AppException(ErrorCode.INDEX_ERROR);
                        }

                        if (ngayDen == null || ngayDi == null) {
                            throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY, "Ngày đến hoặc ngày đi không được null");
                        }

                        long soDemThueLong = ChronoUnit.DAYS.between(ngayDen, ngayDi);
                        if (soDemThueLong < 0) {
                            throw new AppException(ErrorCode.ERROR_NGAYDI, "Ngày đi phải sau ngày đến");
                        }

                        ChiTietBooking chiTietBooking = chiTietBooKingMapper.toCreChiTietDatPhong(chiTietBooKingRequest);
                        chiTietBooking.setBooKing(finalBooKing);

                        BigDecimal giaMacDinh = BigDecimal.ZERO;
                        chiTietBooking.setGiaPhuThuTreEm(giaMacDinh);
                        chiTietBooking.setGiaExtraBed(giaMacDinh);
                        chiTietBooking.setGiaPhuThuAnSang(giaMacDinh);
                        chiTietBooking.setGia(0.0);
                        chiTietBooking.setGioDen(LocalTime.parse("14:00:00"));
                        chiTietBooking.setGioDi(LocalTime.parse("12:00:00"));

                        chiTietBooking.setLoaiPhong(findEntityOrThrow(
                                () -> loaiPhongRepository.findById(chiTietBooKingRequest.getLoaiPhong().getMaLoaiPhong()),
                                ErrorCode.LOAI_PHONG_NOT_FOUND,
                                chiTietBooKingRequest.getLoaiPhong().getMaLoaiPhong()
                        ));

                        chiTietBooking.setLoaiGia(findEntityOrThrow(
                                () -> loaiGiaRepository.findById(chiTietBooKingRequest.getLoaiGia().getMaLoaiGia()),
                                ErrorCode.LOAI_GIA_NOT_FOUND,
                                chiTietBooKingRequest.getLoaiGia().getMaLoaiGia()
                        ));


                        chiTietBooking.setGiaPhong(findEntityOrThrow(
                                () -> giaPhongRepository.findById(chiTietBooKingRequest.getGiaPhong().getMaGiaPhong()),
                                ErrorCode.GIA_PHONG_ERROR,
                                chiTietBooKingRequest.getGiaPhong().getMaGiaPhong()
                        ));


                        // 24-05-2025 bổ sung thêm danh sách gía từng theo ngày
                        if(chiTietBooKingRequest.getGiaPhongTheoNgays() == null || chiTietBooKingRequest.getGiaPhongTheoNgays().isEmpty()) {
                            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
                        }

                        BigDecimal tienPhongTheoNgay = BigDecimal.ZERO;

                        List<GiaPhongTheoNgay> giaPhongTheoNgays = new ArrayList<>();
                        for( GiaPhongTheoNgayRequest giaPhongTheoNgayRequest : chiTietBooKingRequest.getGiaPhongTheoNgays()) {
                            if(giaPhongTheoNgayRequest.getNgay() == null || giaPhongTheoNgayRequest.getGia() == null) {
                                throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
                            }

                            GiaPhongTheoNgay giaPhongTheoNgay = new GiaPhongTheoNgay();
                            giaPhongTheoNgay.setChiTietBooking(chiTietBooking);
                            giaPhongTheoNgay.setNgay(giaPhongTheoNgayRequest.getNgay());
                            giaPhongTheoNgay.setGia(giaPhongTheoNgayRequest.getGia());
                            giaPhongTheoNgay.setMaLoaiPhong(giaPhongTheoNgayRequest.getMaLoaiPhong());
                            giaPhongTheoNgay.setTrangThai("CREATED");
                            giaPhongTheoNgays.add(giaPhongTheoNgay);

                            // Lưu log giá phòng theo ngày
                            Log_GiaPhongTheoNgay log = new Log_GiaPhongTheoNgay();
                            log.setMaChiTietBooking(chiTietBooking.getMaChiTietBooking());

                            log.setNgay(giaPhongTheoNgayRequest.getNgay());
                            log.setGia(giaPhongTheoNgayRequest.getGia());
                            log.setMaLoaiPhong(giaPhongTheoNgayRequest.getMaLoaiPhong());
                            log.setThoiGianTao(LocalDateTime.now());
                            log.setTrangThai("CREATED");
                            logGiaPhongTheoNgay.add(log);

                            tienPhongTheoNgay = tienPhongTheoNgay.add(giaPhongTheoNgayRequest.getGia());

                        }

                        chiTietBooking.setGiaPhongTheoNgays(giaPhongTheoNgays);

                        return chiTietBooking;
                    })
                    .collect(Collectors.toList());


            // Kiểm tra số lượng phần tử trong danh sách sau khi xử lý
            if (request.getChiTietBooKings() == null || request.getChiTietBooKings().size() != chiTietBookings.size()) {
                throw new AppException(ErrorCode.SO_LUONG_BOOKING_KHONG_KHOP);
            }


            chiTietBooKingRepository.saveAll(chiTietBookings);

            // Lưu log giá phòng theo ngày
            logGiaPhongTheoNgayRepository.saveAll(logGiaPhongTheoNgay);

            // SAU KHI SAVE BOKING THANH CONG THI XEP PHONG TU DONG
            // Xếp phòng tự động
                int stt = 1;
                for (ChiTietBooking chiTietBooking : chiTietBookings) {

                    int soPhongCanXep = chiTietBooking.getSoLuong();


                    for (int i = 0; i < soPhongCanXep ; i++) {

                        List<PhongTrongThoiGian> phongTrongThoiGianList = phongRepository.getPhongTrongCurrent(
                                chiTietBooking.getLoaiPhong().getMaLoaiPhong(),
                                chiTietBooking.getNgayDen(),
                                chiTietBooking.getNgayDi()
                        );
                        if (phongTrongThoiGianList.isEmpty()) {
                            throw new AppException(ErrorCode.PHONG_KHONG_DU_TRONG, booKing.getMaBooking());
                        }

                        PhongTrongThoiGian phongDTO = phongTrongThoiGianList.getFirst();

                        createSaveXepPhongBooKing(
                                chiTietBooking,
                                booKing,
                                phongDTO.getMaPhong(),
                                stt,
                                0,
                                BigDecimal.ZERO,
                                chiTietBooking.getGiaPhongTheoNgays()

                        );
                        stt++;
                    }
                }

                booKingRepository.updateDa_xep_phong(true, booKing.getMaBooking());


            return booKingMapper.toBooKingResponse(booKing);
        }

        // Helper method để tránh lặp code
        private <T, ID> T findEntityOrThrow(Supplier<Optional<T>> repositorySupplier, ErrorCode errorCode, ID id) {
            return repositorySupplier.get()
                    .orElseThrow(() -> new AppException(errorCode, String.valueOf(id)));
        }

        // code phan tach chuc nang va gom gon dang loi phan xoa danhsách phòng chi tiết và xoa gia phòng theo ngày




        public void createSaveXepPhongBooKing(ChiTietBooking chiTietBooking, BooKing booKing,String maPhong, int count,int soGiuong, BigDecimal giaExtraBed ,List<GiaPhongTheoNgay> giaPhongTheoNgays) {


            XepPhongBooKing xepPhongBooKing = new XepPhongBooKing();
            xepPhongBooKing.setNgayDen(chiTietBooking.getNgayDen());
            xepPhongBooKing.setGioDen(chiTietBooking.getGioDen());
            xepPhongBooKing.setNgayDi(chiTietBooking.getNgayDi());
            xepPhongBooKing.setGioDi(chiTietBooking.getGioDen());
            xepPhongBooKing.setLoaiGia(chiTietBooking.getLoaiGia());
            xepPhongBooKing.setGia(chiTietBooking.getTienPhong());
            xepPhongBooKing.setStt(chiTietBooking.getLoaiPhong().getMaLoaiPhong() + '-' + (count));
            xepPhongBooKing.setNgayXep(LocalDate.now());
            xepPhongBooKing.setGioXep(LocalTime.now());
            xepPhongBooKing.setPhuThuTienGiuong(giaExtraBed);
            xepPhongBooKing.setSoGiuong(soGiuong);
            xepPhongBooKing.setPhuThuTienTre(BigDecimal.ZERO);
            xepPhongBooKing.setSoTre(0);
            xepPhongBooKing.setPhuThuNguoiLon(BigDecimal.ZERO);
            xepPhongBooKing.setSoNguoiLon(0);
            xepPhongBooKing.setChiTietBooking(chiTietBooking);
            xepPhongBooKing.setBooKing(booKing);
            xepPhongBooKing.setSoDem(chiTietBooking.getSoDem());
            xepPhongBooKing.setThanhTien(chiTietBooking.getTienPhong());
            xepPhongBooKing.setGhiChu(chiTietBooking.getGhiChu());
            xepPhongBooKing.setGiaPhong(chiTietBooking.getGiaPhong());
            xepPhongBooKing.setThoiGianTao(LocalDateTime.now());
            Phong phongAuto = new Phong();
            phongAuto.setMaPhong(maPhong);
            xepPhongBooKing.setPhong(phongAuto);
            xepPhongBooKing.setNguoiTao("online");
            xepPhongBooKing.setPhuThuCheckInSom(BigDecimal.ZERO);
            xepPhongBooKing.setPhuThuCheckInSom(BigDecimal.ZERO);


            xepPhongRepository.save(xepPhongBooKing);
        }

}
