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

public class LoaiPhong {
    @Id
    @Column( nullable = false)
    String maLoaiPhong;
    String tenLoaiPhong;
    int soLuong;
    int soGiuong;
    @Column(columnDefinition = "integer default 0")
    int thuTu;

    @Column(columnDefinition = "integer default 0")
    int soLuongKhach;

    @OneToMany(mappedBy = "loaiPhong")
    @JsonIgnore
    List<Phong> phongs = new ArrayList<>();

    @OneToMany(mappedBy = "loaiPhong", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    List<GiaPhong> giaPhongs = new ArrayList<>();


    @OneToMany(mappedBy = "loaiPhong", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
     @JsonIgnore
    List<ChiTietBooking> chiTietBookings = new ArrayList<>();

//    @OneToMany(mappedBy = "loaiphong", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    List<ChiTietKhuyenMai> chiTietKhuyenMais = new ArrayList<>();
}
