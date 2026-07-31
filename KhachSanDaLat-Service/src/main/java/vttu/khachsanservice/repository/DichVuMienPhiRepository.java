package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.dto.response.DichVuMienPhiResponse;
import vttu.khachsanservice.entity.DichVuMienPhi;

import java.util.List;

public interface DichVuMienPhiRepository extends JpaRepository<DichVuMienPhi, String> {

    @Query("SELECT d FROM DichVuMienPhi d WHERE d.trangThai = true ORDER BY d.tenDichVuMienPhi ASC")
    List<DichVuMienPhi> findAllActive();
}
