package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CCCDInfoResponse {
     String soCccd;
     String hoVaTen;
     String ngaySinh;
     String gioiTinh;
     String quocTich;
     String queQuan;
}
