package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class PhieuDichVu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maPhieuDichVu;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    long autoIncrementId;

    BigDecimal gia;
    int soLuong;
    LocalDate ngaySuDung;
    LocalTime gioSuDung;
    BigDecimal thanhTien;
    String ghiChu;
    String nguoiTao;

    LocalDateTime thoiGianXoa;

//    @OneToMany(mappedBy = "phieuDichVu", cascade = CascadeType.ALL)
//    List<ChiTietDichVu> chiTietDichVus = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "ma_xep_phong_booking")
    XepPhongBooKing xepPhongBooKing;

    @ManyToOne
    @JoinColumn(name = "ma_dichvu")
    DichVu dichVu;

    @ManyToOne
    @JoinColumn(name = "ma_phong")
    Phong phong;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "makhachhang", nullable = true)
//    KhachHang khachHang;


}
