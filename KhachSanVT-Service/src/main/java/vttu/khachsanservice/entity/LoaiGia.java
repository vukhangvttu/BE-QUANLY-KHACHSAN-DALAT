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

public class LoaiGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int maLoaiGia;
    String tenLoaiGia;


    @OneToMany(mappedBy = "loaiGia")

    List<GiaPhong> giaPhongs = new ArrayList<>();

    @OneToMany(mappedBy = "loaiGia", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<ChiTietBooking> chiTietDatPhongs = new ArrayList<>();
}
