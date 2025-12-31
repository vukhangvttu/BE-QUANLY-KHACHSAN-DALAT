package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThongTinLienHeBooKingResponse {
    long maThongTinLienHeBooKing;

    String tenThongTinLienHeBooKing;;
    String emailThongTinLienHeBooKing;
    String sdtThongTinLienHeBooKing;
    String faxThongTinLienHeBooKing;
    String tourCode;
}
