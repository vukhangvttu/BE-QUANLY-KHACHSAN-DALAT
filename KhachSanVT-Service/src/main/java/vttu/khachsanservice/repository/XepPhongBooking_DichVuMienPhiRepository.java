package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.XepPhongBooking_DichVuMienPhi;

import java.time.LocalDate;
import java.util.List;

public interface XepPhongBooking_DichVuMienPhiRepository extends JpaRepository<XepPhongBooking_DichVuMienPhi, Long> {

    // This interface extends JpaRepository, which provides basic CRUD operations.
    // You can define custom query methods here if needed, but currently, it does not have any.
    @Query(value = "select * from xep_phong_booking_dich_vu_mien_phi where ma_xep_phong_booking =:maXepPhongBooking", nativeQuery = true)
    List<XepPhongBooking_DichVuMienPhi> findByMaXepPhongBooking(String maXepPhongBooking);

    @Modifying
    @Query(value = "DELETE FROM xep_phong_booking_dich_vu_mien_phi WHERE ma_xep_phong_booking =?1 AND ngay_bo_sung =?2", nativeQuery = true)
    void deleteByMaXepPhongBooking(String maXepPhongBooking, LocalDate ngayBoSung);

    @Query(value = "SELECT COUNT(*) FROM xep_phong_booking_dich_vu_mien_phi WHERE ma_xep_phong_booking =:maXepPhongBooking AND ngay_bo_sung = :ngayBoSung", nativeQuery = true)
    long countByMaXepPhongBooking(String maXepPhongBooking, LocalDate ngayBoSung);

    @Modifying
    @Query(value = "UPDATE xep_phong_booking_dich_vu_mien_phi SET da_xoa = true WHERE ma_xep_phong_booking =:maXepPhongBooking AND ngay_bo_sung = :ngayBoSung", nativeQuery = true)
    void deleteByMaXepPhongBookingAndNgayBoSung(String maXepPhongBooking, LocalDate ngayBoSung);
}
