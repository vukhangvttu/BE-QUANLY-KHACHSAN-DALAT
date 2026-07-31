package vttu.khachsanservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatsDTO {
    @JsonProperty("stayDate")
    private String stayDate;

    @JsonProperty("roomTypeId")
    private String roomTypeId;

    @JsonProperty("roomTypeName")
    private String roomTypeName;

    @JsonProperty("totalRooms")
    private int totalRooms;
}

