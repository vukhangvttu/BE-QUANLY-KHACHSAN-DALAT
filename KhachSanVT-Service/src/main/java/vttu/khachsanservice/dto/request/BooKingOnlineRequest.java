package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BooKingOnlineRequest {

    String maBooking;
    LocalDate ngayDen;
    LocalDate ngayDi;
    int soNguoiLon;
    BigDecimal tienCoc;
    LocalDateTime ngayCoc;
    int tongSoLuong;
    BigDecimal tongTien;

    KhachHangBooKingRequest khachHangBooKing;

    String ghiChu;
    List<ChiTietBooKingOnlineRequest> chiTietBooKings;


}
