package vttu.khachsanservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.KhachHangBooKingRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.KhachHangBooKingResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.KhachHangBooKingService;

import java.util.List;

@RestController
@RequestMapping("/khang-hang-booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KhachHangBooKingController {

    KhachHangBooKingService khachHangBooKingService;

    @PostMapping
    ApiResponse<KhachHangBooKingResponse> create(@RequestBody @Valid KhachHangBooKingRequest khachHangBooKingRequest) {

        try {

            return ApiResponse.<KhachHangBooKingResponse>builder()
                    .result(khachHangBooKingService.createKhachHangBooKing(khachHangBooKingRequest))
                    .message("Công việc được tạo thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<KhachHangBooKingResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<KhachHangBooKingResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(KhachHangBooKingController.class).error("Error tạo khách hàng booking", e);

            return ApiResponse.<KhachHangBooKingResponse>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping
    ApiResponse<List<KhachHangBooKingResponse>> listAllKHBooKing() {

        try {

            return ApiResponse.<List<KhachHangBooKingResponse>>builder()
                    .result(khachHangBooKingService.listKhachHangBooKing())

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<KhachHangBooKingResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<KhachHangBooKingResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(KhachHangBooKingController.class).error("Error danh sách khách hàng booking", e);

            return ApiResponse.<List<KhachHangBooKingResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

}
