package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.dto.GiaPhongTheoThoiGian;
import vttu.khachsanservice.entity.GiaPhong;

import java.util.List;
import java.util.Map;

public interface GiaPhongRepository extends JpaRepository<GiaPhong, Integer> {

    @Query(value = "select gia, gia_giuong from gia_phong where maloaiphong = :ma_loaiphong and gia_giuong =  true", nativeQuery = true)
    Map<String, Object> getGiaGiuongExtra(String ma_loaiphong);

    // lấy danh sách all giá phòng theo thời gian
    @Query(value = """
SELECT
    lg.ma_loai_gia AS maLoaiGia,
    lg.ten_loai_gia AS tenLoaiGia,
    gp.ma_gia_phong AS maGiaPhong,
    gp.gia AS gia,
    gp.gia_cuoi_tuan AS giaCuoiTuan,
    gp.gia_ngay_thuong AS giaNgayThuong,
    gp.gia_ngay_le AS giaNgayLe,
    gp.gia_giuong AS giaGiuong,
    gp.maloaiphong AS maLoaiPhong
FROM loai_gia lg
LEFT JOIN gia_phong gp ON lg.ma_loai_gia = gp.ma_gia_phong
JOIN gia_phong_thoi_gian gpt ON gp.ma_gia_phong = gpt.ma_gia
WHERE gpt.ngay_bat_dau <= CURRENT_DATE
  AND gpt.ngay_ket_thuc >= CURRENT_DATE
  AND gpt.trang_thai = true
ORDER BY lg.ma_loai_gia, gp.ma_gia_phong;
""", nativeQuery = true)
    List<GiaPhongTheoThoiGian> getAllGiaTheoThoiGian();

    @Query(value = "SELECT * FROM gia_phong WHERE maloaiphong = :maLoaiPhong", nativeQuery = true)
    List<GiaPhong> findByMaLoaiPhong(String maLoaiPhong);
}
