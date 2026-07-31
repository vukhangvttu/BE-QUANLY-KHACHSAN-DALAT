package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.request.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class KhachHangPhongResponse {

    String maKhachHangPhong;

    String ho;
    String ten;
    String ngaySinh;
    int gioiTinh;
    String diaChi;
    String soGiayTo;
    String sdt;
    String lyDoLuuTru;
    int soTuoiTre;
    String ghiChu;
    DanhXungRequest danhXung;
    LoaiTreEmResponse loaiTreEm;
    LoaiGiayToRequest loaiGiayTo;

    TinhThanhRequest tinhThanh;
    HuyenRequest huyen;
    PhuongXaRequest phuongXa;
    QuocGiaRequest quocGia;

}
