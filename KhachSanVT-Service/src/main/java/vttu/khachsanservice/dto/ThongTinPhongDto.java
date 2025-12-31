package vttu.khachsanservice.dto;



import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.response.GiaPhongTheoNgayReponse;
import vttu.khachsanservice.dto.response.XepPhongBooking_DichVuMienPhiResponse;

import java.math.BigDecimal;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ThongTinPhongDto {
    String maXepPhongBooking;
     String maPhong;
     String tenLoaiPhong;
     Double giaPhong;
     String ngayDen;
 String ngayDi;
     Integer soNgayO;
     String ghiChu;
     boolean daThanhToan;
     BigDecimal phuThuTienGiuong;
     int soGiuong;
     BigDecimal phThuTreEm;
     int soTre;
     BigDecimal phuThuNguoiLon;
     int soNguoiLon;
     BigDecimal phuThuCheckinSom;
     List<DichVuDto> danhSachDichVu;
     List<PhuThuDto> danhSachPhuThu;
     List<GiaPhongTheoNgayReponse> danhSachGiaPhongTheoNgay;
     List<XepPhongBooking_DichVuMienPhiDTO> danhSachDichVuMienPhi;

}
