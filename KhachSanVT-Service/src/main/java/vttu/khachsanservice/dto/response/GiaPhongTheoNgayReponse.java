package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GiaPhongTheoNgayReponse {
    String maGiaPhongTheoNgay;
    String maLoaiPhong;
    LocalDate ngay;
    BigDecimal gia;
}
