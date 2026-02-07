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
import vttu.khachsanservice.dto.response.DichVuMienPhiResponse;
import vttu.khachsanservice.dto.response.DichVuResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.DichVuMienPhiService;

import java.util.List;

@RestController
@RequestMapping("/dich-vu-mien-phi")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DichVuMienPhiController {

    DichVuMienPhiService dichVuMienPhiService;

    @GetMapping
    ApiResponse<List<DichVuMienPhiResponse>> getAllDichVuMienPhi() {
        try {

            return ApiResponse.<List<DichVuMienPhiResponse>>builder()
                    .result(dichVuMienPhiService.getAllDichVuMienPhi())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<DichVuMienPhiResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<DichVuMienPhiResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(DichVuMienPhiController.class).error("Error list dịch vụ miễn phí", e);

            return ApiResponse.<List<DichVuMienPhiResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
