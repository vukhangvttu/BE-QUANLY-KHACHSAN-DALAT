package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.BooKingDTO;
import vttu.khachsanservice.dto.XepPhongDTO;
import vttu.khachsanservice.dto.request.BooKingRequest;
import vttu.khachsanservice.dto.request.DichVuRequest;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhieuDichVuReponse {
    String maPhieuDichVu;

    BigDecimal gia;
    int soLuong;
    XepPhongDTO xepPhongBooKing;
    DichVuRequest dichVu;
    BigDecimal thanhTien;
    String ghiChu;
}
