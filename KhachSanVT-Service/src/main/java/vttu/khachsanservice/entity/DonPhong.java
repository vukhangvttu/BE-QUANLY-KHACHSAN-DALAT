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
public class DonPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int MaDonPhong;

    LocalDateTime NgayHienTai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maphong", nullable = true)
    Phong phong;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "matrangthai", nullable = true)
//    TrangThaiPhong trangThaiPhong;
}
