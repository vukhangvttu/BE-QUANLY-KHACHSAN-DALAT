package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.HinhThucThanhToan;

import java.util.List;

public interface HinhThucThanhToanRepository extends JpaRepository<HinhThucThanhToan, Integer> {

    // lay hinh thuc thanh toan theo ma
    @Query(value = "select * from hinh_thuc_thanh_toan where ma_hinh_thuc_thanh_toan in (1,2,5,6,7) order by ma_hinh_thuc_thanh_toan asc", nativeQuery = true)
    List<HinhThucThanhToan> getHinhThucThanhToan();
}
