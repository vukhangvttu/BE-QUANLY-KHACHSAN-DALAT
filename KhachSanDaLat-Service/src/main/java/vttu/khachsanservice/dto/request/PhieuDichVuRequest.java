package vttu.khachsanservice.dto.request;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.PhongDTO;
import vttu.khachsanservice.dto.XepPhongDTO;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.DichVu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhieuDichVuRequest {
    String maPhieuDichVu;

    BigDecimal gia;
    int soLuong;
    XepPhongDTO xepPhongBooKing;
    DichVuRequest dichVu;
    BigDecimal thanhTien;
    String ghiChu;
//    PhongDTO phong;
}
