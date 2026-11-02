package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vttu.khachsanservice.entity.TenNguon;

import java.util.List;

public interface TenNguonRepository extends JpaRepository<TenNguon, Integer> {

    long countByTenNguon(String tenNguon);

    List<TenNguon> findByTrangThai(Boolean trangThai);

    boolean existsByTenNguonAndMaTenNguonNot(String tenNguon, Integer maTenNguon);
}
