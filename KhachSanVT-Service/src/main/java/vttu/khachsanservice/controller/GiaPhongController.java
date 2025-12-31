package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.GiaPhongTheoThoiGian;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.GiaPhongResponse;
import vttu.khachsanservice.dto.response.GiaPhongTheoNgayReponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.GiaPhongService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gia-phong")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GiaPhongController {


    GiaPhongService giaPhongService;

    @GetMapping("/extra-beb/{maphong}")
    ApiResponse<Map<String,Object>> getGiaPhongExtraBed(@PathVariable String maphong) {
        try {

            return ApiResponse.<Map<String,Object>>builder()
                    .result(giaPhongService.getGiaPhongExtra(maphong))

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<Map<String,Object>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<Map<String,Object>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(GiaPhongController.class).error("Error load gia phòng extra", e);

            return ApiResponse.<Map<String,Object>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }

    }
    @GetMapping("/theo-thoi-gian")
    ApiResponse<List<GiaPhongTheoThoiGian>> getAllGiaPhongTheoThoiGian() {
        try {

            return ApiResponse.<List<GiaPhongTheoThoiGian>>builder()
                    .result(giaPhongService.getAllGiaPhongTheoThoiGian())

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<GiaPhongTheoThoiGian>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<GiaPhongTheoThoiGian>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(GiaPhongController.class).error("Error load giá phòng theo thời gian", e);

            return ApiResponse.<List<GiaPhongTheoThoiGian>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/theo-loai-phong/{maLoaiPhong}")
    ApiResponse<List<GiaPhongResponse>> getGiaPhongTheoMaLoaiPhong(@PathVariable String maLoaiPhong) {
        try {

            return ApiResponse.<List<GiaPhongResponse>>builder()
                    .result(giaPhongService.getGiaPhongByMaLoaiPhong(maLoaiPhong))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<GiaPhongResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<GiaPhongResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(GiaPhongController.class).error("Error load giá phòng theo thời gian", e);

            return ApiResponse.<List<GiaPhongResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/theo-xep-phong/{maXepPhongBooking}")
    ApiResponse<List<GiaPhongTheoNgayReponse>> getGiaPhongTheoNgay(@PathVariable String maXepPhongBooking) {
        try {

            return ApiResponse.<List<GiaPhongTheoNgayReponse>>builder()
                    .result(giaPhongService.getGiaPhongTheoNgay(maXepPhongBooking))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<GiaPhongTheoNgayReponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<GiaPhongTheoNgayReponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(GiaPhongController.class).error("Error load giá phòng theo thời gian", e);

            return ApiResponse.<List<GiaPhongTheoNgayReponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
