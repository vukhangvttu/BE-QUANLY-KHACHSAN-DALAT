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

public class ChiTietHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maChiTietHoaDon;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Long autoIncrementId;

    String loaiKhoanMuc;
    String moTa;
    int soLuong;
    String donViTinh;
    BigDecimal donGia;
    BigDecimal thanhTien;
    String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hoadon")
    HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_xephongbooking")
    XepPhongBooKing xepPhongBooKing;
}
