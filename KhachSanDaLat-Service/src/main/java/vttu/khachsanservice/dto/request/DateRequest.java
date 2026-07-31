package vttu.khachsanservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DateRequest {

    @NotNull(message = "Ngày không được rỗng")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate ngay;
}

