package vttu.khachsanservice.dto.request;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.XepPhongDTO;
import vttu.khachsanservice.entity.XepPhongBooKing;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class AnSangMoiNgayRequest {
    long maAnSangMoiNgay;
    LocalDate ngay;
    int daAnSang;
    LocalDateTime thoiGianTao;
    String nguoiTao;

    XepPhongDTO xepPhongBooKing;
}
