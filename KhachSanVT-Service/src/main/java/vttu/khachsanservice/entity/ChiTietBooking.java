package vttu.khachsanservice.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class ChiTietBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maChiTietBooking;
    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;
    @Column(columnDefinition = "integer default 0")
    int soDem;
    int soLuong;
    int soLuongDuBaoPhong;
    int nguoiLon;
    int treEm;

    Double gia;

    @Column(columnDefinition = "integer default 0")
    int soLuongExtraBed;
    BigDecimal giaExtraBed;

    @Column(columnDefinition = "integer default 0")
      int soLuongPhuThuTreEm;
    BigDecimal giaPhuThuTreEm;

    @Column(columnDefinition = "integer default 0")
    int soLuongPhuThuAnSang;
    BigDecimal giaPhuThuAnSang;

    BigDecimal tienPhong;
    String ghiChu;

    @Column(columnDefinition = "integer default 0")
    int index;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_phong", nullable = true)
    @JsonIgnoreProperties("chiTietBooKings")
    LoaiPhong loaiPhong;


    @ManyToOne
    @JoinColumn(name = "ma_booking", nullable = true)
    @JsonIgnoreProperties("chiTietBooKings")
    BooKing booKing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_gia", nullable = true)
    @JsonIgnoreProperties({"giaPhongs"})
    LoaiGia loaiGia;

    @ManyToOne
    @JoinColumn(name = "ma_giaphong", nullable = true)
    GiaPhong giaPhong;

    @OneToMany(mappedBy = "chiTietBooking", cascade = CascadeType.ALL)
    List<DanhSachPhongChiTiet> danhSachPhongChiTiets;

    @OneToMany(mappedBy = "chiTietBooking", cascade = CascadeType.ALL)
    List<GiaPhongTheoNgay> giaPhongTheoNgays;

//    @OneToMany(fetch = FetchType.LAZY)
//    List<DichVuMienPhi> dichVuMienPhis = new ArrayList<>();

}
