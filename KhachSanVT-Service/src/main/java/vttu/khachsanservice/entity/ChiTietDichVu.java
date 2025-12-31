package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class ChiTietDichVu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    Long maChiTietDichVu;
    int soLuong;
    String giamGia;
    String chietKhau;

//    @ManyToOne
//    @JoinColumn(name = "ma_dich_vu")
//     DichVu dichVu;
//
//    @ManyToOne
//    @JoinColumn(name = "ma_phieu_dich_vu")
//    PhieuDichVu phieuDichVu;
}
