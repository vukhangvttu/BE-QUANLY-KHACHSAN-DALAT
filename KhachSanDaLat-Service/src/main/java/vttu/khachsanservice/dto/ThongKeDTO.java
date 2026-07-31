package vttu.khachsanservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class ThongKeDTO {
    private Long tongSoHoDon;
    private Long tongSoDonDat;
    private BigDecimal tongTien;
    private Long tongHuyBooking;
    private LocalDate startDate;
    private LocalDate endDate;
}
