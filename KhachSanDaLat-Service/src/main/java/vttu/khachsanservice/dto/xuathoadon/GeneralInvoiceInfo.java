package vttu.khachsanservice.dto.xuathoadon;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class GeneralInvoiceInfo {

    String transactionUuid;
     String invoiceType;
     String templateCode;
     String invoiceSeries;
     String currencyCode;
     String adjustmentType;
     boolean paymentStatus;
     String paymentType;
     String paymentTypeName;
     boolean cusGetInvoiceRight;
     String userName;
     String invoiceNote;
}
