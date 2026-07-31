package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoaiPhongResponse {

    String maLoaiPhong;
    String tenLoaiPhong;
    int soLuong;
    int soGiuong;
    double phuThu;
    String dienGiai;
}
