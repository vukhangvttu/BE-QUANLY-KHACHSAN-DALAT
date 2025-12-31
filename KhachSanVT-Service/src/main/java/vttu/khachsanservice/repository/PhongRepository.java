package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.PhongDTO;
import vttu.khachsanservice.dto.PhongStatusProjection;
import vttu.khachsanservice.dto.PhongTrongThoiGian;
import vttu.khachsanservice.entity.Phong;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PhongRepository extends JpaRepository<Phong, String> {

    @Query(value = "select p.ma_phong, p.ten_phong, p.ma_loaiphong from loai_phong as lp\n" +
            "join phong p on p.ma_loaiphong = lp.ma_loai_phong\n" +
            "where lp.ma_loai_phong = :ma_loai_phong", nativeQuery = true)
    List<PhongDTO> findPhongByMaLoaiPhong(String ma_loai_phong);


    @Query(value = "SELECT * FROM get_room_status()", nativeQuery = true)
    List<PhongStatusProjection> getPhongWithCurrentStatus();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Phong p SET p.daDo = :daDo WHERE p.maPhong = :maPhong")
    void updatePhongWithCurrentStatus(@Param("daDo") boolean daDo, @Param("maPhong") String maPhong);

    @Query(value = """
        select * from danh_sach_phong_trong_theo_loai(:maloaiphong,:ngay_den,:ngay_di)
""", nativeQuery = true)
    List<PhongTrongThoiGian> getPhongTrongCurrent(String maloaiphong, LocalDate ngay_den, LocalDate ngay_di);

    @Query(value = "select * from  get_du_bao_phong(:ngay_bd, :ngay_kt)", nativeQuery = true)
    List<PhongStatusProjection> getDuBaoPhong(LocalDate ngay_bd, LocalDate ngay_kt);

    // DANH CHO ĐÃ XẾP PHÒNG
    @Query(value = """
        SELECT * FROM danh_sach_phong_trong_theo_loai_xep_phong(:maloaiphong,:ngay_bd, :ngay_kt, :ma_booking)
""", nativeQuery = true)
    List<PhongTrongThoiGian> getDuBaoPhongAndXepPhong(String maloaiphong,LocalDate ngay_bd, LocalDate ngay_kt, String ma_booking);


    // Hiển thị thông tin khách hàng của phon line phòng
    @Query(value = """
        WITH  dich_vu_agg AS (
                SELECT
                    xpb.ma_xep_phong_booking,
                    COALESCE(SUM((dv.gia::numeric * ctdv.so_luong)::numeric), 0) AS tong_tien_dich_vu
                FROM
                    xep_phong_booking xpb
                LEFT JOIN
                    phieu_dich_vu ctdv ON ctdv.ma_xep_phong_booking = xpb.ma_xep_phong_booking
                LEFT JOIN
                    dich_vu dv ON ctdv.ma_dichvu = dv.ma_dich_vu
                GROUP BY
                    xpb.ma_xep_phong_booking
            )
        SELECT xp.ma_booking, lp.ma_loai_phong, lp.ten_loai_phong ,xp.ma_phong, xp.ngay_den, xp.ngay_di,
		xp.gia,xp.phu_thu_tien_giuong,xp.thoi_gian_nhan, booking.ghi_chu, dich_vu_agg.tong_tien_dich_vu,
		nv.ma_nhan_vien
        FROM xep_phong_booking xp
        JOIN phong p on xp.ma_phong = p.ma_phong
        JOIN loai_phong lp on p.ma_loaiphong = lp.ma_loai_phong
        JOIN booking on booking.ma_booking = xp.ma_booking
        JOIN dich_vu_agg ON xp.ma_xep_phong_booking = dich_vu_agg.ma_xep_phong_booking
		JOIN nhan_vien nv ON nv.account_username = booking.nguoi_tao
        where xp.ma_xep_phong_booking = :ma_xepphong;
""", nativeQuery = true)
    Map<String, Object> getThongTinKhachHangBooking(String ma_xepphong);


    @Modifying
    @Query("UPDATE Phong p SET p.daDo = :daDo WHERE p.maPhong IN :maPhongs")
    int updateDaDoStatus(@Param("maPhongs") List<String> maPhongs,
                         @Param("daDo") boolean daDo);

}
