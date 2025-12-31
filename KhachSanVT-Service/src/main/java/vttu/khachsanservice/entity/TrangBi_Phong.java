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

public class TrangBi_Phong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int MaTrangBi;
    String TenTrangBi;

    @ManyToOne
    @JoinColumn(name = "ma_thietbi")
    ThietBi thietbi;
    @ManyToOne
    @JoinColumn(name = "ma_phong")
    Phong phong;
}
