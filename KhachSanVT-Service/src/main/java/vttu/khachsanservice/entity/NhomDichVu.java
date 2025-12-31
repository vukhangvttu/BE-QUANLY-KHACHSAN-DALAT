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
public class NhomDichVu {
    @Id
    @Column( nullable = false)
    String maNhomDichVu;
    String tenNhomDichVu;
    @Column(columnDefinition = "integer default 0")
    int thuTu;

    @OneToMany(mappedBy = "nhomDichVu", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<DichVu> dichVus = new ArrayList<>();

}
