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

public class DichVuBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long autoIncrementId;
    @Column(nullable = false)
    String maDichVu;
    int soLuong;
    BigDecimal DonGia;
    BigDecimal tongTien;
    String ghiChu;

    @ManyToOne
    @JoinColumn(name = "ma_booking")
    BooKing booKing;
}
