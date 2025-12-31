package vttu.khachsanservice.dto.request;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.HoaDon;
import vttu.khachsanservice.entity.XepPhongBooKing;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChiTietHoaDonRequest {
    String maChiTietHoaDon;
    String loaiKhoanMuc;
    String moTa;
    int soLuong;
    BigDecimal donGia;
    BigDecimal thanhTien;
    String donViTinh;
    String ghiChu;
    XepPhongBooKingRequest xepPhongBooKing;
}
