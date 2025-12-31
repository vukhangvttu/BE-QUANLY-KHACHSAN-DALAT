package vttu.khachsanservice.dto;

import java.math.BigDecimal;

public interface GiaPhongTheoThoiGian {
    int getmaLoaiGia();
    String gettenLoaiGia();
    int getmaGiaPhong();
    BigDecimal getgia();
    boolean getgiaCuoiTuan();
    boolean getgiaNgayThuong();
    boolean getgiaNgayLe();
    boolean getgiaGiuong();
    String getmaLoaiPhong();
}
