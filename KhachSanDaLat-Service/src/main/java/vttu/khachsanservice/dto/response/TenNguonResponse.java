package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TenNguonResponse {
    int maTenNguon;
    String tenNguon;
    String diaChi;
    String email;
    String codeVAT;
}
