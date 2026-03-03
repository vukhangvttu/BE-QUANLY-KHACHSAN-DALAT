package vttu.khachsanservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for CCCD Image Analysis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CCCDImageRequest {

    /**
     * Base64 encoded image data
     */
    @NotNull(message = "Image data is required")
    String imageBase64;

    /**
     * Optional: Image format (jpg, png, etc.)
     */
    String imageFormat;

    /**
     * Optional: Whether to enhance image quality before processing
     */
    Boolean enhanceImage;
}

