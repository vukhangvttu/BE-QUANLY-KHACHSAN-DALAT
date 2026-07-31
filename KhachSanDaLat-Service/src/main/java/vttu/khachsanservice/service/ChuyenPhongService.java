package vttu.khachsanservice.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.request.GiaPhongTheoNgayRequest;
import vttu.khachsanservice.dto.request.Log_ChuyenPhongRequest;
import vttu.khachsanservice.entity.DanhSachPhongChiTiet;
import vttu.khachsanservice.entity.GiaPhongTheoNgay;
import vttu.khachsanservice.entity.XepPhongBooKing;
import vttu.khachsanservice.entity.log.Log_ChuyenPhong;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.ChuyenPhongMapper;
import vttu.khachsanservice.repository.*;
import vttu.khachsanservice.repository.log.Log_ChuyenPhongRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChuyenPhongService {

    private final XepPhongRepository xepPhongRepository;
    private final PhongRepository phongRepository;
    private final Log_ChuyenPhongRepository log_ChuyenPhongRepository;
  private final GiaPhongTheoNgayRepository giaPhongTheoNgayRepository;
  private final KhachHangPhongRepository khachHangPhongRepository;
  private final DanhSachPhongChiTietRepository danhSachPhongChiTietRepository;

  @Transactional
    public boolean saveChuyenPhong(String maXepPhong, String maPhongCu, String maPhongMoi, String maloaiphong, List<GiaPhongTheoNgayRequest> requests) {

     XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(maXepPhong)
              .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
      phongRepository.findById(maPhongMoi)
              .orElseThrow(() -> new AppException(ErrorCode.MA_PHONG_NOT_FOUND));
        phongRepository.findById(maPhongCu)
                .orElseThrow(() -> new AppException(ErrorCode.MA_PHONG_NOT_FOUND));

        if(maPhongCu.equals(maPhongMoi))
            throw new AppException(ErrorCode.ERROR_PHONG_CU_PHONG_MOI_GIONG_NHAU);

        if(maloaiphong == null || maloaiphong.equals("")) {
            throw new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND);
        }

        if(requests == null || requests.isEmpty()) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_NOT_FOUND);
        }

        int KiemTraPhong = xepPhongRepository.KiemTraPhongDaTraChua(maPhongMoi);
        if(KiemTraPhong > 0)
            throw new AppException(ErrorCode.ERROR_PHONG_CHUA_TRA, "Phòng: " + maPhongMoi);


        var authentiaction = SecurityContextHolder.getContext().getAuthentication();

        Log_ChuyenPhong log_chuyenPhong = new Log_ChuyenPhong();
        log_chuyenPhong.setMaXepPhong(maXepPhong);
        log_chuyenPhong.setLyDo(maloaiphong);
        log_chuyenPhong.setNoiDung("Chuyển từ phòng: " + maPhongCu + " sang phòng: " + maPhongMoi);
        log_chuyenPhong.setUserName(authentiaction.getName());

        log_ChuyenPhongRepository.save(log_chuyenPhong);

       phongRepository.updatePhongWithCurrentStatus(true, maPhongCu);

       //// xóa toàn bộ giá phòng theo ngày của chi tiết booking hiện tại
       giaPhongTheoNgayRepository.xoaGiaPhongTheoNgay(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking());


        //// tạo mới giá phòng theo ngày cho chi tiết booking
       List<GiaPhongTheoNgay> listGiaPhongTheoNgays = new ArrayList<>();

       for (GiaPhongTheoNgayRequest giaPhongTheoNgay : requests) {

         GiaPhongTheoNgay giaPhong = new GiaPhongTheoNgay();
          giaPhong.setChiTietBooking(xepPhongBooKing.getChiTietBooking());
          giaPhong.setMaLoaiPhong(maPhongMoi);
          giaPhong.setNgay(giaPhongTheoNgay.getNgay());
          giaPhong.setGia(giaPhongTheoNgay.getGia());
         listGiaPhongTheoNgays.add(giaPhong);
       }

       giaPhongTheoNgayRepository.saveAll(listGiaPhongTheoNgays);

       //// cap nhat lai ma phong trong danh sach xep phong chi tiet
    DanhSachPhongChiTiet danhSachPhongChiTiet = danhSachPhongChiTietRepository.findByChiTietBooking_MaChiTietBookingAndMaPhong(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking(), maPhongCu);
          if(danhSachPhongChiTiet == null) {
            throw new AppException(ErrorCode.DANH_SACH_PHONG_CHI_TIET_NOT_FOUND, maPhongCu);
          }
            danhSachPhongChiTiet.setMaPhong(maPhongMoi);
            danhSachPhongChiTietRepository.save(danhSachPhongChiTiet);

        int updatePhong = xepPhongRepository.UpdatePhong(maPhongMoi,maXepPhong,true);
        if (updatePhong == 0) {

            throw new AppException(ErrorCode.UPDATE_DA_XEP_PHONG);
        }

      return true;
  }
}
