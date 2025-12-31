package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class HoaDonVatNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    long idHoaDonVatNhap;

    @Column(name = "ngay_lap")
    private LocalDateTime ngayLap;

    @Column(name = "tong_tien_phong", precision = 15, scale = 2)
    private BigDecimal tongTienPhong;

    @Column(name = "tong_thanh_toan", precision = 15, scale = 2)
    private BigDecimal tongThanhToan;

    @Column(name = "ma_booking", length = 50)
    private String maBooking;

    @Column(name = "nguoi_tao", length = 50)
    private String nguoiTao;

    @Column(name = "ma_hinhthucthanhtoan")
    private Integer maHinhThucThanhToan;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "thong_tin_hoa_don", columnDefinition = "json")
    String invoiceInfo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "thong_tin_dich_vu", columnDefinition = "json")
    private String thongTinDichVu;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "thong_tin_nguoi_mua", columnDefinition = "json")
    private String thongTinNguoiMua;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "thong_tin_nguoi_ban", columnDefinition = "json")
    private String thongTinNguoiBan;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payments", columnDefinition = "json")
    private String payments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "taxpercentage", columnDefinition = "json")
    private String taxPercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hoadon_vat")
    HoaDonVat hoaDonVat;

}
