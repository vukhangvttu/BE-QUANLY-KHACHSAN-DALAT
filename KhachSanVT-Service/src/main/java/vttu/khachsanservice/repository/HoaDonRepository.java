package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import vttu.khachsanservice.entity.HoaDon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, String> {


    @Query(value = "SELECT * FROM danh_sach_xep_phong_booking(:maBooking)", nativeQuery = true)
    List<Map<String, Object>> getBookingDetails(@Param("maBooking") String maBooking);


    // thông tin khách hàng thanh toán
    @Query(value = """
    SELECT
                                     bk.ma_booking,
                                     CONCAT(kh.ho_khach_hang_booking, ' ', kh.ten_khach_hang_booking) AS ten,
                                     CONCAT(bk.ma_danh_xung, '.', kh.ho_khach_hang_booking, ' ', kh.ten_khach_hang_booking) AS tenkhachhang,
                                     kh.sdt_booking,
                                     kh.email_booking,
                                     nkh.ten_nhom_khach_hang,
                                     bk.loai_nguon_khach,
                                     bk.ngay_den,
                                     bk.ngay_di,
                                     bk.tong_tien,
                                     bk.tien_coc,
                                     bk.thoi_gian_tao,
                                     httt.ten_hinh_thuc_thanh_toan AS ten_hinh_thuc_thanh_toan,
                                     CASE
                                         WHEN hd.ma_hinh_thuc_thanh_toan_phu = 0 THEN 'Không'
                                         ELSE COALESCE(httt_phu.ten_hinh_thuc_thanh_toan, 'Không')
                                     END AS ten_hinh_thuc_thanh_toan_phu,
                                     hd.ma_hinhthucthanhtoan,
                                     hd.ma_hinh_thuc_thanh_toan_phu,
                                     hd.gia_tri_thanh_toan_phu,
                                     hd.ghi_chu,
                                     hd.ghi_chu_thanh_toan_phu,
                                     hd.ngay_lap,hd.nguoi_tao,nv.ten_nhan_vien
                                             FROM booking bk
                                             LEFT JOIN khach_hang_booking kh ON kh.ma_khach_hang_booking = bk.ma_khach_hang_booking
                                             LEFT JOIN nhom_khach_hang nkh ON nkh.ma_nhom_khach_hang = bk.ma_nhom_khach_hang
                                             LEFT JOIN hoa_don hd ON hd.ma_booking = bk.ma_booking
                                             LEFT JOIN hinh_thuc_thanh_toan httt ON hd.ma_hinhthucthanhtoan = httt.ma_hinh_thuc_thanh_toan
                                             LEFT JOIN hinh_thuc_thanh_toan httt_phu ON hd.ma_hinh_thuc_thanh_toan_phu = httt_phu.ma_hinh_thuc_thanh_toan
                                             LEFT JOIN account tk on tk.user_name = hd.nguoi_tao
                                             LEFT JOIN nhan_vien nv on nv.account_username = tk.user_name
                                      where bk.ma_booking = :maBooking;
                                    
""", nativeQuery = true)
   Map<String, Object> getThongTinKhachHangBooking(String maBooking);


    // lấy thông tin dành cho xuất phiếu đăng ký khách sạn
    @Query(value = """
         SELECT * FROM xuat_phieu_dang_ky_bk(:mabooking)
""",nativeQuery = true)
    List<Map<String, Object>> getThongTinXuatPhieuDangKy(String mabooking);

 // lấy thông tin dành cho xuất phiếu đăng ký khách sạn danh cho khách OTA
 @Query(value = """
         SELECT * FROM xuat_phieu_dang_ky_bk_ota_ta(:mabooking)
""",nativeQuery = true)
 List<Map<String, Object>> getThongTinXuatPhieuDangKyOTA_TA(String mabooking);


    // danh cho thống kê
//    @Query(value = "SELECT * FROM get_daily_statistics(:date, 0)", nativeQuery = true)
//    Map<String, Object> getDailyStatistics(@Param("date") LocalDate date);

    // Lấy thống kê trong khoảng ngày (từ date - interval đến date)
    @Query(value = "SELECT * FROM get_daily_statistics(:thoigian)", nativeQuery = true)
    Map<String, Object> getDailyStatisticsWithInterval(

            @Param("thoigian") String thoigian);

    // Lấy thống kê trong khoảng thời gian không cố định
    @Query(value = "SELECT * FROM thong_ke_khach_san(:ngayDen, :ngayDi)", nativeQuery = true)
    Map<String, Object> getThongKeTheoKhoanThoiGian(
            @Param("ngayDen") LocalDate ngayDen,
            @Param("ngayDi") LocalDate ngayDi);

    // Lấy thống kê doanh thu all loại phòng theo khoản thời gian
    @Query(value = "SELECT * FROM thong_ke_doanh_thu_xuat_excel_khoan_thoi_gian_theo_bk_id_nguon_(:ngayBD, :ngayKT, :nhomKhachHang,:loaiKhachHang,:maLoaiPhong)", nativeQuery = true)
    List<Map<String, Object>> getThongKeLoaiPhongExcelKhoanThoiGian(
            @Param("ngayBD") LocalDate ngayBD,
            @Param("ngayKT") LocalDate ngayKT,
            @Param("nhomKhachHang") String nhomKhachHang,
            @Param("loaiKhachHang") String loaiKhachHang,
            @Param("maLoaiPhong") String maLoaiPhong)
    ;

    // Lấy thống kê doanh thu từng phòng theo khoản thời gian
    @Query(value = "SELECT * FROM thong_ke_doanh_thu_xuat_excel_khoan_thoi_gian_theo_phong_nguon_(:ngayBD, :ngayKT, :nhomKhachHang,:loaiKhachHang,:maLoaiPhong)", nativeQuery = true)
    List<Map<String, Object>> getThongKePhongExcelKhoanThoiGian(
            @Param("ngayBD") LocalDate ngayBD,
            @Param("ngayKT") LocalDate ngayKT,
             @Param("nhomKhachHang") String nhomKhachHang,
            @Param("loaiKhachHang") String loaiKhachHang,
            @Param("maLoaiPhong") String maLoaiPhong
    );


    // Lấy thống kê doanh thu theo tháng

    @Query(value = "SELECT * FROM thong_ke_doanh_thu_theo_thang()", nativeQuery = true)
    List<Map<String, Object>> getThongKeDoanThuTheoThang();

    // Lấy thống kê theo loại phòng trong khoản thời gian
    @Query(value = "SELECT * FROM thong_ke_loai_phong_khoan_thoi_gian_nguon_khach(:ngayDen, :ngayDi,:nhomKhachHang,:loaiKhachHang ,:loaiPhong)", nativeQuery = true)
    Map<String, Object> getThongKeLoaiPhongKhoanThoiGian(
            @Param("ngayDen") LocalDate ngayDen,
            @Param("ngayDi") LocalDate ngayDi,
            @Param("loaiPhong") String loaiPhong,
            @Param("nhomKhachHang") String nhomKhachHang,
            @Param("loaiKhachHang") String loaiKhachHang)
            ;

    // Lấy thống kê doanh theo buồng phon
    @Query(value = "SELECT * FROM thong_ke_doanh_thu_xuat_excel_khoan_thoi_gian_buong_phong(:ngayBD, :ngayKT)", nativeQuery = true)
    List<Map<String, Object>> getThongKePhongExcelKhoanThoiGianBuongPhong(
            @Param("ngayBD") LocalDate ngayBD,
            @Param("ngayKT") LocalDate ngayKT

    );

    // Lấy thông kê khách lưu trú xuất excel
    @Query(value = "SELECT * FROM thong_ke_khach_hang_phong_xuat_excel_asm(:ngayDen, :ngayDi)", nativeQuery = true)
    List<Map<String, Object>> getThongTinLuuTruKhoanThoiGian(
            @Param("ngayDen") LocalDate ngayDen,
            @Param("ngayDi") LocalDate ngayDi);

    // Lấy thông tin print vat

    @Query(value = "SELECT * FROM get_hoa_don_full_info(:mabooking)", nativeQuery = true )
    List<Map<String, Object>> getThongTinPrintVat(String mabooking);


    // Xuất excel chi tiet đặt phòng
   @Query(value =  "SELECT * FROM get_booking_details(:mabooking)", nativeQuery = true)
   List<Map<String,Object>> getChiTietDatPhong(String mabooking);

   // Xuất excel chi tiết hóa đơn
    @Query(value = "SELECT * FROM get_booking_details_with_payment(:mabooking)" , nativeQuery = true)
    List<Map<String,Object>> getChiTietHoaDon(String mabooking);


    // kiểm tra booking đã có hóa đơn chưa
    @Query("SELECT COUNT(hd)  FROM HoaDon hd WHERE hd.booKing.maBooking = :maBooking")
    int checkHoaDonByMaBooking(@Param("maBooking") String maBooking);

    @Query(value = "SELECT * FROM danh_sach_booking_thanh_toan(:maBooking)", nativeQuery = true)
    List<Map<String,Object>> getAllHoaDonByMaBooKing(@Param("maBooking") String maBooking);

}
