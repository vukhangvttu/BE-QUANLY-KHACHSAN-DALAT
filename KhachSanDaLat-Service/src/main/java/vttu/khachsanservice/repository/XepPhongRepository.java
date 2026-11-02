package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.PhongTrongThoiGian;
import vttu.khachsanservice.entity.XepPhongBooKing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface XepPhongRepository extends JpaRepository<XepPhongBooKing, String> {


    @Query("SELECT xp FROM XepPhongBooKing xp WHERE xp.booKing.maBooking = :ma_booking AND xp.thoiGianXoa IS NULL")
    List<XepPhongBooKing> getXepPhongBooKingsByMaBooking(String ma_booking);

    @Modifying
    @Query("DELETE FROM XepPhongBooKing xp WHERE xp.booKing.maBooking = :ma_booking")
    void deleteXepPhongBooKings(String ma_booking);

    @Query("SELECT COUNT(*) FROM XepPhongBooKing xp WHERE xp.booKing.maBooking = :maBooking")
    long countByMaBooking(String maBooking);

    // cập nhật nhận phòng
    @Modifying
    @Transactional
    @Query("UPDATE XepPhongBooKing xp SET xp.daNhanPhong =:danhanphong, xp.thoiGianNhan = :thoigiannhan , xp.userCheckIn = :userCheckIn,xp.checkInTre =:checkinTre , xp.phuThuCheckInSom =:phuThuCheckInSom  WHERE xp.maXepPhongBooking = :maxepphongbooking")
    int updateDanhanPhong(boolean danhanphong, LocalDateTime thoigiannhan, String maxepphongbooking, String userCheckIn, boolean checkinTre, BigDecimal phuThuCheckInSom);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE XepPhongBooKing xp
            SET xp.daNhanPhong = :daNhanPhong,
                xp.thoiGianNhan = :thoiGianNhan,
                xp.userCheckIn = :userCheckIn,
                xp.checkInTre = CASE
                    WHEN xp.ngayDen < :ngayHienTai
                      OR (xp.ngayDen = :ngayHienTai AND xp.gioDen < :gioHienTai)
                    THEN true
                    ELSE false
                END
            WHERE xp.maXepPhongBooking IN :maXepPhongBookings
            """)
    int updateDaNhanPhongBatch(
            @Param("maXepPhongBookings") List<String> maXepPhongBookings,
            @Param("daNhanPhong") boolean daNhanPhong,
            @Param("thoiGianNhan") LocalDateTime thoiGianNhan,
            @Param("userCheckIn") String userCheckIn,
            @Param("ngayHienTai") LocalDate ngayHienTai,
            @Param("gioHienTai") LocalTime gioHienTai);

    @Query("SELECT COUNT(*) FROM XepPhongBooKing xp WHERE xp.maXepPhongBooking = :maxepphongbooking")
    long countByMaXepPhongBooking(String maxepphongbooking);

    // cật nhật trả phòng
    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.daTraPhong =:datrphong, xp.thoiGianTra = :thoigiantra  WHERE xp.maXepPhongBooking = :maxepphongbooking")
    int updateDaTraPhong(boolean datrphong, LocalDateTime thoigiantra, String maxepphongbooking);

    //Cập nhât đã tả phòng và đã thanh toán     // update toàn bộ ngaydi của xephong theo mabooking
    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.daTraPhong =:datrphong, xp.daThanhToan = :daThanhToan ,xp.thoiGianThanhToan = :thoigiantra, xp.ngayDi = :ngayDi, xp.phuThuCheckOutTre = :phuThuCheckOutTre  WHERE xp.maXepPhongBooking = :maxepphongbooking")
                    int updateDaTraPhongAndThanhToan(boolean datrphong, boolean daThanhToan, LocalDateTime thoigiantra,LocalDate ngayDi, String maxepphongbooking, BigDecimal phuThuCheckOutTre);

    // kiểm tra trạng thái thanh toán của xep phòng
    @Query("SELECT xp FROM XepPhongBooKing xp WHERE xp.booKing.maBooking =:mabooking")
    List<XepPhongBooKing> getXepPhongBooKings(String mabooking);


    @Query(value = """

SELECT
    bk.ma_booking,
    COALESCE(
        NULLIF(
            TRIM(CONCAT(COALESCE(khp.ma_danh_xung, ''), '.', COALESCE(khp.ho, ''), ' ', COALESCE(khp.ten, ''))),
            '.'
        ),
        NULLIF(
            TRIM(CONCAT(COALESCE(bk.ma_danh_xung, ''), '.', COALESCE(kh.ho_khach_hang_booking, ''), ' ', COALESCE(kh.ten_khach_hang_booking, ''))),
            '.'
        ),
        ''
    ) AS tenkhachhang,
    COALESCE(NULLIF(khp.sdt, ''), NULLIF(kh.sdt_booking, ''), '') AS sdt,
    COALESCE(khp.so_giay_to, '') AS so_giay_to,
    COALESCE(NULLIF(bk.loai_nguon_khach, '')) AS ten_nhom_khach_hang,
    xp.ngay_den,
    xp.ngay_di,
    bk.tong_tien,
    bk.tien_coc
FROM booking bk
LEFT JOIN khach_hang_booking kh ON kh.ma_khach_hang_booking = bk.ma_khach_hang_booking
LEFT JOIN nhom_khach_hang nkh ON nkh.ma_nhom_khach_hang = bk.ma_nhom_khach_hang
LEFT JOIN xep_phong_booking xp ON xp.ma_booking = bk.ma_booking
LEFT JOIN khach_hang_phong khp ON khp.ma_xep_phong_booking = xp.ma_xep_phong_booking
WHERE xp.ma_xep_phong_booking = :ma_xepphong
ORDER BY COALESCE(khp.thoi_gian_tao, '1970-01-01'::timestamp) ASC
LIMIT 1;
""", nativeQuery = true)
    Map<String, Object> getThongTinKhachHangBooking(String ma_xepphong);

    // Xuất phiếu đăng ký xếp phòng
    @Query(value = """

WITH gia_phong_gom AS (
    -- Gom các giá giống nhau theo ma_chi_tiet_booking
    SELECT
        gp.ma_chi_tiet_booking,
        gp.gia,
        COUNT(*) AS so_ngay
    FROM gia_phong_theo_ngay gp
    WHERE EXISTS (
        SELECT 1
        FROM xep_phong_booking xpb
        JOIN chi_tiet_booking ctbk ON ctbk.ma_chi_tiet_booking = xpb.ma_chi_tiet_booking
        WHERE xpb.ma_xep_phong_booking = :ma_xepphong
        AND ctbk.ma_chi_tiet_booking = gp.ma_chi_tiet_booking
    )
    GROUP BY gp.ma_chi_tiet_booking, gp.gia
)
SELECT
    lp.ten_loai_phong AS ten_loai_phong,
    COUNT(DISTINCT xp.ma_xep_phong_booking) AS so_luong_dat_phong,
    g.so_ngay AS so_dem,
    g.gia AS gia,
    COALESCE(xp.phu_thu_tien_giuong, 0) AS phu_thu_tien_giuong,
    COALESCE(xp.so_giuong, 0) AS so_giuong,
    COALESCE(xp.phu_thu_tien_tre, 0) AS phu_thu_tre_em,
    COALESCE(xp.so_tre, 0) AS so_tre,
   g.gia * g.so_ngay * COUNT(DISTINCT xp.ma_xep_phong_booking) AS tong_tien,
    STRING_AGG(DISTINCT p.ten_phong, ', ') AS ten_phong,
    MAX(bk.tien_coc) AS tien_coc
FROM xep_phong_booking xp
JOIN chi_tiet_booking ctbk ON ctbk.ma_chi_tiet_booking = xp.ma_chi_tiet_booking
JOIN gia_phong_gom g ON g.ma_chi_tiet_booking = ctbk.ma_chi_tiet_booking
JOIN phong p ON p.ma_phong = xp.ma_phong
JOIN loai_phong lp ON lp.ma_loai_phong = p.ma_loaiphong
JOIN booking bk ON bk.ma_booking = xp.ma_booking
WHERE xp.ma_xep_phong_booking = :ma_xepphong
GROUP BY
    lp.ten_loai_phong,
    g.gia,
    g.so_ngay,
    xp.phu_thu_tien_giuong,
    xp.so_giuong,
    xp.phu_thu_tien_tre,
    xp.so_tre
ORDER BY
    lp.ten_loai_phong, g.gia;

""", nativeQuery = true)
    List<Map<String ,Object>> xuatPhieuDangKyXepPhong(String ma_xepphong);

    // kiểm tra phòng đã trã chưa
    @Query(value = "select count(*) from xep_phong_booking where ma_phong = :maPhong  and da_nhan_phong = true and da_tra_phong = false and thoi_gian_xoa is null and thoi_gian_tra IS NOT NULL;", nativeQuery = true)
    int KiemTraPhongDaTraChua(String maPhong);

    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.ngayDi =:ngayDi, xp.gioDi = :gio_di,xp.soDem = :soDemThue, xp.thanhTien  = :thanhTien WHERE xp.maXepPhongBooking = :ma_xepphong ")
    int UpdateNgayDi(LocalDate ngayDi ,LocalTime gio_di, String ma_xepphong,int soDemThue, BigDecimal thanhTien);

    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.phong.maPhong = :maPhong, xp.chuyenPhong = :chuyen_phong  WHERE xp.maXepPhongBooking = :ma_xepphong")
    int UpdatePhong(String maPhong, String ma_xepphong, boolean chuyen_phong);

    // Cập nhật thêm phụ thu phòng
    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.phuThuTienGiuong =:phuThuTienGiuong, xp.soGiuong=:soGiuong,xp.phuThuNguoiLon=:phuThuNguoiLon, xp.soNguoiLon=:soNguoiLon, xp.phuThuTienTre=:phuThuTreEm, xp.soTre=:soTre, xp.phuThuCheckInSom = :phuThuCheckInSom, xp.phuThuCheckOutTre =:phuThuCheckOutTre,xp.phuThuNangHangPhong = :phuThuNangHangPhong, xp.soLuongNangHangPhong = :soLuongNangHangPhong  ,xp.thanhTien = :tongTien WHERE xp.maXepPhongBooking=:ma_xepphong")
    int UpdatePhuThuPhong(BigDecimal phuThuTienGiuong, int soGiuong, BigDecimal phuThuNguoiLon, int soNguoiLon,BigDecimal phuThuTreEm ,int soTre,BigDecimal phuThuCheckInSom, BigDecimal phuThuCheckOutTre, BigDecimal phuThuNangHangPhong, int  soLuongNangHangPhong,String ma_xepphong, BigDecimal tongTien);

    // Lấy phụ thu nếu có
    @Query(value = "select phu_thu_tien_giuong, so_giuong,phu_thu_nguoi_lon,so_nguoi_lon,phu_thu_tien_tre,so_tre, phu_thu_check_in_som, phu_thu_check_out_tre, phu_thu_nang_hang_phong, so_luong_nang_hang_phong from xep_phong_booking where ma_xep_phong_booking=:ma_xepphong", nativeQuery = true)
    Map<String, Object> getPhuThuXepPhong(String ma_xepphong);

    // cập nhật nếu chi tiết có thay đổi
    @Query("SELECT COUNT (*) FROM XepPhongBooKing xp WHERE xp.chiTietBooking.maChiTietBooking = :ma_chitiet")
    int countDaXepPhong(String ma_chitiet);

    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.ngayDen=:ngayDen,xp.gioDen=:gioDen,xp.ngayDi=:ngayDi,xp.gioDi=:gioDi,xp.loaiGia.maLoaiGia=:maLoaiGia,xp.gia=:gia, xp.giaPhong.maGiaPhong =:maGiaPhong, xp.thanhTien=:thanhTien,xp.soDem = :soDem, xp.ghiChu =:ghiChu WHERE xp.chiTietBooking.maChiTietBooking=:ma_chitiet")
    int UpdateXepPhongTheoChiTiet(LocalDate ngayDen, LocalTime gioDen, LocalDate ngayDi, LocalTime gioDi, int maLoaiGia, double gia,int maGiaPhong, BigDecimal thanhTien,int soDem,String ghiChu, String ma_chitiet);

    // danh sách phòng ăn sáng theo khoảng thời gian
    @Query(value = """
        select * from thong_ke_an_sang_chi_tiet(:ngayKT)

""", nativeQuery = true)
    List<Map<String, Object>> getDanhSachPhongAnSang(LocalDate ngayBD, LocalDate ngayKT);

    // thống kế phòng và số khách ăn sáng theo thời gian
    @Query(value = """
        select * from thong_ke_an_sang(:ngayKT)
""", nativeQuery = true)
    List<Map<String, Object>> getThongKeAnSang(LocalDate ngayBD, LocalDate ngayKT);



    @Query(value = "SELECT ma_phong FROM xep_phong_booking WHERE ma_chi_tiet_booking =  :ma_chitiet and da_nhan_phong = true", nativeQuery = true)

    List<Map<String, Object>> DaXepPhong(String ma_chitiet);


    @Modifying
    @Query("DELETE XepPhongBooKing xp WHERE xp.chiTietBooking.maChiTietBooking = :ma_chitiet")
    void DeleteXepPhongTheoChiTiet(String ma_chitiet);

    @Query(value = "SELECT ma_phong,ma_xep_phong_booking FROM xep_phong_booking WHERE ma_chi_tiet_booking =  :ma_chitiet AND da_nhan_phong = false", nativeQuery = true)

    List<Map<String, Object>> DaXepPhongChuaNhanPhong(String ma_chitiet);


    @Modifying
    @Query("DELETE XepPhongBooKing xp WHERE xp.maXepPhongBooking  = :ma_xep_phong AND xp.daNhanPhong = false")
    void DeleteXepPhongChuaNhanPhong(String ma_xep_phong);


    @Query(value = """
       SELECT COUNT(*)
              FROM xep_phong_booking xpb
              JOIN booking bk ON bk.ma_booking = xpb.ma_booking
              WHERE ma_phong = :maPhong
              AND (
                  (xpb.ngay_den < :ngayDi AND xpb.ngay_di > :ngayDen)  -- Kiểm tra giao nhau
                 -- OR (ngay_den = :ngayDen AND ngay_di = :ngayDi)  -- Kiểm tra khớp chính xác
              )
              AND ma_xep_phong_booking != :ma_xepphong AND da_tra_phong = false AND thoi_gian_xoa is null
              AND bk.ma_trang_thai_booking NOT IN(3,4);
        """, nativeQuery = true)
            int kiemTraPhongDaTonTai(String maPhong, LocalDate ngayDen, LocalDate ngayDi, String ma_xepphong);

  @Query(value = """
       SELECT COUNT(*)
              FROM xep_phong_booking xpb
              JOIN booking bk ON bk.ma_booking = xpb.ma_booking
              WHERE ma_phong = :maPhong
              AND (
                  (xpb.ngay_den < :ngayDi AND xpb.ngay_di > :ngayDen)  -- Kiểm tra giao nhau
                 -- OR (ngay_den = :ngayDen AND ngay_di = :ngayDi)  -- Kiểm tra khớp chính xác
              )
             AND da_tra_phong = false AND thoi_gian_xoa is null
              AND bk.ma_trang_thai_booking NOT IN(3,4);
        """, nativeQuery = true)
  int kiemTraPhongDaTonTaiNoMaChiTietBooking(String maPhong, LocalDate ngayDen, LocalDate ngayDi);
    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.ngayDen =:ngayDen, xp.ngayDi =:ngayDi WHERE xp.booKing.maBooking =:ma_booking ")
    int updateNgayDenNgayDi(String ma_booking, LocalDate ngayDen, LocalDate ngayDi);


    // thông kế số luong khach chuan b den, khach dang o , khach chuan di trong khoang thoi gian
    @Query(value = "SELECT * FROM thong_ke_khach_hang_theo_ngay(:ngayBD,:ngayKT)", nativeQuery = true)
    List<Map<String,Object>> getThongKeKhachHangTheoNgay(LocalDate ngayBD, LocalDate ngayKT);

    @Query("SELECT xp FROM XepPhongBooKing xp WHERE xp.chiTietBooking.maChiTietBooking = :ma_chitiet AND xp.phong.maPhong = :maPhong AND xp.thoiGianXoa IS NULL")
    XepPhongBooKing findByChiTietBookingAndPhong(String ma_chitiet, String maPhong);

    @Modifying
    @Query("DELETE FROM XepPhongBooKing xp WHERE xp.chiTietBooking.maChiTietBooking = :ma_chitiet AND xp.phong.maPhong = :maPhong")
    void xoaPhongTrongXepPhong(String ma_chitiet, String maPhong);

    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.thoiGianXoa = CURRENT_TIMESTAMP  WHERE xp.chiTietBooking.maChiTietBooking = :ma_chitiet")
    void xoaXepPhong(String ma_chitiet);

    @Query("SELECT xp FROM XepPhongBooKing xp WHERE xp.chiTietBooking.maChiTietBooking = :ma_chitiet AND xp.thoiGianXoa IS NULL")
    List<XepPhongBooKing> getXepPhongByMaChiTiet(String ma_chitiet);

    @Modifying
    @Query("UPDATE XepPhongBooKing xp SET xp.phong.maPhong = :maPhong  WHERE xp.chiTietBooking.maChiTietBooking = :ma_chitiet AND xp.phong.maPhong = :maPhong")
    void deleteByChiTietBookingAndPhong(String ma_chitiet, String maPhong);

  @Modifying
  @Query("UPDATE XepPhongBooKing xp SET xp.thoiGianXoa = CURRENT_TIMESTAMP  WHERE xp.chiTietBooking.maChiTietBooking = :ma_chitiet AND xp.phong.maPhong = :maPhong")
  void udpateThoiGianXoaXepPhong(String ma_chitiet, String maPhong);

  ///  CHECK CHẠY HÀNG NGÀY ĐỂ UPDATE PHÒNG SANG DƠ
  @Query(value = """

    SELECT *
    FROM xep_phong_booking x
    WHERE x.da_nhan_phong = TRUE
      AND CURRENT_DATE BETWEEN x.ngay_den AND (x.ngay_di - INTERVAL '1 day');
    
        """, nativeQuery = true)
    List<XepPhongBooKing> getXepPhongDaNhan();

  /// DANH SACH PHONG CHECK-IN
    @Query(value = """
         SELECT ma_xep_phong_booking, xep_phong_booking.ma_phong, phong.ma_loaiphong, ma_booking , xep_phong_booking.da_nhan_phong, xep_phong_booking.thoi_gian_nhan  FROM
        xep_phong_booking
        JOIN phong on phong.ma_phong = xep_phong_booking.ma_phong
        WHERE ma_booking = :ma_booking and thoi_gian_xoa is null order by xep_phong_booking.ma_phong asc;
""", nativeQuery = true)
    List<Map<String, Object>> getDanhSachPhongCheckInByMaBooKing(String ma_booking);

}


