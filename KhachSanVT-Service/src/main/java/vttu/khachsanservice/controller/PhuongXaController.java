package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.PhieuDichVuRequest;
import vttu.khachsanservice.dto.request.PhuongXaRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.HuyenReponse;
import vttu.khachsanservice.dto.response.PhuongXaResponse;
import vttu.khachsanservice.dto.response.TinhThanhResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.PhuongXaService;

import java.util.List;

@RestController
@RequestMapping("/phuong-xa")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhuongXaController {

    PhuongXaService phuongXaService;

//    @GetMapping("/{ma_huyen}")
//    ApiResponse<List<PhuongXaResponse>> getlPhuongXaByMaHuyen(@PathVariable String ma_huyen)  {
//        try {
//
//            return ApiResponse.<List<PhuongXaResponse>>builder()
//                    .result(phuongXaService.getPhuongXaByMaHuyen(ma_huyen))
//                    .code(HttpStatus.OK.value())
//                    .build();
//        } catch (AccessDeniedException e) {
//            return ApiResponse.<List<PhuongXaResponse>>builder()
//                    .message("Bạn không có quyền thực hiện thao tác này")
//                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
//                    .build();
//        } catch (AppException e) {
//            // Handle custom exceptions (e.g., validation or business logic errors)
//            return ApiResponse.<List<PhuongXaResponse>>builder()
//                    .message(e.getMessage())
//                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
//                    .build();
//        } catch (Exception e) {
//            // Log unexpected errors
//            LoggerFactory.getLogger(PhuongXaController.class).error("Error list phường xã", e);
//
//            return ApiResponse.<List<PhuongXaResponse>>builder()
//                    .message("Internal Server Error!")
//                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
//                    .build();
//        }
//    }
    // bổ sung 2026
    @GetMapping("/{ma_tinh}")
    ApiResponse<List<PhuongXaResponse>> getlPhuongXaByMaTinh(@PathVariable String ma_tinh)  {
        try {

            return ApiResponse.<List<PhuongXaResponse>>builder()
                    .result(phuongXaService.getPhuongXaByMaTinh(ma_tinh))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhuongXaResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhuongXaResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhuongXaController.class).error("Error list phường xã", e);

            return ApiResponse.<List<PhuongXaResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PostMapping
    ApiResponse<Boolean> createPhuongXa(@RequestBody PhuongXaRequest request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(phuongXaService.savePhuongXa(request))
                    .message("Thêm phường xã thành công")
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
            LoggerFactory.getLogger(PhuongXaController.class).error("Error save phường xã", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/phuong-xa-user-tu-them")
    ApiResponse<List<PhuongXaResponse>> getPhuongXaUserTuThem()  {
        try {

            return ApiResponse.<List<PhuongXaResponse>>builder()
                    .result(phuongXaService.getAllPhuongXaUserTuThem())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhuongXaResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhuongXaResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhuongXaController.class).error("Error list phường xã", e);

            return ApiResponse.<List<PhuongXaResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
