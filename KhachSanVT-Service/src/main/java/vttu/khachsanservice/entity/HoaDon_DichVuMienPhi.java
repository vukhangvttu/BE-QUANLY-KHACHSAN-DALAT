package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class HoaDon_DichVuMienPhi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    long maHoaDonDichVuMienPhi;

    @ManyToOne
    @JoinColumn(name = "ma_hoa_don", nullable = false)
    HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "ma_dich_vu_mien_phi", nullable = false)
    DichVuMienPhi maDichVuMienPhi;

    @ManyToOne
    @JoinColumn(name = "ma_xep_phong_booking", nullable = false)
    XepPhongBooKing xepPhongBooKing;

    int tongSoLuong;
    int tongSoLuongConLai;
    String ghiChu;
    LocalDateTime thoiGianTao;
}
