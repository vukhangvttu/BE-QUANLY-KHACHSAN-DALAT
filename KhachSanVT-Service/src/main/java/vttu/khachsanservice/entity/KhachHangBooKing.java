package vttu.khachsanservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.response.KhachHangBooKingResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

@Table(name = "khach_hang_booking")
public class KhachHangBooKing {

    @Id
    @Column( nullable = false)
    String maKhachHangBooking;

    @Column(columnDefinition = "serial", updatable = false, insertable = false)// Tự động tăng
    Integer autoIncrementId;

    String hoKhachHangBooking;
    String tenKhachHangBooking;
    String diaChiBooking;
    String emailBooking;
    String sdtBooking;
    String faxBooking;
    String maSoThue;
    LocalDateTime ngayTao;

    @OneToMany(mappedBy = "khachHangBooKing")
    List<BooKing> booKings = new ArrayList<>();


}
