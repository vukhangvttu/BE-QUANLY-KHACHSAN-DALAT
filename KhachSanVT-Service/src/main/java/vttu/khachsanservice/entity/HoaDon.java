package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maHoaDon;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Long autoIncrementId;

    BigDecimal tongTienPhong;
    BigDecimal tongTienDichVu;
    BigDecimal tongThanhToan;
    BigDecimal giamGia;
    BigDecimal phuThu;
    LocalDate ngayDi;
    String ghiChu;
    LocalDateTime ngayLap;
    String nguoiTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_booking")
    BooKing booKing;

    @OneToMany(mappedBy = "hoaDon")
    List<ChiTietHoaDon> chiTietHoaDons = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hinhthucthanhtoan")
    HinhThucThanhToan hinhThucThanhToan;

    @Column(columnDefinition = "integer default 0")
    // bo sung hinh thuc thanh toan phu
    int maHinhThucThanhToanPhu;
    BigDecimal giaTriThanhToanPhu;
    String ghiChuThanhToanPhu;

    @Column(columnDefinition = "DECIMAL(30,2)")
    BigDecimal tongPhuThuCheckInSom;
    @Column(columnDefinition = "DECIMAL(30,2)")
    BigDecimal tongPhuThuCheckInTre;
}
