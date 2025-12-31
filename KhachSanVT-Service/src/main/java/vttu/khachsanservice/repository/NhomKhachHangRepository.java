package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.NhomKhachHang;

import java.util.Optional;

public interface NhomKhachHangRepository extends JpaRepository<NhomKhachHang, String> {

    @Query("SELECT k.maNhomKhachHang FROM NhomKhachHang k ORDER BY k.maNhomKhachHang DESC LIMIT 1")
    Optional<String> findLatestNhomKhachHangId();
}
