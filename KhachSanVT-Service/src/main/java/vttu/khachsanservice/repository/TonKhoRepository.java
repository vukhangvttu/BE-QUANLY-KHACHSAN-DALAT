package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.DichVuMienPhi;
import vttu.khachsanservice.entity.TonKho;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TonKhoRepository extends JpaRepository<TonKho, Long> {
    Optional<TonKho> findByDichVuMienPhi(DichVuMienPhi dichVuMienPhi);

    @Query(value = """
    SELECT
        tk.ma_dich_vu_mien_phi,
        dvmp.ten_dich_vu_mien_phi,
        dvmp.don_vi_tinh,
        tk.so_luong AS ton_kho_ban_dau,
        COALESCE(SUM(xp.so_luong), 0) AS da_su_dung,
        tk.so_luong - COALESCE(SUM(xp.so_luong), 0) AS ton_con_lai
    FROM ton_kho tk
    LEFT JOIN xep_phong_booking_dich_vu_mien_phi xp
        ON xp.ma_dich_vu_mien_phi = tk.ma_dich_vu_mien_phi
       AND xp.da_xoa = false
    LEFT JOIN dich_vu_mien_phi dvmp
        ON dvmp.ma_dich_vu_mien_phi = tk.ma_dich_vu_mien_phi
    GROUP BY
        tk.ma_dich_vu_mien_phi,
        tk.so_luong,
        dvmp.ten_dich_vu_mien_phi,
        dvmp.don_vi_tinh
    ORDER BY tk.ma_dich_vu_mien_phi;    
""", nativeQuery = true)
    List<Map<String, Object>> getTonKho();

  @Query(value = "select * from thong_ke_nhap_xuat_phong_theo_khoang_ngay(?,?)", nativeQuery = true)
    List<Map<String, Object>> getThongKeNhapXuatPhong(LocalDate ngayBD, LocalDate ngayKT);
}
