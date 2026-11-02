package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class DichVu {
    @Id
    @Column( nullable = false)
    String maDichVu;
    String tenDichVu;
    String dvt;
    String gia;
    String trangThai;

    @Column(name = "hinh_anh", length = 255)
    String hinhAnh;

    @ManyToOne
    @JoinColumn(name = "ma_nhomdichvu", nullable = true)
    NhomDichVu nhomDichVu;

    @OneToMany(mappedBy = "dichVu", cascade = CascadeType.ALL)
    List<PhieuDichVu> phieuDichVus = new ArrayList<>();

}

