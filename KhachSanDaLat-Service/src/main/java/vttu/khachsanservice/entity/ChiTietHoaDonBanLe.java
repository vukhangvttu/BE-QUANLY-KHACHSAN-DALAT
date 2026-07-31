package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class ChiTietHoaDonBanLe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maChiTietHoaDon;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Long autoIncrementId;

    String moTa;
    int soLuong;
    BigDecimal donGia;
    BigDecimal thanhTien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_dichvu")
    DichVu dichvu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hoadonbanle")
    HoaDonBanLe hoaDonBanLe;

}
