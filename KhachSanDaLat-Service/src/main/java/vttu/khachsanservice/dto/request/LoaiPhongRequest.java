package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoaiPhongRequest {
    String maLoaiPhong;
    String tenLoaiPhong;
    int soLuong;
    int soGiuong;
    double phuThu;
}
