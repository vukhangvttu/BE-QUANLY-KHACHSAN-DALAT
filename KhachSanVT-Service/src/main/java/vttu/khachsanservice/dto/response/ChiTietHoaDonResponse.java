package vttu.khachsanservice.dto.response;

import jakarta.persistence.Column;
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
public class ChiTietHoaDonResponse {
    String maChiTietHoaDon;

    String loaiKhoanMuc;
    String moTa;
    int soLuong;
    String donViTinh;
    BigDecimal donGia;
    BigDecimal thanhTien;
    String ghiChu;

    HoaDon hoaDon;
    XepPhongBooKing xepPhongBooKing;
}
