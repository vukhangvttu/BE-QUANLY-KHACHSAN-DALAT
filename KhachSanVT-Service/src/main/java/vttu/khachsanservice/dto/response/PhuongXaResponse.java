package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhuongXaResponse {
    String maPhuongXa;
    String tenPhuongXa;
    HuyenReponse huyen;
    String nguoiTao;
    LocalDateTime thoiGianTao;

}
