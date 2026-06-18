package vttu.khachsanservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class KhachHangPhong {

    @Id
    @Column( nullable = false)
    String maKhachHangPhong;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Integer autoIncrementMaKH;

    String ho;
    String ten;
    String ngaySinh;
    int gioiTinh;
    String diaChi;
    String soGiayTo;
    String sdt;
    int soTuoiTre;
    String ghiChu;
    LocalDateTime thoiGianTao;
    String nguoiTao;
    String lyDoLuuTru;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_xung" )
    DanhXung danhXung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_giay_to",nullable = true)
    LoaiGiayTo loaiGiayTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tinh")
    TinhThanh tinhThanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_huyen")
    Huyen huyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phuongxa")
    PhuongXa phuongXa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_quocgia")
    QuocGia quocGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_tre_em")
    LoaiTreEm loaiTreEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_xep_phong_booking")
    @JsonIgnoreProperties("danhSachKhachHang")
    XepPhongBooKing xepPhongBooKing;

}
