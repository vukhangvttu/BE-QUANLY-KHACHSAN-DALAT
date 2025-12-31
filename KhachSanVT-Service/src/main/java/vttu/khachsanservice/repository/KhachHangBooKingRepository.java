package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.KhachHangBooKing;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface KhachHangBooKingRepository extends JpaRepository<KhachHangBooKing, String> {

    @Query("SELECT k.maKhachHangBooking FROM KhachHangBooKing k ORDER BY k.maKhachHangBooking DESC LIMIT 1")
    Optional<String> findLatestKhachHangId();

    @Query(value = "SELECT bk.ma_danh_xung || '.' || khbk.ho_khach_hang_booking || ' ' || khbk.ten_khach_hang_booking as ten_khach,\n" +
            "       bk.loai_nguon_khach,khbk.dia_chi_booking,khbk.ma_so_thue,\n" +
            "       khbk.email_booking, khbk.sdt_booking\n" +
            "FROM booking bk\n" +
            "LEFT JOIN khach_hang_booking khbk ON khbk.ma_khach_hang_booking = bk.ma_khach_hang_booking\n" +
            "WHERE ma_booking = :maBooking", nativeQuery = true)
    Map<String, Object> getThongTinKhachHangXuatVAT(String maBooking);
}
