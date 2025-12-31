package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.HoaDonVatNhap;

import java.util.List;
import java.util.Map;

public interface HoaDonVatNhapRepository extends JpaRepository<HoaDonVatNhap, Long> {

    @Query(value = "SELECT * FROM hoa_don_vat_nhap WHERE  ma_hoadon_vat = :maHoadonVat ORDER BY ngay_lap DESC LIMIT 1" , nativeQuery = true)
    Map<String, Object> getHoaDonVatNhap(String maHoadonVat);
}
