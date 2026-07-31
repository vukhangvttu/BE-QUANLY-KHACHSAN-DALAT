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
import vttu.khachsanservice.dto.request.KhachHangBooKingRequest;
import vttu.khachsanservice.dto.request.KhachHangPhongRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.BooKingResponse;
import vttu.khachsanservice.dto.response.KhachHangBooKingResponse;
import vttu.khachsanservice.dto.response.KhachHangPhongResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.BooKingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BooKingController {

    BooKingService booKingService;

    @PostMapping
    ApiResponse<BooKingResponse> create(@RequestBody BooKingRequest booKingRequest) {

        try {

            return ApiResponse.<BooKingResponse>builder()
                    .result(booKingService.createBooKing(booKingRequest))
                    .message("Booking được tạo thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<BooKingResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<BooKingResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error tạo booking", e);

            return ApiResponse.<BooKingResponse>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @PutMapping("/{ma_booking}")
    ApiResponse<BooKingResponse> update (@PathVariable  String ma_booking, @RequestBody BooKingRequest booKingRequest) {

        try {

            return ApiResponse.<BooKingResponse>builder()
                    .result(booKingService.updateBooKing(ma_booking,booKingRequest))
                    .message("Booking được cập nhật thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<BooKingResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<BooKingResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error tạo booking", e);

            return ApiResponse.<BooKingResponse>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping
    ApiResponse<List<Map<String,Object>>> getAllBooKing() {

        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(booKingService.getAllBooKing())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error getall booking", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/{ma_booking}")
    ApiResponse<BooKingResponse> getBooKingByMaBooKing(@Valid @PathVariable("ma_booking") String maBookingId) {

        try {

            return ApiResponse.<BooKingResponse>builder()
                    .result(booKingService.getBooKingByMaBookingId(maBookingId))
                    .message("Thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<BooKingResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<BooKingResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error tạo booking", e);

            return ApiResponse.<BooKingResponse>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PutMapping("/update-trang-thai/{maBooking}/{maTrangThai}")
    ApiResponse<Boolean> updateTrangThai(
            @PathVariable String maBooking,
            @PathVariable int maTrangThai
    ) {
    try {

            return ApiResponse.<Boolean>builder()
                    .result(booKingService.updateTrangThai(maTrangThai, maBooking).getResult())
                    .message("Update thông tin khách vào phòng thành công")
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
            LoggerFactory.getLogger(KhachHangPhongResponse.class).error("Error update khách hàng trong phòng", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
