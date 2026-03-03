package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.ThongTinPhongDto;
import vttu.khachsanservice.dto.XemHoaDonThanhToanDTO;
import vttu.khachsanservice.dto.request.HoaDonRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.HoaDonService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hoa-don")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HoaDonController {


    HoaDonService hoaDonService;

    @PostMapping("/add-hoa-don/{ma_booking}/{ma_xepphong}")
    ApiResponse<Boolean> createHoaDon(@PathVariable String ma_booking, @PathVariable String ma_xepphong, @RequestBody HoaDonRequest request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(hoaDonService.createHoaDon(ma_booking,ma_xepphong,request))
                    .message("Thanh toán hóa đơn thành công")
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

    // load hiển thị toàn bộ lên để thanh toán
    @GetMapping("/all-thong-tin-thanh-toan/{ma_booking}")
    ApiResponse<List<ThongTinPhongDto>> allThanhToanHoaDon(@PathVariable String ma_booking) {
        try {

            return ApiResponse.<List<ThongTinPhongDto>>builder()
                    .result(hoaDonService.getAllThongTinBooKingByMaBooKing(ma_booking))

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<ThongTinPhongDto>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<ThongTinPhongDto>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(HoaDonController.class).error("Error tạo hóa đơn", e);

            return ApiResponse.<List<ThongTinPhongDto>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/thong-tin-khach-hang-thanh-toan/{ma_booking}")
    ApiResponse<Map<String,Object>> allThonTinKhachHangTT(@PathVariable String ma_booking) {
        try {

            return ApiResponse.<Map<String,Object>>builder()
                    .result(hoaDonService.getThongTinKhachHangBooking(ma_booking))

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
            LoggerFactory.getLogger(HoaDonController.class).error("Erro load thông tin khách hàng thanh toán", e);

            return ApiResponse.<Map<String,Object>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }

    @GetMapping("/thong-tin-xuat-phieu-dang-ky/{ma_booking}")
    ApiResponse<List<Map<String,Object>>> getThongTinXuatPhieuDangKy(@PathVariable String ma_booking) {
        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(hoaDonService.getThongTinXuatPhieuDangKy(ma_booking))

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
            LoggerFactory.getLogger(HoaDonController.class).error("Erro load thông tin xuất phiếu đăng ký", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }

    @PostMapping("/add-all-thong-tin-thanh-toan/{ma_booking}")
    ApiResponse<Boolean> createAllThongTinThanhToan(@PathVariable String ma_booking,@RequestBody HoaDonRequest request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(hoaDonService.createHoaDonFromData(ma_booking,request))
                    .message("Thanh toán hóa đơn thành công")
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


    @GetMapping("/xem-thong-tin-thanh-toan/{ma_booking}")
    ApiResponse<List<XemHoaDonThanhToanDTO>> xemThongTinThanhToan(@PathVariable String ma_booking) {
        try {

            return ApiResponse.<List<XemHoaDonThanhToanDTO>>builder()
                    .result(hoaDonService.getThongTinHoaDonThanhToan(ma_booking))

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<XemHoaDonThanhToanDTO>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<XemHoaDonThanhToanDTO>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(HoaDonController.class).error("Erro load thông tin xuất phiếu đăng ký", e);

            return ApiResponse.<List<XemHoaDonThanhToanDTO>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }
}
