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
import vttu.khachsanservice.dto.response.HinhThucThanhToanResponse;
import vttu.khachsanservice.dto.response.ThiTruongResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.HinhThucThanhToanService;

import java.util.List;

@RestController
@RequestMapping("/hinh-thuc-thanh-toan")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HinhThucThanhToanController {

    HinhThucThanhToanService hinhThucThanhToanService;

    @GetMapping
    ApiResponse<List<HinhThucThanhToanResponse>> getAllHinhThucThanhToan() {
        try {

            return ApiResponse.<List<HinhThucThanhToanResponse>>builder()
                    .result(hinhThucThanhToanService.getAllHinhThucThanhToan())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<HinhThucThanhToanResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<HinhThucThanhToanResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(HinhThucThanhToanController.class).error("Error list hình thức thanh toán", e);

            return ApiResponse.<List<HinhThucThanhToanResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/theo-danh-sach")
    ApiResponse<List<HinhThucThanhToanResponse>> getAllHinhThucThanhToanByMa() {
        try {

            return ApiResponse.<List<HinhThucThanhToanResponse>>builder()
                    .result(hinhThucThanhToanService.getHinhThucThanhToanByMa())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<HinhThucThanhToanResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<HinhThucThanhToanResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(HinhThucThanhToanController.class).error("Error list hình thức thanh toán", e);

            return ApiResponse.<List<HinhThucThanhToanResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

}
