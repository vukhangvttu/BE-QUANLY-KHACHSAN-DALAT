package vttu.khachsanservice.entity.log;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.ChiTietBooking;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Log_DanhSachPhongChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    long id;

    String maPhong;

    int soGiuongThem;

    String  machiTietBooking;

    String trangThai;

    LocalDateTime thoiGianTao;
}
