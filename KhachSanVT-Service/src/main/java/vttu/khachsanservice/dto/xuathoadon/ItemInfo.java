package vttu.khachsanservice.dto.xuathoadon;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemInfo {
    String lineNumber;
    String itemCode;
    String itemName;
    String unitName;
    BigDecimal unitPrice;
        BigDecimal quantity;
    BigDecimal itemTotalAmountWithoutTax;
    BigDecimal taxPercentage;
    BigDecimal taxAmount;
    BigDecimal discount;
    BigDecimal itemDiscount;
}
