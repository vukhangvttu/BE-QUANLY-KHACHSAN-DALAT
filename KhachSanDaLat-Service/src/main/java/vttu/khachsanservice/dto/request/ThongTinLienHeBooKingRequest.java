package vttu.khachsanservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThongTinLienHeBooKingRequest {
    long maThongTinLienHeBooKing;

    String tenThongTinLienHeBooKing;;
    String emailThongTinLienHeBooKing;
    String sdtThongTinLienHeBooKing;
    String faxThongTinLienHeBooKing;
    String tourCode;
}
