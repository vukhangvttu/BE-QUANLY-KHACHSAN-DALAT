package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "ocr_account")

public class TaiKhoanOcr {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maTaiKhoanGg;
    String tenTaiKhoan;
    String duongDanCredentials;
    Integer hanMucThang = 1000;
    Integer soLuotDaDung = 0;
    String trangThai = "ACTIVE";
    Integer doUuTien = 1;
    LocalDateTime thoiGianTao;
    LocalDateTime thoiGianCapNhat;

    @PrePersist
    protected void onCreate() {
        this.thoiGianTao = LocalDateTime.now();
        this.thoiGianCapNhat = LocalDateTime.now();
    }

    // Tự update thời gian khi update
    @PreUpdate
    protected void onUpdate() {
        this.thoiGianCapNhat = LocalDateTime.now();
    }
}

