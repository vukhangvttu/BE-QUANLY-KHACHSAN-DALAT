package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TenNguonRequest {
    int maTenNguon;
    String tenNguon;
    String diaChi;
    String email;
    String codeVAT;
}
