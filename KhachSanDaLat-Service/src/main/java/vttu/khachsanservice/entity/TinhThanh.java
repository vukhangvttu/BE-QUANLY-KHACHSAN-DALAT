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
public class TinhThanh {

    @Id
    @Column( nullable = false)
    String maTinh;
    String tenTinh;
    boolean hieuLuc = true;
    @OneToMany(mappedBy = "tihThanh" ,fetch = FetchType.LAZY)
    List<Huyen> huyens = new ArrayList<>();

  @OneToMany(mappedBy = "tinhThanh" ,fetch = FetchType.LAZY)
  List<PhuongXa> phuongXas = new ArrayList<>();
}
