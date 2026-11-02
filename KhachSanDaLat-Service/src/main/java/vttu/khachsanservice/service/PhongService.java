package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.PhongDTO;
import vttu.khachsanservice.dto.PhongStatusProjection;
import vttu.khachsanservice.dto.PhongTrongThoiGian;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.PhongMapper;
import vttu.khachsanservice.repository.BooKingRepository;
import vttu.khachsanservice.repository.LoaiPhongRepository;
import vttu.khachsanservice.repository.PhongRepository;
import vttu.khachsanservice.repository.XepPhongRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhongService {

    PhongRepository phongRepository;
    PhongMapper phongMapper;
    private final BooKingRepository booKingRepository;
    private final LoaiPhongRepository loaiPhongRepository;
    private final XepPhongRepository xepPhongRepository;

    public List<PhongDTO>getPhongByMaLoaiPhong(String maLoaiPhong ) {

        return phongRepository.findPhongByMaLoaiPhong(maLoaiPhong);
    }

    public List<PhongStatusProjection> getAllPhongChatRoom() {
        return phongRepository.getPhongWithCurrentStatus();
    }

    public List<PhongTrongThoiGian> getAllPhongTrongThoiGian(String maLoaiPhong, LocalDate ngay_den, LocalDate ngay_di) {
        return phongRepository.getPhongTrongCurrent(maLoaiPhong,ngay_den, ngay_di);
    }

    @Transactional
    public boolean updateTrangThaiVeSinh(boolean trangthai,String maPhong) {
        phongRepository.findById(maPhong)
                .orElseThrow(() -> new AppException(ErrorCode.PHONG_NOT_FOUND));

        try {
            phongRepository.updatePhongWithCurrentStatus(trangthai,maPhong);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi update trạng thái vệ sinh: " + e.getMessage());
            return false;
        }
    }

    public List<PhongStatusProjection> getDuBaoPhongTheoKhoanThoiGian(LocalDate ngay_bd, LocalDate ngay_kt) {

        if(ngay_bd == null || ngay_kt == null){
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }

        if (ngay_kt.isBefore(ngay_bd)) {
            throw new AppException(ErrorCode.ERROR_THOI_GIAN_DI);
        }

        return phongRepository.getDuBaoPhong(ngay_bd, ngay_kt);
    }

    public List<PhongTrongThoiGian> getDuBaoPhongTheoKhoanThoiGianAndBooKing(String maLoaiPhong,LocalDate ngay_bd, LocalDate ngay_kt,String ma_booking) {

        if(ngay_bd == null || ngay_kt == null){
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }

        if (ngay_kt.isBefore(ngay_bd)) {
            throw new AppException(ErrorCode.ERROR_THOI_GIAN_DI);
        }

        booKingRepository.findById(ma_booking).orElseThrow(() ->
                new AppException(ErrorCode.BOOKING_NOT_FOUND));

        loaiPhongRepository.findById(maLoaiPhong)
                .orElseThrow(() -> new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND));

        return phongRepository.getDuBaoPhongAndXepPhong(maLoaiPhong,ngay_bd, ngay_kt,ma_booking);
    }

    public Map<String,Object> getThongTinKhachHangBooking(String ma_xepphong){
        xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
        return phongRepository.getThongTinKhachHangBooking(ma_xepphong);
    }

    public List<PhongTrongThoiGian> getDanhSachPhongDangOTheoLoaiService(String maLoaiPhong, LocalDate ngay_den, LocalDate ngay_di) {
        return phongRepository.getDanhSachPhongDangOTheoLoai(maLoaiPhong,ngay_den, ngay_di);
    }
}
