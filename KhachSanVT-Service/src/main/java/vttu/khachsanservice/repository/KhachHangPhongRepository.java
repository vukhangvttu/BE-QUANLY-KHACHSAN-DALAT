package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vttu.khachsanservice.entity.KhachHangPhong;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface KhachHangPhongRepository extends JpaRepository<KhachHangPhong, String> {

    @Query("SELECT khp.maKhachHangPhong  FROM KhachHangPhong khp ORDER BY khp.maKhachHangPhong DESC LIMIT 1")
    Optional<String> findLatestMaKhachHangPhong();

    @Query("SELECT khp FROM KhachHangPhong khp WHERE khp.xepPhongBooKing.maXepPhongBooking = :ma_xepphong")
    List<KhachHangPhong> getKhachHangPhongByXepPhongBooking(String ma_xepphong);

    // lấy tổng số khách theo ma xếp phong
    @Query("SELECT COUNT (*) FROM  KhachHangPhong khp WHERE khp.xepPhongBooKing.maXepPhongBooking=:ma_xepphong")
    int getCountByXepPhongBooking(String ma_xepphong);

    // hiển thị danh sách trước khi xuất
    @Query(value = "SELECT * FROM thong_ke_khach_hang_phong_xuat_excel_asm(:ngayDen, :ngayDi)", nativeQuery = true)
    List<Map<String, Object>> getThongTinLuuTruKhoanThoiGian(
            @Param("ngayDen") LocalDate ngayDen,
            @Param("ngayDi") LocalDate ngayDi);


    // thống kê khách lưu trú theo quý
    @Query(value = "select * from thong_ke_khach_luu_tru_quoc_gia_quy()", nativeQuery = true)
    List<Map<String, Object>> getThongTinLuuTruQuocGiaQuy();
}
