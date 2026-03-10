package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.GiaPhongTheoNgayRequest;
import vttu.khachsanservice.dto.request.NhanPhongRequest;
import vttu.khachsanservice.dto.request.XepPhongBooKingRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.XepPhongBooKingResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.XepPhongService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/xep-phong-booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XepPhongBooKingController {

    XepPhongService xepPhongService;

    @PostMapping("/{ma_booking}")
    ApiResponse<Boolean>  createXepPhongBooKing(@PathVariable String ma_booking, @RequestBody List<XepPhongBooKingRequest> request) {
        try {

            return ApiResponse.<Boolean>builder()
                     .result(xepPhongService.createXepPhongBooKing(request,ma_booking))
                    .message("Xếp phòng booking thành công")
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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error xếp phòng booking", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/{ma_booking}")
    ApiResponse<List<XepPhongBooKingResponse>> getXepPhongByMaBooKing(@PathVariable String ma_booking) {
        try {

            return ApiResponse.<List<XepPhongBooKingResponse>>builder()
                    .result(xepPhongService.getXepPhongBooKingList(ma_booking))

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<XepPhongBooKingResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<XepPhongBooKingResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error xếp phòng booking", e);

            return ApiResponse.<List<XepPhongBooKingResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PutMapping("/{ma_booking}")
    ApiResponse<Boolean> updateXepPhongBooKing(@PathVariable String ma_booking, @RequestBody List<XepPhongBooKingRequest> request) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(xepPhongService.updateXepPhongBooKing(request,ma_booking))
                    .message("Update xếp phòng booking thành công")
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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error xếp phòng booking", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @DeleteMapping("/{ma_booking}")
    ApiResponse<Boolean> deleteXepPhongBooKing(@PathVariable String ma_booking) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(xepPhongService.deleteXepPhongBooKing(ma_booking))
                    .message("Delete xếp phòng booking thành công")
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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error delete phòng booking", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PutMapping("/check-in/{ma_xepphong_booking}")
    ApiResponse<Boolean> updateDaNhanPhong(@PathVariable String ma_xepphong_booking,
                                           @RequestParam boolean danhanphong,
                                           @RequestParam BigDecimal phuThuCheckInSom) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(xepPhongService.updateDaNhanPhong(ma_xepphong_booking,danhanphong,phuThuCheckInSom))
                    .message("Check-in phòng booking thành công")
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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error check-in booking", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/chi-tiet-xep-phong/{ma_xepphong}")
    ApiResponse<XepPhongBooKingResponse> getXepPhongByMaXepPhong(@PathVariable String ma_xepphong) {
        try {

            return ApiResponse.<XepPhongBooKingResponse>builder()
                    .result(xepPhongService.getXepPhongBooKingByMaXepPhong(ma_xepphong))

                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<XepPhongBooKingResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<XepPhongBooKingResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error xếp phòng booking", e);

            return ApiResponse.<XepPhongBooKingResponse>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/thong-tin-khach-hang-thanh-toan/{ma_xepphong}")
    ApiResponse<Map<String,Object>> allThonTinKhachHangTT(@PathVariable String ma_xepphong) {
        try {

            return ApiResponse.<Map<String,Object>>builder()
                    .result(xepPhongService.getThongTinKhachHangBooking(ma_xepphong))

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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error load thông tin khách hàng thanh toán", e);

            return ApiResponse.<Map<String,Object>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }

    @GetMapping("/thong-tin-xuat-phieu-dang-ky/{ma_xepphong}")
    ApiResponse<List<Map<String,Object>>> getThongTinXuatPhieuDangKy(@PathVariable String ma_xepphong) {
        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(xepPhongService.xuatPhieuDangKyXepPhong(ma_xepphong))

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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error xuất thông tin xuất phiếu đăng ký", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }
    @PutMapping("/cap-nhat-ngay-di/{ma_booking}")
    ApiResponse<Boolean> updateNgayDi(@PathVariable String ma_booking,
                                      @RequestParam String ma_xepphong,
                                      @RequestParam LocalDate ngay_di_moi,
                                      @RequestParam LocalTime gio_di_moi,
                                      @RequestParam LocalDate ngay_di_cu,
                                      @RequestParam String ma_loaiphong,
                                      @RequestBody List<GiaPhongTheoNgayRequest> danhSachGiaMoi) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(xepPhongService.UpdateNgayDiCuaXepPhong(ma_booking,ma_xepphong, ngay_di_moi,gio_di_moi, ngay_di_cu, ma_loaiphong,danhSachGiaMoi))
                    .message("Cập nhật ngày đi thành công")
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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error cập nhật ngày đi", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @PutMapping("/phu-thu-xep-phong")
    ApiResponse<Boolean> updatePhuThuXepPhong(@RequestParam BigDecimal phu_thu_giuong,
                                           @RequestParam int so_giuong,
                                           @RequestParam BigDecimal phu_thu_nguoi_lon,
                                           @RequestParam int so_nguoi_lon,
                                           @RequestParam BigDecimal phu_thu_tre,
                                           @RequestParam int  so_tre,
                                           @RequestParam String ma_xepphong) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(xepPhongService.updatePhuThuPhong(phu_thu_giuong, so_giuong, phu_thu_nguoi_lon, so_nguoi_lon ,phu_thu_tre, so_tre,ma_xepphong))
                    .message("Cập nhật phụ thu thành công")
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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error cập nhật ngày đi", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/phu-thu-xep-phong/{ma_xepphong}")
    ApiResponse<Map<String,Object>> getPhuThuXepPhong(@PathVariable String ma_xepphong) {
        try {

            return ApiResponse.<Map<String,Object>>builder()
                    .result(xepPhongService.getPhuThuXepPhong(ma_xepphong))

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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error phu thu xep phong", e);

            return ApiResponse.<Map<String,Object>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }

    @DeleteMapping("/xoa_xep_phong/{ma_chi_tiet_xep_phong}")
    ApiResponse<Boolean> deleteXepPhong(@PathVariable String ma_chi_tiet_xep_phong , @RequestParam String ma_phong) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(xepPhongService.xoaPhongTrongXepPhong(ma_chi_tiet_xep_phong,ma_phong))
                    .message("Delete xếp phòng booking thành công")
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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error delete phòng booking", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @PutMapping("/tra-phong-booking/{ma_xepphong}")
    ApiResponse<Boolean> updateTraPhongBooking(@PathVariable String ma_xepphong) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(xepPhongService.traPhongDonBooking(ma_xepphong))
                    .message("Cập nhật trả phòng thành công")
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
            LoggerFactory.getLogger(XepPhongBooKingController.class).error("Error cập nhật ngày đi", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
