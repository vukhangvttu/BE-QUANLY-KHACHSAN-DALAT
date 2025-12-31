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
public class Huyen {

    @Id
    @Column( nullable = false)
    String maHuyen;
    String tenhuyen;
    boolean hieuLuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tinh", nullable = true)
    TinhThanh tihThanh;

    @OneToMany(mappedBy = "huyen" ,fetch = FetchType.LAZY)
    List<PhuongXa> phuongXas = new ArrayList<>();

}
