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
@Table(name = "trang_thai_booking")
public class TrangThaiBooKing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int maTrangThaiBooKing;
    String tenTrangThaiBooKing;
    int vitri;

    @OneToMany(mappedBy = "trangThaiBooKing")
    List<BooKing> booKings = new ArrayList<>();
}
