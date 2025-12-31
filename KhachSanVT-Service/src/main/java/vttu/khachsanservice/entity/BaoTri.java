package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class BaoTri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int MaBaoTri;
    String LiDo;
    String GhiChu;
    LocalDate NgayBD;
    LocalDate NgayKT;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maphong", nullable = true)
    Phong phong;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "matrangthai", nullable = true)
//    TrangThaiPhong trangThaiPhong;
}
