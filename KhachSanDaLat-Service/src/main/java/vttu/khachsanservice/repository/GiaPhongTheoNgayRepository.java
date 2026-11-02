package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vttu.khachsanservice.entity.GiaPhongTheoNgay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GiaPhongTheoNgayRepository extends JpaRepository<GiaPhongTheoNgay, String> {

   @Modifying
    @Query("DELETE GiaPhongTheoNgay g WHERE g.chiTietBooking.maChiTietBooking = ?1")
    void xoaGiaPhongTheoNgay(String maChiTietBooking);

   @Query("SELECT g FROM GiaPhongTheoNgay g WHERE g.chiTietBooking.maChiTietBooking = ?1")
   List<GiaPhongTheoNgay> findByMaChiTietBooking(String maChiTietBooking);

    @Query("""
    SELECT g
    FROM GiaPhongTheoNgay g
    WHERE g.chiTietBooking.maChiTietBooking = ?1
      AND g.ngay >= ?2
      AND g.ngay < ?3
    ORDER BY g.ngay
""")
    List<GiaPhongTheoNgay> findByChiTietBookingAndNgay(
            String maChiTietBooking,
            LocalDate ngayBatDau,
            LocalDate ngayKetThuc
    );

   @Modifying
    @Query("DELETE FROM GiaPhongTheoNgay g WHERE g.chiTietBooking.maChiTietBooking = ?1 AND g.ngay = ?2")
    void xoaGiaPhongTheoNgayByMaChiTietBookingAndNgay(String maChiTietBooking, LocalDate ngay);

    @Query(value = """
            select gp.* from xep_phong_booking xpb
            left join gia_phong_theo_ngay gp on gp.ma_chi_tiet_booking = xpb.ma_chi_tiet_booking
            where ma_xep_phong_booking = :maChiTietBooking;

""", nativeQuery = true)
    List<GiaPhongTheoNgay> findByMaXepPhongBooking(String maChiTietBooking);

    @Query("SELECT gp FROM GiaPhongTheoNgay gp WHERE gp.chiTietBooking.maChiTietBooking = :maChiTietBooking and gp.ngay = :ngay")
    GiaPhongTheoNgay findGiaPhongTheoNgayByMaChiTietBookingAndNgay(@Param("maChiTietBooking") String maChiTietBooking,
                                                                  @Param("ngay") LocalDate ngay);


    @Modifying
    @Query("UPDATE GiaPhongTheoNgay gp SET gp.gia =:gia WHERE gp.chiTietBooking.maChiTietBooking =:maChiTiet AND gp.ngay =:ngay")
    void updateGiaPhongTheoNgayByMaChiTietBookingAndNgay(@Param("gia") BigDecimal gia,
                                                        @Param("maChiTiet") String maChiTiet,
                                                        @Param("ngay") LocalDate ngay);
    @Modifying
    @Query(value = "DELETE FROM gia_phong_theo_ngay WHERE ma_gia_phong_theo_ngay = :maGiaPhongTheoNgay", nativeQuery = true)
    void deleteByMaGiaPhongTheoNgay(String maGiaPhongTheoNgay);
}
