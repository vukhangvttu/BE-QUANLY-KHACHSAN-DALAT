package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class HoaDonBanLe {
    @Id
    @Column( nullable = false)
    String maHoaDonBanLe;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Long autoIncrementId;

    BigDecimal tongThanhToan;
    BigDecimal giamGia;
    String ghiChu;
    LocalDateTime ngayLap;
    String nguoiTao;

    @OneToMany(mappedBy = "hoaDonBanLe")
    List<ChiTietHoaDonBanLe> chiTietHoaDonBanLe = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hinhthucthanhtoan")
    HinhThucThanhToan hinhThucThanhToan;
}
