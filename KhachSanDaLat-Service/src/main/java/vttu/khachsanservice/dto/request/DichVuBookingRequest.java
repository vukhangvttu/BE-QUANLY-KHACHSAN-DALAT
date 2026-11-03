package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DichVuBookingRequest {

    String maDichVu;
    int soLuong;
    BigDecimal DonGia;
    BigDecimal tongTien;
    String ghiChu;
}
