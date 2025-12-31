package vttu.khachsanservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "xep_phong_booking")
public class XepPhongBooKing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column( nullable = false)
    String maXepPhongBooking;


    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    long autoIncrementId;

    LocalTime gioXep;
    BigDecimal gia;
    boolean daNhanPhong = false;
    LocalDateTime thoiGianNhan;
    boolean chuyenPhong;
    boolean daTraPhong;
    LocalDateTime thoiGianTra;
    String stt;
    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;
    boolean daThanhToan;
    int soGiuong ;
    BigDecimal phuThuTienGiuong;
    int soTre;
    BigDecimal phuThuTienTre;
    @Column(columnDefinition = "integer default 0")
    int soNguoiLon;
    BigDecimal phuThuNguoiLon;

    boolean checkInTre = false;
    boolean checkOutTre = false;

    @Column(columnDefinition = "integer default 0")
     int soDem;
    BigDecimal thanhTien;
    String ghiChu;
    LocalDate ngayXep;
    String nguoiTao;
    LocalDateTime thoiGianTao;
    String userCheckIn;
    BigDecimal phuThuCheckInSom;
    BigDecimal phuThuCheckOutTre;
    LocalDateTime thoiGianXoa;
    LocalDateTime thoiGianThanhToan;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phong")
    Phong phong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_gia")
    LoaiGia loaiGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_booking")
    BooKing booKing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chi_tiet_booking")
    ChiTietBooking chiTietBooking;

    @ManyToOne
    @JoinColumn(name = "ma_giaphong", nullable = true)
    GiaPhong giaPhong;

    @OneToMany(mappedBy = "xepPhongBooKing", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<KhachHangPhong> danhSachKhachHang = new ArrayList<>();
}
