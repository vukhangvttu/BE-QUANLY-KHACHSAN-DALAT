package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CongTyResponse {
    int maCongTy;
    String tenCongTy;
    String tenCongTyVAT;
    String mst;
    String diaChi;
    String diaChiVAT;
    String sdt;
    String email;
    String fax;
    String nguoiDaiDien;
    String chucVu;
}
