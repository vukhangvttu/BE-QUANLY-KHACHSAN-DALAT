package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhongRequest {

    String maPhong;
    String tenPhong;
    float dai;
    float rong;
    float cao;
    float tong;
    String ghiChu;
}
