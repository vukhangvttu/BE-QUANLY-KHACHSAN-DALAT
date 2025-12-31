package vttu.khachsanservice.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.ChiTietHoaDonBanLe;
import vttu.khachsanservice.entity.HoaDonBanLe;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class HoaDonBanLeRequest {

    String maHoaDonBanLe;
    BigDecimal tongThanhToan;
    BigDecimal giamGia;
    String ghiChu;
    HinhThucThanhToanRequest hinhThucThanhToan;
    List<ChiTietHoaDonBanLeRequest> chiTietHoaDonBanLe;
}
