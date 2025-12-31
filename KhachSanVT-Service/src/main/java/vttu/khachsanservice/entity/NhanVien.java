package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class NhanVien {
    @Id
    @Column( nullable = false)
    String maNhanVien;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Integer autoIncrementId;

    String tenNhanVien;
    String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bo_phan", nullable = true)
    BoPhan boPhan;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_username", referencedColumnName = "userName")
    Account account;

}
