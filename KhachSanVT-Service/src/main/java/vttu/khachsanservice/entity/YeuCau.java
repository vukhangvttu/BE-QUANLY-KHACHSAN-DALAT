package vttu.khachsanservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

public class YeuCau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int maYeuCau;
    String dienGiai;
    String ghiChu;

    @ManyToMany(mappedBy = "yeuCaus")
    @JsonBackReference // Ngăn serialize ngược lại
    List<BooKing> booKings = new ArrayList<>();
//    @OneToMany(mappedBy = "yeuCauDatPhong", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    List<BooKing> booKings = new ArrayList<>();
}
