package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vttu.khachsanservice.entity.DanhXung;

public interface DanhXungRepository extends JpaRepository<DanhXung,String> {
}
