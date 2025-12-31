package vttu.khachsanservice.dto.request;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.ChiTietBooKingDTO;
import vttu.khachsanservice.dto.response.LoaiGiaResponse;
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
public class XepPhongBooKingRequest {
    String maXepPhongBooking;
    BigDecimal gia;
    String loaiPhong;
    PhongRequest phong;
    LoaiGiaRequest loaiGia;
    BooKingRequest booKing;
    GiaPhongRequest giaPhong;
    String stt;
    LocalDate ngayDen;
    LocalDate ngayDi;
    LocalTime gioDen;
    LocalTime  gioDi;
     BigDecimal thanhTien;

    ChiTietBooKingDTO chiTietBooking;
}
