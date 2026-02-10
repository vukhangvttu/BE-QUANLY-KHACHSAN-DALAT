package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.PhieuNhapHang;

import java.util.List;

public interface PhieuNhapHangRepository extends JpaRepository<PhieuNhapHang, String> {

    @Query("SELECT p FROM PhieuNhapHang p ORDER BY p.thoiGianTao DESC")
    List<PhieuNhapHang> findAllActivePhieuNhapHang();
}
