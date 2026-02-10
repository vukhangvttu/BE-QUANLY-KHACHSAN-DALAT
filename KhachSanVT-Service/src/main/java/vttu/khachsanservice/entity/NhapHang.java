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

public class NhapHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nhap_hang", nullable = false)
    long maNhapHang;

    @ManyToOne
    @JoinColumn(name = "ma_dich_vu_mien_phi")
    DichVuMienPhi dichVuMienPhi;

    @Column(name = "so_luong", nullable = false)
    Integer soLuong;

    @Column(name = "loai", nullable = false)
    String loai;

    @Column(name = "ghi_chu")
    String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu_nhap_hang")
    PhieuNhapHang phieuNhapHang;

    @Column(name = "da_xoa")
    boolean daXoa;

    @Column(name = "thoi_gian_tao")
    LocalDateTime thoiGianTao;
}
