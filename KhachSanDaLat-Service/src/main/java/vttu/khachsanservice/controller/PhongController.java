package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.PhongDTO;
import vttu.khachsanservice.dto.PhongStatusProjection;
import vttu.khachsanservice.dto.PhongTrongThoiGian;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.KhachHangPhongResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.PhongService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/phong")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhongController {

    PhongService phongService;


    @GetMapping("/chat-room")
    ApiResponse<List<PhongStatusProjection>> getAllPhong() {
        try {

            return ApiResponse.<List<PhongStatusProjection>>builder()
                    .result(phongService.getAllPhongChatRoom())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhongStatusProjection>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhongStatusProjection>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhongController.class).error("Error list phòng theo mã loại phòng", e);

            return ApiResponse.<List<PhongStatusProjection>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }


    @GetMapping
    ApiResponse<List<PhongDTO>> getPhongByMaLoaiPhong(@RequestParam String maLoaiPhong) {
        try {

            return ApiResponse.<List<PhongDTO>>builder()
                    .result(phongService.getPhongByMaLoaiPhong(maLoaiPhong))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhongDTO>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhongDTO>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhongController.class).error("Error list phòng theo mã loại phòng", e);

            return ApiResponse.<List<PhongDTO>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PutMapping("/trang-thai-ve-sinh/{ma_phong}")
    ApiResponse<Boolean> updateTrangThaiVeSinh(@PathVariable String ma_phong, @RequestParam boolean trangThaiVeSinh) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(phongService.updateTrangThaiVeSinh(trangThaiVeSinh,ma_phong))
                    .message("Update trạng thái buồng phòng thành công")
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
            LoggerFactory.getLogger(PhongController.class).error("Error update trạng thái vệ sinh", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/kiem-tra-phong-trong")
    ApiResponse<List<PhongTrongThoiGian>> getListPhongTrongTheoKhoanThoiGian(@RequestParam String maLoaiPhong, @RequestParam LocalDate ngay_den, @RequestParam LocalDate ngay_di) {
        try {

            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .result(phongService.getAllPhongTrongThoiGian(maLoaiPhong,ngay_den,ngay_di))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhongController.class).error("Error list phòng trống theo mã loại phòng và thời gian", e);

            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/kiem-tra-phong-dang-o-theo-loai")
    ApiResponse<List<PhongTrongThoiGian>> getListPhongDangOTheoLoai(@RequestParam String maLoaiPhong, @RequestParam LocalDate ngay_den, @RequestParam LocalDate ngay_di) {
        try {

            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .result(phongService.getDanhSachPhongDangOTheoLoaiService(maLoaiPhong,ngay_den,ngay_di))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhongController.class).error("Error list phòng trống theo mã loại phòng và thời gian", e);

            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/du-bao-phong-khoan-thoi-gian")
    ApiResponse<List<PhongStatusProjection>> getDuBaoPhongTheoThoiGian(LocalDate ngay_bd, LocalDate ngay_kt) {
        try {

            return ApiResponse.<List<PhongStatusProjection>>builder()
                    .result(phongService.getDuBaoPhongTheoKhoanThoiGian(ngay_bd,ngay_kt))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhongStatusProjection>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhongStatusProjection>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhongController.class).error("Error list dự báo phòng", e);

            return ApiResponse.<List<PhongStatusProjection>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/kiem-tra-phong-trong-add-xep-phong/{ma_booking}")
    ApiResponse<List<PhongTrongThoiGian>> getListPhongTrongTheoKhoanThoiGianAndXepPhong(@RequestParam String maLoaiPhong, @RequestParam LocalDate ngay_den, @RequestParam LocalDate ngay_di, @PathVariable String ma_booking) {
        try {

            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .result(phongService.getDuBaoPhongTheoKhoanThoiGianAndBooKing(maLoaiPhong,ngay_den,ngay_di,ma_booking))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhongController.class).error("Error list phòng trống theo mã loại phòng và thời gian", e);

            return ApiResponse.<List<PhongTrongThoiGian>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/thong-tin-khach-hang-booking/{ma_xepphong}")
    ApiResponse<Map<String, Object>> getThongTinKhachHangBooKing(@PathVariable String ma_xepphong) {
        try {

            return ApiResponse.<Map<String, Object>>builder()
                    .result(phongService.getThongTinKhachHangBooking(ma_xepphong))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<Map<String, Object>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<Map<String, Object>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhongController.class).error("Error lấy thông tin booking đẩy lên line phong", e);

            return ApiResponse.<Map<String, Object>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
