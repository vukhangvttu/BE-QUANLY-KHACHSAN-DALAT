package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.HoaDonBanLeRequest;
import vttu.khachsanservice.dto.request.HoaDonRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.HoaDonBanLeResponse;
import vttu.khachsanservice.dto.response.HuyenReponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.HoaDonBanLeService;

import java.util.List;

@RestController
@RequestMapping("/hoa-don-nha-hang")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HoaDonBanLeController {

    HoaDonBanLeService hoaDonBanLeService;

    @PostMapping("/add-hoa-don-ban-le")
    ApiResponse<Boolean> createHoaDonBanLe(@RequestBody HoaDonBanLeRequest request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(hoaDonBanLeService.createHoaDonBanLe(request))
                    .message("Thanh toán hóa đơn thành công")
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
            LoggerFactory.getLogger(HoaDonBanLeController.class).error("Error tạo hóa đơn", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping
    ApiResponse<List<HoaDonBanLeResponse>> getLichSuHoaDon() {

        try {

            return ApiResponse.<List<HoaDonBanLeResponse>>builder()
                    .result(hoaDonBanLeService.getAllHoaDonBanLe())

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<HoaDonBanLeResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<HoaDonBanLeResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(HoaDonBanLeController.class).error("Error lịc sử hóa đơn", e);

            return ApiResponse.<List<HoaDonBanLeResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
