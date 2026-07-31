package vttu.khachsanservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Log_ChuyenPhongRequest {
    long Id;
    String lyDo;
    String maXepPhong;
    String noiDung;
}
