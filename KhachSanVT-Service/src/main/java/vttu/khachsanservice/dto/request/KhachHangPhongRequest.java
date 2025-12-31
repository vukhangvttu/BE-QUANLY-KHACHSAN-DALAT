package vttu.khachsanservice.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class KhachHangPhongRequest {
    String maKhachHangPhong;

    @NotBlank(message = "Họ không được phép trống")
    String ho;
    @NotBlank(message = "Tên không được phép trống")
    String ten;
    @NotBlank(message = "Ngày sinh không được phép trống")
    String ngaySinh;
    int gioiTinh;
    @NotBlank(message = "Địa chỉ không được phép trống")
    String diaChi;


    String soGiayTo;
    String sdt;
    int soTuoiTre;
    String ghiChu;

    @NotNull(message = "Danh xưng không được phép trống")
    DanhXungRequest danhXung;

    LoaiTreEmRequest loaiTreEm;

    LoaiGiayToRequest loaiGiayTo;

    TinhThanhRequest tinhThanh;
    HuyenRequest huyen;
    PhuongXaRequest phuongXa;

    @NotNull(message = "Quốc gia không được phép trống")
    QuocGiaRequest quocGia;

}
