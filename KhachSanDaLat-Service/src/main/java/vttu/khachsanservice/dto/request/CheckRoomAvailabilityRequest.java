package vttu.khachsanservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckRoomAvailabilityRequest {

    @NotEmpty(message = "Danh sách chi tiết booking không được rỗng")
    @Valid
    List<BookingDetailRequest> bookingDetails;
}
