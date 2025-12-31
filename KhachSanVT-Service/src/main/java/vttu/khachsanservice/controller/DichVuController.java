package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.DanhXungResponse;
import vttu.khachsanservice.dto.response.DichVuResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.DichVuService;

import java.util.List;

@RestController
@RequestMapping("/dich-vu")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DichVuController {

    DichVuService dichVuService;

    @GetMapping
    ApiResponse<List<DichVuResponse>> getAllDichVu() {
        try {

            return ApiResponse.<List<DichVuResponse>>builder()
                    .result(dichVuService.getAllDichVu())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<DichVuResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<DichVuResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(DichVuController.class).error("Error list dịch vụ", e);

            return ApiResponse.<List<DichVuResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
