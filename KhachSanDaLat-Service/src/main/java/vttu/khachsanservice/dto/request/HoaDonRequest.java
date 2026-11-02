package vttu.khachsanservice.dto.request;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.ChiTietHoaDon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HoaDonRequest {

    String maHoaDon;
    BigDecimal tongTienPhong;
    BigDecimal tongTienDichVu;
    BigDecimal giamGia;
    BigDecimal phuThu;
    LocalDate ngayDi;
    String ghiChu;
    BigDecimal tongTienPhuThu;
    BigDecimal tongThanhToan;

    HinhThucThanhToanRequest hinhThucThanhToan;
    List<ChiTietHoaDonRequest> chiTietHoaDon;
//    List<HoaDon_DichVuMienPhiRequest> hoaDonDichVuMienPhi;

    int maHinhThucThanhToanPhu;
    BigDecimal giaTriThanhToanPhu;
    String ghiChuThanhToanPhu;

    BigDecimal tongPhuThuCheckInSom;
    BigDecimal tongPhuThuCheckOutTre;
    BigDecimal tongPhuThuNangHangPhong;
//    BooKing booKing;
//    List<ChiTietHoaDon> chiTietHoaDons = new ArrayList<>();
}
