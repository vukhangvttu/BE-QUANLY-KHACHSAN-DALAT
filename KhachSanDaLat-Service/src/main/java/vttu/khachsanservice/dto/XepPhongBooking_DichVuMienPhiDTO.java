package vttu.khachsanservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.request.DichVuMienPhiRequest;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XepPhongBooking_DichVuMienPhiDTO {
    String maXepPhongBooking;
    String maDichVuMienPhi;
    int  soLuong;
    LocalDate ngayBoSung;
}
