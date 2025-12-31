package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.TrangThaiBooKing;

import java.util.List;

public interface TrangThaiBooKingRepository extends JpaRepository<TrangThaiBooKing, Integer> {

    @Query("SELECT t FROM TrangThaiBooKing t WHERE t.maTrangThaiBooKing IN (1,5,3) ORDER BY t.vitri ASC")
    List<TrangThaiBooKing> getTrangThaiBooKing();
}
