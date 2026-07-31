package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.HoaDonVatPhatHanh;

import java.util.List;

public interface HoaDonVatPhatHanhRepository extends JpaRepository<HoaDonVatPhatHanh, Long> {

    @Query("SELECT hd FROM HoaDonVatPhatHanh hd WHERE hd.hoaDonVat.maHoaDonVat = :maHoaDonVat ORDER BY hd.thoiGianTao DESC LIMIT 1 ")
    HoaDonVatPhatHanh getThongTinHoaDonTheoHoaDonVat(String maHoaDonVat);

    @Query("SELECT COUNT (*) FROM HoaDonVatPhatHanh hd WHERE hd.hoaDonVat.maHoaDonVat = :maHoaDonVat")
    int countByMaHoaDonVat(String maHoaDonVat);

    @Modifying
    @Query("UPDATE HoaDonVat  hd SET hd.daHuy = :daHuy WHERE hd.maHoaDonVat = :maHoaDonVat")
    int updateDaHuyHoaDonVatPhatHanh(boolean daHuy,String maHoaDonVat);
}
