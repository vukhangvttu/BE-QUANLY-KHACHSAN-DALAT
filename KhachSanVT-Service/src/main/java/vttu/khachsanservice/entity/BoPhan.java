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

public class BoPhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int MaBoPhan;
    String TenBoPhan;

    @OneToMany(mappedBy = "boPhan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<NhanVien> booKings = new ArrayList<>();
}
