package vttu.khachsanservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomAvailabilityDetailResponse {

    String roomTypeId;
    String roomTypeName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;
    int totalRooms;
    int availableRooms;
    int bookedRooms;
    int requestedQuantity;  // Số lượng phòng yêu cầu
    boolean isAvailable;    // Còn đủ phòng theo yêu cầu không
    String status;
}
