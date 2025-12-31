package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.GiaPhongTheoNgayRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.ChuyenPhongService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/chuyen-phong")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChuyenPhongController {

    ChuyenPhongService chuyenPhongService;

    @PostMapping
    ApiResponse<Boolean> saveChuyenPhong(@RequestParam String ma_xepphong, @RequestParam String ma_phong_cu,
                                         @RequestParam String ma_phong_moi, @RequestParam String ma_loai_phong,
                                          @RequestBody List<GiaPhongTheoNgayRequest> giaPhongTheoNgayRequests) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(chuyenPhongService.saveChuyenPhong(ma_xepphong, ma_phong_cu, ma_phong_moi, ma_loai_phong,giaPhongTheoNgayRequests))
                    .message("Chuyển phòng thành công")
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
            LoggerFactory.getLogger(ChuyenPhongController.class).error("Error chuyển phòng", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
