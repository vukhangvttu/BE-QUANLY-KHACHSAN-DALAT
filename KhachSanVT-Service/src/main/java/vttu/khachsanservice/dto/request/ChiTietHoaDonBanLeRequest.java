package vttu.khachsanservice.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.DichVu;
import vttu.khachsanservice.entity.HoaDonBanLe;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ChiTietHoaDonBanLeRequest {
    String maChiTietHoaDon;
    String moTa;
    int soLuong;
    BigDecimal donGia;
    BigDecimal thanhTien;
    DichVuRequest dichvu;
    HoaDonBanLeRequest hoaDonBanLe;
}
