package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DichVuRequest {
    String maDichVu;
    String tenDichVu;
    String dvt;
    String gia;
    String trangThai;
}
