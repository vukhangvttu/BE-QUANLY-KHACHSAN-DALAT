package vttu.khachsanservice.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;
import vttu.khachsanservice.entity.log.Log_DanhSachPhongChiTiet;

public interface Log_DanhSachPhongChiTietRepositoty extends JpaRepository<Log_DanhSachPhongChiTiet , Long> {

    // No additional methods are defined here, as the repository will inherit basic CRUD operations
    // from JpaRepository. If you need custom queries, you can define them here.
}
