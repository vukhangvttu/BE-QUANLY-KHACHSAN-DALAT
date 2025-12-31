package vttu.khachsanservice.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DichVuDto {
 String tenDichVu;
 Double donGia;
 int soLuong;
 String donViTinh;
 Double thanhTien;

}