package vttu.khachsanservice.entity.log;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.ChiTietBooking;
import vttu.khachsanservice.entity.GiaPhongTheoNgay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Log_GiaPhongTheoNgay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    long id;

    @Column(name = "ma_loai_phong")
    String maLoaiPhong;

    @Column(name = "ngay")
    LocalDate ngay;

    @Column(name = "gia")
    BigDecimal gia;

    String  maChiTietBooking;


    String trangThai;

    LocalDateTime thoiGianTao;
}
