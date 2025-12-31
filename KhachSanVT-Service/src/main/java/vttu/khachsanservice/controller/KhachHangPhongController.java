package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.KhachHangPhongRequest;
import vttu.khachsanservice.dto.request.XepPhongBooKingRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.KhachHangPhongResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.KhachHangPhongService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/khach-hang-phong")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KhachHangPhongController {

    KhachHangPhongService khachHangPhongService;

    @PostMapping("/{ma_xepphong_booking}")
    ApiResponse<KhachHangPhongResponse> createKhachHangPhong(@PathVariable String ma_xepphong_booking, @RequestBody KhachHangPhongRequest request) {
        try {

            return ApiResponse.<KhachHangPhongResponse>builder()
                    .result(khachHangPhongService.createKhachHangPhong(ma_xepphong_booking,request))
                    .message("Thêm thông tin khách vào phòng thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error xếp phòng booking", e);

            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/xep-phong-booking/{ma_xepphong_booking}")
    ApiResponse<List<KhachHangPhongResponse>> getKhachHangPhongByMaXepPhong(@PathVariable String ma_xepphong_booking) {
        try {

            return ApiResponse.<List<KhachHangPhongResponse>>builder()
                    .result(khachHangPhongService.getKhachHangPhongByMaXepPhong(ma_xepphong_booking))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<KhachHangPhongResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<KhachHangPhongResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(KhachHangPhongController.class).error("Error get list khách hạng phòng theo mã xếp phòng", e);

            return ApiResponse.<List<KhachHangPhongResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/{ma_khachhang_phong}")
    ApiResponse<KhachHangPhongResponse> getKhachHangPhongById(@PathVariable String ma_khachhang_phong) {
        try {

            return ApiResponse.<KhachHangPhongResponse>builder()
                    .result(khachHangPhongService.getKhachHangPhongById(ma_khachhang_phong))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(KhachHangPhongController.class).error("Error get list khách hạng phòng theo id", e);

            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @PutMapping("/{ma_khachhang_phong}")
    ApiResponse<KhachHangPhongResponse> updateKhachHangPhong(@PathVariable String ma_khachhang_phong,@RequestBody KhachHangPhongRequest request) {
        try {

            return ApiResponse.<KhachHangPhongResponse>builder()
                    .result(khachHangPhongService.updateKhachHangPhong(ma_khachhang_phong,request))
                    .message("Update thông tin khách vào phòng thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(KhachHangPhongResponse.class).error("Error update khách hàng trong phòng", e);

            return ApiResponse.<KhachHangPhongResponse>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @DeleteMapping("/{ma_khachhang_phong}")
    ApiResponse<Boolean> deleteKhachHangPhong(@PathVariable String ma_khachhang_phong) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(khachHangPhongService.deleteThongTinKhachHang(ma_khachhang_phong))
                    .message("Delete khách hàng trong phòng thành công")
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
            LoggerFactory.getLogger(KhachHangPhongResponse.class).error("Error delete khách hàng trong phòng", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping
    ApiResponse<List<Map<String,Object>>> getThongTinKhachHangLuuTru(@RequestParam LocalDate ngayDen, @RequestParam LocalDate ngayDi) {
        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(khachHangPhongService.getThongTinKhachHangLuuTru(ngayDen, ngayDi))

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
            LoggerFactory.getLogger(KhachHangPhongResponse.class).error("Error load thông tin luu trú", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }
}
