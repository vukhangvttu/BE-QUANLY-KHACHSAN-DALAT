package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class HoaDonVatPhatHanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    long idHoaDonVatPhatHanh;

    String supplierTaxCode;
    String invoiceNo;
    String transactionID;
    String transactionUUID;
    String reservationCode;
    String templateCode;
    LocalDateTime thoiGianTao = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hoadon_vat")
    HoaDonVat hoaDonVat;
}
