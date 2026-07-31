package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuocGiaResponse {
    String maQuocGia;
    String tenQuocGia;
    String tenTiengAnh;
}
