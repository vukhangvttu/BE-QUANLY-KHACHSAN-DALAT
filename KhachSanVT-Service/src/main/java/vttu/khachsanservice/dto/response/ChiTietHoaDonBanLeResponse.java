package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.request.DichVuRequest;
import vttu.khachsanservice.dto.request.HoaDonBanLeRequest;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChiTietHoaDonBanLeResponse {
    String maChiTietHoaDon;
    String moTa;
    int soLuong;
    BigDecimal donGia;
    BigDecimal thanhTien;
    DichVuResponse dichvu;
//    HoaDonBanLeResponse hoaDonBanLe;
}
