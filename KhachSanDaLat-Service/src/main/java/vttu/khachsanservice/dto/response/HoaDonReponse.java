package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HoaDonReponse {
    String maHoaDon;
    BigDecimal tongTienPhong;
    BigDecimal tongTienDichVu;
    BigDecimal giamGia;
    BigDecimal phuThu;
    LocalDate ngayDi;
    String ghiChu;
    BigDecimal tongTienPhuThu;
    BigDecimal tongThanhToan;

    HinhThucThanhToanResponse hinhThucThanhToan;
    List<ChiTietHoaDonResponse> chiTietHoaDon;
}
