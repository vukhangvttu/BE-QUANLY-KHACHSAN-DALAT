package vttu.khachsanservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.response.GiaPhongTheoNgayReponse;
import vttu.khachsanservice.dto.response.HoaDon_DichVuMienPhiResponse;
import vttu.khachsanservice.entity.HoaDon_DichVuMienPhi;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class XemHoaDonThanhToanDTO {
    String maXepPhongBooking;
    String maPhong;
    String tenLoaiPhong;
    Double giaPhong;
    String ngayDen;
    String ngayDi;
    Integer soNgayO;
    boolean daThanhToan;
    BigDecimal phuThuTienGiuong;
    int soGiuong;
    BigDecimal phThuTreEm;
    int soTre;
    BigDecimal phuThuNguoiLon;
    int soNguoiLon;
    List<DichVuDto> danhSachDichVu;
    List<PhuThuDto> danhSachPhuThu;
    BigDecimal tongTienPhong;
    BigDecimal tongTienDichVu;
    BigDecimal tongTienPhuThu;
    BigDecimal tongTienThanhToan;
    String hinhThucThanhToan;
    String ghiChu;
    LocalDateTime ngayLap;
    List<GiaPhongTheoNgayReponse> danhSachGiaPhongTheoNgay;
    List<HoaDon_DichVuMienPhiResponse> danhSachDichVuMienPhi;

    BigDecimal phuThuCheckInSom;
    BigDecimal phuThuCheckOutTre;
}
