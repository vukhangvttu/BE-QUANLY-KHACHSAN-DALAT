package vttu.khachsanservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vttu.khachsanservice.dto.request.SignatureRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("signature")
public class SignatureController {
    // Đường dẫn thư mục vật lý (Có thể cấu hình trong application.properties)
    private static final String UPLOAD_DIR = "D:/uploads/signatures/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadSignature(@RequestBody SignatureRequest request) {
        try {
            // Kiểm tra bookingId
            if (request.getBookingId() == null || request.getBookingId().trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "status", false,
                                "message", "BookingId không được để trống"
                        ));
            }

            // Kiểm tra ảnh
            if (request.getBase64Image() == null || request.getBase64Image().trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "status", false,
                                "message", "Ảnh chữ ký không được để trống"
                        ));
            }

            // Tạo thư mục theo bookingId
            Path bookingDirectory = Paths.get(
                    UPLOAD_DIR,
                    request.getBookingId()
            );

            Files.createDirectories(bookingDirectory);

            // Lấy Base64
            String base64Data = request.getBase64Image();

            // Bỏ tiền tố data:image/png;base64,
            if (base64Data.contains(",")) {
                base64Data = base64Data.substring(
                        base64Data.indexOf(",") + 1
                );
            }

            // Decode Base64
            byte[] imageBytes;

            try {
                imageBytes = Base64.getDecoder().decode(base64Data);
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "status", false,
                                "message", "Dữ liệu Base64 không hợp lệ"
                        ));
            }

            // Tạo tên file
            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

            String fileName = request.getBookingId()
                    + "_" + time + ".png";

            // Đường dẫn file
            Path filePath = bookingDirectory.resolve(fileName);

            // Lưu file
            Files.write(filePath, imageBytes);

            // Thành công
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "status", true,
                            "message", "Lưu chữ ký thành công"
                    ));

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", false,
                            "message", "Lỗi khi lưu file",
                            "error", e.getMessage()
                    ));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", false,
                            "message", "Lỗi hệ thống",
                            "error", e.getMessage()
                    ));
        }
    }
}