package vttu.khachsanservice.dto.xuathoadon;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaxBreakdown {
    BigDecimal taxPercentage;
    BigDecimal taxableAmount;
    BigDecimal taxAmount;

}
