package vttu.khachsanservice.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KhachHangBooKingRequest {
    String maKhachHangBooking;

    @NotBlank(message = "Họ không được phép trống")
    String hoKhachHangBooking;

    @NotBlank(message = "Tên khách hàng  không được phép trống")
    String tenKhachHangBooking;
    String diaChiBooking;
    String emailBooking;
    String sdtBooking;
    String faxBooking;
    String maSoThue;
//    CongTyResquest congTy;
}
