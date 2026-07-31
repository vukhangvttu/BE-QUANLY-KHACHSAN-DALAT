package vttu.khachsanservice.controller;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.BooKingRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.BooKingResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.AnSangMoiNgayService;

@RestController
@RequestMapping("/an-sang-moi-ngay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AnSangMoiNgayController {

    AnSangMoiNgayService anSangMoiNgayService;

    @PostMapping
    ApiResponse<Boolean> createAnSangMoiNgay(@RequestParam String ma_xepphong, @RequestParam String so_luong) {

        try {

            return ApiResponse.<Boolean>builder()
                    .result(anSangMoiNgayService.saveAnSangMoiNgay(ma_xepphong, so_luong))
                    .message("Cập nhật số lượng ăn sáng thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<Boolean>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<Boolean>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(AnSangMoiNgayController.class).error("Error tạo ăn sang mõi ngày", e);

            return ApiResponse.<Boolean>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
