package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.request.ChiTietHoaDonBanLeRequest;
import vttu.khachsanservice.dto.request.HinhThucThanhToanRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HoaDonBanLeResponse {
    String maHoaDonBanLe;
    BigDecimal tongThanhToan;
    BigDecimal giamGia;
    String ghiChu;
    HinhThucThanhToanResponse hinhThucThanhToan;
    LocalDateTime ngayLap;
    List<ChiTietHoaDonBanLeResponse> chiTietHoaDonBanLe;
}
