package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.BooKingDTO;
import vttu.khachsanservice.dto.request.BooKingRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhongHoiNghiResponse {
    String maPhongHoiNghi;
    String tenPhongHoiNghi;
    LocalDate ngayBatDau;
    LocalDate ngayKetThuc;
    int soLuong;
    BigDecimal donGia;
    String ghiChu;

    BooKingDTO booKing;
}
