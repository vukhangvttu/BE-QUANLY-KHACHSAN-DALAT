package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NguonKhachResponse {
    int maNguonKhach;
    String tenNguonKhach;
    String diaChi;
    String sdt;
    String ghiChu;
}
