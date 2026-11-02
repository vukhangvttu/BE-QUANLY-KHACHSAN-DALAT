package vttu.khachsanservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TenNguonRequest {
    int maTenNguon;

    @NotBlank(message = "Tên nguồn không được để trống")
    String tenNguon;

    @NotBlank(message = "Đại chỉ không được để trống")
    String diaChi;
    @NotBlank(message = "Email không được để trống")
    String email;
    @NotBlank(message = "Code VAT không được để trống")
    String codeVAT;
}
