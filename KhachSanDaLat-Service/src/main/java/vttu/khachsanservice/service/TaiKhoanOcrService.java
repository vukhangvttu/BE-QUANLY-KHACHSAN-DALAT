package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.entity.TaiKhoanOcr;
import vttu.khachsanservice.repository.TaiKhoanOcrRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaiKhoanOcrService {

    TaiKhoanOcrRepository taiKhoanOcrRepository;

    /**
     * Lấy tài khoản OCR còn hạn mức sử dụng (ACTIVE + soLuotDaDung < hanMucThang)
     * Ưu tiên theo doUuTien ASC
     */
    public TaiKhoanOcr getAvailableAccount() {
        List<TaiKhoanOcr> accounts = taiKhoanOcrRepository.findAvailableAccounts();
        if (accounts.isEmpty()) {
            return null;
        }
        return accounts.get(0);
    }

    /**
     * Tăng số lượt đã dùng lên 1 sau khi gọi API thành công
     */
    @Transactional
    public void incrementUsage(TaiKhoanOcr account) {
        account.setSoLuotDaDung(account.getSoLuotDaDung() + 1);

        // Nếu đạt hạn mức → tự động chuyển trạng thái EXHAUSTED
        if (account.getSoLuotDaDung() >= account.getHanMucThang()) {
            account.setTrangThai("EXHAUSTED");
            System.out.println("⚠ Tài khoản OCR [" + account.getTenTaiKhoan() + "] đã hết hạn mức tháng ("
                    + account.getSoLuotDaDung() + "/" + account.getHanMucThang() + ")");
        }

        taiKhoanOcrRepository.save(account);
        System.out.println("📊 OCR usage: " + account.getTenTaiKhoan()
                + " - " + account.getSoLuotDaDung() + "/" + account.getHanMucThang());
    }

    /**
     * Reset số lượt đã dùng về 0 cho tất cả tài khoản vào ngày 1 hàng tháng lúc 00:00
     * Đồng thời khôi phục trạng thái EXHAUSTED → ACTIVE
     */
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void resetMonthlyUsage() {
        System.out.println("🔄 Bắt đầu reset hạn mức OCR hàng tháng...");

        // Reset soLuotDaDung = 0
        taiKhoanOcrRepository.resetAllMonthlyUsage();

        // Khôi phục trạng thái EXHAUSTED → ACTIVE
        List<TaiKhoanOcr> allAccounts = taiKhoanOcrRepository.findAll();
        for (TaiKhoanOcr acc : allAccounts) {
            if ("EXHAUSTED".equals(acc.getTrangThai())) {
                acc.setTrangThai("ACTIVE");
                taiKhoanOcrRepository.save(acc);
            }
        }

        System.out.println("✅ Đã reset hạn mức OCR cho " + allAccounts.size() + " tài khoản");
    }

    /**
     * Lấy tổng số lượt còn lại của tất cả tài khoản
     */
    public int getTotalRemainingUsage() {
        List<TaiKhoanOcr> accounts = taiKhoanOcrRepository.findAvailableAccounts();
        return accounts.stream()
                .mapToInt(a -> a.getHanMucThang() - a.getSoLuotDaDung())
                .sum();
    }
}

