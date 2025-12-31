package vttu.khachsanservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class XepPhongBooking_DichVuMienPhi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( nullable = false)
    long maChiTietBookingDichVuMienPhi;

    @ManyToOne
    @JoinColumn(name = "ma_xep_phong_booking", nullable = false)
    XepPhongBooKing maXepPhongBooking;

    @ManyToOne
    @JoinColumn(name = "ma_dich_vu_mien_phi", nullable = false)
    DichVuMienPhi maDichVuMienPhi;

    int  soLuong;
    LocalDate ngayBoSung;
    LocalDateTime thoiGianTao;

}
