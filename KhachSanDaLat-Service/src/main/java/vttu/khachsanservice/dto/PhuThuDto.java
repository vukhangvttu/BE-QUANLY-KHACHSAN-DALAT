package vttu.khachsanservice.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhuThuDto {
    String moTa;
    BigDecimal donGia;
    int soLuong;
  BigDecimal thanhTien;
}
