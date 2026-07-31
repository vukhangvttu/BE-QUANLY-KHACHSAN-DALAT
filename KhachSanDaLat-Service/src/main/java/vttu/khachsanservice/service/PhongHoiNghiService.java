package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.PhongHoiNghi;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.BooKingRepository;
import vttu.khachsanservice.repository.PhongHoiNghiRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhongHoiNghiService {
    private final PhongHoiNghiRepository phongHoiNghiRepository;
    private final BooKingRepository booKingRepository;


    public boolean deleteAllByMaBooking(String maBooking, String maPhongHoiNghi) {
        try {
           BooKing booKing = booKingRepository.findById(maBooking)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

            phongHoiNghiRepository.deleteAllByMaBooking(maBooking);

            // Assuming deleteAllByMaBooking does not throw an exception if no records are deleted,
            // you might want to check if records were actually deleted if necessary.


            PhongHoiNghi phongHoiNghi = phongHoiNghiRepository.findById(maPhongHoiNghi)
                    .orElseThrow(()->new AppException(ErrorCode.PHONG_NOT_FOUND));

            BigDecimal tongTien = booKing.getTongTien().subtract(phongHoiNghi.getDonGia());

            booKingRepository.updateTongTien(tongTien,maBooking);
            return true; // Return true if deletion is successful
        } catch (Exception e) {
            // Log the exception if needed
            System.err.println("Error deleting by maBooking: " + e.getMessage());
            return false; // Return false if there is an error
        }
    }

    // kiêm tra phòng hội nghị đã được đặt chưa
    public boolean kiemTraPhongHoiNghiTrongKhoanThoiGian(LocalDate ngayBD, LocalDate ngayKT){

        if(ngayBD == null || ngayKT==null){
            throw new AppException(ErrorCode.ERROR_NGAY);
        }

        if (ngayKT.isBefore(ngayBD)) {
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }

        return phongHoiNghiRepository.getDuBaoPhongHoiNghi(ngayBD,ngayKT);
    }

}
