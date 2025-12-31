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

public class TrangThaiPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int maTrangThaiPhong;
    String tenTrangThaiPhong;

    int priority;

//    @OneToMany(mappedBy = "trangThaiPhong", cascade = CascadeType.ALL)
//    List<DonPhong> donPhong = new ArrayList<>();
//
//    @OneToMany(mappedBy = "trangThaiPhong", cascade = CascadeType.ALL)
//    List<BaoTri> baoTris;


}
