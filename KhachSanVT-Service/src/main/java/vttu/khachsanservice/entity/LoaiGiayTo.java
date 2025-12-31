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
public class LoaiGiayTo {
    @Id
    @Column( nullable = false)
    String maLoaiGiayTo;
    String tenLoaiGiaTo;
}
