package vttu.khachsanservice.dto.request;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.XepPhongDTO;
import vttu.khachsanservice.entity.DichVuMienPhi;
import vttu.khachsanservice.entity.HoaDon;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HoaDon_DichVuMienPhiRequest {

    long maHoaDonDichVuMienPhi;
//    HoaDonRequest hoaDon;
    DichVuMienPhiRequest dichVuMienPhi;
    XepPhongDTO xepPhongBooKing;
    int tongSoLuong;
    int tongSoLuongConLai;
    String ghiChu;
}
