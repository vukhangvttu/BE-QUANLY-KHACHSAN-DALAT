package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.PhieuDichVuRequest;
import vttu.khachsanservice.dto.request.XepPhongBooKingRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.KhachHangPhongResponse;
import vttu.khachsanservice.dto.response.PhieuDichVuReponse;
import vttu.khachsanservice.dto.response.PhuongXaResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.PhieuDichVuService;

import java.util.List;

@RestController
@RequestMapping("/phieu-dich-vu")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhieuDichVuController {

    PhieuDichVuService phieuDichVuService;

    @PostMapping("/{ma_phong}/{ma_booking}")
    ApiResponse<Boolean> createPhieuDichVu(@PathVariable String ma_phong,@PathVariable String ma_booking,@RequestBody List<PhieuDichVuRequest> request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(phieuDichVuService.createPhieuDichVu(ma_phong,ma_booking,request))
                    .message("Thêm dịch vụ thành công")
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
            LoggerFactory.getLogger(PhieuDichVuController.class).error("Error save phiếu dịch vụ", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/{ma_xep_phong_booking}")
    ApiResponse<List<PhieuDichVuReponse>> getAllPhieuDichVuByMaBooKing(@PathVariable String ma_xep_phong_booking)  {
        try {

            return ApiResponse.<List<PhieuDichVuReponse>>builder()
                    .result(phieuDichVuService.getAllPhieuDichVuByMaBooKing(ma_xep_phong_booking))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhieuDichVuReponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhieuDichVuReponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhieuDichVuController.class).error("Error list phiếu dịch vụ theo booking", e);

            return ApiResponse.<List<PhieuDichVuReponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PutMapping("/{ma_booking}/{ma_phong}/{ma_xepphong_booking}")
    ApiResponse<Boolean> updatePhieuDichVu(@PathVariable String ma_booking ,@PathVariable String ma_phong,@PathVariable String ma_xepphong_booking,@RequestBody List<PhieuDichVuRequest> request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(phieuDichVuService.updatePhieuDichVu(ma_booking,ma_phong,ma_xepphong_booking,request))
                    .message("Update dịch vụ thành công")
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
            LoggerFactory.getLogger(PhieuDichVuController.class).error("Error update phiếu dịch vụ", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @DeleteMapping("/{ma_booking}/{ma_phieudichvu}")
    ApiResponse<Boolean> deleteDichVuTrongPhieu(@PathVariable String ma_booking ,@PathVariable String ma_phieudichvu) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(phieuDichVuService.deleteDichVuTrongPhieu(ma_booking,ma_phieudichvu))
                    .message("Delete dịch vụ trong phiếu thành công")
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
            LoggerFactory.getLogger(KhachHangPhongResponse.class).error("Error delete dịch vụ trong phiếu thành công", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
