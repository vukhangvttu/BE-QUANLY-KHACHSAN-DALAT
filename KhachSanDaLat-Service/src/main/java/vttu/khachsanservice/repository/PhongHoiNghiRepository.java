package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.PhongHoiNghi;

import java.time.LocalDate;
import java.util.List;

public interface PhongHoiNghiRepository extends JpaRepository<PhongHoiNghi,String> {

    @Query("SELECT phongHoiNghi FROM PhongHoiNghi phongHoiNghi WHERE phongHoiNghi.booKing.maBooking = :maBooking")
    PhongHoiNghi getPhongHoiNghiBooking(String maBooking);

    @Modifying
    @Query("DELETE FROM PhongHoiNghi phongHoiNghi WHERE phongHoiNghi.booKing.maBooking = :maBooking")
    void deleteAllByMaBooking(String maBooking);

    // kiểm tra phòng hội nghị đã được đặt trong khoản thời gian này hay chưa
    @Query(value = """
    SELECT NOT EXISTS (
        SELECT 1
        FROM phong_hoi_nghi
        WHERE ngay_bat_dau <= :ngaykt AND ngay_ket_thuc >= :ngaybd
    ) AS is_available;
""" , nativeQuery = true)
    boolean getDuBaoPhongHoiNghi(LocalDate ngaybd, LocalDate ngaykt);
}
