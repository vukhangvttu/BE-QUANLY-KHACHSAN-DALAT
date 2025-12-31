package vttu.khachsanservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO Request cho loại phòng và số lượng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomTypeQuantityRequest {

    /**
     * Mã loại phòng
     */
    @NotBlank(message = "Mã loại phòng không được rỗng")
    String roomTypeCode;

    /**
     * Số lượng phòng cần
     */
    @Min(value = 1, message = "Số lượng phòng phải lớn hơn 0")
    int quantity;
}
