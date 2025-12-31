package vttu.khachsanservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDetailRequest {

    @NotBlank(message = "Mã loại phòng không được rỗng")
    String roomTypeId;

    @NotNull(message = "Số lượng phòng không được rỗng")
    @Min(value = 1, message = "Số lượng phòng phải lớn hơn 0")
    Integer quantity;

    @NotEmpty(message = "Danh sách ngày không được rỗng")
    @Valid
    List<DateRequest> listDate;
}
