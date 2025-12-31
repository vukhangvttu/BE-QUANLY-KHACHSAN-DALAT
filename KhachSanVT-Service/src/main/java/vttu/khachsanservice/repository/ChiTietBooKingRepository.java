package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vttu.khachsanservice.dto.PhuThuKhacDto;
import vttu.khachsanservice.entity.ChiTietBooking;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChiTietBooKingRepository extends JpaRepository<ChiTietBooking, String> {

    @Query("SELECT ct FROM ChiTietBooking ct where ct.booKing.maBooking = :maBooking")
    List<ChiTietBooking> getChiTietBookingByMaBooking(String maBooking);

    @Query("SELECT bk.maChiTietBooking  FROM ChiTietBooking bk ORDER BY bk.maChiTietBooking DESC LIMIT 1")
    Optional<String> findLatestMaCTBooKing();

    @Query(value = "select * from chi_tiet_booking where ma_booking = :maBooking and ma_loai_phong = :maLoaiPhong", nativeQuery = true)
    ChiTietBooking kiemTraXemPhongDaTonTaiChua(String maBooking, String maLoaiPhong);


        @Query("SELECT c FROM ChiTietBooking c WHERE c.booKing.maBooking = :maBooking AND c.loaiPhong.maLoaiPhong IN :maLoaiPhongs")
        List<ChiTietBooking> findByMaBookingAndLoaiPhong(@Param("maBooking") String maBooking,
                                                         @Param("maLoaiPhongs") List<String> maLoaiPhongs);

        @Query("SELECT COUNT(*) FROM ChiTietBooking CT WHERE CT.booKing.maBooking = :mabooking")
        Long countChiTietBookingByMaBooking(@Param("mabooking") String maBooking);

        @Query("SELECT COUNT(*) FROM ChiTietBooking CT WHERE CT.maChiTietBooking =:maChiTietBooking")
        Long countChiTietBookingByMaChiTietBooking(@Param("maChiTietBooking") String maChiTietBooking);

        // Lấy thông tin xuất phiếu chi tiết booking
    @Query(value = """
         SELECT * FROM xuat_phieu_chi_tiet_dat_phong(:mabooking)
""",nativeQuery = true)
    List<Map<String, Object>> getThongTinXuatChiTietBooking(String mabooking);


    @Query(value = """
        SELECT
         ctbk.ngay_den, ctbk.ngay_di, lp.ten_loai_phong,
         (ctbk.gia_extra_bed * so_luong_extra_bed * so_dem) AS tong_tien_phu_thu_extra,
         (ctbk.gia_phu_thu_tre_em * so_luong_phu_thu_tre_em * so_dem) AS tong_tien,
         ctbk.gia_phu_thu_tre_em as gia,
         ctbk.gia_phu_thu_tre_em,so_luong_phu_thu_tre_em, so_dem,gia_extra_bed,so_luong_extra_bed,
        ctbk.so_luong_phu_thu_an_sang, ctbk.gia_phu_thu_an_sang
        FROM chi_tiet_booking ctbk\s
        LEFT JOIN loai_phong lp ON lp.ma_loai_phong = ctbk.ma_loai_phong
        WHERE ma_booking = :mabooking
""", nativeQuery = true)
    List<Map<String, Object>> getThongTinXuatChiTietPhuThuBooking(String mabooking);


    // kiem tra ma phong da ton tai trong chi tiet booking trong khoan thoi gian
  @Query(value = """
           SELECT COUNT(*)
                FROM chi_tiet_booking ct
                JOIN danh_sach_phong_chi_tiet ds ON ct.ma_chi_tiet_booking = ds.ma_chi_tiet_booking
                JOIN booking bk ON ct.ma_booking = bk.ma_booking
                WHERE ds.ma_phong = :maPhong
                  AND ct.ngay_den < :ngayDi
                  AND ct.ngay_di > :ngayDen
                  AND bk.ma_trang_thai_booking NOT IN(3,4);

""", nativeQuery = true)
  int kiemTraMaPhongDaTonTaiTrongChiTietBooking(@Param("maPhong") String maPhong,
                                                  @Param("ngayDen") LocalDate ngayDen,
                                                  @Param("ngayDi") LocalDate ngayDi);

  @Query(value = """
           SELECT COUNT(*)
                FROM chi_tiet_booking ct
                JOIN danh_sach_phong_chi_tiet ds ON ct.ma_chi_tiet_booking = ds.ma_chi_tiet_booking
                JOIN booking bk ON ct.ma_booking = bk.ma_booking
                WHERE ds.ma_phong = :maPhong
                  AND ct.ma_chi_tiet_booking != :maChiTietBooking -- loại trừ bản ghi đang cập nhật
                  AND ct.ngay_den < :ngayDi
                  AND ct.ngay_di > :ngayDen
                  AND bk.ma_trang_thai_booking NOT IN(3,4);


""", nativeQuery = true)
  int kiemTraMaPhongDaTonTaiTrongChiTietBookingUpdate(@Param("maPhong") String maPhong,
                                                @Param("ngayDen") LocalDate ngayDen,
                                                @Param("ngayDi") LocalDate ngayDi,
                                                @Param("maChiTietBooking") String maChiTietBooking);


  // LAY DANH DACH PHU THU TRE EM RA THANH TOAN
  @Query(value = """
		 select  ctbk.so_luong_phu_thu_tre_em, ctbk.gia_phu_thu_tre_em,
               ctbk.so_dem, COALESCE(ctbk.so_luong_phu_thu_tre_em *ctbk.gia_phu_thu_tre_em * ctbk.so_dem , 0) AS thanhTienPhuThuTreEm,
			    ctbk.so_luong_phu_thu_an_sang, ctbk.gia_phu_thu_an_sang,
				COALESCE(ctbk.so_luong_phu_thu_an_sang *ctbk.gia_phu_thu_an_sang * ctbk.so_dem , 0) AS thanhTienPhuThuAnSang

        from chi_tiet_booking ctbk
        where ctbk.ma_booking = :maBooking and (ctbk.so_luong_phu_thu_tre_em != 0 or ctbk.so_luong_phu_thu_an_sang !=0)

""" , nativeQuery = true)
  List<Map<String,Object>> getDanhSachPhuThuTreEmThanhToan(String maBooking);
}
