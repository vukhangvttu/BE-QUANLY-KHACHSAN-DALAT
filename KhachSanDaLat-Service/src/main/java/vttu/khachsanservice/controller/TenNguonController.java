package vttu.khachsanservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.BooKingRequest;
import vttu.khachsanservice.dto.request.TenNguonRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.BooKingResponse;
import vttu.khachsanservice.dto.response.TenNguonResponse;
import vttu.khachsanservice.entity.TenNguon;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.TenNguonService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ten-nguon")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TenNguonController {

    TenNguonService tenNguonService;

    @PostMapping
    ApiResponse<TenNguonResponse> create(@RequestBody TenNguonRequest tenNguonRequest) {

        try {

            return ApiResponse.<TenNguonResponse>builder()
                    .result(tenNguonService.createTenNguon(tenNguonRequest))
                    .message("Thêm thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<TenNguonResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<TenNguonResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error tạo tên nguồn", e);

            return ApiResponse.<TenNguonResponse>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping
    ApiResponse<List<TenNguon>> getAllTenNguon() {

        try {

            return ApiResponse.<List<TenNguon>>builder()
                    .result(tenNguonService.getAllTenNguon())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<TenNguon>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<TenNguon>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error getall tên nguồn", e);

            return ApiResponse.<List<TenNguon>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PutMapping
    ApiResponse<TenNguonResponse> update(@RequestBody TenNguonRequest tenNguonRequest) {

        try {

            return ApiResponse.<TenNguonResponse>builder()
                    .result(tenNguonService.updateTenNguon(tenNguonRequest))
                    .message("Cập nhật thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<TenNguonResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<TenNguonResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error tạo tên nguồn", e);

            return ApiResponse.<TenNguonResponse>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/{ma_mguon_khach}")
    ApiResponse<TenNguonResponse> getTenNguonByID(@Valid @PathVariable("ma_mguon_khach") Integer ma_mguon_khach) {

        try {

            return ApiResponse.<TenNguonResponse>builder()
                    .result(tenNguonService.getTenNguonById(ma_mguon_khach))
                    .message("Thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<TenNguonResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<TenNguonResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error get nguồn khách", e);

            return ApiResponse.<TenNguonResponse>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
