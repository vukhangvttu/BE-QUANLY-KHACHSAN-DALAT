package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.HoaDon_DichVuMienPhi;

import java.util.List;

public interface HoaDon_DichVuMienPhiRepository extends JpaRepository<HoaDon_DichVuMienPhi, Long> {

    @Query(value = "select * from hoa_don_dich_vu_mien_phi where ma_hoa_don = :maHoaDon",nativeQuery = true)
    List<HoaDon_DichVuMienPhi> getHoaDonDichVuMienPhiByMaHoaDon(String maHoaDon);
}
