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

public class KhuVuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int maKhuVuc;
    String tenKhuVucEn;
    String tenKhucVucVi;

    @OneToMany(mappedBy = "khuVuc")
    List<BooKing> booKings = new ArrayList<>();
}
