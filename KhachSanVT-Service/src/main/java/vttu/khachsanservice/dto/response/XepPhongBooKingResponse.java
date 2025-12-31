package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.BooKingDTO;
import vttu.khachsanservice.dto.PhongDTO;
import vttu.khachsanservice.dto.PhongDTOXepPhong;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.Phong;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XepPhongBooKingResponse {

    String maXepPhongBooking;

    LocalDate ngayXep;
    LocalTime gioXep ;
    BigDecimal gia;
    boolean daNhanPhong;
    boolean chuyenPhong;
    String stt;

    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;

    int soGiuong;
    BigDecimal phuThuTienGiuong;
     BigDecimal phuThuNguoiLon;
    int soNguoiLon;

    int soTre;
    BigDecimal phuThuTienTre;

    int soDem;
    BigDecimal thanhTien;

    PhongDTOXepPhong phong;
    LoaiGiaResponse loaiGia;
    BooKingDTO booKing;


}
