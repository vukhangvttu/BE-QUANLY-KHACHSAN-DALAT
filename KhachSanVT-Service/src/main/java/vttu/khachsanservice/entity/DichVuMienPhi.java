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
@Table(
        name = "dich_vu_mien_phi",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"ma_dich_vu_mien_phi"})
        }
)
public class DichVuMienPhi {
    @Id
    @Column( nullable = false)
    String maDichVuMienPhi;
    String tenDichVuMienPhi;

}

