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
import vttu.khachsanservice.dto.response.LoaiGiaResponse;
import vttu.khachsanservice.dto.response.LoaiTheResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.LoaiGiaService;

import java.util.List;

@RestController
@RequestMapping("/loai-gia")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoaiGiaController {

    LoaiGiaService loaiGiaService;

    @GetMapping
    ApiResponse<List<LoaiGiaResponse>> getAllLoaiGia() {
        try {

            return ApiResponse.<List<LoaiGiaResponse>>builder()
                    .result(loaiGiaService.getAllLoaiGia())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<LoaiGiaResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<LoaiGiaResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(LoaiTheController.class).error("Error list loại giá", e);

            return ApiResponse.<List<LoaiGiaResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
