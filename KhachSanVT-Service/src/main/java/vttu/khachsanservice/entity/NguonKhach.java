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

public class NguonKhach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int maNguonKhach;
    String tenNguonKhach;
    String diaChi;
    String sdt;
    String ghiChu;

    @OneToMany(mappedBy = "nguonKhach")
    List<BooKing> booKings = new ArrayList<>();


}
