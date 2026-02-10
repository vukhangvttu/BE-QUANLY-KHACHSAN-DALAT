package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.NhapHang;

import java.util.List;

public interface NhapHangRepository extends JpaRepository<NhapHang, Long> {

    @Query("SELECT h FROM NhapHang h WHERE h.phieuNhapHang.maPhieuNhapHang = :maPhieuNhapHang")
    List<NhapHang> findByMaPhieuNhapHang(String maPhieuNhapHang);
}
