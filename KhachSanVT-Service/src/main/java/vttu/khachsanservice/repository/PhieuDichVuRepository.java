package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vttu.khachsanservice.entity.PhieuDichVu;

import java.util.List;
import java.util.Optional;

public interface PhieuDichVuRepository extends JpaRepository<PhieuDichVu, String> {


    @Query(value = """
        select pdv.* from phieu_dich_vu pdv
         join xep_phong_booking xp on pdv.ma_xep_phong_booking = xp.ma_xep_phong_booking
         join phong p on p.ma_phong = pdv.ma_phong
        where pdv.ma_xep_phong_booking = :ma_xepphong_booking and pdv.thoi_gian_xoa IS NULL
        order by pdv.auto_increment_id asc
""", nativeQuery = true)
    List<PhieuDichVu> getAllPhieuDichVuByMaBooKing(@Param("ma_xepphong_booking") String ma_xepphong_booking);

    @Query("SELECT pdv FROM PhieuDichVu pdv WHERE pdv.xepPhongBooKing.maXepPhongBooking = :maXepPhongBooking AND pdv.thoiGianXoa IS NULL")
    List<PhieuDichVu> getPhieuDichVuByMaBooKing(String maXepPhongBooking);


    // Cập nhât thời gian xóa
    @Modifying
    @Query("UPDATE PhieuDichVu pdv SET pdv.thoiGianXoa = CURRENT_TIMESTAMP WHERE pdv.maPhieuDichVu = :maPhieuDichVu")
    void updateThoiGianXoa(@Param("maPhieuDichVu") String maPhieuDichVu);
}
