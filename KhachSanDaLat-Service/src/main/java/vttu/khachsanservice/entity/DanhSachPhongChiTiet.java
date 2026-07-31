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
public class DanhSachPhongChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int id;
    String maPhong;

    @Column(columnDefinition = "integer default 0")
    int soGiuongThem;

    @ManyToOne
    @JoinColumn(name = "ma_chi_tiet_booking", nullable = true)
    ChiTietBooking chiTietBooking;
}
