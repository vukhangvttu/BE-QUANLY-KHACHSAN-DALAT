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
import vttu.khachsanservice.dto.response.NguonKhachResponse;
import vttu.khachsanservice.dto.response.ThiTruongResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.ThiTruongService;

import java.util.List;

@RestController
@RequestMapping("/thi-truong")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThiTruongController {

    ThiTruongService thiTruongService;


    @GetMapping
    ApiResponse<List<ThiTruongResponse>> getAllThiTruong() {
        try {

            return ApiResponse.<List<ThiTruongResponse>>builder()
                    .result(thiTruongService.getAllThiTruong())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<ThiTruongResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<ThiTruongResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(ThiTruongController.class).error("Error list thị trường", e);

            return ApiResponse.<List<ThiTruongResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
