package vttu.khachsanservice.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class LoaiDichVu {
    @Id
    @Column( nullable = false)
    String maLoaiDichVu;
    String tenLoaiDichVu;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ma_nhomdichvu", nullable = true)
//    NhomDichVu nhomDichVu;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ma_nhomdichvu", nullable = true)
//    DichVu dichVu;
}
