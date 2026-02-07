package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.ThongTinPhongDto;
import vttu.khachsanservice.dto.request.DichVuVATCusTomeRequest;
import vttu.khachsanservice.dto.request.HoaDonRequest;
import vttu.khachsanservice.dto.request.HoaDonVatRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.DichVuVATCusTomeReponse;
import vttu.khachsanservice.dto.response.HoaDonVatResponse;
import vttu.khachsanservice.dto.xuathoadon.InvoiceRequest;
import vttu.khachsanservice.dto.xuathoadon.ItemInfo;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.service.API_Viettel;
import vttu.khachsanservice.service.HoaDonVatService;
import vttu.khachsanservice.service.KhachHangBooKingService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hoa-don-vat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HoaDonVatController {

    HoaDonVatService hoaDonVatService;
    API_Viettel apiViettel;
    KhachHangBooKingService khachHangBooKingService;

    @PostMapping("/add-hoa-don/{ma_booking}")
    ApiResponse<Boolean> createHoaDon(@PathVariable String ma_booking, @RequestBody HoaDonVatRequest request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(hoaDonVatService.createHoaDonVat(ma_booking, request))
                    .message("Tạo thông tin xuất VAT thành công")
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
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn vat", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/get-thong-tin-hoa-don-vat/{ma_booking}")
    ApiResponse<List<HoaDonVatResponse>> getHoaDonVatByMaBooKing(@PathVariable String ma_booking) {
        try {

            return ApiResponse.<List<HoaDonVatResponse>>builder()
                    .result(hoaDonVatService.getHoaDonVatByMaBooKing(ma_booking))

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<HoaDonVatResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<HoaDonVatResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(HoaDonController.class).error("Error tạo hóa đơn", e);

            return ApiResponse.<List<HoaDonVatResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PutMapping("/edit-hoa-don")
    ApiResponse<Boolean> updateHoaDon(@RequestBody HoaDonVatRequest request) {
        try {

            InvoiceRequest invoiceRequest = new InvoiceRequest();

            return ApiResponse.<Boolean>builder()
                    .result(hoaDonVatService.updateHoaDonVat(request))
                    .message("Cập nhật thông tin xuất VAT thành công")
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
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn vat", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/print-hoa-don-vat-nhap/{ma_booking}/{ma_hoadon_vat}")
    public ResponseEntity<?> xuatHoaDonNhap(@PathVariable String ma_booking, @PathVariable String ma_hoadon_vat) {
        try {


            InvoiceRequest invoiceRequest = hoaDonVatService.getThongTinXuatHoaDonNhap(ma_booking, ma_hoadon_vat);

            if (invoiceRequest == null) {
                throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
            }

            // 4. Gọi đến Viettel API để tạo và tải file PDF
            return apiViettel.createAndDownloadInvoice(ma_hoadon_vat,invoiceRequest);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AppException e) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("errorCode", e.getErrorCode());
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception e) {
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn VAT", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/xac_nhan-hoa-don-vat-nhap/{ma_hoadon_vat}")
    ApiResponse<Boolean> xacNhanHoaDonNhap(@PathVariable String ma_hoadon_vat) {
        try {

            InvoiceRequest invoiceRequest = hoaDonVatService.getThongTinXuatHoaDonXacNhan(ma_hoadon_vat);

            if (invoiceRequest == null) {
                throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
            }

            return ApiResponse.<Boolean>builder()
                    .result(apiViettel.createInvoicePhatHanh(ma_hoadon_vat,invoiceRequest))
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
            LoggerFactory.getLogger(HoaDonController.class).error("Error tạo hóa đơn", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/print-hoa-don-vat-phat-hanh/{ma_hoadon_vat}")
    public ResponseEntity<?> xuatHoaDonVatPhatHanh(@PathVariable String ma_hoadon_vat) {
        try {

            // 4. Gọi đến Viettel API để tạo và tải file PDF
            return apiViettel.xuatPDFHoaDonPhatHanh(ma_hoadon_vat);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AppException e) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("errorCode", e.getErrorCode());
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception e) {
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn VAT", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/huy-hoa-don-vat-phat-hanh")
    public ResponseEntity<?> huyHoaDonVatPhatHanh(@RequestParam String ma_hoadon_vat,
                                                  @RequestParam String van_ban_thoa_thuan,
                                                  @RequestParam LocalDate ngay_thoa_thuan,
                                                  @RequestParam String ly_do) {

        try {

            // 4. Gọi đến Viettel API để tạo và tải file PDF
            return apiViettel.cancelTransactionInvoice(ma_hoadon_vat,van_ban_thoa_thuan,ngay_thoa_thuan,ly_do);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AppException e) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("errorCode", e.getErrorCode());
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception e) {
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn VAT", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list-code-vat")
    public ApiResponse<List<Map<String,Object>>> getListCodeVAT() {
        try {

            return ApiResponse.<List<Map<String, Object>>>builder()
                    .result(hoaDonVatService.getListCodeVat())
                    .message("Tạo thông tin người mua thành công")
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
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn vat", e);

            return ApiResponse.<List<Map<String, Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/get-list-item-info/{ma_booking}/{ma_hoadon_vat}")
    public ApiResponse< List<ItemInfo>> getListCodeVAT(@PathVariable String ma_booking, @PathVariable String ma_hoadon_vat) {
        try {

            return ApiResponse.<List<ItemInfo>>builder()
                    .result(hoaDonVatService.getThongTinDichVuVAT(ma_booking,ma_hoadon_vat))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.< List<ItemInfo>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.< List<ItemInfo>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn vat", e);

            return ApiResponse.< List<ItemInfo>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    // lưu thông tin dịch vụ tùy chỉnh
    @PostMapping("/add-dich-vu-tuy-chinh")
    ApiResponse<Boolean> createDichVuTuynChinhVAT( @RequestBody List<DichVuVATCusTomeRequest> request) {
        try {

            return ApiResponse.<Boolean>builder()
//                    .result(hoaDonVatService.createHoaDonVat(ma_booking, request))
                    .message("Tạo thông tin người mua thành công")
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
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn vat", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }


    // lấy thông tin khách hàng booking đỏ lên giao diện hóa đơn vat
    @GetMapping("/thong-tin-khach-hang-booking/{ma_booking}")
    ApiResponse<Map<String,Object>> getThongTinKhachHangBooking(@PathVariable String ma_booking) {
        try {

            return ApiResponse.<Map<String,Object>>builder()
                    .result(khachHangBooKingService.getThongTinKhachHangXuatVAT(ma_booking))
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
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn vat", e);

            return ApiResponse.<Map<String,Object>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

}
