package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.XepPhongDTO;
import vttu.khachsanservice.dto.request.DichVuMienPhiRequest;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XepPhongBooking_DichVuMienPhiResponse {
    XepPhongDTO maXepPhongBooking;
    DichVuMienPhiRequest maDichVuMienPhi;
    XepPhongDTO xepPhongBooking;
    int soLuong;
    LocalDate ngayBoSung;
    String nguoiTao;
}
