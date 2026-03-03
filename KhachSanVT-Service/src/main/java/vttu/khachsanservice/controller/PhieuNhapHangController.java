package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.NhapHangRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.NhapHangReponse;
import vttu.khachsanservice.dto.response.TonKhoReponse;
import vttu.khachsanservice.entity.PhieuNhapHang;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.PhieuNhapHangService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/phieu-nhap-hang")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhieuNhapHangController {

    PhieuNhapHangService phieuDichVuService;

    @PostMapping()
    ApiResponse<PhieuNhapHang> createPhieuDichVu(@RequestBody List<NhapHangRequest> request) {
        try {

            return ApiResponse.<PhieuNhapHang>builder()
                    .result(phieuDichVuService.savePhieuNhapHang(request))
                    .message("Nhập hàng thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<PhieuNhapHang>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<PhieuNhapHang>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhieuDichVuController.class).error("Error save nhập hàng", e);

            return ApiResponse.<PhieuNhapHang>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/list")
    ApiResponse<List<PhieuNhapHang>> getAllPhong() {
        try {

            return ApiResponse.<List<PhieuNhapHang>>builder()
                    .result(phieuDichVuService.getAllPhieuNhapHang())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<PhieuNhapHang>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<PhieuNhapHang>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhieuNhapHangController.class).error("Error list phiếu nhập hàng", e);

            return ApiResponse.<List<PhieuNhapHang>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/chi-tiet-nhap-hang/{maPhieuNhap}")
    ApiResponse<List<NhapHangReponse>> getAllPhongByMaPhieuNhap(@PathVariable String maPhieuNhap) {
        try {

            return ApiResponse.<List<NhapHangReponse>>builder()
                    .result(phieuDichVuService.getAllNhapHangByMaPhieuNhapHang(maPhieuNhap))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<NhapHangReponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<NhapHangReponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhieuNhapHangController.class).error("Error list phiếu nhập hàng", e);

            return ApiResponse.<List<NhapHangReponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }

    @DeleteMapping("/{maNhapHang}")
    ApiResponse<Void> xoaPhieuNhapHang(@PathVariable String maNhapHang) {
        try {
            phieuDichVuService.xoaPhieuNhapHang(maNhapHang);

            return ApiResponse.<Void>builder()
                    .code(HttpStatus.OK.value())
                    .message("Xóa phiếu nhập hàng thành công")
                    .build();

        } catch (AccessDeniedException e) {
            return ApiResponse.<Void>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value())
                    .build();

        } catch (AppException e) {
            return ApiResponse.<Void>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value())
                    .build();

        } catch (Exception e) {
            LoggerFactory.getLogger(HinhThucThanhToanController.class)
                    .error("Error xóa phiếu nhập hàng", e);

            return ApiResponse.<Void>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
        }
    }

    ///
    @PutMapping("/{maPhieuNhapHang}")
    ApiResponse<Boolean> updatePhieuNhapHang(@PathVariable String maPhieuNhapHang,@RequestBody List<NhapHangRequest> request) {
        try {
            boolean result = phieuDichVuService.updateOrCreateNhapHang(maPhieuNhapHang,request);

            if (result) {
                return ApiResponse.<Boolean>builder()
                        .result(true)
                        .message("Nhập hàng thành công")
                        .code(HttpStatus.OK.value())
                        .build();
            } else {
                return ApiResponse.<Boolean>builder()
                        .result(false)
                        .message("Nhập hàng thất bại")
                        .code(HttpStatus.BAD_REQUEST.value())
                        .build();
            }
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
            LoggerFactory.getLogger(PhieuDichVuController.class).error("Error save nhập hàng", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("ton-kho")
    ApiResponse<List<Map<String, Object>>> getDanhSachTonKho() {
        try {

            return ApiResponse.<List<Map<String, Object>>>builder()
                    .result(phieuDichVuService.getAllTonKho())
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<Map<String, Object>>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<Map<String, Object>>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhieuNhapHangController.class).error("Error list phiếu nhập hàng", e);

            return ApiResponse.<List<Map<String, Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }

    }
    @GetMapping("thong-ke-nhap-xuat")
    ApiResponse<List<Map<String, Object>>> getThongKeNhapXuatTheoKhoanThoiGian(@RequestParam LocalDate ngayBatDau, @RequestParam LocalDate ngayKetThuc) {
        try {

            return ApiResponse.<List<Map<String, Object>>>builder()
                    .result(phieuDichVuService.getThongKeNhapXuat(ngayBatDau, ngayKetThuc))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<Map<String, Object>>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<Map<String, Object>>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(PhieuNhapHangController.class).error("Error thống kê nhập xuất", e);

            return ApiResponse.<List<Map<String, Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }

    }
}
