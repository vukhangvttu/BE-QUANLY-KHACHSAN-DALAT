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

public class GiaPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int maGiaPhong;
    Double gia;
    boolean coVAT;
    boolean coAnSang;
    Double tienAnSang;
    Double tienAnSangTreEm;
    String vat;
    boolean giaNgayThuong;
    boolean giaCuoiTuan;
    boolean giaNgayLe;
    @Column(columnDefinition = "boolean default false")
    boolean giaGiuong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maloaiphong", nullable = true)
    LoaiPhong loaiPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maloaigia", nullable = true)
    LoaiGia loaiGia;
}
