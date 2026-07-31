package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.DichVuVATCusTomeRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.DichVuVATCusTomeReponse;
import vttu.khachsanservice.entity.DichVuVATCusTome;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.DichVuVATCustomeService;

import java.util.List;

@RestController
@RequestMapping("/dich-vu-vat-custome")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DichVuVATCustomeController {

    DichVuVATCustomeService dichVuVATCustomeService;
    @PostMapping("/{ma_hoadon_vat}")
    ApiResponse<Boolean> createDichVuTuynChinhVAT(@PathVariable String ma_hoadon_vat,@RequestBody List<DichVuVATCusTomeRequest> request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(dichVuVATCustomeService.createDichVuTuynChinhVAT(ma_hoadon_vat,request))
                    .message("Tạo thông tin dịch vụ tuỳ chỉnh VAT thành công")
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

    @GetMapping("/{ma_hoadon_vat}")
    ApiResponse<List<DichVuVATCusTomeReponse>> getDichVuTuynChinhVAT(@PathVariable String ma_hoadon_vat) {
        try {

            return ApiResponse.<List<DichVuVATCusTomeReponse>>builder()
                    .result(dichVuVATCustomeService.getDichVuVATCustome(ma_hoadon_vat))

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<DichVuVATCusTomeReponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<DichVuVATCusTomeReponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(HoaDonVatController.class).error("Error tạo hóa đơn vat", e);

            return ApiResponse.<List<DichVuVATCusTomeReponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

}
