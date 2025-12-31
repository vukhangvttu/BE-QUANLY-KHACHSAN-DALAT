package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrangThaiBooKingResponse {
    int maTrangThaiBooKing;
    String tenTrangThaiBooKing;
    String ghiChu;
}
