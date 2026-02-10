package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vttu.khachsanservice.entity.DichVuMienPhi;
import vttu.khachsanservice.entity.TonKho;

import java.util.Optional;

public interface TonKhoRepository extends JpaRepository<TonKho, Long> {
    Optional<TonKho> findByDichVuMienPhi(DichVuMienPhi dichVuMienPhi);
}
