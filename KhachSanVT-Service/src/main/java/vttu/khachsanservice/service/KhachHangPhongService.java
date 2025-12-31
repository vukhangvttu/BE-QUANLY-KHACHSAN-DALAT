package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.request.KhachHangPhongRequest;
import vttu.khachsanservice.dto.response.KhachHangPhongResponse;
import vttu.khachsanservice.entity.Huyen;
import vttu.khachsanservice.entity.KhachHangPhong;
import vttu.khachsanservice.entity.LoaiGiayTo;
import vttu.khachsanservice.entity.XepPhongBooKing;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.KhachHangPhongMapper;
import vttu.khachsanservice.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KhachHangPhongService {

    KhachHangPhongRepository khangHangPhongRepository;
    KhachHangPhongMapper khachHangPhongMapper;
    private final DanhXungRepository danhXungRepository;
    private final LoaiGiayToRepository loaiGiayToRepository;
    private final QuocGiaRepository quocGiaRepository;
    private final XepPhongRepository xepPhongRepository;
    private final KhachHangPhongRepository khachHangPhongRepository;
    private final LoaiTreEmRepository loaiTreEmRepository;
    private final TinhThanhRepository tinhThanhRepository;
    private final HuyenRepository huyenRepository;
    private final PhuongXaRepository phuongXaRepository;


    // tạo id mã string tự động tăng
    public synchronized String generateNewBookingId() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // Ví dụ: "20250303"
        Optional<String> latestId = khangHangPhongRepository.findLatestMaKhachHangPhong();
        if (latestId.isPresent() && latestId.get().startsWith("KH-P" + datePrefix)) {
            int number = Integer.parseInt(latestId.get().substring(12)); // Sau "BK20250303"
            return String.format("KH-P%s%03d", datePrefix, number + 1);
        }
        return "KH-P" + datePrefix + "001"; // Ví dụ: "BK20250303001"
    }

//    @Transactional
//    public KhachHangPhongResponse createKhachHangPhong(String ma_xepphong, KhachHangPhongRequest request) {
//
//      XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(ma_xepphong)
//                        .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND, ma_xepphong));
//
//        danhXungRepository.findById(request.getDanhXung().getMaDanhXung())
//                .orElseThrow(() -> new AppException(ErrorCode.DANH_XUNG_NOT_FOUND));
//
//
//        quocGiaRepository.findById(request.getQuocGia().getMaQuocGia())
//                .orElseThrow(() -> new AppException(ErrorCode.QUOC_TICH_NOT_FOUND));
//
//        loaiTreEmRepository.findById(request.getLoaiTreEm().getMaLoaiTreEm())
//                .orElseThrow(() -> new AppException(ErrorCode.LOAI_TRE_EM_NOT_FOUND));
//
//
//        tinhThanhRepository.findById(request.getTinhThanh().getMaTinh())
//                .orElseThrow(() -> new AppException(ErrorCode.TINH_THANH_NOT_FOUND));
//
////        huyenRepository.findById(request.getHuyen().getMaHuyen())
////                .orElseThrow(() -> new AppException(ErrorCode.HUYEN_NOT_FOUND));
//
//        phuongXaRepository.findById(request.getPhuongXa().getMaPhuongXa())
//                .orElseThrow(() -> new AppException(ErrorCode.PHUONG_XA_NOT_FOUND));
//
//        if(request.getNgaySinh() == null || request.getNgaySinh().isEmpty()) {
//            throw new AppException(ErrorCode.NGAY_SINH_NOT_FOUND);
//        }
//
//
//        KhachHangPhong khachHangPhong = khachHangPhongMapper.toCreateKhachHangPhong(request);
//
//        // CAP NHAT HUYEN SAU KHI SAP NHAP
//      Huyen huyen = new Huyen();
//      huyen.setMaHuyen("000000000");
//
//      khachHangPhong.setHuyen(huyen);
//
//
//        if(request.getSoTuoiTre() == 0) {
//            if (request.getLoaiGiayTo().getMaLoaiGiayTo() != null && !request.getLoaiGiayTo().getMaLoaiGiayTo().isEmpty()) {
//                LoaiGiayTo loaiGiayTo = loaiGiayToRepository.findById(request.getLoaiGiayTo().getMaLoaiGiayTo())
//                        .orElseThrow(() -> new AppException(ErrorCode.LOAI_GIAY_TO_NOT_FOUND));
//                khachHangPhong.setLoaiGiayTo(loaiGiayTo);
//            } else {
//                khachHangPhong.setLoaiGiayTo(null); // Nếu null, Hibernate sẽ lưu null vào DB
//            }
//
//            if(request.getSoGiayTo().equals(""))
//                throw new AppException(ErrorCode.SO_GIAY_TO_NOT_FOUND);
//        }
//
//        var authentiaction = SecurityContextHolder.getContext().getAuthentication();
//        khachHangPhong.setXepPhongBooKing(xepPhongBooKing);
//        khachHangPhong.setMaKhachHangPhong(generateNewBookingId());
//        khachHangPhong.setThoiGianTao(LocalDateTime.now());
//        khachHangPhong.setNguoiTao(authentiaction.getName());
//
//        if(!request.getDanhXung().getMaDanhXung().equals("Children"))
//        {
//            khachHangPhong.setSoTuoiTre(0);
//
//        }
//
//        khachHangPhong = khangHangPhongRepository.save(khachHangPhong);
//        return khachHangPhongMapper.toKhachHangPhongResponse(khachHangPhong);
//
//    }

@Transactional
public KhachHangPhongResponse createKhachHangPhong(String ma_xepphong, KhachHangPhongRequest request) {

  XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(ma_xepphong)
    .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND, ma_xepphong));

  danhXungRepository.findById(request.getDanhXung().getMaDanhXung())
    .orElseThrow(() -> new AppException(ErrorCode.DANH_XUNG_NOT_FOUND));


  quocGiaRepository.findById(request.getQuocGia().getMaQuocGia())
    .orElseThrow(() -> new AppException(ErrorCode.QUOC_TICH_NOT_FOUND));

  loaiTreEmRepository.findById(request.getLoaiTreEm().getMaLoaiTreEm())
    .orElseThrow(() -> new AppException(ErrorCode.LOAI_TRE_EM_NOT_FOUND));


  tinhThanhRepository.findById(request.getTinhThanh().getMaTinh())
    .orElseThrow(() -> new AppException(ErrorCode.TINH_THANH_NOT_FOUND));

  huyenRepository.findById(request.getHuyen().getMaHuyen())
    .orElseThrow(() -> new AppException(ErrorCode.HUYEN_NOT_FOUND));

  phuongXaRepository.findById(request.getPhuongXa().getMaPhuongXa())
    .orElseThrow(() -> new AppException(ErrorCode.PHUONG_XA_NOT_FOUND));

  if(request.getNgaySinh() == null || request.getNgaySinh().isEmpty()) {
    throw new AppException(ErrorCode.NGAY_SINH_NOT_FOUND);
  }


  KhachHangPhong khachHangPhong = khachHangPhongMapper.toCreateKhachHangPhong(request);


  if(request.getSoTuoiTre() == 0) {
    if (request.getLoaiGiayTo().getMaLoaiGiayTo() != null && !request.getLoaiGiayTo().getMaLoaiGiayTo().isEmpty()) {
      LoaiGiayTo loaiGiayTo = loaiGiayToRepository.findById(request.getLoaiGiayTo().getMaLoaiGiayTo())
        .orElseThrow(() -> new AppException(ErrorCode.LOAI_GIAY_TO_NOT_FOUND));
      khachHangPhong.setLoaiGiayTo(loaiGiayTo);
    } else {
      khachHangPhong.setLoaiGiayTo(null); // Nếu null, Hibernate sẽ lưu null vào DB
    }

    if(request.getSoGiayTo().equals(""))
      throw new AppException(ErrorCode.SO_GIAY_TO_NOT_FOUND);
  }

  var authentiaction = SecurityContextHolder.getContext().getAuthentication();
  khachHangPhong.setXepPhongBooKing(xepPhongBooKing);
  khachHangPhong.setMaKhachHangPhong(generateNewBookingId());
  khachHangPhong.setThoiGianTao(LocalDateTime.now());
  khachHangPhong.setNguoiTao(authentiaction.getName());

  if(!request.getDanhXung().getMaDanhXung().equals("Children"))
  {
    khachHangPhong.setSoTuoiTre(0);

  }

  khachHangPhong = khangHangPhongRepository.save(khachHangPhong);
  return khachHangPhongMapper.toKhachHangPhongResponse(khachHangPhong);

}



    public List<KhachHangPhongResponse> getKhachHangPhongByMaXepPhong(String ma_xepphong) {
        xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(()-> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));

        return khachHangPhongRepository.getKhachHangPhongByXepPhongBooking(ma_xepphong)
                .stream().map(khachHangPhongMapper::toKhachHangPhongResponse).collect(Collectors.toList());
    }

    public KhachHangPhongResponse getKhachHangPhongById(String ma_KhachHangPhong) {
        khachHangPhongRepository.findById(ma_KhachHangPhong)
                .orElseThrow(() -> new  AppException(ErrorCode.KHACH_HANG_PHONG_NOT_FOUND));

        return khachHangPhongMapper.toKhachHangPhongResponse(khachHangPhongRepository.findById(ma_KhachHangPhong).get());
    }

    @Transactional
    public KhachHangPhongResponse updateKhachHangPhong(String ma_xepphong,KhachHangPhongRequest request) {

        khachHangPhongRepository.findById(request.getMaKhachHangPhong())
                       .orElseThrow(() -> new AppException(ErrorCode.KHACH_HANG_PHONG_NOT_FOUND));

        xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND, ma_xepphong));


        danhXungRepository.findById(request.getDanhXung().getMaDanhXung())
                .orElseThrow(() -> new AppException(ErrorCode.DANH_XUNG_NOT_FOUND));

        quocGiaRepository.findById(request.getQuocGia().getMaQuocGia())
                .orElseThrow(() -> new AppException(ErrorCode.QUOC_TICH_NOT_FOUND));


        tinhThanhRepository.findById(request.getTinhThanh().getMaTinh())
                .orElseThrow(() -> new AppException(ErrorCode.TINH_THANH_NOT_FOUND));

//        huyenRepository.findById(request.getHuyen().getMaHuyen())
//                .orElseThrow(() -> new AppException(ErrorCode.HUYEN_NOT_FOUND));

        phuongXaRepository.findById(request.getPhuongXa().getMaPhuongXa())
                .orElseThrow(() -> new AppException(ErrorCode.PHUONG_XA_NOT_FOUND));

        if(request.getNgaySinh() == null || request.getNgaySinh().isEmpty()) {
            throw new AppException(ErrorCode.NGAY_SINH_NOT_FOUND);
        }

        KhachHangPhong khachHangPhong = khachHangPhongMapper.toCreateKhachHangPhong(request);

        if(request.getSoTuoiTre() == 0) {
            if (request.getLoaiGiayTo().getMaLoaiGiayTo() != null && !request.getLoaiGiayTo().getMaLoaiGiayTo().isEmpty()) {
                LoaiGiayTo loaiGiayTo = loaiGiayToRepository.findById(request.getLoaiGiayTo().getMaLoaiGiayTo())
                        .orElseThrow(() -> new AppException(ErrorCode.LOAI_GIAY_TO_NOT_FOUND));
                khachHangPhong.setLoaiGiayTo(loaiGiayTo);
            } else {
                khachHangPhong.setLoaiGiayTo(null); // Nếu null, Hibernate sẽ lưu null vào DB
            }

            if(request.getSoGiayTo().equals(""))
                throw new AppException(ErrorCode.MA_PHONG_NOT_FOUND);
        }

        KhachHangPhong khp = khachHangPhongRepository.findById(request.getMaKhachHangPhong())
                .orElseThrow(() -> new AppException(ErrorCode.KHACH_HANG_PHONG_NOT_FOUND));

        XepPhongBooKing xepPhongBooKing = new XepPhongBooKing();
        xepPhongBooKing.setMaXepPhongBooking(ma_xepphong);
        khachHangPhong.setXepPhongBooKing(xepPhongBooKing);
        khachHangPhong.setNguoiTao(khp.getNguoiTao());
        khachHangPhong.setThoiGianTao(khp.getThoiGianTao());

        khachHangPhong = khangHangPhongRepository.save(khachHangPhong);
        return khachHangPhongMapper.toKhachHangPhongResponse(khachHangPhong);

    }

    public boolean deleteThongTinKhachHang(String ma_xepphong_booking) {
        khachHangPhongRepository.findById(ma_xepphong_booking)
                .orElseThrow(() -> new AppException(ErrorCode.KHACH_HANG_PHONG_NOT_FOUND));

        try {
            khachHangPhongRepository.deleteById(ma_xepphong_booking);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa khách hàng: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String,Object>> getThongTinKhachHangLuuTru(LocalDate ngayDen, LocalDate ngayDi) {
        if (ngayDen == null || ngayDi == null) {
            throw new AppException(ErrorCode.ERROR_NGAY);
        }
        if(ngayDi.isBefore(ngayDen)) {
            throw new AppException(ErrorCode.ERROR_NGAY,"không được nhỏ hơn ngày đến");
        }
        return khachHangPhongRepository.getThongTinLuuTruKhoanThoiGian(ngayDen, ngayDi);
    }
}
