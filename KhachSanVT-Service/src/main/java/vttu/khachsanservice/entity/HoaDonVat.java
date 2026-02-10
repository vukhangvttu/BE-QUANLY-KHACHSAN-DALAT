package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class HoaDonVat {
    @Id
    @Column( nullable = false)
    String maHoaDonVat;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Long autoIncrementId;

    String tenKhach;
    LocalDate ngayLap;
    LocalDate ngayDen;
    LocalDate ngayDi;
    String mauHoaDon;
    String kiHieu;
    String diaChi;
    String tenCongTy;
    String codeVat;
    String soTaiKhoan;
    BigDecimal tong;
    BigDecimal tongVat;
    String email;
    String dienThoai;
    String cccd;
    String ghiChu;
    String noiDungThayThe;
    LocalDateTime thoiGianTao;
    String nguoiTao;
    @Column(columnDefinition = "boolean default false")
    boolean xacNhan;
    @Column(columnDefinition = "integer default 0")
    int soLuongInNhap;

    @Column(columnDefinition = "boolean default false")
    boolean daHuy;

    @Column(columnDefinition = "boolean default false")
    boolean daInNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_booking")
    BooKing booKing;

    @Size(max = 10)
    String loaiGiayTo;
}
