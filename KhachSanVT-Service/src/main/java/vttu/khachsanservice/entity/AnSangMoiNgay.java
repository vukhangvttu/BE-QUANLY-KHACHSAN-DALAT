package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class AnSangMoiNgay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    long maAnSangMoiNgay;
    LocalDate ngay;
    int daAnSang;
    LocalDateTime thoiGianTao;
    String nguoiTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_xep_phong")
    XepPhongBooKing xepPhongBooKing;
}
