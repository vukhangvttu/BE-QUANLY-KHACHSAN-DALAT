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

public class PhieuNhapHang {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maPhieuNhapHang;
    float tongSoLuong;
    LocalDate ngayNhap;
    String nguoiTao;
    LocalDateTime thoiGianTao = LocalDateTime.now();
    Boolean daXoa;
}
