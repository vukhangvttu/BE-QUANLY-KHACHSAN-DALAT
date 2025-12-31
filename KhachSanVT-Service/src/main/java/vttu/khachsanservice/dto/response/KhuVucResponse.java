package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KhuVucResponse {
    int maKhuVuc;
    String tenKhuVucEn;
    String tenKhucVucVi;
}
