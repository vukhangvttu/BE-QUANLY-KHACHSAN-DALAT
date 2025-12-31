package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.ChiTietBooKingDTO;
import vttu.khachsanservice.dto.XepPhongDTO;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XepPhongBooking_DichVuMienPhiRequest {

    XepPhongDTO xepPhongBooking;
    DichVuMienPhiRequest dichVuMienPhi;
    int  soLuong;
    LocalDate ngayBoSung;

}
