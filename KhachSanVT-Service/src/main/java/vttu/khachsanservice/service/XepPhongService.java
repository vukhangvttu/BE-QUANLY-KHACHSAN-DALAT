package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import vttu.khachsanservice.dto.LoaiPhongTrongDTO;
import vttu.khachsanservice.dto.PhongDTO;
import vttu.khachsanservice.dto.PhongTrongThoiGian;
import vttu.khachsanservice.dto.request.GiaPhongTheoNgayRequest;
import vttu.khachsanservice.dto.request.XepPhongBooKingRequest;
import vttu.khachsanservice.dto.response.XepPhongBooKingResponse;
import vttu.khachsanservice.entity.*;
import vttu.khachsanservice.entity.log.Log_GiaPhongTheoNgay;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.XepPhongBooKingMapper;
import vttu.khachsanservice.repository.*;
import vttu.khachsanservice.repository.log.Log_GiaPhongTheoNgayRepository;
import vttu.khachsanservice.utils.DateConverter;
import vttu.khachsanservice.utils.VietnamHolidayUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class XepPhongService {

    XepPhongRepository xepPhongRepository;
    XepPhongBooKingMapper xepPhongBooKingMapper;
    private final PhongRepository phongRepository;
    private final BooKingRepository booKingRepository;
    private final LoaiGiaRepository loaiGiaRepository;

    private static final Logger logger = LoggerFactory.getLogger(XepPhongService.class);
    private final LoaiPhongRepository loaiPhongRepository;
    private final ChiTietBooKingRepository chiTietBooKingRepository;
    private final PhongService phongService;
    private final DanhSachPhongChiTietRepository danhSachPhongChiTietRepository;
    private final GiaPhongTheoNgayRepository giaPhongTheoNgayRepository;
    private final GiaPhongRepository giaPhongRepository;
    private final Log_GiaPhongTheoNgayRepository log_GiaPhongTheoNgayRepository;

    @Transactional
    public boolean createXepPhongBooKing(List<XepPhongBooKingRequest> request, String ma_booking) {
        if (request == null || request.isEmpty()) {
            throw new AppException(ErrorCode.XEP_PHONG_IS_EMPTY);
        }


        // Kiểm tra booking tồn tại trong DB
        BooKing booKing = booKingRepository.findById(ma_booking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

        // Kiểm tra trùng lặp maPhong trong request
        Set<String> maPhongSet = new HashSet<>();
        for (XepPhongBooKingRequest req : request) {
            String maPhong = req.getPhong().getMaPhong();
            if (!maPhongSet.add(maPhong)) {
                throw new AppException(ErrorCode.DUPLICATE_MA_PHONG, "Phòng " + maPhong + " bị trùng lặp trong request.");
            }
        }


        // Lấy tất cả mã phòng từ request để tìm một lần trong database
        List<String> maPhongs = request.stream()
                .map(r -> r.getPhong().getMaPhong())
                .collect(Collectors.toList());

        // Tìm tất cả các phòng trong database bằng một truy vấn duy nhất
        Map<String, Phong> phongMap = phongRepository.findAllById(maPhongs).stream()
                .collect(Collectors.toMap(Phong::getMaPhong, Function.identity()));


        List<Integer> maLoaiGias = request.stream()
                .map(r -> r.getLoaiGia().getMaLoaiGia())
                .collect(Collectors.toList());

        Map<Integer, LoaiGia> loaiGiaMap = loaiGiaRepository.findAllById(maLoaiGias).stream()
                .collect(Collectors.toMap(LoaiGia::getMaLoaiGia, Function.identity()));

        // Tạo danh sách XepPhongBooKing
        List<XepPhongBooKing> xepPhongBooKingList = request.stream()
                .map(xepPhongRequest -> {
                    Phong phong = phongMap.get(xepPhongRequest.getPhong().getMaPhong());
                    LoaiGia loaiGia = loaiGiaMap.get(xepPhongRequest.getLoaiGia().getMaLoaiGia());

                    int KiemTraPhong = xepPhongRepository.KiemTraPhongDaTraChua(xepPhongRequest.getPhong().getMaPhong());
//                    if(KiemTraPhong > 0)
//                        throw new AppException(ErrorCode.ERROR_PHONG_CHUA_TRA, "Phòng: " + xepPhongRequest.getPhong().getMaPhong());

                    List<PhongTrongThoiGian> kiemtraPhong = phongService.getDuBaoPhongTheoKhoanThoiGianAndBooKing(
                            xepPhongRequest.getLoaiPhong(),
                            xepPhongRequest.getNgayDen(),
                            xepPhongRequest.getNgayDi(),
                            ma_booking);

//                    if (!kiemtraPhong.isEmpty()){
//                        throw new AppException(ErrorCode.ERROR_KIEMTRA_PHONG_TRONG, "Phòng: "+ xepPhongRequest.getPhong().getMaPhong()+" đang được đặt trước đó");
//                    }

                    boolean kiemtra = false;
                    for (PhongTrongThoiGian phongTrongThoiGian : kiemtraPhong) {

                        if(phongTrongThoiGian.getMaPhong().equals(xepPhongRequest.getPhong().getMaPhong()))
                        {
                            kiemtra = true;
                        }

                    }

                    if(!kiemtra){
                        throw new AppException(ErrorCode.ERROR_KIEMTRA_PHONG_TRONG, "Phòng: "+ xepPhongRequest.getPhong().getMaPhong()+" đang được đặt trước đó");
                    }

                    // Kiểm tra phòng có tồn tại không
                    if (phong == null) {
                        throw new AppException(ErrorCode.MA_PHONG_NOT_FOUND);
                    }
                    if(loaiGia == null)
                    {
                        throw new AppException(ErrorCode.LOAI_GIA_NOT_FOUND);
                    }

                    // Kiểm tra giá phòng hợp lệ
                    if (xepPhongRequest.getGia().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new AppException(ErrorCode.GIA_PHONG_ERROR);
                    }



                    // Tạo đối tượng XepPhongBooKing
                    XepPhongBooKing xepPhongBooKing = xepPhongBooKingMapper.toceateXepPhongBooKing(xepPhongRequest);

                    chiTietBooKingRepository.findById(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking())
                            .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
                    var authentiaction = SecurityContextHolder.getContext().getAuthentication();
                    xepPhongBooKing.setNguoiTao(authentiaction.getName());
                    xepPhongBooKing.setNgayXep(LocalDate.now());
                    xepPhongBooKing.setThoiGianTao(LocalDateTime.now());
                    xepPhongBooKing.setGioXep(LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
                    xepPhongBooKing.setPhuThuTienGiuong(BigDecimal.ZERO);
                    xepPhongBooKing.setSoGiuong(0);
                    xepPhongBooKing.setPhuThuTienTre(BigDecimal.ZERO);
                    xepPhongBooKing.setSoTre(0);
                    xepPhongBooKing.setPhuThuNguoiLon(BigDecimal.ZERO);
xepPhongBooKing.setSoNguoiLon(0);

                    return xepPhongBooKing;
                })
                .collect(Collectors.toList());

        // Lưu danh sách vào database
        xepPhongRepository.saveAll(xepPhongBooKingList);

        // Cập nhật trạng thái đã xếp phòng
        int updatedRows = booKingRepository.updateDa_xep_phong(true, ma_booking);
        if (updatedRows == 0) {
            logger.warn("Không thể cập nhật trạng thái da_xep_phong cho ma_booking: {}", ma_booking);
            throw new AppException(ErrorCode.UPDATE_DA_XEP_PHONG);
        }

        return true;
    }

    public List<XepPhongBooKingResponse> getXepPhongBooKingList(String ma_booking) {
        List<XepPhongBooKing> xp = xepPhongRepository.getXepPhongBooKingsByMaBooking(ma_booking);
        return xepPhongRepository.getXepPhongBooKingsByMaBooking(ma_booking)
                .stream().map(xepPhongBooKingMapper::toXepPhongBooKingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean updateXepPhongBooKing(List<XepPhongBooKingRequest> request, String ma_booking) {
        if (request == null || request.isEmpty()) {
            throw new AppException(ErrorCode.XEP_PHONG_IS_EMPTY);
        }

        // Kiểm tra booking tồn tại trong DB
        BooKing booKing = booKingRepository.findById(ma_booking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

        // Kiểm tra trùng lặp maPhong trong request
        Set<String> maPhongSet = new HashSet<>();
        for (XepPhongBooKingRequest req : request) {
            String maPhong = req.getPhong().getMaPhong();
            if (!maPhongSet.add(maPhong)) {
                throw new AppException(ErrorCode.DUPLICATE_MA_PHONG, "Phòng " + maPhong + " bị trùng lặp trong request.");
            }
        }

        // Lấy danh sách maPhong từ request
        List<String> maPhongs = request.stream()
                .map(r -> r.getPhong().getMaPhong())
                .collect(Collectors.toList());

        // Lấy danh sách maLoaiGia từ request
        List<Integer> maLoaiGias = request.stream()
                .map(r -> r.getLoaiGia().getMaLoaiGia())
                .collect(Collectors.toList());

        // Lấy dữ liệu từ database bằng một lần truy vấn
        Map<String, Phong> phongMap = phongRepository.findAllById(maPhongs).stream()
                .collect(Collectors.toMap(Phong::getMaPhong, Function.identity()));

        Map<Integer, LoaiGia> loaiGiaMap = loaiGiaRepository.findAllById(maLoaiGias).stream()
                .collect(Collectors.toMap(LoaiGia::getMaLoaiGia, Function.identity()));



        // Tạo danh sách XepPhongBooKing để cập nhật
        List<XepPhongBooKing> xepPhongBooKingList = request.stream()
                .map(xepPhongRequest -> {
                    Phong phong = phongMap.get(xepPhongRequest.getPhong().getMaPhong());
                    LoaiGia loaiGia = loaiGiaMap.get(xepPhongRequest.getLoaiGia().getMaLoaiGia());



                    // Kiểm tra phòng và loại giá có tồn tại không
                    if (phong == null) {
                        throw new AppException(ErrorCode.MA_PHONG_NOT_FOUND, "Phòng " + xepPhongRequest.getPhong().getMaPhong() + " không tồn tại.");
                    }
                    if (loaiGia == null) {
                        throw new AppException(ErrorCode.LOAI_GIA_NOT_FOUND, "Loại giá " + xepPhongRequest.getLoaiGia().getMaLoaiGia() + " không tồn tại.");
                    }

                    // Kiểm tra giá phòng hợp lệ
                    if (xepPhongRequest.getGia().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new AppException(ErrorCode.GIA_PHONG_ERROR, "Giá phòng phải lớn hơn 0.");
                    }

                    // Lấy đối tượng từ database
                    XepPhongBooKing existingXepPhong = xepPhongRepository.findById(xepPhongRequest.getMaXepPhongBooking())
                            .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND,
                                    "Mã xếp phòng booking " + xepPhongRequest.getMaXepPhongBooking() + " không tồn tại."));

                    if(!existingXepPhong.getPhong().getMaPhong().equals(xepPhongRequest.getPhong().getMaPhong())) {
                        int KiemTraPhong = xepPhongRepository.KiemTraPhongDaTraChua(xepPhongRequest.getPhong().getMaPhong());

                    if(KiemTraPhong > 0)
                        throw new AppException(ErrorCode.ERROR_PHONG_CHUA_TRA, "Phòng: " + xepPhongRequest.getPhong().getMaPhong());

                    }

                    // Cập nhật thông tin đối tượng
                    existingXepPhong.setPhong(phong);
                    existingXepPhong.setLoaiGia(loaiGia);
                    existingXepPhong.setGia(xepPhongRequest.getGia());
                    existingXepPhong.setNgayDen(xepPhongRequest.getNgayDen());
                    existingXepPhong.setNgayDi(xepPhongRequest.getNgayDi());
                    existingXepPhong.setGioDen(xepPhongRequest.getGioDen());
                    existingXepPhong.setGioDi(xepPhongRequest.getGioDi());

                    return existingXepPhong;
                })
                .collect(Collectors.toList());

        // Cập nhật danh sách vào database
        xepPhongRepository.saveAll(xepPhongBooKingList);

        // Cập nhật trạng thái đã xếp phòng cho booking
//        booKingRepository.updateDa_xep_phong(true, ma_booking);

        return true;
    }


    @Transactional
    public boolean deleteXepPhongBooKing(String ma_booking) {
        // Kiểm tra đầu vào
        if (ma_booking == null || ma_booking.trim().isEmpty()) {
            logger.warn("Mã booking không hợp lệ: {}", ma_booking);
            throw new IllegalArgumentException("Mã booking không được để trống hoặc null");
        }

        try {
            // Kiểm tra xem BooKing có tồn tại không
            if (!booKingRepository.existsById(ma_booking)) {
                logger.warn("Không tìm thấy BooKing với ma_booking: {}", ma_booking);
                throw new IllegalArgumentException("Mã booking không tồn tại: " + ma_booking);
            }

            // Kiểm tra xem có bản ghi XepPhongBooKing nào để xóa không
            long countBefore = xepPhongRepository.countByMaBooking(ma_booking);
            if (countBefore == 0) {
                logger.info("Không tìm thấy xếp phòng nào để xóa với ma_booking: {}", ma_booking);
                return false;
            }

            // Thực hiện xóa XepPhongBooKing
            xepPhongRepository.deleteXepPhongBooKings(ma_booking);
            logger.info("Đã xóa {} xếp phòng với ma_booking: {}", countBefore, ma_booking);

            // Kiểm tra lại để xác nhận xóa thành công
            long countAfter = xepPhongRepository.countByMaBooking(ma_booking);
            if (countAfter > 0) {
                logger.error("Xóa xếp phòng không hoàn tất, vẫn còn {} bản ghi với ma_booking: {}", countAfter, ma_booking);
                throw new RuntimeException("Xóa xếp phòng không thành công, vẫn còn bản ghi tồn tại");
            }

            // Cập nhật trạng thái đã xếp phòng
            int updatedRows = booKingRepository.updateDa_xep_phong(false, ma_booking);
            if (updatedRows == 0) {
                logger.warn("Không thể cập nhật trạng thái da_xep_phong cho ma_booking: {}", ma_booking);
                throw new AppException(ErrorCode.UPDATE_DA_XEP_PHONG);
            }

            logger.info("Đã cập nhật trạng thái da_xep_phong thành false cho ma_booking: {}", ma_booking);
            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi xóa xếp phòng với ma_booking: {}", ma_booking, e);
            throw new RuntimeException("Không thể xóa xếp phòng: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean updateDaNhanPhong(String ma_xepphong_booking, boolean daNhanPhong, BigDecimal phuThuCheckInSom) {
        // Kiểm tra đầu vào
        if (ma_xepphong_booking == null || ma_xepphong_booking.trim().isEmpty()) {
            logger.warn("Mã xếp phòng booking không hợp lệ: {}", ma_xepphong_booking);
            throw new IllegalArgumentException("Mã xếp phòng booking không được để trống hoặc null");
        }

        LocalDate ngayHienTai = LocalDate.now();

        try {
            // Kiểm tra xem BooKing có tồn tại không
            XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(ma_xepphong_booking)
                    .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));

            if(ngayHienTai.isBefore(xepPhongBooKing.getNgayDen())) {
                throw new AppException(ErrorCode.ERROR_NGAYDEN, "Ngày check-in phải là ngày hiện tại hoặc tương lai.");
            }

            // Kiểm tra xem có bản ghi XepPhongBooKing nào để xóa không
            long countBefore = xepPhongRepository.countByMaXepPhongBooking(ma_xepphong_booking);
            if (countBefore == 0) {
                logger.info("Không tìm thấy xếp phòng nào để update với ma_xepphongbooking: {}", ma_xepphong_booking);
                return false;
            }

            LocalDate ngayDen = xepPhongBooKing.getNgayDen();
            LocalTime gioDen = xepPhongBooKing.getGioDen();

            // Kết hợp thành LocalDateTime
            LocalDateTime thoiGianDen = LocalDateTime.of(ngayDen, gioDen);
            LocalDateTime hienTai = LocalDateTime.now(); // Lấy thời gian hiện tại

            var authentiaction = SecurityContextHolder.getContext().getAuthentication().getName();

            // So sánh
            if (thoiGianDen.isBefore(hienTai)) {
                xepPhongRepository.updateDanhanPhong(daNhanPhong, LocalDateTime.now(),ma_xepphong_booking,authentiaction, true,phuThuCheckInSom);
                logger.info("Đã update {} xếp phòng với ma_xephongbooking: {}", countBefore, ma_xepphong_booking);
            } else {
                xepPhongRepository.updateDanhanPhong(daNhanPhong, LocalDateTime.now(),ma_xepphong_booking,authentiaction,false,phuThuCheckInSom);
                logger.info("Đã update {} xếp phòng với ma_xephongbooking: {}", countBefore, ma_xepphong_booking);
            }
                // Thực hiện xóa XepPhongBooKing

            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi update đã nhận phòng xếp phòng với ma_booking: {}", ma_xepphong_booking, e);
            throw new RuntimeException("Không thể update đã nhận  phòng xếp phòng: " + e.getMessage(), e);
        }
    }

    public XepPhongBooKingResponse getXepPhongBooKingByMaXepPhong(String ma_xepphong_booking) {
       XepPhongBooKing xepPhongBooKing =  xepPhongRepository.findById(ma_xepphong_booking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));

        return xepPhongBooKingMapper.toXepPhongBooKingResponse(xepPhongBooKing);
    }

    public Map<String, Object> getThongTinKhachHangBooking(String ma_xepphong){
        xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
        return xepPhongRepository.getThongTinKhachHangBooking(ma_xepphong);
    }

    public List<Map<String, Object>> xuatPhieuDangKyXepPhong(String ma_xepphong){
        xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
        return xepPhongRepository.xuatPhieuDangKyXepPhong(ma_xepphong);
    }

    // cập nhật ngày đi của xếp phòng cho từng phòng
@Transactional
    public boolean UpdateNgayDiCuaXepPhong(String ma_booking, String ma_xepphong, LocalDate ngay_di_moi, LocalTime gio_di_moi, LocalDate ngay_di_cu, String ma_loai_phong, List<GiaPhongTheoNgayRequest> requests){

       XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
        loaiPhongRepository.findById(ma_loai_phong)
                .orElseThrow(()-> new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND));
        if(ngay_di_moi == null || gio_di_moi == null){
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }

        if(requests.isEmpty())
            throw new AppException(ErrorCode.ERROR_GIA_PHONG_THEO_NGAY_IS_EMPTY);

        if(ma_loai_phong.isEmpty()){
            throw new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND);
        }

        loaiPhongRepository.findById(ma_loai_phong)
                .orElseThrow(() -> new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND));

       // kiêm tra ngay di moi cua phong xem co bi trung voi phong dang duoc dat hay ko

//    List<XepPhongBooKing> getXepPhongBooKings = xepPhongRepository.getXepPhongBooKingsByMaBooking(ma_booking);
//
//    for (XepPhongBooKing xp : getXepPhongBooKings) {
//        System.err.println(xp.getPhong().getMaPhong());
//
//        int countTrung = xepPhongRepository.kiemTraPhongDaTonTai(xp.getPhong().getMaPhong(), xepPhongBooKing.getNgayDen(), ngay_di_moi,xp.getMaXepPhongBooking());
//        if(countTrung > 0){
//            throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING, "Ngày đi: " + DateConverter.converDate(ngay_di_moi) + " do bị trùng với booking khác, Phòng: " + xp.getPhong().getMaPhong() + " đã được xếp. " );
//        }
//    }

    // kiểm tra ngày đi mới có hợp lệ không

  XepPhongBooKing getXepPhongBooKings = xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));

  int countTrung = xepPhongRepository.kiemTraPhongDaTonTai(getXepPhongBooKings.getPhong().getMaPhong(), xepPhongBooKing.getNgayDen(), ngay_di_moi,getXepPhongBooKings.getMaXepPhongBooking());
        if(countTrung > 0){
            throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING, "Ngày đi: " + DateConverter.converDate(ngay_di_moi) + " do bị trùng với booking khác, Phòng: " + getXepPhongBooKings.getPhong().getMaPhong() + " đã được xếp. " );
        }

    LocalDate hienTai = LocalDate.now();

    if (ngay_di_moi.isBefore(hienTai)) {
            throw new AppException(ErrorCode.ERROR_THOI_GIAN_DI);
    }

        LoaiPhongTrongDTO kiemTraSoLuongLoaiPhong = loaiPhongRepository.kiemTraLoaiPhongTrongMaLoaiPhong(ngay_di_cu,ngay_di_moi,ma_loai_phong);
            if(kiemTraSoLuongLoaiPhong.getSoPhongTrong() <= 0)
                throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING, "ngày đi bởi vì số lượng loại phòng đã hết");

    long soDemThueLong = ChronoUnit.DAYS.between(xepPhongBooKing.getNgayDen(), ngay_di_moi);
    if (soDemThueLong < 0) {
        throw new AppException(ErrorCode.ERROR_NGAYDI, "Ngày đi phải sau ngày đến");
    }

    int soDemThue = (soDemThueLong == 0) ? 1 : (int) soDemThueLong;


    // thành tiền hiện tại
    BigDecimal tongTienPhong = xepPhongBooKing.getThanhTien();

    BigDecimal tongTienPhuThu = xepPhongBooKing.getPhuThuTienGiuong().multiply(BigDecimal.valueOf(xepPhongBooKing.getSoGiuong())).add(xepPhongBooKing.getPhuThuTienTre().multiply(BigDecimal.valueOf(xepPhongBooKing.getSoTre())));

    BigDecimal thanhTien = tongTienPhong.add(tongTienPhuThu);

    List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgays = new ArrayList<>();

    if(ngay_di_moi.isBefore(ngay_di_cu)){

        // Nếu ngày đi mới trùng với ngày đến => không thực hiện xóa
        if (ngay_di_moi.equals(xepPhongBooKing.getNgayDen())) {
            System.out.println("Ngày đi mới bằng ngày đến, không thực hiện xóa.");

        }else {

            // Tính số ngày giữa ngay_di_cu và ngay_di_moi
            List<LocalDate> ngayBiTruList = new ArrayList<>();
            LocalDate temp = ngay_di_moi; // Bắt đầu từ ngày sau ngày mới

            while (temp.isBefore(ngay_di_cu)) {
                ngayBiTruList.add(temp);

                // Xóa đúng ngày hiện tại
                giaPhongTheoNgayRepository.xoaGiaPhongTheoNgayByMaChiTietBookingAndNgay(
                        xepPhongBooKing.getChiTietBooking().getMaChiTietBooking(),
                        temp
                );

                System.err.println(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());

                GiaPhongTheoNgay giaPhongTheoNgay = giaPhongTheoNgayRepository.findGiaPhongTheoNgayByMaChiTietBookingAndNgay(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking(),
                        temp);


                if (giaPhongTheoNgay != null) {
                    tongTienPhong = tongTienPhong.subtract(giaPhongTheoNgay.getGia());
                    Log_GiaPhongTheoNgay logGiaPhongTheoNgay = new Log_GiaPhongTheoNgay();
                    logGiaPhongTheoNgay.setMaChiTietBooking(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());
                    logGiaPhongTheoNgay.setMaLoaiPhong(ma_loai_phong);
                    logGiaPhongTheoNgay.setNgay(giaPhongTheoNgay.getNgay());
                    logGiaPhongTheoNgay.setGia(giaPhongTheoNgay.getGia());
                    logGiaPhongTheoNgay.setThoiGianTao(LocalDateTime.now());
                    logGiaPhongTheoNgay.setTrangThai("DECTED");
                    logGiaPhongTheoNgays.add(logGiaPhongTheoNgay);
                }

                temp = temp.plusDays(1); // Sau khi xử lý mới tăng

            }

            System.out.println("Các ngày bị trừ:");
            ngayBiTruList.forEach(System.out::println);
        }
    }else if(ngay_di_moi.isAfter(ngay_di_cu)){
//        // Tính số ngày giữa ngay_di_cu và ngay_di_moi
//        List<LocalDate> ngayBiCongList = new ArrayList<>();
//        LocalDate temp = ngay_di_cu.plusDays(1); // Bắt đầu từ ngày sau ngày mới
//
//        List<GiaPhong> giaPhongList = giaPhongRepository.findByMaLoaiPhong(ma_loai_phong);
//
//        if(giaPhongList.isEmpty()){
//            throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING, "Không tìm thấy giá phòng cho loại phòng: " + xepPhongBooKing.getPhong().getMaPhong());
//        }
//
//
//
//        List<GiaPhongTheoNgay> giaPhongTheoNgays = new ArrayList<>();
//        List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgays = new ArrayList<>();
//        while (!temp.isAfter(ngay_di_moi.minusDays(1))) {
//            ngayBiCongList.add(temp);
//
//            // kiểm tra có phải ngày thứ 7 không
//            boolean isSaturday = temp.getDayOfWeek().getValue() == 6; // 6 là thứ 7 trong Java
//
//            // nếu là ngày thứ 7 thì lấy giá phòng cuối tuần, nếu không thì lấy giá phòng bình thường
//            LocalDate finalTemp = temp;
//
//            Double giaPhongSave = (double) 0;
//
//            int year = LocalDate.now().getYear();
//
//            List<LocalDate> ngayLeList  = VietnamHolidayUtils.getVietnamHolidays(year);
//
//            boolean isNgayLe = ngayLeList.contains(temp);
//
//            for (GiaPhong giaPhong : giaPhongList) {
//                if (isNgayLe && giaPhong.isGiaNgayLe()) {
//                    giaPhongSave = giaPhong.getGia();
//                    break;
//                } else if (!isNgayLe && isSaturday && giaPhong.isGiaCuoiTuan()) {
//                    giaPhongSave = giaPhong.getGia();
//                    break;
//                } else if (!isNgayLe && !isSaturday && giaPhong.isGiaNgayThuong()) {
//                    giaPhongSave = giaPhong.getGia();
//                    break;
//                }
//            }
//
//            GiaPhongTheoNgay giaPhongTheoNgay = new GiaPhongTheoNgay();
//            giaPhongTheoNgay.setChiTietBooking(xepPhongBooKing.getChiTietBooking());
//            giaPhongTheoNgay.setNgay(temp);
//            giaPhongTheoNgay.setGia(BigDecimal.valueOf(giaPhongSave));
//            giaPhongTheoNgay.setTrangThai("CREATED");
//            giaPhongTheoNgays.add(giaPhongTheoNgay);
//
//            Log_GiaPhongTheoNgay logGiaPhongTheoNgay = new Log_GiaPhongTheoNgay();
//            logGiaPhongTheoNgay.setMaChiTietBooking(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());
//            logGiaPhongTheoNgay.setMaLoaiPhong(ma_loai_phong);
//            logGiaPhongTheoNgay.setNgay(temp);
//            logGiaPhongTheoNgay.setGia(BigDecimal.valueOf(giaPhongSave));
//            logGiaPhongTheoNgay.setThoiGianTao(LocalDateTime.now());
//            logGiaPhongTheoNgays.add(logGiaPhongTheoNgay);
//
//            temp = temp.plusDays(1); // Sau khi xử lý mới tăng
//        }
//        // Lưu các ngày bị cộng vào
//        giaPhongTheoNgayRepository.saveAll(giaPhongTheoNgays);
//        log_GiaPhongTheoNgayRepository.saveAll(logGiaPhongTheoNgays);
//
//        System.out.println("Các ngày bị cộng:");
//        ngayBiCongList.forEach(System.out::println);

        if(requests == null && requests.isEmpty()){
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
        }

        List<GiaPhongTheoNgay> listGiaPhongTheoNgay = giaPhongTheoNgayRepository.findByMaXepPhongBooking(ma_xepphong);

        if(listGiaPhongTheoNgay.isEmpty()) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
        }

        List<GiaPhongTheoNgay> listGiaPhongTheoNgayUpdate = new ArrayList<>();


        // Lấy danh sách hiện tại từ DB
        List<GiaPhongTheoNgay> giaPhongTheoNgayDB = giaPhongTheoNgayRepository.findByMaXepPhongBooking(ma_xepphong);

// Đưa DB thành Map để so sánh dễ
        Map<LocalDate, GiaPhongTheoNgay> giaPhongMap = new HashMap<>();
        for (GiaPhongTheoNgay g : giaPhongTheoNgayDB) {
            giaPhongMap.put(g.getNgay(), g);
        }

        for (GiaPhongTheoNgayRequest request : requests) {
            LocalDate ngay = request.getNgay();
            BigDecimal gia = request.getGia();

            if (giaPhongMap.containsKey(ngay)) {
                // --- Cập nhật nếu đã tồn tại ngày ---
                giaPhongTheoNgayRepository.updateGiaPhongTheoNgayByMaChiTietBookingAndNgay(
                        gia,
                        xepPhongBooKing.getChiTietBooking().getMaChiTietBooking(),
                        ngay
                );

                // Ghi log cập nhật
                Log_GiaPhongTheoNgay log = new Log_GiaPhongTheoNgay();
                log.setMaChiTietBooking(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());
                log.setMaLoaiPhong(ma_loai_phong);
                log.setNgay(ngay);
                log.setGia(gia);
                log.setThoiGianTao(LocalDateTime.now());
                log.setTrangThai("UPDATED");
                logGiaPhongTheoNgays.add(log);
            } else {
                // --- Tạo mới nếu chưa có ngày ---
                GiaPhongTheoNgay newGia = new GiaPhongTheoNgay();
                newGia.setChiTietBooking(xepPhongBooKing.getChiTietBooking());
                newGia.setNgay(ngay);
                newGia.setGia(gia);
                newGia.setMaLoaiPhong(ma_loai_phong);
                // set các field cần thiết khác...
                giaPhongTheoNgayRepository.save(newGia);

                // Ghi log tạo mới
                Log_GiaPhongTheoNgay log = new Log_GiaPhongTheoNgay();
                log.setMaChiTietBooking(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());
                log.setMaLoaiPhong(ma_loai_phong);
                log.setNgay(ngay);
                log.setGia(gia);
                log.setThoiGianTao(LocalDateTime.now());
                log.setTrangThai("CREATED");
                logGiaPhongTheoNgays.add(log);
            }

            // Cộng tiền
            tongTienPhong = tongTienPhong.add(gia);
        }



        giaPhongTheoNgayRepository.saveAll(listGiaPhongTheoNgayUpdate);
        log_GiaPhongTheoNgayRepository.saveAll(logGiaPhongTheoNgays);

    }

    // ngày đi mới và ngày đi cũ bằng nhau thì update giá phòng theo ngày
    if (ngay_di_moi.equals(ngay_di_cu)) {
        List<GiaPhongTheoNgay> giaPhongTheoNgayUpdate = giaPhongTheoNgayRepository.findByMaXepPhongBooking(ma_xepphong);

        // Gom các request theo ngày, tránh trùng
        Map<LocalDate, GiaPhongTheoNgayRequest> requestMap = new HashMap<>();
        for (GiaPhongTheoNgayRequest request : requests) {
            if (requestMap.containsKey(request.getNgay())) {
                throw new RuntimeException("Trùng ngày trong danh sách request: " + request.getNgay());
            }
            requestMap.put(request.getNgay(), request);
        }

        for (GiaPhongTheoNgay giaPhongTheoNgay : giaPhongTheoNgayUpdate) {
            LocalDate ngay = giaPhongTheoNgay.getNgay();

            if (requestMap.containsKey(ngay)) {
                GiaPhongTheoNgayRequest request = requestMap.get(ngay);

                // Cập nhật
                giaPhongTheoNgayRepository.updateGiaPhongTheoNgayByMaChiTietBookingAndNgay(
                        request.getGia(),
                        xepPhongBooKing.getChiTietBooking().getMaChiTietBooking(),
                        request.getNgay()
                );

                // Ghi log
                Log_GiaPhongTheoNgay logGiaPhongTheoNgay = new Log_GiaPhongTheoNgay();
                logGiaPhongTheoNgay.setMaChiTietBooking(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());
                logGiaPhongTheoNgay.setMaLoaiPhong(ma_loai_phong);
                logGiaPhongTheoNgay.setNgay(request.getNgay());
                logGiaPhongTheoNgay.setGia(request.getGia());
                logGiaPhongTheoNgay.setThoiGianTao(LocalDateTime.now());
                logGiaPhongTheoNgay.setTrangThai("UPDATED");

                logGiaPhongTheoNgays.add(logGiaPhongTheoNgay);

                // Cộng tiền
                tongTienPhong = tongTienPhong.add(request.getGia());
            } else {
                throw new RuntimeException("Không tìm thấy ngày tương ứng trong danh sách request: " + ngay);
            }
        }
    }


    log_GiaPhongTheoNgayRepository.saveAll(logGiaPhongTheoNgays);

        // Cập nhật trạng thái đã xếp phòng
        int updateNgayDi = xepPhongRepository.UpdateNgayDi(ngay_di_moi,gio_di_moi,ma_xepphong,soDemThue,thanhTien);
        if (updateNgayDi == 0) {
            logger.warn("Không thể cập nhật ngày đi xếp phong : {}", ma_xepphong);
            throw new AppException(ErrorCode.UPDATE_DA_XEP_PHONG);
        }


        return true;
    }

    // cập nhật phụ thu phòng
    @Transactional
    public boolean updatePhuThuPhong(BigDecimal phuThuTienGiuong,int soGiuong,BigDecimal phuThuNguoiLon, int soNguoiLon, BigDecimal phuThuTreEm, int soTre, String ma_xepphong){

       XepPhongBooKing xepPhongBooKing =  xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));

        if(phuThuTienGiuong == null || phuThuTienGiuong.compareTo(BigDecimal.ZERO) < 0){
            throw new AppException(ErrorCode.ERROR_PHU_THU_TIEN_GUONG);
        }
        if(phuThuTreEm == null || phuThuTreEm.compareTo(BigDecimal.ZERO) < 0){
            throw new AppException(ErrorCode.ERROR_PHU_THU_TIEN_TRE_EM);
        }
        if(phuThuNguoiLon == null || phuThuNguoiLon.compareTo(BigDecimal.ZERO) < 0){
            throw new AppException(ErrorCode.ERROR_PHU_THU_TIEN_TRE_EM);
        }

        if(soGiuong < 0){
            throw new AppException(ErrorCode.ERROR_PHU_THU_TIEN_GUONG);
        }
        if(soTre < 0){
            throw new AppException(ErrorCode.ERROR_PHU_THU_TIEN_TRE_EM);
        }
        if(soNguoiLon < 0){
            throw new AppException(ErrorCode.ERROR_PHU_THU_TIEN_TRE_EM);
        }

        long soDemThueLong = ChronoUnit.DAYS.between(xepPhongBooKing.getNgayDen(), xepPhongBooKing.getNgayDi());
        if (soDemThueLong < 0) {
            throw new AppException(ErrorCode.ERROR_NGAYDI, "Ngày đi phải sau ngày đến");
        }

        if(soGiuong == 0)
            phuThuTienGiuong = BigDecimal.ZERO;
        if(soTre == 0)
            phuThuTreEm = BigDecimal.ZERO;
        if(soNguoiLon == 0)
            phuThuNguoiLon = BigDecimal.ZERO;

        int soDemThue = (soDemThueLong == 0) ? 1 : (int) soDemThueLong;

        BigDecimal tongTienPhuThuGiuong = phuThuTienGiuong.multiply(BigDecimal.valueOf(soDemThue));
        BigDecimal tongTienPhuThuTre = phuThuTreEm.multiply(BigDecimal.valueOf(soDemThue));
        BigDecimal tongTienPhuThuNguoiLon = phuThuNguoiLon.multiply(BigDecimal.valueOf(soDemThue));

        BigDecimal tongTienPhuThu = tongTienPhuThuGiuong.add(tongTienPhuThuTre).add(tongTienPhuThuNguoiLon);

        BigDecimal thanhTien = tongTienPhuThu.add(xepPhongBooKing.getThanhTien());

        // Cập nhật phụ thu
        int updatePhuThu = xepPhongRepository.UpdatePhuThuPhong(phuThuTienGiuong,soGiuong,phuThuNguoiLon,soNguoiLon,phuThuTreEm,soTre,ma_xepphong,thanhTien);
        if (updatePhuThu == 0) {
            logger.warn("Không thể cập nhật phụ thu : {}", ma_xepphong);
            throw new AppException(ErrorCode.UPDATE_DA_XEP_PHONG);
        }
        return true;
    }

    public Map<String,Object> getPhuThuXepPhong(String ma_xepphong){
        xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
        return xepPhongRepository.getPhuThuXepPhong(ma_xepphong);
    }

    @Transactional
    public boolean xoaPhongTrongXepPhong(String ma_chitiet_booking, String soPhong) {
        // Kiểm tra đầu vào
        if (ma_chitiet_booking == null || ma_chitiet_booking.trim().isEmpty()) {
            logger.warn("Mã chi tiết  booking không hợp lệ: {}", ma_chitiet_booking);
            throw new IllegalArgumentException("Mã chi tiết booking không được để trống hoặc null");
        }

        try {
            // Kiểm tra xem BooKing có tồn tại không
            XepPhongBooKing xepPhongBooKing = xepPhongRepository.findByChiTietBookingAndPhong(ma_chitiet_booking,soPhong);

            if (xepPhongBooKing == null) {
                logger.info("Không tìm thấy xếp phòng nào để update với ma_xepphongbooking: {}", ma_chitiet_booking);
                return false;
            }

            if(xepPhongBooKing.isDaNhanPhong())
                throw new AppException(ErrorCode.ERROR_CHECK_NOT);

            // Thực hiện xóa XepPhongBooKing
            xepPhongRepository.xoaPhongTrongXepPhong(ma_chitiet_booking, soPhong);

            // sau khi xoa xep phong xong quay lai xoa danh sach phong chi tiet

            danhSachPhongChiTietRepository.deleteByChiTietBooking_MaChiTietBooking(ma_chitiet_booking);

            return true;

        } catch (Exception e) {
            logger.error("Lỗi khi update đã nhận phòng xếp phòng với ma_booking: {}", ma_chitiet_booking, e);
            throw new RuntimeException("Không thể update đã nhận  phòng xếp phòng: " + e.getMessage(), e);
        }
    }

}
