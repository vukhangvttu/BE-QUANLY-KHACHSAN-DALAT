package vttu.khachsanservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.*;

import vttu.khachsanservice.dto.request.ChiTietHoaDonRequest;
import vttu.khachsanservice.dto.request.HoaDonRequest;
import vttu.khachsanservice.dto.request.HoaDon_DichVuMienPhiRequest;
import vttu.khachsanservice.dto.response.GiaPhongTheoNgayReponse;
import vttu.khachsanservice.dto.response.HoaDon_DichVuMienPhiResponse;
import vttu.khachsanservice.entity.*;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.ChiTietHoaDonMapper;
import vttu.khachsanservice.mapper.HoaDonMapper;
import vttu.khachsanservice.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HoaDonService {

    HoaDonRepository hoaDonRepository;
    HoaDonMapper hoaDonMapper;
    ChiTietHoaDonMapper chiTietHoaDonMapper;
    private final BooKingRepository booKingRepository;
    private final XepPhongRepository xepPhongRepository;
    private final PhieuDichVuRepository phieuDichVuRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final PhongService phongService;
    private final ObjectMapper objectMapper;
    private final HinhThucThanhToanRepository hinhThucThanhToanRepository;
    private final DichVuMienPhiRepository dichVuMienPhiRepository;
    private final ChiTietBooKingRepository chiTietBooKingRepository;
    private final GiaPhongTheoNgayRepository giaPhongTheoNgayRepository;


    @Transactional
    public boolean createHoaDon(String maBooKing, String maXepPhong, HoaDonRequest hoaDonRequest) {
        // 1. Kiểm tra và lấy dữ liệu từ database
        BooKing booKing = booKingRepository.findById(maBooKing)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND, maBooKing));

        XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(maXepPhong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND, maXepPhong));

        if (hoaDonRequest == null) {
            throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
        }

        // 2. Tạo hóa đơn từ request
        HoaDon hoaDon = hoaDonMapper.toCteateHoaDon(hoaDonRequest);
        hoaDon.setBooKing(booKing);

        // Tính số ngày thuê
        LocalDate ngayDen = xepPhongBooKing.getNgayDen();
        LocalDate ngayDi = hoaDonRequest.getNgayDi();

        if (ngayDen == null || ngayDi == null) {
            throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY, "Ngày đến hoặc ngày đi không được null");
        }

        long soDemThueLong = ChronoUnit.DAYS.between(ngayDen, ngayDi);
        if (soDemThueLong < 0) {
            throw new AppException(ErrorCode.ERROR_NGAYDI, "Ngày đi phải sau ngày đến");
        }

        int soDemThue = (soDemThueLong == 0) ? 1 : (int) soDemThueLong;

//        BigDecimal tienPhong = xepPhongBooKing.getGia().multiply(BigDecimal.valueOf(soDemThue));



        List<GiaPhongTheoNgay> giaPhongTheoNgayList =  giaPhongTheoNgayRepository.findByMaChiTietBooking(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());

        if(giaPhongTheoNgayList.isEmpty()) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
        }

        BigDecimal tienPhong = giaPhongTheoNgayList.stream()
                .map(GiaPhongTheoNgay::getGia)              // Lấy giá từng ngày
                .filter(Objects::nonNull)                   // Lọc giá trị null (phòng tránh NPE)
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // Cộng dồn


        // 3. Kiểm tra tổng tiền phòng
        if (tienPhong.compareTo(hoaDon.getTongTienPhong()) != 0) {
            throw new AppException(ErrorCode.ERROR_HOADON_VA_XEPPHONG_TIENPHONG);
        }

        // 4. Tính tổng tiền dịch vụ
        List<PhieuDichVu> phieuDichVu = phieuDichVuRepository.getPhieuDichVuByMaBooKing(maXepPhong);
        BigDecimal tongTienDV = phieuDichVu.stream()
                .map(pdv -> pdv.getGia().multiply(BigDecimal.valueOf(pdv.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Kiểm tra tổng tiền dịch vụ
        if (hoaDonRequest.getTongTienDichVu().compareTo(tongTienDV) != 0) {
            throw new AppException(ErrorCode.ERROR_HOADON_VA_PDV_TIENDV);
        }

        BigDecimal giamGia = hoaDonRequest.getGiamGia() != null ? hoaDonRequest.getGiamGia() : BigDecimal.ZERO;


        // 6. Kiểm tra tổng thanh toán
        BigDecimal tongThanhToan = tienPhong.add(tongTienDV).add(hoaDonRequest.getPhuThu()).subtract(giamGia);
        if (tongThanhToan.compareTo(hoaDonRequest.getTongThanhToan()) != 0) {
            throw new AppException(ErrorCode.ERROR_TONG_THANH_TOAN);
        }

        // 7. Kiểm tra chi tiết hóa đơn
        if (hoaDonRequest.getChiTietHoaDon() == null || hoaDonRequest.getChiTietHoaDon().isEmpty()) {
            throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
        }


        List<ChiTietHoaDon> danhSachChiTietHoaDon = new ArrayList<>();
        for (ChiTietHoaDonRequest chiTietHoaDonRequest : hoaDonRequest.getChiTietHoaDon()) {
            if (chiTietHoaDonRequest.getLoaiKhoanMuc() == null || chiTietHoaDonRequest.getLoaiKhoanMuc().isBlank()) {
                throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
            }
            if (chiTietHoaDonRequest.getDonGia() == null || chiTietHoaDonRequest.getDonGia().compareTo(BigDecimal.ZERO) == 0) {
                throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
            }

            BigDecimal thanhTien = BigDecimal.ZERO;
            // kiểm tra tổng tiền phụ thu ;
            String loaiKhoanMuc = chiTietHoaDonRequest.getLoaiKhoanMuc();
            if(loaiKhoanMuc.equals("TIEN_PHONG")) {
                thanhTien = chiTietHoaDonRequest.getDonGia();
                if(chiTietHoaDonRequest.getDonGia().compareTo(BigDecimal.ZERO) == 0) {
                    if(chiTietHoaDonRequest.getGhiChu().isEmpty())
                        throw new AppException(ErrorCode.GHI_CHU_EMPTY );
                }
            } else if(loaiKhoanMuc.equals("DICH_VU")) {
                thanhTien = chiTietHoaDonRequest.getDonGia().multiply(BigDecimal.valueOf(chiTietHoaDonRequest.getSoLuong()));
            }
            else {
                thanhTien = chiTietHoaDonRequest.getDonGia().multiply(BigDecimal.valueOf(soDemThue));
                if(chiTietHoaDonRequest.getGhiChu().isEmpty())
                    throw new AppException(ErrorCode.GHI_CHU_EMPTY );
            }


            if(thanhTien.compareTo(chiTietHoaDonRequest.getThanhTien()) != 0) {
                throw new AppException(ErrorCode.TONG_TIEN_CHI_TIET_KHONG_KHOP);
            }

            // Kiểm tra mã xếp phòng hợp lệ
            xepPhongRepository.findById(chiTietHoaDonRequest.getXepPhongBooKing().getMaXepPhongBooking())
                    .orElseThrow(() -> new AppException(ErrorCode.XEP_PHONG_IS_EMPTY));

            // Chuyển đổi và thêm vào danh sách
            ChiTietHoaDon chiTietHoaDon = chiTietHoaDonMapper.toCreateChiTietHoaDon(chiTietHoaDonRequest);
            chiTietHoaDon.setHoaDon(hoaDon);
            danhSachChiTietHoaDon.add(chiTietHoaDon);
        }

        // 26-05-2025 bổ sung thêm danh sách dịch vụ miễn phí


        var authentiaction = SecurityContextHolder.getContext().getAuthentication();
        // 8. Lưu dữ liệu vào database
        hoaDon.setNgayLap(LocalDateTime.now());
        hoaDon.setNguoiTao(authentiaction.getName());
        hoaDonRepository.save(hoaDon);
        chiTietHoaDonRepository.saveAll(danhSachChiTietHoaDon);

//        xepPhongRepository.updateDaTraPhong(true, LocalDateTime.now(), maXepPhong);
        xepPhongRepository.updateDaTraPhongAndThanhToan(true,true, LocalDateTime.now(), hoaDonRequest.getNgayDi(),maXepPhong, hoaDonRequest.getTongPhuThuCheckOutTre());

        phongService.updateTrangThaiVeSinh(true, xepPhongBooKing.getPhong().getMaPhong());

        // kiểm tra nếu tất cả xếp phòng của booking đã thành toán thì chuyển trạng thái booking sang đã thành toán
        boolean checkThanhToan = true;
        List<XepPhongBooKing> checkListThanhToan = xepPhongRepository.getXepPhongBooKings(maBooKing);

        for (XepPhongBooKing xps : checkListThanhToan) {
            if (!xps.isDaNhanPhong()) { // Nếu có một dòng isDaNhanPhong = false
                checkThanhToan = false; // Đặt checkThanhToan thành false
                break; // Thoát vòng lặp sớm để tối ưu
            }
        }

        if(checkThanhToan) {
            booKingRepository.updateTrangThaiBooKing(4,hoaDonRequest.getNgayDi(),maBooKing);
        }


        return true;
    }

    public List<ThongTinPhongDto> getAllThongTinBooKingByMaBooKing(String maBooking) {

        booKingRepository.findById(maBooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));


        List<ThongTinPhongDto> results = new ArrayList<>();

        List<Map<String, Object>> rawData = hoaDonRepository.getBookingDetails(maBooking);

        for (Map<String, Object> row : rawData) {
            try {
                // Chuyển đổi JSON string thành List<DichVuDto>
                List<DichVuDto> danhSachDichVu = objectMapper.readValue(
                        row.get("danh_sach_dich_vu").toString(),
                        new TypeReference<List<DichVuDto>>() {
                        }
                );

                List<PhuThuDto> danhSachPhuThu= objectMapper.readValue(
                        row.get("danh_sach_phu_thu").toString(),
                        new TypeReference<List<PhuThuDto>>() {
                        }
                );

                List<GiaPhongTheoNgayReponse> danhSachGiaPhongTheoNgay = objectMapper.readValue(
                        row.get("danh_sach_gia_phong_theo_ngay").toString(),
                        new TypeReference<List<GiaPhongTheoNgayReponse>>() {
                        }
                );

                List<XepPhongBooking_DichVuMienPhiDTO> dichVuMienPhiResponses = objectMapper.readValue(
                        row.get("danh_sach_dich_vu_mien_phi").toString(),
                        new TypeReference<List<XepPhongBooking_DichVuMienPhiDTO>>() {
                        }
                );

                // Tạo DTO
                ThongTinPhongDto phong = new ThongTinPhongDto();
                phong.setMaXepPhongBooking(row.get("ma_xep_phong_booking").toString());
                phong.setMaPhong(row.get("ma_phong").toString());
                phong.setTenLoaiPhong(row.get("ten_loai_phong").toString());
                phong.setGiaPhong((double) 0);
                phong.setNgayDen(row.get("ngay_den").toString());
                phong.setNgayDi(row.get("ngay_di").toString());
                phong.setSoNgayO(Integer.parseInt(row.get("so_ngay_o").toString()));
                phong.setDaThanhToan(Boolean.parseBoolean(row.get("da_Thanh_toan").toString()));

                String stringValue = row.get("phu_thu_tien_giuong").toString();
                BigDecimal decimalValue = new BigDecimal(stringValue);
                phong.setPhuThuTienGiuong(decimalValue);
                phong.setSoGiuong(Integer.parseInt(row.get("so_giuong").toString()));

                String stringValueTreEm = row.get("phu_thu_tre_em").toString();
                BigDecimal decimalValueTreEm = new BigDecimal(stringValueTreEm);
                phong.setPhThuTreEm(decimalValueTreEm);


                phong.setSoTre(Integer.parseInt(row.get("so_tre").toString()));

                stringValue = row.get("phu_thu_nguoi_lon").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setPhuThuNguoiLon(decimalValue);
                phong.setSoNguoiLon(Integer.parseInt(row.get("so_nguoi_lon").toString()));


                stringValue = row.get("phu_thu_check_in_som").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setPhuThuCheckinSom(decimalValue);


                phong.setDanhSachDichVu(danhSachDichVu);
                phong.setDanhSachPhuThu(danhSachPhuThu);
                phong.setDanhSachGiaPhongTheoNgay(danhSachGiaPhongTheoNgay);

                results.add(phong);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return results;


    }

    public Map<String,Object> getThongTinKhachHangBooking(String maBooking) {
        booKingRepository.findById(maBooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));
        return hoaDonRepository.getThongTinKhachHangBooking(maBooking);
    }

    public List<Map<String,Object>> getThongTinXuatPhieuDangKy(String mabooking) {
        booKingRepository.findById(mabooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));
        return hoaDonRepository.getThongTinXuatPhieuDangKy(mabooking);
    }

    public List<Map<String,Object>> getThongTinXuatPhieuDangKyOTA_TA(String mabooking) {
        booKingRepository.findById(mabooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));
        return hoaDonRepository.getThongTinXuatPhieuDangKyOTA_TA(mabooking);
    }

    @Transactional
    public boolean createHoaDonFromData(String ma_booking, HoaDonRequest hoaDonRequestNew) {
        // Xác thực yêu cầu
        if (hoaDonRequestNew == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (hoaDonRequestNew.getChiTietHoaDon() == null || hoaDonRequestNew.getChiTietHoaDon().isEmpty()) {
            throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
        }

        BooKing booKing = booKingRepository.findById(ma_booking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

        HinhThucThanhToan hinhThucThanhToan = hinhThucThanhToanRepository.findById(hoaDonRequestNew.getHinhThucThanhToan().getMaHinhThucThanhToan())
                .orElseThrow(() -> new AppException(ErrorCode.HINH_THUC_THAN_TOAN_NOT_FOUND));

        if(hoaDonRequestNew.getMaHinhThucThanhToanPhu() != 0){
            hinhThucThanhToanRepository.findById(hoaDonRequestNew.getMaHinhThucThanhToanPhu())
                    .orElseThrow(() -> new AppException(ErrorCode.HINH_THUC_THAN_TOAN_NOT_FOUND));

            if(hoaDonRequestNew.getGiaTriThanhToanPhu() == null || hoaDonRequestNew.getGiaTriThanhToanPhu().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.INVALID_GIA_TRI_THANH_TOAN_PHU);
            }

            if(hoaDonRequestNew.getMaHinhThucThanhToanPhu() == 7){
                    if(hoaDonRequestNew.getGhiChuThanhToanPhu().isEmpty())
                        throw new AppException(ErrorCode.GHI_CHU_EMPTY);
            }
        }

        // Xác thực từng chi tiết hóa đơn
        for (ChiTietHoaDonRequest cthdReq : hoaDonRequestNew.getChiTietHoaDon()) {
            if (cthdReq.getLoaiKhoanMuc() == null || cthdReq.getLoaiKhoanMuc().isBlank()) {
                throw new AppException(ErrorCode.INVALID_KHOAN_MUC);
            }
            if (cthdReq.getDonGia() == null || cthdReq.getDonGia().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.INVALID_DON_GIA);
            }
            if (cthdReq.getSoLuong() <= 0) {
                throw new AppException(ErrorCode.INVALID_SO_LUONG);
            }
            if (cthdReq.getThanhTien() == null || cthdReq.getThanhTien().compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.INVALID_THANH_TIEN);
            }
            if (cthdReq.getXepPhongBooKing() == null ||
                    cthdReq.getXepPhongBooKing().getMaXepPhongBooking() == null ||
                    cthdReq.getXepPhongBooKing().getMaXepPhongBooking().isBlank()) {
                throw new AppException(ErrorCode.XEP_PHONG_IS_EMPTY);
            }
        }

        // Trích xuất tất cả mã xếp phòng
        Set<String> maXepPhongBookings = hoaDonRequestNew.getChiTietHoaDon()
                .stream()
                .map(cthd -> cthd.getXepPhongBooKing().getMaXepPhongBooking())
                .collect(Collectors.toSet());

        // Kiểm tra xem tất cả mã xếp phòng có tồn tại
        for (String maXepPhongBooking : maXepPhongBookings) {
            if (!xepPhongRepository.existsById(maXepPhongBooking)) {
                throw new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND, maXepPhongBooking);
            }
        }

        // Lấy tất cả thông tin xếp phòng
        List<XepPhongBooKing> xepPhongBookings = xepPhongRepository.findAllById(maXepPhongBookings);

        // Kiểm tra xem tất cả phòng thuộc cùng một booking
        String maBooKing = xepPhongBookings.get(0).getBooKing().getMaBooking();
        for (XepPhongBooKing xpb : xepPhongBookings) {
            if (!xpb.getBooKing().getMaBooking().equals(maBooKing)) {
                throw new AppException(ErrorCode.INCONSISTENT_BOOKING_ID);
            }
        }

        // Kiểm tra tổng tiền phòng
        BigDecimal calculatedTongTienPhong = BigDecimal.ZERO;
        // Kiểm tra tổng tiền phụ thu khác giường hoặc trẻ em
        BigDecimal calculatedTongTienPhuThu = BigDecimal.ZERO;

        // KIEM TRA TONG TIEN PHU THU TRE EM KHI TAO BOOKING
        BigDecimal calculatedTongTienPhuThuTreEm = BigDecimal.ZERO;

        // KIEM TRA TONG TIEN PHU THU AN SANG KHI TAO BOOKING
        BigDecimal calculatedTongTienPhuThuAnSang = BigDecimal.ZERO;

        // Kiểm tra tổng phụ thu check in sớm
        BigDecimal calculatedTongTienPhuThuCheckInSom = hoaDonRequestNew.getTongPhuThuCheckInSom();

        // Kiểm tra tổng phụ thu check out muộn
        BigDecimal calculatedTongTienPhuThuCheckOutMuon = hoaDonRequestNew.getTongPhuThuCheckOutTre();

        List<Map<String,Object>> phuThuTreEmList = chiTietBooKingRepository.getDanhSachPhuThuTreEmThanhToan(ma_booking);
        if(phuThuTreEmList != null && !phuThuTreEmList.isEmpty()) {
            for (Map<String, Object> phuThuTreEm : phuThuTreEmList) {
                if (phuThuTreEm.get("thanhTienPhuThuTreEm") == null || phuThuTreEm.get("thanhTienPhuThuTreEm").toString().isEmpty()) {
                    throw new AppException(ErrorCode.INVALID_THANH_TIEN_PHU_THU);
                }
                BigDecimal phuThuTienTre = new BigDecimal(phuThuTreEm.get("thanhTienPhuThuTreEm").toString());
                BigDecimal phuThuAnSang = new BigDecimal(phuThuTreEm.get("thanhTienPhuThuAnSang").toString());

                calculatedTongTienPhuThuTreEm = calculatedTongTienPhuThuTreEm.add(phuThuTienTre);
                calculatedTongTienPhuThuAnSang = calculatedTongTienPhuThuAnSang.add(phuThuAnSang);

            }
        }
      calculatedTongTienPhuThu = calculatedTongTienPhuThu.add(calculatedTongTienPhuThuTreEm).add(calculatedTongTienPhuThuCheckInSom).add(calculatedTongTienPhuThuCheckOutMuon).add(calculatedTongTienPhuThuAnSang);


      // Tạo và lưu hóa đơn
        HoaDon hoaDon = new HoaDon();

        for (XepPhongBooKing xpb : xepPhongBookings) {


            XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(xpb.getMaXepPhongBooking())
                    .orElseThrow(() -> new AppException(ErrorCode.XEP_PHONG_IS_EMPTY));

            if(xepPhongBooKing.isDaThanhToan())
                continue;

                // Tính số ngày thuê cho từng phòng
                LocalDate ngayDen = xpb.getNgayDen();
                LocalDate ngayDi = hoaDonRequestNew.getNgayDi();

                if (ngayDen == null || ngayDi == null) {
                    throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY, "Ngày đến hoặc ngày đi không được null");
                }

                long soDemThueLong = ChronoUnit.DAYS.between(ngayDen, ngayDi);
                if (soDemThueLong < 0) {
                    throw new AppException(ErrorCode.ERROR_NGAYDI, "Ngày đi phải sau ngày đến");
                }

                int soDemThue = (soDemThueLong == 0) ? 1 : (int) soDemThueLong;
//            BigDecimal tienPhong = xpb.getGia().multiply(BigDecimal.valueOf(soDemThue));


                List<GiaPhongTheoNgay> giaPhongTheoNgayList = giaPhongTheoNgayRepository.findByMaChiTietBooking(xpb.getChiTietBooking().getMaChiTietBooking());

                if (giaPhongTheoNgayList.isEmpty()) {
                    throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
                }

                BigDecimal tienPhong = giaPhongTheoNgayList.stream()
                        .map(GiaPhongTheoNgay::getGia)              // Lấy giá từng ngày
                        .filter(Objects::nonNull)                   // Lọc giá trị null (phòng tránh NPE)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);  // Cộng dồn

                calculatedTongTienPhong = calculatedTongTienPhong.add(tienPhong);

                BigDecimal tienPhuThu = xpb.getPhuThuTienGiuong().multiply(BigDecimal.valueOf(xpb.getSoGiuong()))
                        .add(xpb.getPhuThuTienTre().multiply(BigDecimal.valueOf(xpb.getSoTre())))
                        .add(xpb.getPhuThuNguoiLon().multiply(BigDecimal.valueOf(xpb.getSoNguoiLon())));

                calculatedTongTienPhuThu = calculatedTongTienPhuThu.add(tienPhuThu.multiply(BigDecimal.valueOf(soDemThue)));


        }

        if (hoaDonRequestNew.getTongTienPhong().compareTo(calculatedTongTienPhong) != 0) {
            throw new AppException(ErrorCode.ERROR_HOADON_VA_XEPPHONG_TIENPHONG);
        }

        if(hoaDonRequestNew.getTongTienPhuThu().compareTo(calculatedTongTienPhuThu) != 0) {
            throw new AppException(ErrorCode.ERROR_HOADON_VA_XEPPHONG_TIENPHUTHU);
        }

        // Kiểm tra tổng tiền dịch vụ
        BigDecimal calculatedTongTienDichVu = BigDecimal.ZERO;
        for (XepPhongBooKing xpb : xepPhongBookings) {
            List<PhieuDichVu> phieuDichVu = phieuDichVuRepository.getPhieuDichVuByMaBooKing(xpb.getMaXepPhongBooking());
            BigDecimal tongTienDV = phieuDichVu.stream()
                    .map(pdv -> pdv.getGia().multiply(BigDecimal.valueOf(pdv.getSoLuong())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            calculatedTongTienDichVu = calculatedTongTienDichVu.add(tongTienDV);
        }
        if (hoaDonRequestNew.getTongTienDichVu().compareTo(calculatedTongTienDichVu) != 0) {
            throw new AppException(ErrorCode.ERROR_HOADON_VA_PDV_TIENDV);
        }


        // Kiểm tra tổng thanh toán
        BigDecimal giamGia = hoaDonRequestNew.getGiamGia() != null ? hoaDonRequestNew.getGiamGia() : BigDecimal.ZERO;
        BigDecimal phuThu = hoaDonRequestNew.getPhuThu() != null ? hoaDonRequestNew.getPhuThu() : BigDecimal.ZERO;
        BigDecimal calculatedTongThanhToan = calculatedTongTienPhong.add(calculatedTongTienDichVu).add(calculatedTongTienPhuThu).add(phuThu).subtract(giamGia).subtract(booKing.getTienCoc());
        if (hoaDonRequestNew.getTongThanhToan().compareTo(calculatedTongThanhToan) != 0)  {
            throw new AppException(ErrorCode.ERROR_TONG_THANH_TOAN);
        }
        var authentiaction = SecurityContextHolder.getContext().getAuthentication();

        hoaDon.setBooKing(booKing);
        hoaDon.setTongTienPhong(hoaDonRequestNew.getTongTienPhong());
        hoaDon.setTongTienDichVu(hoaDonRequestNew.getTongTienDichVu());
        hoaDon.setGiamGia(hoaDonRequestNew.getGiamGia());
        hoaDon.setPhuThu(hoaDonRequestNew.getPhuThu());
        hoaDon.setNgayDi(hoaDonRequestNew.getNgayDi());
        hoaDon.setTongThanhToan(hoaDonRequestNew.getTongThanhToan());
        hoaDon.setHinhThucThanhToan(hinhThucThanhToan);
        hoaDon.setNgayLap(LocalDateTime.now());
        hoaDon.setNguoiTao(authentiaction.getName());
        hoaDon.setGhiChu(hoaDonRequestNew.getGhiChu());
        hoaDon.setMaHinhThucThanhToanPhu(hoaDonRequestNew.getMaHinhThucThanhToanPhu());
        hoaDon.setGiaTriThanhToanPhu(hoaDonRequestNew.getGiaTriThanhToanPhu() == null ? BigDecimal.ZERO : hoaDonRequestNew.getGiaTriThanhToanPhu());
        hoaDon.setGhiChuThanhToanPhu(hoaDonRequestNew.getGhiChuThanhToanPhu() == null ? "" : hoaDonRequestNew.getGhiChuThanhToanPhu());
        hoaDon.setTongPhuThuCheckInSom(hoaDonRequestNew.getTongPhuThuCheckInSom() == null ? BigDecimal.ZERO : hoaDonRequestNew.getTongPhuThuCheckInSom());
        hoaDon.setTongPhuThuCheckOutTre(hoaDonRequestNew.getTongPhuThuCheckOutTre() == null ? BigDecimal.ZERO : hoaDonRequestNew.getTongPhuThuCheckOutTre());
        hoaDonRepository.save(hoaDon);

        // Tạo và lưu chi tiết hóa đơn
        List<ChiTietHoaDon> danhSachChiTietHoaDon = new ArrayList<>();
        for (ChiTietHoaDonRequest cthdReq : hoaDonRequestNew.getChiTietHoaDon()) {

            ChiTietHoaDon cthd = new ChiTietHoaDon();
            cthd.setLoaiKhoanMuc(cthdReq.getLoaiKhoanMuc());
            cthd.setMoTa(cthdReq.getMoTa());
            cthd.setSoLuong(cthdReq.getSoLuong());
            cthd.setDonGia(cthdReq.getDonGia());
            cthd.setThanhTien(cthdReq.getThanhTien());
            cthd.setDonViTinh(cthdReq.getDonViTinh());
            cthd.setGhiChu(cthdReq.getGhiChu());
            XepPhongBooKing xpb = xepPhongBookings.stream()
                    .filter(x -> x.getMaXepPhongBooking().equals(cthdReq.getXepPhongBooKing().getMaXepPhongBooking()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.XEP_PHONG_IS_EMPTY));

            if(xpb.isDaThanhToan())
                throw new AppException(ErrorCode.ERROR_HOA_DON_DA_THANH_TOAN);
            cthd.setXepPhongBooKing(xpb);
            cthd.setHoaDon(hoaDon);
            danhSachChiTietHoaDon.add(cthd);


            xepPhongBookings.stream()
                    .filter(b -> b.getMaXepPhongBooking().equals(xpb.getMaXepPhongBooking()))
                    .forEach(b -> {
                        if (cthdReq.getLoaiKhoanMuc().equals("PHU_THU_CHECK_IN_TRE")) {
                            b.setPhuThuCheckOutTre(cthdReq.getDonGia());
                        }else   b.setPhuThuCheckOutTre(BigDecimal.valueOf(0));
                    });


        }


        // Cập nhật trạng thái xếp phòng và thanh toán
        for (XepPhongBooKing xpb : xepPhongBookings) {
            xepPhongRepository.updateDaTraPhongAndThanhToan(true,true, LocalDateTime.now(), hoaDonRequestNew.getNgayDi() ,xpb.getMaXepPhongBooking(), xpb.getPhuThuCheckOutTre());
        }

        // Cập nhật trạng thái phòng cần dọn dẹp
        for (XepPhongBooKing xpb : xepPhongBookings) {
            phongService.updateTrangThaiVeSinh(true, xpb.getPhong().getMaPhong());

        }

        chiTietHoaDonRepository.saveAll(danhSachChiTietHoaDon);


        booKingRepository.updateTrangThaiBooKing(4,hoaDonRequestNew.getNgayDi(), ma_booking);
        return true;
    }

    public List<XemHoaDonThanhToanDTO> getThongTinHoaDonThanhToan(String maBooking) {

        booKingRepository.findById(maBooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));


        List<XemHoaDonThanhToanDTO> results = new ArrayList<>();

        List<Map<String, Object>> rawData = hoaDonRepository.getAllHoaDonByMaBooKing(maBooking);

        for (Map<String, Object> row : rawData) {
            try {
                // Chuyển đổi JSON string thành List<DichVuDto>
                List<DichVuDto> danhSachDichVu = objectMapper.readValue(
                        row.get("danh_sach_dich_vu").toString(),
                        new TypeReference<List<DichVuDto>>() {
                        }
                );

                List<PhuThuDto> danhSachPhuThu= objectMapper.readValue(
                        row.get("danh_sach_phu_thu").toString(),
                        new TypeReference<List<PhuThuDto>>() {
                        }
                );

                List<GiaPhongTheoNgayReponse> danhSachGiaPhongTheoNgay = objectMapper.readValue(
                        row.get("danh_sach_gia_phong_theo_ngay").toString(),
                        new TypeReference<List<GiaPhongTheoNgayReponse>>() {
                        }
                );


                // Tạo DTO
                XemHoaDonThanhToanDTO phong = new XemHoaDonThanhToanDTO();
                phong.setMaXepPhongBooking(row.get("ma_xep_phong_booking").toString());
                phong.setMaPhong(row.get("ma_phong").toString());
                phong.setTenLoaiPhong(row.get("ten_loai_phong").toString());
                phong.setGiaPhong((double) 0);
                phong.setNgayDen(row.get("ngay_den").toString());
                phong.setNgayDi(row.get("ngay_di").toString());
                phong.setSoNgayO(Integer.parseInt(row.get("so_ngay_o").toString()));
                phong.setDaThanhToan(Boolean.parseBoolean(row.get("da_Thanh_toan").toString()));

                String stringValue = row.get("phu_thu_tien_giuong").toString();
                BigDecimal decimalValue = new BigDecimal(stringValue);
                phong.setPhuThuTienGiuong(decimalValue);
                phong.setSoGiuong(Integer.parseInt(row.get("so_giuong").toString()));

                String stringValueTreEm = row.get("phu_thu_tre_em").toString();
                BigDecimal decimalValueTreEm = new BigDecimal(stringValueTreEm);
                phong.setPhThuTreEm(decimalValueTreEm);


                phong.setSoTre(Integer.parseInt(row.get("so_tre").toString()));

                stringValue = row.get("phu_thu_nguoi_lon").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setPhuThuNguoiLon(decimalValue);
                phong.setSoNguoiLon(Integer.parseInt(row.get("so_nguoi_lon").toString()));

                stringValue = row.get("phu_thu_check_in_som").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setPhuThuCheckInSom(decimalValue);

                stringValue = row.get("phu_thu_check_out_tre").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setPhuThuCheckOutTre(decimalValue);

                stringValue = row.get("tong_tien_phong_thanh_toan").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setTongTienPhong(decimalValue);

                stringValue = row.get("tong_tien_dich_vu_thanh_toan").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setTongTienDichVu(decimalValue);

                stringValue = row.get("tong_tien_phu_thu").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setTongTienPhuThu(decimalValue);

                stringValue = row.get("tong_tien_thanh_toan").toString();
                decimalValue = new BigDecimal(stringValue);
                phong.setTongTienThanhToan(decimalValue);

                stringValue = row.get("hinh_thuc_thanh_toan").toString();
                phong.setHinhThucThanhToan(stringValue);

                stringValue = row.get("ghi_chu").toString();
                phong.setGhiChu(stringValue);

                stringValue = row.get("ngay_lap").toString();
                if (stringValue.length() > 19) {
                    stringValue = stringValue.substring(0, 19); // "2025-05-29 15:15:46"
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime ngayLap = LocalDateTime.parse(stringValue, formatter);
                phong.setNgayLap(ngayLap);

                phong.setDanhSachDichVu(danhSachDichVu);
                phong.setDanhSachPhuThu(danhSachPhuThu);
                phong.setDanhSachGiaPhongTheoNgay(danhSachGiaPhongTheoNgay);

                results.add(phong);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return results;


    }

}
