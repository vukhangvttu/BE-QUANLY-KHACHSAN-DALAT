package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.request.HoaDonVatRequest;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DichVuVATCusTomeReponse {
    String maDichVuVATCustome;
    String lineNumber;
    String itemCode;
    String itemName;
    String unitName;
    int quantity;
    BigDecimal unitPrice;
    HoaDonVatRequest hoaDonVat;
}
