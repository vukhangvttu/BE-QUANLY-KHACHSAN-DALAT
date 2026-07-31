package vttu.khachsanservice.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ChiTietBooKingRequest {
    String maChiTietBooking;
    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;
    int soDem;
    int soLuong;
    int SoLuongDuBaoPhong;
    int nguoiLon;
    int treEm;
    int soLuongExtraBed;

    int index;
    int soLuongPhuThuTreEm;
    BigDecimal giaPhuThuTreEm;

    int soLuongPhuThuAnSang;
    BigDecimal giaPhuThuAnSang;

    BigDecimal giaExtraBed;
    BigDecimal gia;
    BigDecimal tienPhong;
    String ghiChu;
    LoaiPhongRequest loaiPhong;
    LoaiGiaRequest loaiGia;
    GiaPhongRequest giaPhong;

    List<DanhSachPhongChiTietRequest> danhSachPhongChiTiets;
    List<GiaPhongTheoNgayRequest> giaPhongTheoNgays;

}
