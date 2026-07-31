package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.HoaDonVat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HoaDonVatRepository extends JpaRepository<HoaDonVat, String> {

    @Query("SELECT hd.maHoaDonVat  FROM HoaDonVat hd ORDER BY hd.maHoaDonVat DESC LIMIT 1")
    Optional<String> findLatestMaHoaDonVat();

    @Query("SELECT hd FROM HoaDonVat hd WHERE hd.booKing.maBooking = :ma_booking")
    List<HoaDonVat> getHoaDonVatByMaBooKing(String ma_booking);

    @Query(value = "SELECT * FROM get_thong_tin_xuat_nhap_hoa_don_vat(:maBooking,:maHoaDonVat)" ,nativeQuery = true)
    Map<String, Object> getThongTinXuatNhapHoaDonVat(String maBooking, String maHoaDonVat);

    // cập nhật trạng thái đã phát hành hóa đơn
    @Modifying
    @Query("UPDATE HoaDonVat hd SET hd.xacNhan = :xacnhan WHERE hd.maHoaDonVat = :mahoaDonVat")
    int updatePhatHanhHoaDonVat(boolean xacnhan,String mahoaDonVat);

    // Cập nhật đã in nháp và sô lượng đã in lần thứ mâý
    @Modifying
    @Query("UPDATE HoaDonVat hd SET hd.daInNhap = :daInNhap, hd.soLuongInNhap = :soluong  WHERE hd.maHoaDonVat = :maHoaDonVat")
    int updateDaInNhapHoaDonVat(boolean daInNhap, int soluong, String maHoaDonVat);

    // List thông tin hóa đơn VAT có mã số thuế lấy gần nhất của khách hàng
    @Query(value = "SELECT DISTINCT ON (code_vat) code_vat, ten_khach,ten_cong_ty, dia_chi, dien_thoai, email, cccd, ghi_chu\n" +
            "FROM hoa_don_vat\n" +
            "WHERE code_vat != ''\n" +
            "  AND LENGTH(code_vat) >= 10\n" +
            "ORDER BY code_vat, thoi_gian_tao DESC;", nativeQuery = true)
    List<Map<String,Object>> getListCodeVat();

    // Lấy list dich vụ
    @Query(value ="SELECT * FROM get_thong_tin_dich_vu_vat(:ma_booking)", nativeQuery = true)
    Map<String,Object> getThongTinDichVuVAT(String ma_booking);

}
