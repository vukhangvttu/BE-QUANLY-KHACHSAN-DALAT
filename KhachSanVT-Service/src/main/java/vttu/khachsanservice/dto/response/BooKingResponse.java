package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.request.*;
import vttu.khachsanservice.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BooKingResponse {

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
    LocalDateTime thoiGianTao;
    String ghiChu;
    String loaiNguonKhach;
    boolean daXepPhong;
    DanhXungResponse danhXung;

    KhachHangBooKingResponse khachHangBooKing;

    ThongTinLienHeBooKingResponse thongTinLienHeBooKing;

    HinhThucThanhToanResponse hinhThucThanhToan;

    NguonKhachResponse nguonKhach;

    ThiTruongResponse thiTruong;

    KhuVucResponse khuVuc;

    MucDichDenResponse mucDichDen;

    GiamGiaResponse giamGia;

    TrangThaiBooKingResponse trangThaiBooKing;

    LoaiTheResponse loaiThe;

    NhomKhachHangResponse nhomKhachHang;

    List<YeuCauResponse> yeuCaus;

//    List<PhongHoiNghiResponse> phongHoiNghis;
//    List<ChiTietBooKingResponse> chiTietBooKings;
}
