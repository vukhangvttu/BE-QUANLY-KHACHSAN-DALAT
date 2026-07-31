package vttu.khachsanservice.dto.response;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.PhongDTOXepPhong;
import vttu.khachsanservice.dto.request.GiaPhongRequest;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.DanhSachPhongChiTiet;
import vttu.khachsanservice.entity.LoaiGia;
import vttu.khachsanservice.entity.LoaiPhong;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChiTietBooKingResponse {
    String maChiTietBooking;
    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;
    int soDem;
    int soLuong;
    int soLuongDuBaoPhong;
    int nguoiLon;
    int treEm;
    int soLuongExtraBed;
    BigDecimal giaExtraBed;
    Double gia;
    BigDecimal tienPhong;
    int soLuongPhuThuTreEm;
    BigDecimal giaPhuThuTreEm;
    int soLuongPhuThuAnSang;
    BigDecimal giaPhuThuAnSang;
    LoaiPhongResponse loaiPhong;
     String ghiChu;
    int index;

//    BooKingResponse booKing;

    LoaiGiaResponse loaiGia;
    GiaPhongRequest giaPhong;
    List<DanhSachPhongChiTietReponse> danhSachPhongChiTiets;
    List<GiaPhongTheoNgayReponse> giaPhongTheoNgays;
}
