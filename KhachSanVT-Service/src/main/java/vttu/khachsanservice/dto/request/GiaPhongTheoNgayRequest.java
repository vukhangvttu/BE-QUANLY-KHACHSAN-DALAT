package vttu.khachsanservice.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.ChiTietBooking;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GiaPhongTheoNgayRequest {
    String maGiaPhongTheoNgay;
    String maLoaiPhong;
    LocalDate ngay;
    BigDecimal gia;

}
