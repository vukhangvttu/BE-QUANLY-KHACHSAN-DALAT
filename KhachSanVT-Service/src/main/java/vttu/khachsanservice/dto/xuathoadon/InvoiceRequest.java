package vttu.khachsanservice.dto.xuathoadon;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceRequest {
     GeneralInvoiceInfo generalInvoiceInfo;
     BuyerInfo buyerInfo;
     SellerInfo sellerInfo;
     List<Payment> payments;
     List<ItemInfo> itemInfo;
     List<TaxBreakdown> taxBreakdowns;
        List<MetaData> metadata;
}
