package vttu.khachsanservice.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.TinhThanh;

import java.util.List;

public interface TinhThanhRepository extends JpaRepository<TinhThanh, String> {

  @Query(value = "SELECT * FROM tinh_thanh WHERE hieu_luc = true", nativeQuery = true)
  List<TinhThanh> findAllTinhThanhHieuLuc();
}
