package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vttu.khachsanservice.entity.InvalidatedToken;

public interface InvalidatedTokenResponsitory extends JpaRepository<InvalidatedToken, String> {
}
