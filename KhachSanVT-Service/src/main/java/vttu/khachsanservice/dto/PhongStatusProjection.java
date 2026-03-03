package vttu.khachsanservice.dto;

import java.time.LocalTime;

public interface PhongStatusProjection {
    String gettenkhachhang();
    String getMaPhong();         // Ánh xạ đến cột ma_phong
    String getTenPhong();
    int getSoGiuongThem();
    String getMaLoaiPhong();
    String getTenLoaiPhong();
    String getDienGiai();
    int getSoGiuongDaSuDung();
    String getTrangThaiHienTai();
    String getMaBooking();
    Boolean  getDanhanPhong();
    Boolean getDaTraPhong();

    String getNgayDen();
    String getNgayDi();
    String getGioDi();
    String getNgayHienTai();
    String getMaxepphongbooking();
    String getTrangThaiVeSinh();
    String getGhiChu();
    float getTong();
    int getsoLuongKhach();
    int getTrangthaibooking();
    String getMaTang();

    String getTenNhomKhachHang();
    String getTrangThaiTuongLai();

    Boolean getDangBaoTri();
}
