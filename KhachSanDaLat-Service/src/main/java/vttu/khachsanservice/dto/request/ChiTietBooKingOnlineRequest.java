package vttu.khachsanservice.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ChiTietBooKingOnlineRequest {
    String maChiTietBooking;
    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;
    int soDem;
    int soLuong;
    int nguoiLon;

    int index;

    BigDecimal tienPhong;
    String ghiChu;
    LoaiPhongRequest loaiPhong;
    LoaiGiaRequest loaiGia;
    GiaPhongRequest giaPhong;

    List<GiaPhongTheoNgayRequest> giaPhongTheoNgays;

}
