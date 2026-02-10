package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NhapHangRequest {

    String maNhapHang;

    String maDichVu;

    Integer soLuong;

    String loai;

    String ghiChu;

}
