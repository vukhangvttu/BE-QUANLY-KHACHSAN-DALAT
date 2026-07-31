package vttu.khachsanservice.dto.request;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.HoaDonVat;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DichVuVATCusTomeRequest {
    String maDichVuVATCustome;
    String lineNumber;
    String itemCode;
    String itemName;
    String unitName;
    int quantity;
    BigDecimal unitPrice;
    HoaDonVatRequest hoaDonVat;
}
