package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DichVuBookingReponse {
    String maDichVu;
    int soLuong;
    BigDecimal DonGia;
    BigDecimal tongTien;
    String ghiChu;
}
