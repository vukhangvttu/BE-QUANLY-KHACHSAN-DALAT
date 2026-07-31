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

public class TonKho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ton_kho", nullable = false)
    long maTonKho;

    @ManyToOne
    @JoinColumn(name = "ma_dich_vu_mien_phi")
    DichVuMienPhi dichVuMienPhi;

    /** Số lượng tồn hiện tại */
    @Column(name = "so_luong", nullable = false)
     Integer soLuong;

}
