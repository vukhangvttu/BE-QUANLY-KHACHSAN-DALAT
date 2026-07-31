package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class HinhThucThanhToanRequest {

    int maHinhThucThanhToan;
    String tenHinhThucThanhToan;
    String tenHinhThucTiengAnh;
    String loai;
    String phi;
}
