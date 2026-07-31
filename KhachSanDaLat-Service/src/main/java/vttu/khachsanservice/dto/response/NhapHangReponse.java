package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NhapHangReponse {
    long maNhapHang;

    DichVuMienPhiResponse dichVuMienPhi;

    Integer soLuong;

    String loai;

    String ghiChu;

    boolean daXoa;
}
