package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vttu.khachsanservice.entity.ChiTietBooking;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ThongKeRepository extends JpaRepository<ChiTietBooking,String> {

    // thống kê phòng đã bán theo khoản thời gian
    @Query(value ="SELECT * FROM fn_thong_ke_cong_suat_phong(:thangBatDau,:namBatDau ,:thangKetThuc, :namKetThuc)", nativeQuery = true)
    List<Map<String, Object>> thongKePhongDaBanTheoKhoanThoiGian(int thangBatDau, int namBatDau, int thangKetThuc, int namKetThuc);

    // Lấy thống kê BÁO CÁO DOANH SỐ KPI NHÂN VIÊN
    @Query(value = "SELECT * FROM thong_ke_doanh_so_kpi_nhan_vien(:ngayBD, :ngayKT, :xuatFull, :userName)", nativeQuery = true)
    List<Map<String, Object>> getThongKeDanhSoKPINhanVienExcelKhoanThoiGian(
            @Param("ngayBD") LocalDate ngayBD,
            @Param("ngayKT") LocalDate ngayKT,
            @Param("xuatFull") Boolean xuatFull,
            @Param("userName") String userName
    );
}
