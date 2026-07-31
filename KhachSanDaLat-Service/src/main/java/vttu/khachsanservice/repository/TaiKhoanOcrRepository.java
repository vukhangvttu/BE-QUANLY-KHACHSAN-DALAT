package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.TaiKhoanOcr;

import java.util.List;

public interface TaiKhoanOcrRepository extends JpaRepository<TaiKhoanOcr, String> {

    // Lấy tài khoản ACTIVE, còn hạn mức, sắp xếp theo độ ưu tiên
    @Query("SELECT t FROM TaiKhoanOcr t WHERE t.trangThai = 'ACTIVE' AND t.soLuotDaDung < t.hanMucThang ORDER BY t.doUuTien ASC")
    List<TaiKhoanOcr> findAvailableAccounts();

    // Reset số lượt đã dùng về 0 cho tất cả tài khoản (gọi đầu mỗi tháng)
    @Modifying
    @Query("UPDATE TaiKhoanOcr t SET t.soLuotDaDung = 0, t.thoiGianCapNhat = CURRENT_TIMESTAMP")
    void resetAllMonthlyUsage();
}


