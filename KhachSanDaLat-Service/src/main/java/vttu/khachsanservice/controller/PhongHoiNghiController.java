package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.PhongHoiNghiService;

import java.time.LocalDate;

@RestController
@RequestMapping("/phong-hoi-nghi")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhongHoiNghiController {
    private final PhongHoiNghiService phongHoiNghiService;

    @DeleteMapping("/{ma_booking}/{ma_phong_hoi_nghi}")
    ApiResponse<Boolean> deletePhongHoiNghi(@PathVariable String ma_booking , @PathVariable String ma_phong_hoi_nghi) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(phongHoiNghiService.deleteAllByMaBooking(ma_booking,ma_phong_hoi_nghi))
                    .message("Delete chi tiết thành công")
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
            LoggerFactory.getLogger(ChiTietBooKingController.class).error("Error delete phòng hội nghi", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/kiem-tra-phong-hoi-nghi")
    ApiResponse<Boolean> kiemTraPhongHoiNghi(@RequestParam LocalDate ngayBD, LocalDate ngayKT) {
        try {

            boolean check = phongHoiNghiService.kiemTraPhongHoiNghiTrongKhoanThoiGian(ngayBD,ngayKT);
            return ApiResponse.<Boolean>builder()
                    .result(check)
                    .message("Gọi kiểm tra thành công")
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
            LoggerFactory.getLogger(ChiTietBooKingController.class).error("Error kiểm tra phòng hội nghị", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
