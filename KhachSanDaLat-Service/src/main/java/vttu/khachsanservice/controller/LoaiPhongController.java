package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vttu.khachsanservice.dto.LoaiPhongTrongDTO;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.LoaiGiaResponse;
import vttu.khachsanservice.dto.response.LoaiPhongResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.LoaiPhongService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/loai-phong")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoaiPhongController {

    LoaiPhongService loaiPhongService;

    @GetMapping
    ApiResponse<List<LoaiPhongResponse>> getAllLoaiPhong() {
        try {

            return ApiResponse.<List<LoaiPhongResponse>>builder()
                    .result(loaiPhongService.getAllLoaiPhong())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<LoaiPhongResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<LoaiPhongResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(LoaiPhongController.class).error("Error list loại phòng", e);

            return ApiResponse.<List<LoaiPhongResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/kiem-tra-loai-phong-trong")
    ApiResponse<List<LoaiPhongTrongDTO>> getAllLoaiPhongTrongTrongKhoanTH(
            @RequestParam LocalDate ngayDen, @RequestParam LocalDate ngayDi
            ) {
        try {

            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .result(loaiPhongService.getAllLoaiPhongTrong(ngayDen,ngayDi))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(LoaiPhongService.class).error("Error list lấy loại phòng trống", e);

            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/kiem-tra-loai-phong-dang-o-theo-loai")
    ApiResponse<List<LoaiPhongTrongDTO>> getThongKePhongDangOTheoLoaiController(
            @RequestParam LocalDate ngayDen, @RequestParam LocalDate ngayDi
    ) {
        try {

            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .result(loaiPhongService.getThongKePhongDangOTheoLoaiService(ngayDen,ngayDi))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(LoaiPhongService.class).error("Error list lấy loại phòng trống", e);

            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/xem-du-bao-phong")
    ApiResponse<List<LoaiPhongTrongDTO>> getXemDuBaoPhongTrong(
            @RequestParam LocalDate ngayDen, @RequestParam LocalDate ngayDi
    ) {
        try {

            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .result(loaiPhongService.xemDuBaoPhong(ngayDen,ngayDi))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(LoaiPhongService.class).error("Error list lấy loại phòng trống", e);

            return ApiResponse.<List<LoaiPhongTrongDTO>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
