package vttu.khachsanservice.dto.xuathoadon;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BuyerInfo {
    String buyerName;
    String buyerLegalName;
    String buyerTaxCode;
    String buyerAddressLine;
    String buyerPhoneNumber;
    String buyerEmail;
    String buyerIdNo;
    String buyerIdType;
}
