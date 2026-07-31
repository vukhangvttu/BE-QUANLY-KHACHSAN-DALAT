package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class KhachHangBooKingResponse {

    String maKhachHangBooking;

    String hoKhachHangBooking;
    String tenKhachHangBooking;
    String diaChiBooking;
    String emailBooking;
    String sdtBooking;
    String faxBooking;
    String maSoThue;
}
