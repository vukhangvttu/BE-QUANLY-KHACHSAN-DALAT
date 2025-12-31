package vttu.khachsanservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class Phong {

    @Id
    @Column( nullable = false)
    String maPhong;
    String tenPhong;
    float dai;
    float rong;
    float cao;
    float tong;
    String ghiChu;
    boolean daDo = false;
    boolean dangBaoTri = false;
    @Column(columnDefinition = "integer default 0")

    int soGiuongThem;

    @Column(columnDefinition = "integer default 0")
    int soLuongKhach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tang")
    Tang tang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loaiphong")
    LoaiPhong loaiPhong;

    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL)
    List<TrangBi_Phong> trangBi_phongs;

    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL)
    List<DonPhong> donPhongs;

    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL)
    List<BaoTri> baoTris;


//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "phong_trangthai",
//            joinColumns = @JoinColumn(name = "ma_phong"),
//            inverseJoinColumns  = @JoinColumn(name = "ma_trang_thai")
//    )
//    List<TrangThaiPhong> trangThaiPhongs = new ArrayList<>();
}
