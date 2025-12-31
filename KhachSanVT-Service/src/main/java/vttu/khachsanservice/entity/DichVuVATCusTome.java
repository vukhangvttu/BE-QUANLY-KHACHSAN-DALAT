package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class DichVuVATCusTome {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maDichVuVATCustome;
    String lineNumber;
    String itemCode;
    String itemName;
    String unitName;
    int quantity;
    BigDecimal unitPrice;

    LocalDateTime thoiGianTao;
    String nguoiTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hoadon_vat")
    HoaDonVat hoaDonVat;
}
