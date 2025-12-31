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
public class NhomKhachHang {

    @Id
    @Column( nullable = false)
    String maNhomKhachHang;
    String tenNhomKhachHang;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Integer autoIncrementId;

    @OneToMany(mappedBy = "nhomKhachHang", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<BooKing> booKings = new ArrayList<>();
}
