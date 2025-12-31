package vttu.khachsanservice.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import vttu.khachsanservice.entity.Phong;
import vttu.khachsanservice.entity.XepPhongBooKing;
import vttu.khachsanservice.repository.PhongRepository;
import vttu.khachsanservice.repository.XepPhongRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final XepPhongRepository xepPhongRepository;
    private final JdbcTemplate jdbcTemplate;

//    @Scheduled(cron = "*/10 * * * * ?")
@Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateRoomStatusDaily() {
        long startTime = System.currentTimeMillis();
        log.info("=".repeat(80));
        log.info("Bắt đầu scheduled task cập nhật trạng thái phòng");
        log.info("Thời gian bắt đầu: {}", java.time.LocalDateTime.now());

        try {
            List<XepPhongBooKing> danhSachXepPhongDaNhan = xepPhongRepository.getXepPhongDaNhan();

            log.info("Tìm thấy {} phòng đã nhận cần cập nhật trạng thái",
                    danhSachXepPhongDaNhan.size());

            if (danhSachXepPhongDaNhan.isEmpty()) {
                log.info("Không có phòng nào cần cập nhật");
                return;
            }

            List<String> maPhongs = danhSachXepPhongDaNhan.stream()
                    .map(xp -> xp.getPhong().getMaPhong())
                    .distinct()
                    .toList();

            // Sử dụng JdbcTemplate để thực hiện batch update
            int updatedCount = 0;

            if (!maPhongs.isEmpty()) {
                // Tạo câu SQL với parameter placeholders
                StringBuilder sql = new StringBuilder(
                        "UPDATE phong SET da_do = true WHERE ma_phong IN (");

                for (int i = 0; i < maPhongs.size(); i++) {
                    sql.append("?");
                    if (i < maPhongs.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");

                // Thực thi update
                updatedCount = jdbcTemplate.update(sql.toString(), maPhongs.toArray());
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("=".repeat(80));
            log.info("KẾT QUẢ THỰC THI SCHEDULED TASK:");
            log.info("- Tổng số phòng cần cập nhật: {}", maPhongs.size());
            log.info("- Số phòng đã cập nhật: {}", updatedCount);
            log.info("- Thời gian thực thi: {} ms", duration);
            log.info("- Thời gian kết thúc: {}", java.time.LocalDateTime.now());
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("LỖI NGHIÊM TRỌNG khi thực thi scheduled task: {}", e.getMessage(), e);
            throw e;
        }
    }
}