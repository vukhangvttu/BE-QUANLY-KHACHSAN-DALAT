package vttu.khachsanservice.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.BooKingDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HoaDonVatRequest {

    String maHoaDonVat;
    LocalDate ngayDen;
    LocalDate ngayDi;
    String tenKhach;
    LocalDate ngayLap;
    String mauHoaDon;
    String kiHieu;
    String diaChi;
    String tenCongTy;
    String codeVat;
    String soTaiKhoan;
    BigDecimal tong;
    BigDecimal tongVat;
    String email;
    String dienThoai;
    String cccd;
    String ghiChu;
    String noiDungThayThe;
    BooKingDTO booKing;
    String loaiGiayTo;
}
