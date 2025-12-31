package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HoaDon_DichVuMienPhiResponse {

    String maHoaDon;
    String tenDichVuMienPhi;
    int tongSoLuong;
    int tongSoLuongConLai;
    String ghiChu;
}
