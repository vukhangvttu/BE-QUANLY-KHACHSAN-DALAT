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
public class ChuyenNoiBo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int MaChuyenNoiBo;
    String LiDo;
    String GhiChu;
    LocalDate NgayBD;
    LocalDate NgayKT;

    // khóa ngoài
    int MaPhong;
    int MaTrangThai;

}
