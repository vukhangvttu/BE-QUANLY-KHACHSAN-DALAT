package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.LoaiPhongTrongDTO;
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
import vttu.khachsanservice.utils.DateConverter;

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
public class BooKingService {

    KhachHangBooKingRepository khachHangBooKingRepository;
    KhachHangBooKingMapper khachHangBooKingMapper;
    ThongTinLienHeRepository thongTinLienHeRepository;
    ThongTinLienHeBooKingMapper thongTinLienHeBooKingMapper;
    BooKingRepository booKingRepository;
    BooKingMapper booKingMapper;
    ChiTietBooKingRepository chiTietBooKingRepository;
    ChiTietBooKingMapper chiTietBooKingMapper;
    KhachHangBooKingService khachHangBooKingService;

    YeuCauRepository yeuCauRepository;

    private final DanhXungRepository danhXungRepository;
    private final NhomKhachHangRepository nhomKhachHangRepository;
    private final TrangThaiBooKingRepository trangThaiBooKingRepository;
    private final LoaiTheRepository loaiTheRepository;
    private final LoaiGiaRepository loaiGiaRepository;
    private final LoaiPhongRepository loaiPhongRepository;
    private final XepPhongRepository xepPhongRepository;
    private final PhongRepository phongRepository;
    private final GiaPhongRepository giaPhongRepository;
    private final DanhSachPhongChiTietRepository danhSachPhongChiTietRepository;
    private final GiaPhongTheoNgayRepository giaPhongTheoNgayRepository;

    Log_GiaPhongTheoNgayRepository logGiaPhongTheoNgayRepository;
    private final Log_DanhSachPhongChiTietRepositoty log_DanhSachPhongChiTietRepositoty;

    // tạo id mã string tự động tăng
    public synchronized String generateNewBookingId() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // Ví dụ: "20250303"
        Optional<String> latestId = booKingRepository.findLatestMaBooKing();
        if (latestId.isPresent() && latestId.get().startsWith("BK" + datePrefix)) {
            int number = Integer.parseInt(latestId.get().substring(10)); // Sau "BK20250303"
            return String.format("BK%s%03d", datePrefix, number + 1);
        }
        return "BK" + datePrefix + "001"; // Ví dụ: "BK20250303001"
    }

@Transactional
public BooKingResponse createBooKing(BooKingRequest request) {
    // Validate các entity liên quan
    validateRelatedEntities(request);

    // Lưu thông tin khách hàng và liên hệ
    KhachHangBooKing khachHangBooKing = saveCustomerInfo(request);
    ThongTinLienHeBooKing thongTinLienHeBooKing = saveContactInfo(request);

    // Tạo và lưu BooKing chính
    BooKing booKing = createAndSaveMainBooking(request, khachHangBooKing, thongTinLienHeBooKing);

    var authentiaction = SecurityContextHolder.getContext().getAuthentication();
  // Kiểm tra giá trị sau khi lưu
  log.info("After saving BooKing - maBooking: {}, tongTien: {}",
    booKing.getMaBooking(), booKing.getTongTien());

    if(request.getChiTietBooKings() == null || request.getChiTietBooKings().isEmpty()) {
        throw new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY);
    }

    // Xử lý ChiTietBooking
    ProcessChiTietBookingResult result = processChiTietBookings(request, booKing);

    // Kiểm tra tổng tiền
    validateTotalAmount(result.tongTien, request.getTongTien());

    // Kiểm tra số lượng booking
    if (request.getChiTietBooKings().size() != result.chiTietBookings.size()) {
        throw new AppException(ErrorCode.SO_LUONG_BOOKING_KHONG_KHOP);
    }

    chiTietBooKingRepository.saveAll(result.chiTietBookings);
    log_DanhSachPhongChiTietRepositoty.saveAll(result.logDanhSachPhongChiTietList);
    logGiaPhongTheoNgayRepository.saveAll(result.logGiaPhongTheoNgay);

    // Xếp phòng tự động nếu có chọn phòng
    if (!result.khongCoPhong) {
        autoAssignRooms(result.chiTietBookings, booKing, authentiaction);
        booKingRepository.updateDa_xep_phong(true, booKing.getMaBooking());
    }


    return booKingMapper.toBooKingResponse(booKing);
}

    public List<Map<String,Object>> getAllBooKing(){
        return booKingRepository.danhSachBooKing();

    }

    public BooKingResponse getBooKingByMaBookingId(String maBookingId){

        booKingRepository.findById(maBookingId)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND, maBookingId));

        BooKing booKing = booKingRepository.findById(maBookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND, "" + maBookingId));

        return booKingMapper.toBooKingResponse(booKingRepository.findById(maBookingId).get());
    }

  @Transactional
  public BooKingResponse updateBooKing(String maBooking, BooKingRequest request) {
    // 1. Tìm bản ghi BooKing cần cập nhật
    BooKing booKing = booKingRepository.findById(maBooking)
      .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND, "Booking không tồn tại: " + maBooking));

    // Kiểm tra các thực thể liên quan
    findEntityOrThrow(
      () -> danhXungRepository.findById(request.getDanhXung().getMaDanhXung()),
      ErrorCode.DANH_XUNG_NOT_FOUND,
      request.getDanhXung().getMaDanhXung()
    );
    findEntityOrThrow(
      () -> nhomKhachHangRepository.findById(request.getNhomKhachHang().getMaNhomKhachHang()),
      ErrorCode.NHOM_KHACH_HANG_NOT_FOUND,
      request.getNhomKhachHang().getMaNhomKhachHang()
    );

    findEntityOrThrow(
      () -> trangThaiBooKingRepository.findById(request.getTrangThaiBooKing().getMaTrangThaiBooKing()),
      ErrorCode.TRANG_THAI_BOOKING_NOT_FOUND,
      request.getTrangThaiBooKing().getMaTrangThaiBooKing()
    );
    findEntityOrThrow(
      () -> loaiTheRepository.findById(request.getLoaiThe().getMaLoaiThe()),
      ErrorCode.LOAI_THE_NOT_FOUND,
      request.getLoaiThe().getMaLoaiThe()
    );

    // Ánh xạ request sang BooKing entity
    BooKing booKingUpdate = booKingMapper.toCreateBooKing(request);

    // Xử lý yêu cầu
    if (request.getYeuCaus() != null && !request.getYeuCaus().isEmpty()) {
      List<YeuCau> yeuCausMoi = request.getYeuCaus().stream()
        .map(yeuCauRequest -> yeuCauRepository.findById(yeuCauRequest.getMaYeuCau())
          .orElseThrow(() -> new AppException(ErrorCode.MA_YEUCAU_NOT_FOUND, "Yêu cầu không tồn tại: " + yeuCauRequest.getMaYeuCau())))
        .toList();
      booKingUpdate.getYeuCaus().clear();
      booKingUpdate.getYeuCaus().addAll(yeuCausMoi);
    }

    // Lưu KhachHangBooKing
    KhachHangBooKing khachHangBooKing = khachHangBooKingMapper.toCreateKhachHangBooKing(request.getKhachHangBooKing());
    khachHangBooKing = khachHangBooKingRepository.save(khachHangBooKing);

    // Lưu ThongTinLienHeBooKing
    ThongTinLienHeBooKing thongTinLienHeBooKing = thongTinLienHeBooKingMapper.toThongTinLienHeBooKing(request.getThongTinLienHeBooKing());
    thongTinLienHeBooKing = thongTinLienHeRepository.save(thongTinLienHeBooKing);

    // Khởi tạo tổng tiền
    AtomicReference<BigDecimal> tongTien = new AtomicReference<>(BigDecimal.ZERO);

    // Kiểm tra ChiTietBooKings không rỗng
    if (request.getChiTietBooKings() == null || request.getChiTietBooKings().isEmpty()) {
      throw new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY);
    }


    var authentication = SecurityContextHolder.getContext().getAuthentication();
    List<ChiTietBooking> chiTietBookings = new ArrayList<>();
    List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList = new ArrayList<>();
    int stt = 1;

    for (ChiTietBooKingRequest chiTietBooKingRequest : request.getChiTietBooKings()) {
      String maLoaiPhong = chiTietBooKingRequest.getLoaiPhong().getMaLoaiPhong();
      LoaiPhong loaiPhong = loaiPhongRepository.findById(maLoaiPhong)
        .orElseThrow(() -> new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND));
      LoaiGia loaiGia = loaiGiaRepository.findById(chiTietBooKingRequest.getLoaiGia().getMaLoaiGia())
        .orElseThrow(() -> new AppException(ErrorCode.LOAI_GIA_NOT_FOUND));
      GiaPhong giaPhong = giaPhongRepository.findById(chiTietBooKingRequest.getGiaPhong().getMaGiaPhong())
        .orElseThrow(() -> new AppException(ErrorCode.GIA_PHONG_ERROR));


      // Kiểm tra ngày đến và ngày đi nếu đã xếp phòng
//      boolean kiemTraNgayDenVaNgayDi = false;
//      if (booKing.isDaXepPhong()) {
//        int updateXP = xepPhongRepository.updateNgayDenNgayDi(maBooking, request.getNgayDen(), request.getNgayDi());
//        if (updateXP == 0) {
//          throw new AppException(ErrorCode.XEP_PHONG_IS_EMPTY);
//        }
//        if (!booKing.getNgayDen().isEqual(request.getNgayDen()) || !booKing.getNgayDi().isEqual(request.getNgayDi())) {
//          kiemTraNgayDenVaNgayDi = true;
//        }
//      }

      // Kiểm tra ngày đến và ngày đi nếu đã xếp phòng
//      if (kiemTraNgayDenVaNgayDi) {

        if(chiTietBooKingRequest.getMaChiTietBooking() != null && !chiTietBooKingRequest.getMaChiTietBooking().isEmpty()) {
          ChiTietBooking chiTietBooking = chiTietBooKingRepository.findById(chiTietBooKingRequest.getMaChiTietBooking())
            .orElseThrow(() -> new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY,
              "Chi tiết booking không tồn tại: " + chiTietBooKingRequest.getMaChiTietBooking()));

          List<XepPhongBooKing> getXepPhongBooKings = xepPhongRepository.getXepPhongBooKingsByMaBooking(maBooking);

          for (XepPhongBooKing xp : getXepPhongBooKings) {
            int countTrung = xepPhongRepository.kiemTraPhongDaTonTai(
              xp.getPhong().getMaPhong(), chiTietBooKingRequest.getNgayDen(), chiTietBooKingRequest.getNgayDi(), xp.getMaXepPhongBooking());

            log.info(xp.getPhong().getMaPhong() + " - " + xp.getNgayDen() + " - " + xp.getNgayDi() + " - " + xp.getMaXepPhongBooking());

            if (countTrung > 0) {
              System.out.println("Oke");
              throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING,
                "Ngày đến: " + DateConverter.converDate(xp.getNgayDen()) +
                  ", Ngày đi: " + DateConverter.converDate(xp.getNgayDi()) +
                  " do đã xếp phòng nên bị trùng với booking khác, Phòng: " + xp.getPhong().getMaPhong() + " đã tồn tại.");
            }
          }
        }else {
          // Lấy danh sách phòng từ request
          List<DanhSachPhongChiTietRequest> dsPhongkt = chiTietBooKingRequest.getDanhSachPhongChiTiets();
          if (dsPhongkt == null || dsPhongkt.isEmpty()) {
            throw new AppException(ErrorCode.DANH_SACH_PHONG_CHI_TIET_NOT_FOUND);
          }

          for(DanhSachPhongChiTietRequest phongDTO : dsPhongkt) {
            Phong phong = phongRepository.findById(phongDTO.getMaPhong())
              .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng: " + phongDTO.getMaPhong()));

            // Kiểm tra phòng đã tồn tại trong khoảng thời gian
            int kiemTraPhongDaTonTai = chiTietBooKingRepository.kiemTraMaPhongDaTonTaiTrongChiTietBooking(
              phongDTO.getMaPhong(), chiTietBooKingRequest.getNgayDen(), chiTietBooKingRequest.getNgayDi());
            if (kiemTraPhongDaTonTai > 0) {
              throw new AppException(ErrorCode.ERROR_PHONG_DA_DAT,
                "Phòng " + phongDTO.getMaPhong() + " đã tồn tại trong khoảng thời gian này.");
            }
          }
        }



      // Tính số ngày thuê
      LocalDate ngayDen = chiTietBooKingRequest.getNgayDen();
      LocalDate ngayDi = chiTietBooKingRequest.getNgayDi();
      if (ngayDen == null || ngayDi == null) {
        throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY, "Ngày đến hoặc ngày đi không được null");
      }
      long soDemThueLong = ChronoUnit.DAYS.between(ngayDen, ngayDi);
      if (soDemThueLong < 0) {
        throw new AppException(ErrorCode.ERROR_NGAYDI, "Ngày đi phải sau ngày đến");
      }
      int soDemThue = (soDemThueLong == 0) ? 1 : (int) soDemThueLong;

      /// / PHU THU EXTRA BED
      BigDecimal tienExtra = chiTietBooKingRequest.getGiaExtraBed()
        .multiply(BigDecimal.valueOf(soDemThue))
        .multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuongExtraBed()));

      /// / PHU THU TRE EM
      BigDecimal tienPhuThuTreEm = chiTietBooKingRequest.getGiaPhuThuTreEm()
        .multiply(BigDecimal.valueOf(soDemThue))
        .multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuongPhuThuTreEm()));

      /// / PHU THU AN SANG
      BigDecimal tienPhuThuAnSang = chiTietBooKingRequest.getGiaPhuThuAnSang()
        .multiply(BigDecimal.valueOf(soDemThue))
        .multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuongPhuThuAnSang()));

      // Kiểm tra số lượng phòng trống
      LoaiPhongTrongDTO kiemTraLoaiPhongTrong = loaiPhongRepository
        .kiemTraLoaiPhongTrongMaLoaiPhong(chiTietBooKingRequest.getNgayDen(), chiTietBooKingRequest.getNgayDi(), maLoaiPhong);
      long soPhongTrong = 0;
      if (!chiTietBooKingRequest.getMaChiTietBooking().isEmpty()) {
        ChiTietBooking chiTietBooking = chiTietBooKingRepository.findById(chiTietBooKingRequest.getMaChiTietBooking())
          .orElseThrow(() -> new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY,
            "Chi tiết booking không tồn tại: " + chiTietBooKingRequest.getMaChiTietBooking()));
         long soPhongDB = chiTietBooking.getSoLuong();

        if (chiTietBooKingRequest.getSoLuong() > soPhongDB) {
          soPhongTrong = chiTietBooKingRequest.getSoLuong() - soPhongDB;
        }
      }
      if (kiemTraLoaiPhongTrong.getSoPhongTrong() < soPhongTrong) {
        throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING,
          "Ngày đến: " + chiTietBooKingRequest.getNgayDen() + ", Ngày đi: " + chiTietBooKingRequest.getNgayDi() +
            ", Phòng: " + maLoaiPhong + " không đủ.");
      }

      // Kiểm tra trùng mã phòng trong request
      Set<String> maPhongSet = new HashSet<>();
      for (DanhSachPhongChiTietRequest phongDTO : chiTietBooKingRequest.getDanhSachPhongChiTiets()) {
        String maPhong = phongDTO.getMaPhong();
        if (!maPhongSet.add(maPhong)) {
          throw new AppException(ErrorCode.DUPLICATE_MA_PHONG, "Phòng " + maPhong + " bị trùng lặp trong request.");
        }
      }

      ChiTietBooking chiTietBooking = null;
      if (!chiTietBooKingRequest.getMaChiTietBooking().isEmpty()) {
        chiTietBooking = chiTietBooKingRepository.findById(chiTietBooKingRequest.getMaChiTietBooking())
          .orElseThrow(() -> new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY,
            "Chi tiết booking không tồn tại: " + chiTietBooKingRequest.getMaChiTietBooking()));
      }

      boolean duLieuMoi = chiTietBooking == null;
      if (!duLieuMoi) {
        // Cập nhật ChiTietBooking hiện có
        if (!maLoaiPhong.equals(chiTietBooking.getLoaiPhong().getMaLoaiPhong())) {
          List<Map<String, Object>> xepPhong = xepPhongRepository.DaXepPhong(chiTietBooKingRequest.getMaChiTietBooking());
          if (!xepPhong.isEmpty()) {
            StringBuilder listPhong = new StringBuilder();
            for (Map<String, Object> xep : xepPhong) {
              listPhong.append(xep.get("ma_phong")).append(",");
            }
            throw new AppException(ErrorCode.ERROR_LOAI_PHONG_MISMATCH, "Loại phòng không khớp vì đã check-in: " + listPhong);
          } else {
            xepPhongRepository.DeleteXepPhongTheoChiTiet(chiTietBooKingRequest.getMaChiTietBooking());
            booKingUpdate.setDaXepPhong(false);
          }
        }


        List<DanhSachPhongChiTietRequest> dsPhongkt = chiTietBooKingRequest.getDanhSachPhongChiTiets();
        List<String> maPhongInput = dsPhongkt.stream().map(DanhSachPhongChiTietRequest::getMaPhong).toList();
        List<XepPhongBooKing> xepPhongBooKings = xepPhongRepository.getXepPhongByMaChiTiet(chiTietBooking.getMaChiTietBooking());
        List<String> maPhongDaXep = xepPhongBooKings.stream()
          .filter(xp -> xp.getThoiGianXoa() == null)
          .map(xp -> xp.getPhong().getMaPhong())
          .toList();

        // Đếm tần suất mã phòng
        Map<String, Long> inputPhongCount = maPhongInput.stream()
          .collect(Collectors.groupingBy(maPhong -> maPhong, Collectors.counting()));
        Map<String, Long> daXepPhongCount = maPhongDaXep.stream()
          .collect(Collectors.groupingBy(maPhong -> maPhong, Collectors.counting()));

        // Kiểm tra số lượng phòng hợp lệ
        if (chiTietBooKingRequest.getSoLuong() != maPhongInput.size()) {
          throw new AppException(ErrorCode.SO_LUONG_ERROR,
            "Số lượng phòng trong request không khớp với danh sách phòng: " + maPhongInput);
        }

        // Kiểm tra sự thay đổi
        boolean roomsChanged = chiTietBooKingRequest.getSoLuong() != chiTietBooking.getSoLuong() ||
          !inputPhongCount.equals(daXepPhongCount);

        // Cập nhật ChiTietBooking
        chiTietBooking.setLoaiPhong(loaiPhong);
        chiTietBooking.setLoaiGia(loaiGia);
        chiTietBooking.setGiaPhong(giaPhong);
        chiTietBooking = chiTietBooKingMapper.toCreChiTietDatPhong(chiTietBooKingRequest);
        chiTietBooking.setBooKing(booKing);

        // Khai báo dsPhong một lần duy nhất
        List<DanhSachPhongChiTiet> dsPhong = new ArrayList<>();

        // Xử lý danh sách phòng chi tiết và XepPhongBooKing
        if (roomsChanged) {
          // Xác định các phòng cần giữ (đã check-in hoặc thanh toán) và các phòng cần xóa
          List<String> roomsToKeep = xepPhongBooKings.stream()
            .filter(xp -> xp.getThoiGianXoa() == null && (xp.isDaNhanPhong() || xp.isDaThanhToan()))
            .map(xp -> xp.getPhong().getMaPhong())
            .collect(Collectors.toList());

          // Kiểm tra xem các phòng đã check-in có nằm trong maPhongInput không
          for (String maPhong : roomsToKeep) {
            if (!maPhongInput.contains(maPhong)) {
              throw new AppException(ErrorCode.CHITIETBOOKING_DA_XEP_PHONG,
                "Phòng " + maPhong + " đã check-in hoặc thanh toán, không thể xóa khỏi danh sách phòng. Dòng " + stt + ".");
            }
          }

          // Xóa các bản ghi XepPhongBooKing chưa check-in và chưa thanh toán
          for (XepPhongBooKing xepPhong : xepPhongBooKings) {
            if (xepPhong.getThoiGianXoa() == null && !roomsToKeep.contains(xepPhong.getPhong().getMaPhong())) {
              xepPhongRepository.udpateThoiGianXoaXepPhong(chiTietBooking.getMaChiTietBooking(), xepPhong.getPhong().getMaPhong());
            }
          }

          // Xóa toàn bộ DanhSachPhongChiTiet cũ
          danhSachPhongChiTietRepository.deleteByChiTietBooking_MaChiTietBooking(chiTietBooking.getMaChiTietBooking());

          // Tạo mới hoặc cập nhật XepPhongBooKing và DanhSachPhongChiTiet cho danh sách phòng từ request
          int sttMoi = 1;
          for (DanhSachPhongChiTietRequest phongDTO : dsPhongkt) {
            String maPhong = phongDTO.getMaPhong();
            Phong phong = phongRepository.findById(maPhong)
              .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng: " + maPhong));

            // Kiểm tra phòng đã tồn tại trong khoảng thời gian
            int kiemTraPhongDaTonTai = chiTietBooKingRepository.kiemTraMaPhongDaTonTaiTrongChiTietBookingUpdate(
              maPhong, ngayDen, ngayDi, chiTietBooking.getMaChiTietBooking());
            if (kiemTraPhongDaTonTai > 0) {
              throw new AppException(ErrorCode.ERROR_PHONG_DA_DAT,
                "Phòng " + maPhong + " đã tồn tại trong khoảng thời gian này.");
            }

            // Tạo hoặc cập nhật DanhSachPhongChiTiet
            DanhSachPhongChiTiet danhSachPhongChiTiet = new DanhSachPhongChiTiet();
            danhSachPhongChiTiet.setMaPhong(maPhong);
            danhSachPhongChiTiet.setChiTietBooking(chiTietBooking);
            danhSachPhongChiTiet.setSoGiuongThem(phongDTO.getSoGiuongThem());
            dsPhong.add(danhSachPhongChiTiet);

            Log_DanhSachPhongChiTiet log = new Log_DanhSachPhongChiTiet();
            log.setMachiTietBooking(chiTietBooking.getMaChiTietBooking());
            log.setMaPhong(maPhong);
            log.setSoGiuongThem(phongDTO.getSoGiuongThem());
            log.setThoiGianTao(LocalDateTime.now());
            log.setTrangThai("UPDATE");
            logDanhSachPhongChiTietList.add(log);

            // Nếu phòng đã check-in hoặc thanh toán, chỉ cập nhật
            XepPhongBooKing existingXepPhong = xepPhongBooKings.stream()
              .filter(xp -> xp.getPhong().getMaPhong().equals(maPhong) && xp.getThoiGianXoa() == null)
              .findFirst()
              .orElse(null);

            int extraBed = (sttMoi - 1 < chiTietBooking.getSoLuongExtraBed()) ? 1 : 0;
            BigDecimal gia = (sttMoi - 1 < chiTietBooking.getSoLuongExtraBed()) ? chiTietBooking.getGiaExtraBed() : BigDecimal.ZERO;

            if (existingXepPhong != null && (existingXepPhong.isDaNhanPhong() || existingXepPhong.isDaThanhToan())) {
              updateXepPhongBooKing(
                existingXepPhong,
                chiTietBooking,
                booKing,
                maPhong,
                sttMoi,
                extraBed,
                gia,
                authentication
              );
            } else {
              createSaveXepPhongBooKing(
                chiTietBooking,
                booKing,
                maPhong,
                sttMoi,
                extraBed,
                gia,
                authentication,
                chiTietBooking.getGiaPhongTheoNgays()
              );
            }
            sttMoi++;
          }
        } else {
          // Không thay đổi danh sách phòng, chỉ cập nhật DanhSachPhongChiTiet
          danhSachPhongChiTietRepository.deleteByChiTietBooking_MaChiTietBooking(chiTietBooking.getMaChiTietBooking());
          int sttMoi = 1;
          for (DanhSachPhongChiTietRequest phongDTO : dsPhongkt) {
            String maPhong = phongDTO.getMaPhong();
            Phong phong = phongRepository.findById(maPhong)
              .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng: " + maPhong));

            DanhSachPhongChiTiet danhSachPhongChiTiet = new DanhSachPhongChiTiet();
            danhSachPhongChiTiet.setMaPhong(phong.getMaPhong());
            danhSachPhongChiTiet.setChiTietBooking(chiTietBooking);
            danhSachPhongChiTiet.setSoGiuongThem(phongDTO.getSoGiuongThem());
            dsPhong.add(danhSachPhongChiTiet);

            Log_DanhSachPhongChiTiet log = new Log_DanhSachPhongChiTiet();
            log.setMachiTietBooking(chiTietBooking.getMaChiTietBooking());
            log.setMaPhong(phong.getMaPhong());
            log.setSoGiuongThem(phongDTO.getSoGiuongThem());
            log.setThoiGianTao(LocalDateTime.now());
            log.setTrangThai("UPDATE");
            logDanhSachPhongChiTietList.add(log);

            // Cập nhật XepPhongBooKing nếu cần (ví dụ: cập nhật extraBed, giá)
            XepPhongBooKing xepPhong = xepPhongBooKings.stream()
              .filter(xp -> xp.getPhong().getMaPhong().equals(maPhong) && xp.getThoiGianXoa() == null)
              .findFirst()
              .orElse(null);
            if (xepPhong != null) {
              int extraBed = (sttMoi - 1 < chiTietBooking.getSoLuongExtraBed()) ? 1 : 0;
              BigDecimal gia = (sttMoi - 1 < chiTietBooking.getSoLuongExtraBed()) ? chiTietBooking.getGiaExtraBed() : BigDecimal.ZERO;
              updateXepPhongBooKing(
                xepPhong,
                chiTietBooking,
                booKing,
                maPhong,
                sttMoi,
                extraBed,
                gia,
                authentication
              );
            }
            sttMoi++;
          }
        }

        // Lưu DanhSachPhongChiTiet
        danhSachPhongChiTietRepository.saveAll(dsPhong);
        chiTietBooking.setDanhSachPhongChiTiets(dsPhong);

        // Xử lý giá phòng theo ngày
        if (chiTietBooKingRequest.getGiaPhongTheoNgays() == null || chiTietBooKingRequest.getGiaPhongTheoNgays().isEmpty()) {
          throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
        }
        giaPhongTheoNgayRepository.xoaGiaPhongTheoNgay(chiTietBooking.getMaChiTietBooking());
        List<GiaPhongTheoNgay> giaPhongTheoNgays = new ArrayList<>();
        List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgay = new ArrayList<>();
        for (GiaPhongTheoNgayRequest giaPhongTheoNgayRequest : chiTietBooKingRequest.getGiaPhongTheoNgays()) {
          if (giaPhongTheoNgayRequest.getNgay() == null || giaPhongTheoNgayRequest.getGia() == null) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
          }
          GiaPhongTheoNgay giaPhongTheoNgay = new GiaPhongTheoNgay();
          giaPhongTheoNgay.setChiTietBooking(chiTietBooking);
          giaPhongTheoNgay.setNgay(giaPhongTheoNgayRequest.getNgay());
          giaPhongTheoNgay.setGia(giaPhongTheoNgayRequest.getGia());
          giaPhongTheoNgay.setMaLoaiPhong(giaPhongTheoNgayRequest.getMaLoaiPhong());
          giaPhongTheoNgays.add(giaPhongTheoNgay);

          Log_GiaPhongTheoNgay log = new Log_GiaPhongTheoNgay();
          log.setMaChiTietBooking(chiTietBooking.getMaChiTietBooking());
          log.setNgay(giaPhongTheoNgayRequest.getNgay());
          log.setGia(giaPhongTheoNgayRequest.getGia());
          log.setMaLoaiPhong(giaPhongTheoNgayRequest.getMaLoaiPhong());
          log.setThoiGianTao(LocalDateTime.now());
          log.setTrangThai(duLieuMoi ? "CREATE" : "UPDATE");
          logGiaPhongTheoNgay.add(log);

          tongTien.updateAndGet(current ->
            current.add(giaPhongTheoNgayRequest.getGia().multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuong()))));
        }

        tongTien.updateAndGet(current ->
            current.add(tienExtra).add(tienPhuThuTreEm).add(tienPhuThuAnSang)
        );

        giaPhongTheoNgayRepository.saveAll(giaPhongTheoNgays);
        logGiaPhongTheoNgayRepository.saveAll(logGiaPhongTheoNgay);
        chiTietBooking.setGiaPhongTheoNgays(giaPhongTheoNgays);
      } else {
        // Tạo mới ChiTietBooking
        chiTietBooking = chiTietBooKingMapper.toCreChiTietDatPhong(chiTietBooKingRequest);
        chiTietBooking.setBooKing(booKing);
        chiTietBooking.setLoaiPhong(loaiPhong);
        chiTietBooking.setLoaiGia(loaiGia);
        chiTietBooking.setGiaPhong(giaPhong);
        chiTietBooking.setMaChiTietBooking(null);
        chiTietBooking = chiTietBooKingRepository.save(chiTietBooking);

        List<DanhSachPhongChiTietRequest> dsPhongkt = chiTietBooKingRequest.getDanhSachPhongChiTiets();
        List<DanhSachPhongChiTiet> dsPhong = new ArrayList<>();
        int sttMoi = 1;
        for (DanhSachPhongChiTietRequest phongDTO : dsPhongkt) {
          String maPhong = phongDTO.getMaPhong();
          Phong phong = phongRepository.findById(maPhong)
            .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng: " + maPhong));

          int kiemTraPhongDaTonTai = chiTietBooKingRepository.kiemTraMaPhongDaTonTaiTrongChiTietBooking(
            maPhong, chiTietBooKingRequest.getNgayDen(), chiTietBooKingRequest.getNgayDi());
          if (kiemTraPhongDaTonTai > 0) {
            throw new AppException(ErrorCode.ERROR_PHONG_DA_DAT,
              "Phòng " + maPhong + " đã tồn tại trong khoảng thời gian này.");
          }

          int extraBed = (sttMoi - 1 < chiTietBooking.getSoLuongExtraBed()) ? 1 : 0;
          BigDecimal gia = (sttMoi - 1 < chiTietBooking.getSoLuongExtraBed()) ? chiTietBooking.getGiaExtraBed() : BigDecimal.ZERO;

          createSaveXepPhongBooKing(
            chiTietBooking,
            booKing,
            maPhong,
            sttMoi,
            extraBed,
            gia,
            authentication,
            chiTietBooking.getGiaPhongTheoNgays()
          );

          DanhSachPhongChiTiet danhSachPhongChiTiet = new DanhSachPhongChiTiet();
          danhSachPhongChiTiet.setMaPhong(maPhong);
          danhSachPhongChiTiet.setChiTietBooking(chiTietBooking);
          danhSachPhongChiTiet.setSoGiuongThem(phongDTO.getSoGiuongThem());
          dsPhong.add(danhSachPhongChiTiet);

          Log_DanhSachPhongChiTiet log = new Log_DanhSachPhongChiTiet();
          log.setMachiTietBooking(chiTietBooking.getMaChiTietBooking());
          log.setMaPhong(maPhong);
          log.setSoGiuongThem(phongDTO.getSoGiuongThem());
          log.setThoiGianTao(LocalDateTime.now());
          log.setTrangThai("CREATE");
          logDanhSachPhongChiTietList.add(log);
          sttMoi++;
        }
        chiTietBooking.setDanhSachPhongChiTiets(dsPhong);
        danhSachPhongChiTietRepository.saveAll(dsPhong);

        // Xử lý giá phòng theo ngày
        if (chiTietBooKingRequest.getGiaPhongTheoNgays() == null || chiTietBooKingRequest.getGiaPhongTheoNgays().isEmpty()) {
          throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
        }
        giaPhongTheoNgayRepository.xoaGiaPhongTheoNgay(chiTietBooking.getMaChiTietBooking());
        List<GiaPhongTheoNgay> giaPhongTheoNgays = new ArrayList<>();
        List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgay = new ArrayList<>();
        for (GiaPhongTheoNgayRequest giaPhongTheoNgayRequest : chiTietBooKingRequest.getGiaPhongTheoNgays()) {
          if (giaPhongTheoNgayRequest.getNgay() == null || giaPhongTheoNgayRequest.getGia() == null) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
          }
          GiaPhongTheoNgay giaPhongTheoNgay = new GiaPhongTheoNgay();
          giaPhongTheoNgay.setChiTietBooking(chiTietBooking);
          giaPhongTheoNgay.setNgay(giaPhongTheoNgayRequest.getNgay());
          giaPhongTheoNgay.setGia(giaPhongTheoNgayRequest.getGia());
          giaPhongTheoNgay.setMaLoaiPhong(giaPhongTheoNgayRequest.getMaLoaiPhong());
          giaPhongTheoNgays.add(giaPhongTheoNgay);

          Log_GiaPhongTheoNgay log = new Log_GiaPhongTheoNgay();
          log.setMaChiTietBooking(chiTietBooking.getMaChiTietBooking());
          log.setNgay(giaPhongTheoNgayRequest.getNgay());
          log.setGia(giaPhongTheoNgayRequest.getGia());
          log.setMaLoaiPhong(giaPhongTheoNgayRequest.getMaLoaiPhong());
          log.setThoiGianTao(LocalDateTime.now());
          log.setTrangThai("CREATE");
          logGiaPhongTheoNgay.add(log);



          tongTien.updateAndGet(current ->
            current.add(giaPhongTheoNgayRequest.getGia().multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuong()))));
        }
        tongTien.updateAndGet(current ->
          current.add(tienExtra).add(tienPhuThuTreEm).add(tienPhuThuAnSang)
        );

        giaPhongTheoNgayRepository.saveAll(giaPhongTheoNgays);
        logGiaPhongTheoNgayRepository.saveAll(logGiaPhongTheoNgay);
        chiTietBooking.setGiaPhongTheoNgays(giaPhongTheoNgays);
      }

      chiTietBookings.add(chiTietBooking);
      booKingUpdate.setDaXepPhong(true);
      stt++;
    }

    // Cập nhật BooKing
    booKingUpdate.setChiTietBookings(chiTietBookings);
    booKingUpdate.setMaBooking(maBooking);
    booKingUpdate.setKhachHangBooKing(khachHangBooKing);
    booKingUpdate.setThongTinLienHeBooKing(thongTinLienHeBooKing);
    booKingUpdate.setThoiGianTao(booKing.getThoiGianTao());
    booKingUpdate.setNguoiTao(booKing.getNguoiTao());
    booKingUpdate.setLoaiNguonKhach(request.getLoaiNguonKhach().toUpperCase());
    booKingUpdate.setTongTien(request.getTongTien());

    // Kiểm tra tổng tiền và số lượng chi tiết booking
    if (tongTien.get().compareTo(request.getTongTien()) != 0) {
      throw new AppException(ErrorCode.TONG_TIEN_KHONG_KHOP);
    }
    if (request.getChiTietBooKings().size() != chiTietBookings.size()) {
      throw new AppException(ErrorCode.SO_LUONG_BOOKING_KHONG_KHOP);
    }

    booKingUpdate = booKingRepository.save(booKingUpdate);
    log_DanhSachPhongChiTietRepositoty.saveAll(logDanhSachPhongChiTietList);

    return booKingMapper.toBooKingResponse(booKingUpdate);
  }

    /**
     * Xác thực các thực thể liên quan (DanhXung, NhomKhachHang, KhuVuc,...)
     * @param request Yêu cầu chứa thông tin các thực thể cần xác thực
     * @throws AppException Nếu bất kỳ thực thể nào không tồn tại
     */
    private void validateRelatedEntities(BooKingRequest request) {
        findEntityOrThrow(
                () -> danhXungRepository.findById(request.getDanhXung().getMaDanhXung()),
                ErrorCode.DANH_XUNG_NOT_FOUND, request.getDanhXung().getMaDanhXung());
        findEntityOrThrow(
                () -> nhomKhachHangRepository.findById(request.getNhomKhachHang().getMaNhomKhachHang()),
                ErrorCode.NHOM_KHACH_HANG_NOT_FOUND, request.getNhomKhachHang().getMaNhomKhachHang());

        findEntityOrThrow(
                () -> trangThaiBooKingRepository.findById(request.getTrangThaiBooKing().getMaTrangThaiBooKing()),
                ErrorCode.TRANG_THAI_BOOKING_NOT_FOUND, request.getTrangThaiBooKing().getMaTrangThaiBooKing());
        findEntityOrThrow(
                () -> loaiTheRepository.findById(request.getLoaiThe().getMaLoaiThe()),
                ErrorCode.LOAI_THE_NOT_FOUND, request.getLoaiThe().getMaLoaiThe());
    }

    // Helper method để tránh lặp code
    private <T, ID> T findEntityOrThrow(Supplier<Optional<T>> repositorySupplier, ErrorCode errorCode, ID id) {
        return repositorySupplier.get()
                .orElseThrow(() -> new AppException(errorCode, String.valueOf(id)));
    }

    /**
     * Lưu thông tin khách hàng
     */
    private KhachHangBooKing saveCustomerInfo(BooKingRequest request) {
        KhachHangBooKing khachHangBooKing = khachHangBooKingMapper.toCreateKhachHangBooKing(request.getKhachHangBooKing());
        khachHangBooKing.setMaKhachHangBooking(khachHangBooKingService.generateNewId());
        khachHangBooKing.setNgayTao(LocalDateTime.now());
        return khachHangBooKingRepository.save(khachHangBooKing);
    }

    /**
     * Lưu thông tin liên hệ
     */
    private ThongTinLienHeBooKing saveContactInfo(BooKingRequest request) {
        ThongTinLienHeBooKing thongTinLienHeBooKing = thongTinLienHeBooKingMapper.toThongTinLienHeBooKing(request.getThongTinLienHeBooKing());
        return thongTinLienHeRepository.save(thongTinLienHeBooKing);
    }

    /**
     * Tạo và lưu booking chính
     */
    private BooKing createAndSaveMainBooking(BooKingRequest request, KhachHangBooKing khachHangBooKing, ThongTinLienHeBooKing thongTinLienHeBooKing) {
        BooKing booKing = booKingMapper.toCreateBooKing(request);
        booKing.setMaBooking(generateNewBookingId());
        booKing.setKhachHangBooKing(khachHangBooKing);
        booKing.setThongTinLienHeBooKing(thongTinLienHeBooKing);

        // Xử lý YeuCau
        if (CollectionUtils.isNotEmpty(request.getYeuCaus())) {
            List<YeuCau> yeuCaus = request.getYeuCaus().stream()
                    .map(yeuCauRequest -> findEntityOrThrow(
                            () -> yeuCauRepository.findById(yeuCauRequest.getMaYeuCau()),
                            ErrorCode.MA_YEUCAU_NOT_FOUND,
                            yeuCauRequest.getMaYeuCau()
                    ))
                    .collect(Collectors.toList());
            booKing.setYeuCaus(yeuCaus);
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        booKing.setNguoiTao(authentication.getName());
        booKing.setThoiGianTao(LocalDateTime.now());
        booKing.setLoaiNguonKhach(request.getLoaiNguonKhach().toUpperCase());
        return booKingRepository.save(booKing);
    }

    /**
     * Validate và tính toán số đêm thuê
     */
    private int validateAndCalculateNights(LocalDate ngayDen, LocalDate ngayDi) {
        if (ngayDen == null || ngayDi == null) {
            throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY, "Ngày đến hoặc ngày đi không được null");
        }
        long soDemThueLong = ChronoUnit.DAYS.between(ngayDen, ngayDi);
        if (soDemThueLong < 0) {
            throw new AppException(ErrorCode.ERROR_NGAYDI, "Ngày đi phải sau ngày đến");
        }
        return (soDemThueLong == 0) ? 1 : (int) soDemThueLong;
    }

    /**
     * Validate phòng không bị trùng lặp và đã được đặt
     */
    private void validateRoomDuplication(List<DanhSachPhongChiTietRequest> danhSachPhongChiTiets, LocalDate ngayDen, LocalDate ngayDi) {
        Set<String> maPhongSet = new HashSet<>();
        for (DanhSachPhongChiTietRequest req : danhSachPhongChiTiets) {
            String maPhong = req.getMaPhong();
            if (!maPhongSet.add(maPhong)) {
                throw new AppException(ErrorCode.DUPLICATE_MA_PHONG, "Phòng " + maPhong + " bị trùng lặp trong request.");
            }
            int kiemTraPhong = chiTietBooKingRepository.kiemTraMaPhongDaTonTaiTrongChiTietBooking(maPhong, ngayDen, ngayDi);
            if (kiemTraPhong > 0) {
                throw new AppException(ErrorCode.ERROR_PHONG_DA_DAT, "Phòng " + maPhong + " đã được đặt trong khoảng thời gian này.");
            }
        }
    }

    /**
     * Validate số lượng phòng extra bed
     */
    private void validateExtraBedRooms(List<DanhSachPhongChiTietRequest> danhSachPhongChiTiets, int soLuongExtraBed) {
        if (danhSachPhongChiTiets.isEmpty()) {
            return;
        }

        int kiemTraSoLuongPhongExtra = 0;
        for (DanhSachPhongChiTietRequest phongDTO : danhSachPhongChiTiets) {
            Phong phongKT = phongRepository.findById(phongDTO.getMaPhong())
                    .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng với mã: " + phongDTO.getMaPhong()));
            if (phongKT.getSoGiuongThem() > 0) {
                kiemTraSoLuongPhongExtra++;
            }
        }

        if (soLuongExtraBed > 0 && kiemTraSoLuongPhongExtra < soLuongExtraBed) {
            throw new AppException(ErrorCode.ERROR_SO_LUONG_PHONG_EXTRA);
        }
    }

    /**
     * Xử lý danh sách phòng chi tiết
     */
    private List<DanhSachPhongChiTiet> processDanhSachPhongChiTiet(
            List<DanhSachPhongChiTietRequest> requests,
            ChiTietBooking chiTietBooking,
            List<Log_DanhSachPhongChiTiet> logList) {

        List<DanhSachPhongChiTiet> dsPhong = new ArrayList<>();
        for (DanhSachPhongChiTietRequest phongDTO : requests) {
            Phong phong = phongRepository.findById(phongDTO.getMaPhong())
                    .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng với mã: " + phongDTO.getMaPhong()));

            DanhSachPhongChiTiet danhSachPhongChiTiet = new DanhSachPhongChiTiet();
            danhSachPhongChiTiet.setMaPhong(phong.getMaPhong());
            danhSachPhongChiTiet.setChiTietBooking(chiTietBooking);
            danhSachPhongChiTiet.setSoGiuongThem(phongDTO.getSoGiuongThem());
            dsPhong.add(danhSachPhongChiTiet);

            // Lưu log
            Log_DanhSachPhongChiTiet logDanhSachPhongChiTiet = new Log_DanhSachPhongChiTiet();
            logDanhSachPhongChiTiet.setMaPhong(phong.getMaPhong());
            logDanhSachPhongChiTiet.setSoGiuongThem(phongDTO.getSoGiuongThem());
            logDanhSachPhongChiTiet.setMachiTietBooking(chiTietBooking.getMaChiTietBooking());
            logDanhSachPhongChiTiet.setTrangThai("CREATED");
            logDanhSachPhongChiTiet.setThoiGianTao(LocalDateTime.now());
            logList.add(logDanhSachPhongChiTiet);
        }
        return dsPhong;
    }

    /**
     * Xử lý giá phòng theo ngày
     */
    private BigDecimal processGiaPhongTheoNgay(
            List<GiaPhongTheoNgayRequest> requests,
            ChiTietBooking chiTietBooking,
            List<GiaPhongTheoNgay> giaPhongTheoNgays,
            List<Log_GiaPhongTheoNgay> logList) {

        if (requests == null || requests.isEmpty()) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
        }

        BigDecimal tienPhongTheoNgay = BigDecimal.ZERO;
        for (GiaPhongTheoNgayRequest giaPhongTheoNgayRequest : requests) {
            if (giaPhongTheoNgayRequest.getNgay() == null || giaPhongTheoNgayRequest.getGia() == null) {
                throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
            }

            GiaPhongTheoNgay giaPhongTheoNgay = new GiaPhongTheoNgay();
            giaPhongTheoNgay.setChiTietBooking(chiTietBooking);
            giaPhongTheoNgay.setNgay(giaPhongTheoNgayRequest.getNgay());
            giaPhongTheoNgay.setGia(giaPhongTheoNgayRequest.getGia());
            giaPhongTheoNgay.setMaLoaiPhong(giaPhongTheoNgayRequest.getMaLoaiPhong());
            giaPhongTheoNgay.setTrangThai("CREATED");
            giaPhongTheoNgays.add(giaPhongTheoNgay);

            // Lưu log
            Log_GiaPhongTheoNgay log = new Log_GiaPhongTheoNgay();
            log.setMaChiTietBooking(chiTietBooking.getMaChiTietBooking());
            log.setNgay(giaPhongTheoNgayRequest.getNgay());
            log.setGia(giaPhongTheoNgayRequest.getGia());
            log.setMaLoaiPhong(giaPhongTheoNgayRequest.getMaLoaiPhong());
            log.setThoiGianTao(LocalDateTime.now());
            log.setTrangThai("CREATED");
            logList.add(log);

            tienPhongTheoNgay = tienPhongTheoNgay.add(giaPhongTheoNgayRequest.getGia());
        }
        return tienPhongTheoNgay;
    }

    /**
     * Tính tổng tiền cho một chi tiết booking
     */
    private BigDecimal calculateTotalAmount(
            BigDecimal tienPhongTheoNgay,
            int soLuong,
            int soDemThue,
            ChiTietBooKingRequest request) {

        BigDecimal tienExtra = request.getGiaExtraBed()
                .multiply(BigDecimal.valueOf(soDemThue))
                .multiply(BigDecimal.valueOf(request.getSoLuongExtraBed()));

        BigDecimal tienPhuThuTreEm = request.getGiaPhuThuTreEm()
                .multiply(BigDecimal.valueOf(soDemThue))
                .multiply(BigDecimal.valueOf(request.getSoLuongPhuThuTreEm()));

        BigDecimal tienPhuThuAnSang = request.getGiaPhuThuAnSang()
                .multiply(BigDecimal.valueOf(soDemThue))
                .multiply(BigDecimal.valueOf(request.getSoLuongPhuThuAnSang()));

        return tienPhongTheoNgay.multiply(BigDecimal.valueOf(soLuong))
                .add(tienExtra)
                .add(tienPhuThuTreEm)
                .add(tienPhuThuAnSang);
    }

    /**
     * Class để lưu kết quả xử lý chi tiết booking
     */
    private static class ProcessChiTietBookingResult {
        List<ChiTietBooking> chiTietBookings;
        List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList;
        List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgay;
        BigDecimal tongTien;
        boolean khongCoPhong;

        ProcessChiTietBookingResult(
                List<ChiTietBooking> chiTietBookings,
                List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList,
                List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgay,
                BigDecimal tongTien,
                boolean khongCoPhong) {
            this.chiTietBookings = chiTietBookings;
            this.logDanhSachPhongChiTietList = logDanhSachPhongChiTietList;
            this.logGiaPhongTheoNgay = logGiaPhongTheoNgay;
            this.tongTien = tongTien;
            this.khongCoPhong = khongCoPhong;
        }
    }

    /**
     * Xử lý tất cả chi tiết booking
     */
    private ProcessChiTietBookingResult processChiTietBookings(BooKingRequest request, BooKing booKing) {
        AtomicReference<BigDecimal> tongTien = new AtomicReference<>(BigDecimal.ZERO);
        List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList = new ArrayList<>();
        AtomicBoolean khongCoPhong = new AtomicBoolean(false);
        List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgay = new ArrayList<>();

        List<ChiTietBooking> chiTietBookings = request.getChiTietBooKings().stream()
                .map(chiTietBooKingRequest -> {
                    // Validate cơ bản
                    if (chiTietBooKingRequest.getSoLuong() > chiTietBooKingRequest.getSoLuongDuBaoPhong()) {
                        throw new AppException(ErrorCode.SO_LUONG_ERROR);
                    }
                    if (chiTietBooKingRequest.getIndex() == 0) {
                        throw new AppException(ErrorCode.INDEX_ERROR);
                    }

                    // Validate và tính số đêm thuê
                    int soDemThue = validateAndCalculateNights(
                            chiTietBooKingRequest.getNgayDen(),
                            chiTietBooKingRequest.getNgayDi()
                    );

                    // Validate phòng
                    validateRoomDuplication(
                            chiTietBooKingRequest.getDanhSachPhongChiTiets(),
                            chiTietBooKingRequest.getNgayDen(),
                            chiTietBooKingRequest.getNgayDi()
                    );

                    // Validate extra bed
                    if (!chiTietBooKingRequest.getDanhSachPhongChiTiets().isEmpty()) {
                        if (chiTietBooKingRequest.getDanhSachPhongChiTiets().size() != chiTietBooKingRequest.getSoLuong()) {
                            throw new AppException(ErrorCode.ERROR_SO_LUONG_PHONG);
                        }
                        validateExtraBedRooms(
                                chiTietBooKingRequest.getDanhSachPhongChiTiets(),
                                chiTietBooKingRequest.getSoLuongExtraBed()
                        );
                    } else {
                        khongCoPhong.set(true);
                    }

                    // Tạo chi tiết booking
                    ChiTietBooking chiTietBooking = chiTietBooKingMapper.toCreChiTietDatPhong(chiTietBooKingRequest);
                    chiTietBooking.setBooKing(booKing);

                    // Set các entity liên quan
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

                    // Xử lý danh sách phòng chi tiết
                    List<DanhSachPhongChiTiet> dsPhong = processDanhSachPhongChiTiet(
                            chiTietBooKingRequest.getDanhSachPhongChiTiets(),
                            chiTietBooking,
                            logDanhSachPhongChiTietList
                    );
                    chiTietBooking.setDanhSachPhongChiTiets(dsPhong);

                    // Xử lý giá phòng theo ngày
                    List<GiaPhongTheoNgay> giaPhongTheoNgays = new ArrayList<>();
                    BigDecimal tienPhongTheoNgay = processGiaPhongTheoNgay(
                            chiTietBooKingRequest.getGiaPhongTheoNgays(),
                            chiTietBooking,
                            giaPhongTheoNgays,
                            logGiaPhongTheoNgay
                    );
                    chiTietBooking.setGiaPhongTheoNgays(giaPhongTheoNgays);

                    // Tính tổng tiền
                    BigDecimal tong = calculateTotalAmount(
                            tienPhongTheoNgay,
                            chiTietBooking.getSoLuong(),
                            soDemThue,
                            chiTietBooKingRequest
                    );

                    tongTien.updateAndGet(v -> v.add(tong));

                    return chiTietBooking;
                })
                .collect(Collectors.toList());

        return new ProcessChiTietBookingResult(
                chiTietBookings,
                logDanhSachPhongChiTietList,
                logGiaPhongTheoNgay,
                tongTien.get(),
                khongCoPhong.get()
        );
    }

    /**
     * Validate tổng tiền
     */
    private void validateTotalAmount(BigDecimal tongTienTinhDuoc, BigDecimal tongTienRequest) {

        if (tongTienTinhDuoc.compareTo(tongTienRequest) != 0) {
            throw new AppException(ErrorCode.TONG_TIEN_KHONG_KHOP);
        }
    }

    /**
     * Tự động xếp phòng
     */
    private void autoAssignRooms(List<ChiTietBooking> chiTietBookings, BooKing booKing, Authentication authentication) {
        int stt = 1;
        for (ChiTietBooking chiTietBooking : chiTietBookings) {
            List<DanhSachPhongChiTiet> phongChiTietList = chiTietBooking.getDanhSachPhongChiTiets();
            int soPhongCanXep = chiTietBooking.getSoLuong();
            int soExtraBed = chiTietBooking.getSoLuongExtraBed();
            BigDecimal giaExtraBed = chiTietBooking.getGiaExtraBed();

            for (int i = 0; i < soPhongCanXep && i < phongChiTietList.size(); i++) {
                DanhSachPhongChiTiet phongDTO = phongChiTietList.get(i);
                int extraBed = (i < soExtraBed) ? 1 : 0;
                BigDecimal gia = (i < soExtraBed) ? giaExtraBed : BigDecimal.ZERO;

                createSaveXepPhongBooKing(
                        chiTietBooking,
                        booKing,
                        phongDTO.getMaPhong(),
                        stt,
                        extraBed,
                        gia,
                        authentication,
                        chiTietBooking.getGiaPhongTheoNgays()
                );
                stt++;
            }
        }
    }

    // code phan tach chuc nang va gom gon dang loi phan xoa danhsách phòng chi tiết và xoa gia phòng theo ngày



    public void createSaveXepPhongBooKing(ChiTietBooking chiTietBooking, BooKing booKing,String maPhong, int count,int soGiuong, BigDecimal giaExtraBed, Authentication authentication ,List<GiaPhongTheoNgay> giaPhongTheoNgays) {

        BigDecimal tongGiaPhong = BigDecimal.ZERO;
        for(GiaPhongTheoNgay giaPhongTheoNgay : giaPhongTheoNgays){
            tongGiaPhong = tongGiaPhong.add(giaPhongTheoNgay.getGia());
        }

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
        xepPhongBooKing.setThanhTien(tongGiaPhong.add(chiTietBooking.getGiaExtraBed().multiply(new BigDecimal(chiTietBooking.getSoDem()))));
        xepPhongBooKing.setGhiChu(chiTietBooking.getGhiChu());
        xepPhongBooKing.setGiaPhong(chiTietBooking.getGiaPhong());
        xepPhongBooKing.setThoiGianTao(LocalDateTime.now());
        Phong phongAuto = new Phong();
        phongAuto.setMaPhong(maPhong);
        xepPhongBooKing.setPhong(phongAuto);
        xepPhongBooKing.setNguoiTao(authentication.getName());
        xepPhongBooKing.setPhuThuCheckInSom(BigDecimal.ZERO);
        xepPhongBooKing.setPhuThuCheckInSom(BigDecimal.ZERO);


        xepPhongRepository.save(xepPhongBooKing);
    }

    @Transactional
    public void updateXepPhongBooKing(XepPhongBooKing xp,ChiTietBooking chiTietBooking, BooKing booKing,String maPhong, int count,int soGiuong, BigDecimal giaExtraBed, Authentication authentication) {


        XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(xp.getMaXepPhongBooking())
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND, "Không tìm thấy xếp phòng với mã: " + xp.getMaXepPhongBooking()));
        xepPhongBooKing.setNgayDen(chiTietBooking.getNgayDen());
        xepPhongBooKing.setGioDen(chiTietBooking.getGioDen());
        if(xepPhongBooKing.isDaThanhToan()) {
            xepPhongBooKing.setNgayDi(xepPhongBooKing.getNgayDi());
        }else {
            xepPhongBooKing.setNgayDi(chiTietBooking.getNgayDi());
        }
        xepPhongBooKing.setNgayDi(chiTietBooking.getNgayDi());
        xepPhongBooKing.setGioDi(chiTietBooking.getGioDi());
        xepPhongBooKing.setLoaiGia(chiTietBooking.getLoaiGia());
        // gia dung de luu tong tien phong
        xepPhongBooKing.setGia(chiTietBooking.getTienPhong());
        xepPhongBooKing.setStt(chiTietBooking.getLoaiPhong().getMaLoaiPhong() + '-' + (count));
//        xepPhongBooKing.setNgayXep(LocalDate.now());
//        xepPhongBooKing.setGioXep(LocalTime.now());
        xepPhongBooKing.setPhuThuTienGiuong(chiTietBooking.getGiaExtraBed());
        xepPhongBooKing.setSoGiuong(soGiuong);
        xepPhongBooKing.setChiTietBooking(chiTietBooking);
        xepPhongBooKing.setBooKing(booKing);
        xepPhongBooKing.setSoDem(chiTietBooking.getSoDem());
        xepPhongBooKing.setThanhTien(chiTietBooking.getTienPhong().add(chiTietBooking.getGiaExtraBed().multiply(BigDecimal.valueOf(chiTietBooking.getSoDem()))));
        xepPhongBooKing.setGhiChu(chiTietBooking.getGhiChu());
        xepPhongBooKing.setGiaPhong(chiTietBooking.getGiaPhong());
        Phong phongAuto = new Phong();
        phongAuto.setMaPhong(maPhong);
        xepPhongBooKing.setPhong(phongAuto);
        xepPhongBooKing.setNguoiTao(authentication.getName());

        xepPhongRepository.save(xepPhongBooKing);
    }

}
