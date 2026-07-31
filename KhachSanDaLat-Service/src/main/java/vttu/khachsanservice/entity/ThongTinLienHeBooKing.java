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
@Table(name = "thong_tin_lien_he_booking")
public class ThongTinLienHeBooKing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    long maThongTinLienHeBooKing;

    String tenThongTinLienHeBooKing;;
    String emailThongTinLienHeBooKing;
    String sdtThongTinLienHeBooKing;
    String faxThongTinLienHeBooKing;
    String tourCode;

}
