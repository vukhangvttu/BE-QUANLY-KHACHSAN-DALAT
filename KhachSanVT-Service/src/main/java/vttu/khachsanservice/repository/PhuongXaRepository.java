package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.PhuongXa;

import java.util.List;

public interface PhuongXaRepository extends JpaRepository<PhuongXa, String> {

    @Query(value = "SELECT xp FROM PhuongXa xp WHERE xp.huyen.maHuyen = :maHuyen")
    List<PhuongXa> getPhuongXaByMaHuyen(String maHuyen);

  @Query(value = "SELECT xp FROM PhuongXa xp WHERE xp.tinhThanh.maTinh = :maTinh")
  List<PhuongXa> getPhuongXaByMaTinh(String maTinh);

    @Query(value = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(ma_phuong_xa FROM 9 FOR 2) AS INTEGER)), 0)
            FROM phuong_xa
            WHERE ma_huyen = :maHuyen;

""", nativeQuery = true)
    int getMaxMaPhuongXaInHuyen(String maHuyen);

  @Query(value = """
             SELECT COALESCE(
                              MAX(CAST(RIGHT(ma_phuong_xa, 5) AS INTEGER)),
                              0
                          )
                          FROM phuong_xa
            WHERE ma_tinh = :maTinh;

""", nativeQuery = true)
  int getMaxMaPhuongXaInTinh(String maTinh);

    @Query(value = "select * from phuong_xa  where nguoi_tao is not null and thoi_gian_tao is not null and hieu_luc = true order by thoi_gian_tao desc" , nativeQuery = true)
    List<PhuongXa> getListPhuongXaTuTao( );
}
