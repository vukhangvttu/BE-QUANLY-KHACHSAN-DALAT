package vttu.khachsanservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

@Table(name = "booking")
public class BooKing {
    @Id
    @Column( nullable = false)
    String maBooking;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Long autoIncrementId;
    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;
    int soNguoiLon;
    int soTreEm;
    BigDecimal tienCoc;
    int tongSoLuong;
    String ghiChu;
    BigDecimal tongTien;
    LocalDateTime thoiGianTao = LocalDateTime.now();
    String nguoiTao;
    boolean daXepPhong;
    String loaiNguonKhach;
    BigDecimal  tongTienDichVu;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ma_nguon_khach")
//    NguonKhach nguonKhach;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ma_thi_truong")
//    ThiTruong thiTruong;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ma_khu_vuc")
//    KhuVuc khuVuc;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ma_muc_dich_den")
//    MucDichDen mucDichDen;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ma_giam_gia")
//    GiamGia giamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_trang_thai_booking")
    TrangThaiBooKing trangThaiBooKing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_the_thanh_toan")
    LoaiThe loaiThe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khach_hang_booking")
    KhachHangBooKing khachHangBooKing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nhom_khach_hang")
    NhomKhachHang nhomKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_thong_tin_lien_he")
    ThongTinLienHeBooKing thongTinLienHeBooKing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_xung")
    DanhXung danhXung;


    @OneToMany(mappedBy = "booKing",cascade = CascadeType.ALL)
    @JsonManagedReference
    List<ChiTietBooking> chiTietBookings;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "yeu_cau_booking",
            joinColumns = @JoinColumn(name = "ma_booking"),
            inverseJoinColumns  = @JoinColumn(name = "ma_yeu_cau")
    )
    @JsonManagedReference
    List<YeuCau> yeuCaus = new ArrayList<>();

    @OneToMany(mappedBy = "booKing")
    List<HoaDon> hoaDons = new ArrayList<>();

    @OneToMany(mappedBy = "booKing",cascade = CascadeType.ALL)
    @JsonManagedReference
    List<DichVuBooking> dichVuBookings;
}