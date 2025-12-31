package vttu.khachsanservice.dto.request;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.BooKing;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhongHoiNghiRequest {
    String maPhongHoiNghi;
    String tenPhongHoiNghi;
    LocalDate ngayBatDau;
    LocalDate ngayKetThuc;
    boolean duBao;
    int soLuong;
    BigDecimal donGia;
    String ghiChu;

    BooKingRequest booKing;
}
