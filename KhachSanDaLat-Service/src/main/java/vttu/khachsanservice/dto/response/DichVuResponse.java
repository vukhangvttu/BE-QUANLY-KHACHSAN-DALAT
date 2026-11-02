package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DichVuResponse {
    String maDichVu;
    String tenDichVu;
    String dvt;
    String gia;
    String hinhAnh;
    String trangThai;
}
