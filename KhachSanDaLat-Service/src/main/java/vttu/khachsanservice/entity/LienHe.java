package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class LienHe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    int maLienHe;
    String tenPhapLy;
    String maSoThue;
    String diaChi;
    String sdt;
    String email;
    String tenNganHang;
    String soTaiKhoan;
}
