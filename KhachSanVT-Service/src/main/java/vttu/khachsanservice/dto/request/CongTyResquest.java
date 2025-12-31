package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CongTyResquest {
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
