package vttu.khachsanservice.dto.xuathoadon;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerInfo {
    String sellerLegalName;
    String sellerTaxCode;
    String sellerAddressLine;
    String sellerPhoneNumber;
    String sellerEmail;
    String sellerBankName;
    String sellerBankAccount;
}
