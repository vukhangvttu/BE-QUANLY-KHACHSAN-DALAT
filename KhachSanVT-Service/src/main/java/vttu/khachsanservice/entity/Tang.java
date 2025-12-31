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

public class Tang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int MaTang;
    String Tang;

    @OneToMany(mappedBy = "tang", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Phong> phongs = new ArrayList<>();
}
