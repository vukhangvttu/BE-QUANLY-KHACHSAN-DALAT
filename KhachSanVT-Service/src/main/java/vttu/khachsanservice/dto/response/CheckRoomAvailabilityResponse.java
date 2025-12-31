package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckRoomAvailabilityResponse {

    boolean allAvailable;
    String message;
    List<RoomAvailabilityDetailResponse> availabilityDetails;
}

