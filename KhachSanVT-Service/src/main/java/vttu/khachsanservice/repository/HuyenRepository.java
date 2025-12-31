package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.Huyen;

import java.util.List;

public interface HuyenRepository extends JpaRepository<Huyen, String> {

    @Query(value = "SELECT h FROM Huyen h WHERE h.tihThanh.maTinh = :matinh")
    List<Huyen> getHuyenByTinh(String matinh);
}
