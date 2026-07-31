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

public class PhuongXa {

    @Id
    @Column( nullable = false)
    String maPhuongXa;
    String tenPhuongXa;
    boolean hieuLuc;
    String nguoiTao;
    LocalDateTime thoiGianTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_huyen", nullable = true)
    Huyen huyen;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ma_tinh", nullable = true)
  TinhThanh tinhThanh;
}
