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
    PhongHoiNghiMapper phongHoiNghiMapper;
    private final DanhXungRepository danhXungRepository;
    private final KhuVucRepository khuVucRepository;
    private final ThiTruongRepository thiTruongRepository;
    private final HinhThucThanhToanRepository hinhThucThanhToanRepository;
    private final MucDichDenRepository mucDichDenRepository;
    private final NguonKhachRepository nguonKhachRepository;
    private final NhomKhachHangRepository nhomKhachHangRepository;
    private final GiamGiaRepository giamGiaRepository;
    private final TrangThaiBooKingRepository trangThaiBooKingRepository;
    private final LoaiTheRepository loaiTheRepository;
    private final LoaiGiaRepository loaiGiaRepository;
    private final LoaiPhongRepository loaiPhongRepository;
    private final XepPhongRepository xepPhongRepository;
    private final PhongRepository phongRepository;
    private final GiaPhongRepository giaPhongRepository;
    private final PhongHoiNghiRepository phongHoiNghiRepository;
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
    // Helper method để tìm entity
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
            () -> khuVucRepository.findById(request.getKhuVuc().getMaKhuVuc()),
            ErrorCode.KHU_VUC_NOT_FOUND,
            request.getKhuVuc().getMaKhuVuc()
    );

     findEntityOrThrow(
            () -> thiTruongRepository.findById(request.getThiTruong().getMaThiTruong()),
            ErrorCode.THI_TRUONG_NOT_FOUND,
            request.getThiTruong().getMaThiTruong()
    );

     findEntityOrThrow(
            () -> hinhThucThanhToanRepository.findById(request.getHinhThucThanhToan().getMaHinhThucThanhToan()),
            ErrorCode.HINH_THUC_THAN_TOAN_NOT_FOUND,
            request.getHinhThucThanhToan().getMaHinhThucThanhToan()
    );

     findEntityOrThrow(
            () -> mucDichDenRepository.findById(request.getMucDichDen().getMaMucDich()),
            ErrorCode.MUC_DICH_DEN_NOT_FOUND, // Sửa mã lỗi
            request.getMucDichDen().getMaMucDich()
    );

     findEntityOrThrow(
            () -> nguonKhachRepository.findById(request.getNguonKhach().getMaNguonKhach()),
            ErrorCode.NGUON_KHACH_NOT_FOUND,
            request.getNguonKhach().getMaNguonKhach()
    );

    findEntityOrThrow(
            () -> giamGiaRepository.findById(request.getGiamGia().getMaGiamGia()),
            ErrorCode.GIAM_GIA_NOT_FOUND,
            request.getGiamGia().getMaGiamGia()
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


    // Lưu KhachHangBooKing
    KhachHangBooKing khachHangBooKing = khachHangBooKingMapper.toCreateKhachHangBooKing(request.getKhachHangBooKing());
    khachHangBooKing.setMaKhachHangBooking(khachHangBooKingService.generateNewId());
    khachHangBooKing.setNgayTao(LocalDateTime.now());
    khachHangBooKing = khachHangBooKingRepository.save(khachHangBooKing);

    // Lưu ThongTinLienHeBooKing
    ThongTinLienHeBooKing thongTinLienHeBooKing = thongTinLienHeBooKingMapper.toThongTinLienHeBooKing(request.getThongTinLienHeBooKing());
    thongTinLienHeBooKing = thongTinLienHeRepository.save(thongTinLienHeBooKing);

    // Tạo và lưu BooKing chính
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

    var authentiaction = SecurityContextHolder.getContext().getAuthentication();
    booKing.setNguoiTao(authentiaction.getName());
    booKing.setThoiGianTao(LocalDateTime.now());
    booKing.setLoaiNguonKhach(request.getLoaiNguonKhach().toUpperCase());
    booKing = booKingRepository.save(booKing);
  // Kiểm tra giá trị sau khi lưu
  log.info("After saving BooKing - maBooking: {}, tongTien: {}",
    booKing.getMaBooking(), booKing.getTongTien());
    // Xử lý phòng hội nghị
//    BigDecimal tongTiengHoiNghi = BigDecimal.ZERO;
//    if(!request.getPhongHoiNghis().isEmpty()) {
//        for (PhongHoiNghiRequest phongHoiNghiRequest : request.getPhongHoiNghis()) {
//
//            if(!phongHoiNghiRequest.isDuBao()) {
//                throw new AppException(ErrorCode.NOT_ALLOW_DU_BAO_PHONG_HOI_NGHI);
//            }
//            LocalDate ngayBD = phongHoiNghiRequest.getNgayBatDau();
//            LocalDate ngayKT = phongHoiNghiRequest.getNgayKetThuc();
//
//            if (ngayBD == null || ngayKT == null) {
//                throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY, "Ngày đến hoặc ngày đi không được null");
//            }
//
//            long soDemThueLong = ChronoUnit.DAYS.between(ngayBD, ngayKT);
//            if (soDemThueLong < 0) {
//                throw new AppException(ErrorCode.ERROR_NGAYDI, "Hội nghị ngày đi phải sau ngày đến");
//            }
//
//            if(phongHoiNghiRequest.getSoLuong() == 0) {
//                throw new AppException(ErrorCode.INVALID_SO_LUONG);
//            }
//            if(phongHoiNghiRequest.getDonGia() == null) {
//                throw new AppException(ErrorCode.INVALID_DON_GIA);
//            }
//
//
//            PhongHoiNghi phongHoiNghi= phongHoiNghiMapper.toCreateHoiNghi(phongHoiNghiRequest);
//            phongHoiNghi.setBooKing(booKing);
//            phongHoiNghiRepository.save(phongHoiNghi);
//
//            tongTiengHoiNghi = phongHoiNghi.getDonGia();
//        }
//    }

    if(request.getChiTietBooKings() == null || request.getChiTietBooKings().isEmpty()) {
        throw new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY);
    }

    // Xử lý ChiTietBooking
    AtomicReference<BigDecimal> tongTien = new AtomicReference<>(BigDecimal.ZERO);
    BooKing finalBooKing = booKing;
    List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList = new ArrayList<>();
    AtomicBoolean khongCoPhong = new AtomicBoolean(false);
    List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgay = new ArrayList<>();
    List<ChiTietBooking> chiTietBookings = request.getChiTietBooKings().stream()
            .map(chiTietBooKingRequest -> {
                if (chiTietBooKingRequest.getSoLuong() > chiTietBooKingRequest.getSoLuongDuBaoPhong()) {
                    throw new AppException(ErrorCode.SO_LUONG_ERROR);
                }
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

  //              int soDemThue = (soDemThueLong == 0) ? 1 : (int) soDemThueLong;
                // Bổ sung ẩn giá phòng cho tất cả các ngày
//                BigDecimal tienPhong = chiTietBooKingRequest.getGia().multiply(BigDecimal.valueOf(soDemThue).multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuong())));


                // 13-05-2025 bổ sung chon phong


                // Kiểm tra trùng lặp maPhong trong request
                Set<String> maPhongSet = new HashSet<>();
                for (DanhSachPhongChiTietRequest req : chiTietBooKingRequest.getDanhSachPhongChiTiets()) {
                    String maPhong = req.getMaPhong();
                    if (!maPhongSet.add(maPhong)) {
                        throw new AppException(ErrorCode.DUPLICATE_MA_PHONG, "Phòng " + maPhong + " bị trùng lặp trong request.");
                    }
                    int kiemTraPhong = chiTietBooKingRepository.kiemTraMaPhongDaTonTaiTrongChiTietBooking(maPhong, ngayDen, ngayDi);
                    if (kiemTraPhong > 0) {
                        throw new AppException(ErrorCode.ERROR_PHONG_DA_DAT, "Phòng " + maPhong + " đã được đặt trong khoảng thời gian này.");
                    }
                }

                // kiem tra ma phong có tồn tại trong hệ thống trong khoản thơi gian



                int soLuongExtra = chiTietBooKingRequest.getSoLuongExtraBed();

                if (!chiTietBooKingRequest.getDanhSachPhongChiTiets().isEmpty()) {

                    if(chiTietBooKingRequest.getDanhSachPhongChiTiets().size() != chiTietBooKingRequest.getSoLuong()) {
                        throw new AppException(ErrorCode.ERROR_SO_LUONG_PHONG);
                    }

                    int kiemTraSoLuongPhongExtra = 0;

                    for(DanhSachPhongChiTietRequest phongDTO : chiTietBooKingRequest.getDanhSachPhongChiTiets()) {
                        Phong phongKT = phongRepository.findById(phongDTO.getMaPhong())
                                .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng với mã: " + phongDTO.getMaPhong()));

                        if(phongKT.getSoGiuongThem() > 0) {
                            kiemTraSoLuongPhongExtra++;
                        }

                    }
                    if(chiTietBooKingRequest.getSoLuongExtraBed() > 0) {
                        if(kiemTraSoLuongPhongExtra < soLuongExtra) {
                            throw new AppException(ErrorCode.ERROR_SO_LUONG_PHONG_EXTRA);
                        }
                    }

                } else
                    khongCoPhong.set(true);


                ChiTietBooking chiTietBooking = chiTietBooKingMapper.toCreChiTietDatPhong(chiTietBooKingRequest);
                chiTietBooking.setBooKing(finalBooKing);

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

                List<DanhSachPhongChiTiet> dsPhong = new ArrayList<>();

                for (DanhSachPhongChiTietRequest phongDTO : chiTietBooKingRequest.getDanhSachPhongChiTiets()) {
                    Phong phong = phongRepository.findById(phongDTO.getMaPhong())
                            .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng với mã: " + phongDTO.getMaPhong()));
                    DanhSachPhongChiTiet danhSachPhongChiTiet = new DanhSachPhongChiTiet();
                    danhSachPhongChiTiet.setMaPhong(phong.getMaPhong());
                    danhSachPhongChiTiet.setChiTietBooking(chiTietBooking);
                    danhSachPhongChiTiet.setSoGiuongThem(phongDTO.getSoGiuongThem());
                    dsPhong.add(danhSachPhongChiTiet);

                    // Lưu log danh sách phòng chi tiết
                    Log_DanhSachPhongChiTiet logDanhSachPhongChiTiet = new Log_DanhSachPhongChiTiet();
                    logDanhSachPhongChiTiet.setMaPhong(phong.getMaPhong());
                    logDanhSachPhongChiTiet.setSoGiuongThem(phongDTO.getSoGiuongThem());
                    logDanhSachPhongChiTiet.setMachiTietBooking(chiTietBooking.getMaChiTietBooking());
                    logDanhSachPhongChiTiet.setTrangThai("CREATED");
                    logDanhSachPhongChiTiet.setThoiGianTao(LocalDateTime.now());
                    logDanhSachPhongChiTietList.add(logDanhSachPhongChiTiet);
                }
                chiTietBooking.setDanhSachPhongChiTiets(dsPhong);


                // 24-05-2025 bổ sung thêm danh sách gía từng theo ngày
                if(chiTietBooKingRequest.getGiaPhongTheoNgays() == null || chiTietBooKingRequest.getGiaPhongTheoNgays().isEmpty()) {
                    throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
                }

                // Kiểm tra giá phòng theo ngày
//                BigDecimal tienPhongTheoNgay = chiTietBooKingRequest.getGiaPhongTheoNgays().stream()
//                        .map(GiaPhongTheoNgayRequest::getGia)
//                        .reduce(BigDecimal.ZERO, BigDecimal::add);
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

              //// TINH SO DEM THUE
                int soDemThue = (soDemThueLong == 0) ? 1 : (int) soDemThueLong;

                /// / TINH TIEN EXTRA BED
                BigDecimal tienExtra = chiTietBooKingRequest.getGiaExtraBed().multiply(BigDecimal.valueOf(soDemThue)).multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuongExtraBed()));

                /// / TINH TIEN PHU THU TRE EM
                BigDecimal tienPhuThuTreEm = chiTietBooKingRequest.getGiaPhuThuTreEm().multiply(BigDecimal.valueOf(soDemThue)).multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuongPhuThuTreEm()));

                /// TINH TIEN PHU THU AN SANG
                BigDecimal tienPhuThuAnSang = chiTietBooKingRequest.getGiaPhuThuAnSang().multiply(BigDecimal.valueOf(soDemThue)).multiply(BigDecimal.valueOf(chiTietBooKingRequest.getSoLuongPhuThuAnSang()));

              /// TINH TIEN PHONG
                BigDecimal tong = tienPhongTheoNgay.multiply(BigDecimal.valueOf(chiTietBooking.getSoLuong())).add(tienExtra).add(tienPhuThuTreEm).add(tienPhuThuAnSang);


                tongTien.updateAndGet(v -> v.add(tong));


                return chiTietBooking;
            })
            .collect(Collectors.toList());

    tongTien.set(tongTien.get().subtract(request.getTienCoc()));


// So sánh tổng tiền tính được với tổng tiền từ request
    if (tongTien.get().compareTo(request.getTongTien()) != 0) {
        throw new AppException(ErrorCode.TONG_TIEN_KHONG_KHOP);
    }


    // Kiểm tra số lượng phần tử trong danh sách sau khi xử lý
    if (request.getChiTietBooKings() == null || request.getChiTietBooKings().size() != chiTietBookings.size()) {
        throw new AppException(ErrorCode.SO_LUONG_BOOKING_KHONG_KHOP);
    }

    chiTietBooKingRepository.saveAll(chiTietBookings);

    // Lưu log danh sách phòng chi tiết
    log_DanhSachPhongChiTietRepositoty.saveAll(logDanhSachPhongChiTietList);

    // Lưu log giá phòng theo ngày
    logGiaPhongTheoNgayRepository.saveAll(logGiaPhongTheoNgay);

    // SAU KHI SAVE BOKING THANH CONG THI XEP PHONG TU DONG
  // Xếp phòng tự động
  if (!khongCoPhong.get()) {
    int stt = 1;
    for (ChiTietBooking chiTietBooking : chiTietBookings) {
      List<DanhSachPhongChiTiet> phongChiTietList = chiTietBooking.getDanhSachPhongChiTiets();
      int soPhongCanXep = chiTietBooking.getSoLuong();
      int soExtraBed = chiTietBooking.getSoLuongExtraBed();
      int soLuongPhuThuTreEm = chiTietBooking.getSoLuongPhuThuTreEm();
      BigDecimal giaExtraBed = chiTietBooking.getGiaExtraBed();

      for (int i = 0; i < soPhongCanXep && i < phongChiTietList.size(); i++) {
        DanhSachPhongChiTiet phongDTO = phongChiTietList.get(i);

        int extraBed = (i < soExtraBed) ? 1 : 0;
        BigDecimal gia = (i < soExtraBed) ? giaExtraBed : BigDecimal.ZERO;
        // Chỉ gán soLuongPhuThuTreEm cho phòng đầu tiên
        int phuThuTreEm = (i == 0 && soLuongPhuThuTreEm > 0) ? soLuongPhuThuTreEm : 0;

        createSaveXepPhongBooKing(
          chiTietBooking,
          booKing,
          phongDTO.getMaPhong(),
          stt,
          extraBed,
          gia,
          authentiaction,
          chiTietBooking.getGiaPhongTheoNgays()

        );
        stt++;
      }
    }

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
      () -> khuVucRepository.findById(request.getKhuVuc().getMaKhuVuc()),
      ErrorCode.KHU_VUC_NOT_FOUND,
      request.getKhuVuc().getMaKhuVuc()
    );
    findEntityOrThrow(
      () -> thiTruongRepository.findById(request.getThiTruong().getMaThiTruong()),
      ErrorCode.THI_TRUONG_NOT_FOUND,
      request.getThiTruong().getMaThiTruong()
    );
    findEntityOrThrow(
      () -> hinhThucThanhToanRepository.findById(request.getHinhThucThanhToan().getMaHinhThucThanhToan()),
      ErrorCode.HINH_THUC_THAN_TOAN_NOT_FOUND,
      request.getHinhThucThanhToan().getMaHinhThucThanhToan()
    );
    findEntityOrThrow(
      () -> mucDichDenRepository.findById(request.getMucDichDen().getMaMucDich()),
      ErrorCode.MUC_DICH_DEN_NOT_FOUND,
      request.getMucDichDen().getMaMucDich()
    );
    findEntityOrThrow(
      () -> nguonKhachRepository.findById(request.getNguonKhach().getMaNguonKhach()),
      ErrorCode.NGUON_KHACH_NOT_FOUND,
      request.getNguonKhach().getMaNguonKhach()
    );
    findEntityOrThrow(
      () -> giamGiaRepository.findById(request.getGiamGia().getMaGiamGia()),
      ErrorCode.GIAM_GIA_NOT_FOUND,
      request.getGiamGia().getMaGiamGia()
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

            log.info(xp.getPhong().getMaPhong() + " - " + chiTietBooKingRequest.getNgayDen() + " - " + chiTietBooKingRequest.getNgayDi() + " - " + xp.getMaXepPhongBooking());

            if (countTrung > 0) {
              throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING,
                "Ngày đến: " + DateConverter.converDate(chiTietBooKingRequest.getNgayDen()) +
                  ", Ngày đi: " + DateConverter.converDate(chiTietBooKingRequest.getNgayDi()) +
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

@Transactional
public BooKingResponse updateBooKing1(String maBooking, BooKingRequest request) {
    // 1. Tìm và xác thực bản ghi BooKing
    BooKing booKing = findBooKingById(maBooking);

    // 2. Xác thực các thực thể liên quan
    validateRelatedEntities(request);

    // 3. Cập nhật thông tin BooKing
    BooKing booKingUpdate = updateBooKingDetails(booKing, request);

    // 4. Xử lý yêu cầu bổ sung
    updateYeuCauList(booKingUpdate, request.getYeuCaus());

    // 5. Lưu thông tin khách hàng và liên hệ
    KhachHangBooKing khachHangBooKing = saveKhachHangBooKing(request.getKhachHangBooKing());
    ThongTinLienHeBooKing thongTinLienHeBooKing = saveThongTinLienHeBooKing(request.getThongTinLienHeBooKing());


    // 6. Khởi tạo danh sách log danh sách phòng chi tiết
    List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList = new ArrayList<>();


    // 7. Xử lý chi tiết booking
    List<ChiTietBooking> chiTietBookings = processChiTietBookings(booKing, request, maBooking,logDanhSachPhongChiTietList);


    // 8. Cập nhật thông tin tổng quát và lưu
    booKingUpdate.setChiTietBookings(chiTietBookings);
    booKingUpdate.setMaBooking(maBooking);
    booKingUpdate.setKhachHangBooKing(khachHangBooKing);
    booKingUpdate.setThongTinLienHeBooKing(thongTinLienHeBooKing);
    booKingUpdate.setThoiGianTao(booKing.getThoiGianTao());
    booKingUpdate.setNguoiTao(booKing.getNguoiTao());
    booKingUpdate.setLoaiNguonKhach(request.getLoaiNguonKhach().toUpperCase());
    booKingUpdate.setTongTien(request.getTongTien());

    // 9. Kiểm tra tổng tiền và số lượng chi tiết booking
    validateTongTienAndChiTietBookings(booKingUpdate, request);

    // 10. Lưu và trả về kết quả
    booKingRepository.save(booKingUpdate);
    log_DanhSachPhongChiTietRepositoty.saveAll(logDanhSachPhongChiTietList);

    return booKingMapper.toBooKingResponse(booKingUpdate);
}

    /**
     * Tìm bản ghi BooKing theo mã
     * @param maBooking Mã booking cần tìm
     * @return Đối tượng BooKing
     * @throws AppException Nếu không tìm thấy booking
     */
    private BooKing findBooKingById(String maBooking) {
        return booKingRepository.findById(maBooking)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND, "Booking không tồn tại: " + maBooking));
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
                () -> khuVucRepository.findById(request.getKhuVuc().getMaKhuVuc()),
                ErrorCode.KHU_VUC_NOT_FOUND, request.getKhuVuc().getMaKhuVuc());
        findEntityOrThrow(
                () -> thiTruongRepository.findById(request.getThiTruong().getMaThiTruong()),
                ErrorCode.THI_TRUONG_NOT_FOUND, request.getThiTruong().getMaThiTruong());
        findEntityOrThrow(
                () -> hinhThucThanhToanRepository.findById(request.getHinhThucThanhToan().getMaHinhThucThanhToan()),
                ErrorCode.HINH_THUC_THAN_TOAN_NOT_FOUND, request.getHinhThucThanhToan().getMaHinhThucThanhToan());
        findEntityOrThrow(
                () -> mucDichDenRepository.findById(request.getMucDichDen().getMaMucDich()),
                ErrorCode.MUC_DICH_DEN_NOT_FOUND, request.getMucDichDen().getMaMucDich());
        findEntityOrThrow(
                () -> nguonKhachRepository.findById(request.getNguonKhach().getMaNguonKhach()),
                ErrorCode.NGUON_KHACH_NOT_FOUND, request.getNguonKhach().getMaNguonKhach());
        findEntityOrThrow(
                () -> giamGiaRepository.findById(request.getGiamGia().getMaGiamGia()),
                ErrorCode.GIAM_GIA_NOT_FOUND, request.getGiamGia().getMaGiamGia());
        findEntityOrThrow(
                () -> trangThaiBooKingRepository.findById(request.getTrangThaiBooKing().getMaTrangThaiBooKing()),
                ErrorCode.TRANG_THAI_BOOKING_NOT_FOUND, request.getTrangThaiBooKing().getMaTrangThaiBooKing());
        findEntityOrThrow(
                () -> loaiTheRepository.findById(request.getLoaiThe().getMaLoaiThe()),
                ErrorCode.LOAI_THE_NOT_FOUND, request.getLoaiThe().getMaLoaiThe());
    }

    /**
     * Cập nhật thông tin chi tiết của BooKing
     * @param booKing Đối tượng BooKing hiện tại
     * @param request Yêu cầu chứa thông tin cần cập nhật
     * @return Đối tượng BooKing đã được cập nhật
     */
    private BooKing updateBooKingDetails(BooKing booKing, BooKingRequest request) {
        BooKing booKingUpdate = booKingMapper.toCreateBooKing(request);
        booKingUpdate.setThoiGianTao(booKing.getThoiGianTao());
        booKingUpdate.setNguoiTao(booKing.getNguoiTao());
        return booKingUpdate;
    }

    /**
     * Cập nhật danh sách yêu cầu bổ sung
     * @param booKingUpdate Đối tượng BooKing cần cập nhật danh sách yêu cầu
     * @param yeuCaus Danh sách yêu cầu từ request
     * @throws AppException Nếu mã yêu cầu không tồn tại
     */
    private void updateYeuCauList(BooKing booKingUpdate, List<YeuCauRequest> yeuCaus) {
        if (yeuCaus != null && !yeuCaus.isEmpty()) {
            List<YeuCau> yeuCausMoi = yeuCaus.stream()
                    .map(yeuCauRequest -> yeuCauRepository.findById(yeuCauRequest.getMaYeuCau())
                            .orElseThrow(() -> new AppException(ErrorCode.MA_YEUCAU_NOT_FOUND, "YeuCau không tồn tại: " + yeuCauRequest.getMaYeuCau())))
                    .toList();
            booKingUpdate.getYeuCaus().clear();
            booKingUpdate.getYeuCaus().addAll(yeuCausMoi);
        }
    }

    /**
     * Lưu thông tin khách hàng booking
     * @param khachHangRequest Thông tin khách hàng từ request
     * @return Đối tượng KhachHangBooKing đã được lưu
     */
    private KhachHangBooKing saveKhachHangBooKing(KhachHangBooKingRequest khachHangRequest) {
        KhachHangBooKing khachHangBooKing = khachHangBooKingMapper.toCreateKhachHangBooKing(khachHangRequest);
        khachHangBooKing.setMaKhachHangBooking(khachHangBooKingService.generateNewId());
        return khachHangBooKingRepository.save(khachHangBooKing);
    }

    /**
     * Lưu thông tin liên hệ booking
     * @param thongTinRequest Thông tin liên hệ từ request
     * @return Đối tượng ThongTinLienHeBooKing đã được lưu
     */
    private ThongTinLienHeBooKing saveThongTinLienHeBooKing(ThongTinLienHeBooKingRequest thongTinRequest) {
        ThongTinLienHeBooKing thongTinLienHeBooKing = thongTinLienHeBooKingMapper.toThongTinLienHeBooKing(thongTinRequest);
        return thongTinLienHeRepository.save(thongTinLienHeBooKing);
    }

    /**
     * Xử lý chi tiết booking và các thông tin liên quan
     * @param booKing Đối tượng BooKing hiện tại
     * @param request Yêu cầu chứa danh sách chi tiết booking
     * @param maBooking Mã booking
     * @return Danh sách ChiTietBooking đã xử lý
     * @throws AppException Nếu có lỗi trong quá trình xử lý
     */
    private List<ChiTietBooking> processChiTietBookings(BooKing booKing, BooKingRequest request, String maBooking,List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList) {
        if (request.getChiTietBooKings() == null || request.getChiTietBooKings().isEmpty()) {
            throw new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY);
        }

        List<String> maLoaiPhongList = request.getChiTietBooKings().stream()
                .map(chiTiet -> chiTiet.getLoaiPhong().getMaLoaiPhong())
                .collect(Collectors.toList());

        Map<String, ChiTietBooking> existingChiTietBookings = chiTietBooKingRepository
                .findByMaBookingAndLoaiPhong(maBooking, maLoaiPhongList)
                .stream()
                .collect(Collectors.toMap(
                        chiTiet -> chiTiet.getLoaiPhong().getMaLoaiPhong(),
                        chiTiet -> chiTiet
                ));

        boolean kiemTraNgayDenVaNgayDi = checkAndUpdateXepPhong(booKing, request, maBooking);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AtomicReference<BigDecimal> tongTien = new AtomicReference<>(BigDecimal.ZERO);
        List<ChiTietBooking> chiTietBookings = new ArrayList<>();
        int stt = 1;

        for (ChiTietBooKingRequest chiTietRequest : request.getChiTietBooKings()) {
            ChiTietBooking chiTietBooking = processSingleChiTietBooking(
                    chiTietRequest, booKing, maBooking, existingChiTietBookings,
                    kiemTraNgayDenVaNgayDi, authentication, tongTien, stt,logDanhSachPhongChiTietList);



            chiTietBookings.add(chiTietBooking);
            stt++;
        }

        return chiTietBookings;
    }

    /**
     * Kiểm tra và cập nhật thông tin xếp phòng
     * @param booKing Đối tượng BooKing hiện tại
     * @param request Yêu cầu chứa thông tin ngày đến, ngày đi
     * @param maBooking Mã booking
     * @return True nếu ngày đến/ngày đi thay đổi và đã xếp phòng
     * @throws AppException Nếu không thể cập nhật thông tin xếp phòng
     */
    private boolean checkAndUpdateXepPhong(BooKing booKing, BooKingRequest request, String maBooking) {
        boolean kiemTraNgayDenVaNgayDi = false;
        if (booKing.isDaXepPhong()) {
            int updateXP = xepPhongRepository.updateNgayDenNgayDi(maBooking, request.getNgayDen(), request.getNgayDi());
            if (updateXP == 0) {
                throw new AppException(ErrorCode.XEP_PHONG_IS_EMPTY);
            }
            if (!booKing.getNgayDen().isEqual(request.getNgayDen()) || !booKing.getNgayDi().isEqual(request.getNgayDi())) {
                kiemTraNgayDenVaNgayDi = true;
            }
        }
        return kiemTraNgayDenVaNgayDi;
    }

    /**
     * Xử lý một chi tiết booking cụ thể
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @param booKing Đối tượng BooKing
     * @param maBooking Mã booking
     * @param existingChiTietBookings Map chứa các chi tiết booking hiện có
     * @param kiemTraNgayDenVaNgayDi Kiểm tra thay đổi ngày đến/ngày đi
     * @param authentication Thông tin xác thực
     * @param tongTien Tổng tiền tích lũy
     * @param stt Số thứ tự
     * @return Đối tượng ChiTietBooking đã xử lý
     * @throws AppException Nếu có lỗi trong quá trình xử lý
     */
    private ChiTietBooking processSingleChiTietBooking(ChiTietBooKingRequest chiTietRequest, BooKing booKing, String maBooking,
                                                       Map<String, ChiTietBooking> existingChiTietBookings, boolean kiemTraNgayDenVaNgayDi,
                                                       Authentication authentication, AtomicReference<BigDecimal> tongTien, int stt,List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList) {
        String maLoaiPhong = chiTietRequest.getLoaiPhong().getMaLoaiPhong();

        // Xác thực các thực thể liên quan
        LoaiPhong loaiPhong = loaiPhongRepository.findById(maLoaiPhong)
                .orElseThrow(() -> new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND));
        LoaiGia loaiGia = loaiGiaRepository.findById(chiTietRequest.getLoaiGia().getMaLoaiGia())
                .orElseThrow(() -> new AppException(ErrorCode.LOAI_GIA_NOT_FOUND));
        GiaPhong giaPhong = giaPhongRepository.findById(chiTietRequest.getGiaPhong().getMaGiaPhong())
                .orElseThrow(() -> new AppException(ErrorCode.GIA_PHONG_ERROR));

        // Kiểm tra ngày đến và ngày đi
        validateNgayDenNgayDi(chiTietRequest, kiemTraNgayDenVaNgayDi, maBooking);

        // Tính số đêm thuê và tiền extra bed
        BigDecimal tienExtra = calculateTienExtra(chiTietRequest);


        // Kiểm tra số lượng phòng trống
        validateSoPhongTrong(chiTietRequest, maLoaiPhong);

        // Xử lý chi tiết booking
        ChiTietBooking chiTietBooking = existingChiTietBookings.getOrDefault(maLoaiPhong, null);
        boolean duLieuMoi = chiTietBooking == null;

        if (!duLieuMoi) {
            chiTietBooking = updateExistingChiTietBooking(chiTietRequest, chiTietBooking, booKing, loaiPhong, loaiGia, giaPhong,
                    authentication, stt,logDanhSachPhongChiTietList);
        } else {
            chiTietBooking = createNewChiTietBooking(chiTietRequest, booKing, loaiPhong, loaiGia, giaPhong,
                    authentication, stt,logDanhSachPhongChiTietList);
        }

        // Cập nhật giá phòng theo ngày
        updateGiaPhongTheoNgay(chiTietBooking, chiTietRequest, tongTien, tienExtra);

        return chiTietBooking;
    }

    /**
     * Kiểm tra ngày đến và ngày đi
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @param kiemTraNgayDenVaNgayDi Kiểm tra thay đổi ngày đến/ngày đi
     * @param maBooking Mã booking
     * @throws AppException Nếu ngày đến/ngày đi không hợp lệ hoặc phòng bị trùng
     */
    private void validateNgayDenNgayDi(ChiTietBooKingRequest chiTietRequest, boolean kiemTraNgayDenVaNgayDi, String maBooking) {
        LocalDate ngayDen = chiTietRequest.getNgayDen();
        LocalDate ngayDi = chiTietRequest.getNgayDi();

        if (ngayDen == null || ngayDi == null) {
            throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY, "Ngày đến hoặc ngày đi không được null");
        }

        long soDemThueLong = ChronoUnit.DAYS.between(ngayDen, ngayDi);
        if (soDemThueLong < 0) {
            throw new AppException(ErrorCode.ERROR_NGAYDI, "Ngày đi phải sau ngày đến");
        }

        if (kiemTraNgayDenVaNgayDi) {
            List<XepPhongBooKing> xepPhongBooKings = xepPhongRepository.getXepPhongBooKingsByMaBooking(maBooking);
            for (XepPhongBooKing xp : xepPhongBooKings) {
                int countTrung = xepPhongRepository.kiemTraPhongDaTonTai(
                        xp.getPhong().getMaPhong(), ngayDen, ngayDi, xp.getMaXepPhongBooking());
                if (countTrung > 0) {
                    throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING,
                            "Ngày đến: " + DateConverter.converDate(ngayDen) + ", Ngày đi: " + DateConverter.converDate(ngayDi) +
                                    ", Phòng: " + xp.getPhong().getMaPhong() + " đã tồn tại.");
                }
            }
        }
    }

    /**
     * Tính tiền extra bed
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @return Số tiền extra bed
     */
    private BigDecimal calculateTienExtra(ChiTietBooKingRequest chiTietRequest) {
        LocalDate ngayDen = chiTietRequest.getNgayDen();
        LocalDate ngayDi = chiTietRequest.getNgayDi();
        long soDemThueLong = ChronoUnit.DAYS.between(ngayDen, ngayDi);
        int soDemThue = (soDemThueLong == 0) ? 1 : (int) soDemThueLong;

        return chiTietRequest.getGiaExtraBed()
                .multiply(BigDecimal.valueOf(soDemThue))
                .multiply(BigDecimal.valueOf(chiTietRequest.getSoLuongExtraBed()));
    }

    private BigDecimal calulateTienPhongTheoNgay(ChiTietBooKingRequest chiTietRequest) {

        // Tính tiền phòng theo ngày
        if (chiTietRequest.getGiaPhongTheoNgays() == null || chiTietRequest.getGiaPhongTheoNgays().isEmpty()) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY, "Giá phòng theo ngày không được trống");
        }
        BigDecimal tongTienPhong = BigDecimal.ZERO;
        for( GiaPhongTheoNgayRequest giaPhongTheoNgayRequest : chiTietRequest.getGiaPhongTheoNgays()) {
            tongTienPhong = tongTienPhong.add(giaPhongTheoNgayRequest.getGia());
        }

        return tongTienPhong
                .multiply(BigDecimal.valueOf(chiTietRequest.getSoLuong()));
    }

    /**
     * Kiểm tra số lượng phòng trống
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @param maLoaiPhong Mã loại phòng
     * @throws AppException Nếu không đủ phòng trống
     */
    private void validateSoPhongTrong(ChiTietBooKingRequest chiTietRequest, String maLoaiPhong) {
        LoaiPhongTrongDTO kiemTraLoaiPhongTrong = loaiPhongRepository
                .kiemTraLoaiPhongTrongMaLoaiPhong(chiTietRequest.getNgayDen(), chiTietRequest.getNgayDi(), maLoaiPhong);

        long soPhongTrong = 0;
        if (!chiTietRequest.getMaChiTietBooking().isEmpty()) {
            long soPhongDB = chiTietBooKingRepository.countChiTietBookingByMaChiTietBooking(chiTietRequest.getMaChiTietBooking());
            if (chiTietRequest.getSoLuong() > soPhongDB) {
                soPhongTrong = chiTietRequest.getSoLuong() - soPhongDB;
            }
        }

        if (kiemTraLoaiPhongTrong.getSoPhongTrong() <= 0 || kiemTraLoaiPhongTrong.getSoPhongTrong() < soPhongTrong  && soPhongTrong < 0) {
            throw new AppException(ErrorCode.ERROR_CAP_NHAT_NGAY_BOOKING,
                    "Ngày đến: " + chiTietRequest.getNgayDen() + ", Ngày đi: " + chiTietRequest.getNgayDi() +
                            ", Phòng: " + maLoaiPhong + " không đủ.");
        }
    }

    /**
     * Cập nhật chi tiết booking hiện có
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @param chiTietBooking Chi tiết booking hiện tại
     * @param booKing Đối tượng BooKing
     * @param loaiPhong Loại phòng
     * @param loaiGia Loại giá
     * @param giaPhong Giá phòng
     * @param authentication Thông tin xác thực
     * @param stt Số thứ tự
     * @return Chi tiết booking đã cập nhật
     * @throws AppException Nếu có lỗi trong quá trình cập nhật
     */
    private ChiTietBooking updateExistingChiTietBooking(ChiTietBooKingRequest chiTietRequest, ChiTietBooking chiTietBooking,
                                                        BooKing booKing, LoaiPhong loaiPhong, LoaiGia loaiGia, GiaPhong giaPhong,
                                                        Authentication authentication, int stt,List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList) {
        if (!chiTietRequest.getLoaiPhong().getMaLoaiPhong().equals(chiTietBooking.getLoaiPhong().getMaLoaiPhong())) {
            List<Map<String, Object>> xepPhong = xepPhongRepository.DaXepPhong(chiTietRequest.getMaChiTietBooking());
            StringBuilder listPhong = new StringBuilder();
            for (Map<String, Object> xep : xepPhong) {
                listPhong.append(xep.get("ma_phong")).append(",");
            }
            if (!xepPhong.isEmpty()) {
                throw new AppException(ErrorCode.ERROR_LOAI_PHONG_MISMATCH, "Phòng đã check-in: " + listPhong.toString());
            } else {
                xepPhongRepository.DeleteXepPhongTheoChiTiet(chiTietRequest.getMaChiTietBooking());
                booKing.setDaXepPhong(false);
            }
        }

        updateRoomAssignments(chiTietRequest, chiTietBooking, booKing, authentication, stt);

//        chiTietBooking.setLoaiPhong(loaiPhong);
//        chiTietBooking.setLoaiGia(loaiGia);
//        chiTietBooking.setGiaPhong(giaPhong);
        chiTietBooking = chiTietBooKingMapper.toCreChiTietDatPhong(chiTietRequest);
        chiTietBooking.setBooKing(booKing);

        updateDanhSachPhongChiTiet(chiTietBooking, chiTietRequest,logDanhSachPhongChiTietList);

        return chiTietBooking;
    }

    /**
     * Tạo mới chi tiết booking
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @param booKing Đối tượng BooKing
     * @param loaiPhong Loại phòng
     * @param loaiGia Loại giá
     * @param giaPhong Giá phòng
     * @param authentication Thông tin xác thực
     * @param stt Số thứ tự
     * @return Chi tiết booking mới
     */
    private ChiTietBooking createNewChiTietBooking(ChiTietBooKingRequest chiTietRequest, BooKing booKing, LoaiPhong loaiPhong,
                                                   LoaiGia loaiGia, GiaPhong giaPhong, Authentication authentication, int stt,List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList) {
        ChiTietBooking chiTietBooking = chiTietBooKingMapper.toCreChiTietDatPhong(chiTietRequest);
        chiTietBooking.setBooKing(booKing);
        chiTietBooking.setLoaiPhong(loaiPhong);
        chiTietBooking.setLoaiGia(loaiGia);
        chiTietBooking.setGiaPhong(giaPhong);
        chiTietBooking.setMaChiTietBooking(null);
        chiTietBooking = chiTietBooKingRepository.save(chiTietBooking);

        updateDanhSachPhongChiTiet(chiTietBooking, chiTietRequest,logDanhSachPhongChiTietList);

        for (int i = 0; i < chiTietBooking.getSoLuong(); i++) {
            DanhSachPhongChiTietRequest phongDTO = chiTietRequest.getDanhSachPhongChiTiets().get(i);
            int extraBed = (i < chiTietBooking.getSoLuongExtraBed()) ? 1 : 0;
            BigDecimal gia = (i < chiTietBooking.getSoLuongExtraBed()) ? chiTietBooking.getGiaExtraBed() : BigDecimal.ZERO;

            createSaveXepPhongBooKing(chiTietBooking, booKing, phongDTO.getMaPhong(), stt, extraBed, gia, authentication,chiTietBooking.getGiaPhongTheoNgays());
        }
        booKing.setDaXepPhong(true);

        return chiTietBooking;
    }

    /**
     * Cập nhật danh sách phòng chi tiết
     * @param chiTietBooking Chi tiết booking
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @throws AppException Nếu phòng không tồn tại
     */
    private void updateDanhSachPhongChiTiet(ChiTietBooking chiTietBooking, ChiTietBooKingRequest chiTietRequest,List<Log_DanhSachPhongChiTiet> logDanhSachPhongChiTietList) {
        danhSachPhongChiTietRepository.deleteByChiTietBooking_MaChiTietBooking(chiTietBooking.getMaChiTietBooking());
        List<DanhSachPhongChiTiet> dsPhong = new ArrayList<>();


        for (DanhSachPhongChiTietRequest phongDTO : chiTietRequest.getDanhSachPhongChiTiets()) {
            Phong phong = phongRepository.findById(phongDTO.getMaPhong())
                    .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND, "Không tìm thấy phòng với mã: " + phongDTO.getMaPhong()));
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
        }

        chiTietBooking.setDanhSachPhongChiTiets(dsPhong);
    }

    /**
     * Cập nhật giá phòng theo ngày
     * @param chiTietBooking Chi tiết booking
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @param tongTien Tổng tiền tích lũy
     * @param tienExtra Tiền extra bed
     * @throws AppException Nếu danh sách giá phòng theo ngày không hợp lệ
     */
    private void updateGiaPhongTheoNgay(ChiTietBooking chiTietBooking, ChiTietBooKingRequest chiTietRequest,
                                        AtomicReference<BigDecimal> tongTien, BigDecimal tienExtra) {
        if (chiTietRequest.getGiaPhongTheoNgays() == null || chiTietRequest.getGiaPhongTheoNgays().isEmpty()) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_EMPTY);
        }

        giaPhongTheoNgayRepository.xoaGiaPhongTheoNgay(chiTietBooking.getMaChiTietBooking());
        List<Log_GiaPhongTheoNgay> logGiaPhongTheoNgay = new ArrayList<>();
        List<GiaPhongTheoNgay> giaPhongTheoNgays = new ArrayList<>();

        for (GiaPhongTheoNgayRequest giaPhongTheoNgayRequest : chiTietRequest.getGiaPhongTheoNgays()) {
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
            log.setTrangThai("UPDATE");
            logGiaPhongTheoNgay.add(log);

            tongTien.updateAndGet(current ->
                    current.add(giaPhongTheoNgayRequest.getGia()
                            .multiply(BigDecimal.valueOf(chiTietRequest.getSoLuong()))
                            .add(tienExtra)));
        }

//        logGiaPhongTheoNgayRepository.saveAll(logGiaPhongTheoNgays);
        chiTietBooking.setGiaPhongTheoNgays(giaPhongTheoNgays);
    }

    /**
     * Cập nhật xếp phòng khi số lượng hoặc danh sách phòng thay đổi
     * @param chiTietRequest Yêu cầu chi tiết booking
     * @param chiTietBooking Chi tiết booking hiện tại
     * @param booKing Đối tượng BooKing
     * @param authentication Thông tin xác thực
     * @param stt Số thứ tự
     * @throws AppException Nếu có lỗi trong quá trình cập nhật
     */
    private void updateRoomAssignments(ChiTietBooKingRequest chiTietRequest, ChiTietBooking chiTietBooking,
                                       BooKing booKing, Authentication authentication, int stt) {
        List<DanhSachPhongChiTietRequest> dsPhongkt = chiTietRequest.getDanhSachPhongChiTiets();
        List<String> maPhongInput = dsPhongkt.stream().map(DanhSachPhongChiTietRequest::getMaPhong).toList();

        List<XepPhongBooKing> xepPhongBooKings = xepPhongRepository.getXepPhongByMaChiTiet(chiTietBooking.getMaChiTietBooking());
        List<String> maPhongDaXep = xepPhongBooKings.stream().map(xp -> xp.getPhong().getMaPhong()).toList();

        boolean roomsChanged = !new HashSet<>(maPhongInput).containsAll(maPhongDaXep) || !new HashSet<>(maPhongDaXep).containsAll(maPhongInput);
        List<String> roomsToDelete = maPhongDaXep.stream().filter(maPhong -> !maPhongInput.contains(maPhong)).toList();
        List<String> roomsToAdd = maPhongInput.stream().filter(maPhong -> !maPhongDaXep.contains(maPhong)).toList();

        for (String maPhong : roomsToDelete) {
            XepPhongBooKing xepPhong = xepPhongBooKings.stream()
                    .filter(xp -> xp.getPhong().getMaPhong().equals(maPhong))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND, "Phòng không tìm thấy: " + maPhong));
            if (xepPhong.getThoiGianNhan() != null) {
                throw new AppException(ErrorCode.CHITIETBOOKING_DA_XEP_PHONG,
                        "Phòng " + maPhong + " đã check-in, không thể đổi, Dòng " + stt + ".");
            }
            if (xepPhong.isDaThanhToan()) {
                throw new AppException(ErrorCode.ERROR_HOA_DON_DA_THANH_TOAN,
                        "Phòng " + maPhong + " đã thanh toán, không thể xóa.");
            }
            xepPhongRepository.udpateThoiGianXoaXepPhong(chiTietBooking.getMaChiTietBooking(), maPhong);
        }

        if (roomsChanged) {
            for (XepPhongBooKing xepPhong : xepPhongBooKings) {
                String maPhong = xepPhong.getPhong().getMaPhong();
                if (maPhongInput.contains(maPhong)) {
                    int index = maPhongInput.indexOf(maPhong);
                    DanhSachPhongChiTietRequest phongDTO = dsPhongkt.get(index);
                    int extraBed = (index < chiTietBooking.getSoLuongExtraBed()) ? 1 : 0;
                    BigDecimal gia = (index < chiTietBooking.getSoLuongExtraBed()) ? chiTietBooking.getGiaExtraBed() : BigDecimal.ZERO;
                    updateXepPhongBooKing(xepPhong, chiTietBooking, booKing, maPhong, stt, extraBed, gia, authentication);
                }
            }

            int currentIndex = 0;
            for (String maPhong : roomsToAdd) {
                int index = maPhongInput.indexOf(maPhong);
                DanhSachPhongChiTietRequest phongDTO = dsPhongkt.get(index);
                int extraBed = (currentIndex < chiTietBooking.getSoLuongExtraBed()) ? 1 : 0;
                BigDecimal gia = (currentIndex < chiTietBooking.getSoLuongExtraBed()) ? chiTietBooking.getGiaExtraBed() : BigDecimal.ZERO;
                createSaveXepPhongBooKing(chiTietBooking, booKing, maPhong, stt, extraBed, gia, authentication, chiTietBooking.getGiaPhongTheoNgays());
                currentIndex++;
            }
            booKing.setDaXepPhong(true);
        } else {
            for (XepPhongBooKing xepPhong : xepPhongBooKings) {
                int index = maPhongInput.indexOf(xepPhong.getPhong().getMaPhong());
                DanhSachPhongChiTietRequest phongDTO = dsPhongkt.get(index);
                int extraBed = (index < chiTietBooking.getSoLuongExtraBed()) ? 1 : 0;
                BigDecimal gia = (index < chiTietBooking.getSoLuongExtraBed()) ? chiTietBooking.getGiaExtraBed() : BigDecimal.ZERO;
                updateXepPhongBooKing(xepPhong, chiTietBooking, booKing, phongDTO.getMaPhong(), stt, extraBed, gia, authentication);
            }
        }
    }

    /**
     * Kiểm tra tổng tiền và số lượng chi tiết booking
     * @param booKingUpdate Đối tượng BooKing đã cập nhật
     * @param request Yêu cầu chứa thông tin cần kiểm tra
     * @throws AppException Nếu tổng tiền hoặc số lượng chi tiết booking không khớp
     */
    private void validateTongTienAndChiTietBookings(BooKing booKingUpdate, BooKingRequest request) {
        if (booKingUpdate.getTongTien().compareTo(request.getTongTien()) != 0) {
            throw new AppException(ErrorCode.TONG_TIEN_KHONG_KHOP);
        }
        if (request.getChiTietBooKings().size() != booKingUpdate.getChiTietBookings().size()) {
            throw new AppException(ErrorCode.SO_LUONG_BOOKING_KHONG_KHOP);
        }
    }

    // Helper method để tránh lặp code
    private <T, ID> T findEntityOrThrow(Supplier<Optional<T>> repositorySupplier, ErrorCode errorCode, ID id) {
        return repositorySupplier.get()
                .orElseThrow(() -> new AppException(errorCode, String.valueOf(id)));
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

    @Transactional
    public void createAndSaveXepPhongBooKing(ChiTietBooking chiTietBooking, BooKing booKing, int index, Authentication authentication) {
        List<PhongTrongThoiGian> dsPhong = phongRepository.getPhongTrongCurrent(
                chiTietBooking.getLoaiPhong().getMaLoaiPhong(),
                chiTietBooking.getNgayDen(),
                chiTietBooking.getNgayDi()
        );

        if (dsPhong.isEmpty()) {
            throw new AppException(ErrorCode.PHONG_NOT_FOUND, "Không còn phòng trống.");
        }

        int count = xepPhongRepository.countDaXepPhong(chiTietBooking.getMaChiTietBooking());


        XepPhongBooKing xepPhongBooKing = new XepPhongBooKing();
        xepPhongBooKing.setNgayDen(chiTietBooking.getNgayDen());
        xepPhongBooKing.setGioDen(chiTietBooking.getGioDen());
        xepPhongBooKing.setNgayDi(chiTietBooking.getNgayDi());
        xepPhongBooKing.setGioDi(chiTietBooking.getGioDi());
        xepPhongBooKing.setLoaiGia(chiTietBooking.getLoaiGia());
        xepPhongBooKing.setGia(BigDecimal.valueOf(chiTietBooking.getGia()));
        xepPhongBooKing.setStt(chiTietBooking.getLoaiPhong().getMaLoaiPhong() + '-' + (count + 1));
        xepPhongBooKing.setNgayXep(LocalDate.now());
        xepPhongBooKing.setGioXep(LocalTime.now());
        xepPhongBooKing.setPhuThuTienGiuong(BigDecimal.ZERO);
        xepPhongBooKing.setSoGiuong(0);
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

        Phong phongAuto = new Phong();
        phongAuto.setMaPhong(dsPhong.getFirst().getMaPhong());
        xepPhongBooKing.setPhong(phongAuto);
        xepPhongBooKing.setNguoiTao(authentication.getName());

        xepPhongRepository.save(xepPhongBooKing);
    }
}
