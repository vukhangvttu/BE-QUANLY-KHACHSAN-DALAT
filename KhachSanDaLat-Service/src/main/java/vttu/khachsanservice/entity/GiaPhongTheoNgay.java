package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
//@Table(
//        name = "gia_phong_theo_ngay",
//        uniqueConstraints = {
//                @UniqueConstraint(columnNames = {"ma_loai_phong", "ngay"})
//        }
//)
public class GiaPhongTheoNgay {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maGiaPhongTheoNgay;

    @Column(name = "ma_loai_phong")
    String maLoaiPhong;

    @Column(name = "ngay")
    LocalDate ngay;

    @Column(name = "gia")
    BigDecimal gia;

    String trangThai;

    @ManyToOne
    @JoinColumn(name = "ma_chi_tiet_booking", nullable = true)
    ChiTietBooking chiTietBooking;

}
