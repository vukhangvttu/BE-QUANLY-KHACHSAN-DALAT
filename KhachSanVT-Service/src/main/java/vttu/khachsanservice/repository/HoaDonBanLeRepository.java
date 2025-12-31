package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.HoaDonBanLe;

import java.util.Optional;

public interface HoaDonBanLeRepository extends JpaRepository<HoaDonBanLe, String> {
    @Query("SELECT hd.maHoaDonBanLe  FROM HoaDonBanLe hd ORDER BY hd.maHoaDonBanLe DESC LIMIT 1")
    Optional<String> findLatestMaHoaDonBanLe();
}
