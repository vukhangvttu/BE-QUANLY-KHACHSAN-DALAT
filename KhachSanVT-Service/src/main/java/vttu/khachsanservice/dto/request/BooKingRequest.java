package vttu.khachsanservice.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.PhongDTO;
import vttu.khachsanservice.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BooKingRequest {

    String maBooking;
    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;
    int soNguoiLon;
    int soTreEm;
    float tiLeChietKhau;
    BigDecimal tienCoc;
    LocalDateTime ngayCoc;
    int tongSoLuong;
    BigDecimal tongTien;

    boolean daXepPhong;
    String ghiChu;
    String loaiNguonKhach;

    DanhXungRequest danhXung;

    KhachHangBooKingRequest khachHangBooKing;

    ThongTinLienHeBooKingRequest thongTinLienHeBooKing;

    HinhThucThanhToanRequest hinhThucThanhToan;

    NguonKhachRequest nguonKhach;

    ThiTruongRequest thiTruong;

    KhucVucRequest khuVuc;

    MucDichDenRequest mucDichDen;

    GiamGiaRequest giamGia;

    TrangThaiBooKingRequest trangThaiBooKing;

    LoaiTheRequest loaiThe;

    NhomKhachHangRequest nhomKhachHang;

    List<YeuCauRequest> yeuCaus;

    List<ChiTietBooKingRequest> chiTietBooKings;


//    List<PhongHoiNghiRequest> phongHoiNghis;
}
