package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.PhuThuKhacDto;
import vttu.khachsanservice.dto.response.*;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.ChiTietBooKingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chi-tiet-booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChiTietBooKingController {
    ChiTietBooKingService chiTietBooKingService;

    @GetMapping("/{ma_booking}")
    ApiResponse<List<ChiTietBooKingResponse>>  getChiTietBooKingByMaBooKing(
            @PathVariable("ma_booking") String ma_booking) {

        try {

            return ApiResponse.<List<ChiTietBooKingResponse>>builder()
                    .result(chiTietBooKingService.getChiTietBooKingByMaBooKing(ma_booking))
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<ChiTietBooKingResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<ChiTietBooKingResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(ChiTietBooKingController.class).error("Error chi tiết booking", e);

            return ApiResponse.<List<ChiTietBooKingResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @DeleteMapping("/{ma_chitiet}/{ma_booking}")
    ApiResponse<Boolean> deleteChiTietBooKing(
            @PathVariable("ma_chitiet") String ma_chitiet,
            @PathVariable("ma_booking") String ma_booking) {
        try {

            return ApiResponse.<Boolean>builder()
                    .result(chiTietBooKingService.deleteChiTietBooking(ma_chitiet,ma_booking))
                    .message("Delete chi tiết thành công")
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
            LoggerFactory.getLogger(ChiTietBooKingController.class).error("Error delete chi tiết booking", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/gia-phong-theo-ngay/{ma_xep_phong}")
    ApiResponse<List<GiaPhongTheoNgayReponse>>  getListGiaPhongTheoNgay(
            @PathVariable("ma_xep_phong") String ma_xep_phong) {

        try {

            return ApiResponse.<List<GiaPhongTheoNgayReponse>>builder()
                    .result(chiTietBooKingService.getGiaPhongTheoNgayByMaChiTietBooking(ma_xep_phong))
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
            LoggerFactory.getLogger(ChiTietBooKingController.class).error("Error chi tiết booking", e);

            return ApiResponse.<List<GiaPhongTheoNgayReponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/xuat-phieu-chi-tiet-dat-phong/{ma_booking}")
    ApiResponse<List<Map<String,Object>>> getThongTinXuatPhieuChiTietBooking(
            @PathVariable("ma_booking") String ma_booking) {
        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(chiTietBooKingService.getThongTinChiTietBooking(ma_booking))

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
            LoggerFactory.getLogger(ChiTietBooKingController.class).error("Erro load thông tin xuất phiếu đăng ký", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }

    }

  @GetMapping("/xuat-phieu-chi-tiet-dat-phong-phu-thu/{ma_booking}")
  ApiResponse<List<Map<String,Object>>> getThongTinXuatPhieuPhuThuChiTietBooking(
      @PathVariable("ma_booking") String ma_booking) {
    try {

      return ApiResponse.<List<Map<String,Object>>>builder()
        .result(chiTietBooKingService.getThongTinChiTietPhuThuBooking(ma_booking))

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
      LoggerFactory.getLogger(ChiTietBooKingController.class).error("Erro load thông tin xuất phiếu đăng ký", e);

      return ApiResponse.<List<Map<String,Object>>>builder()
        .message("Internal Server Error!")
        .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
        .build();
    }


  }


  @GetMapping("/phu-thu/{ma_booking}")
  ApiResponse<List<Map<String,Object>>> getPhuThuTreEm(
      @PathVariable("ma_booking") String ma_booking) {
    try {

      return ApiResponse.<List<Map<String,Object>>>builder()
        .result(chiTietBooKingService.getPhuThuTreEmKhiCreateBooking(ma_booking))

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
      LoggerFactory.getLogger(ChiTietBooKingController.class).error("Erro GET PHU THU TRE EM", e);

      return ApiResponse.<List<Map<String,Object>>>builder()
        .message("Internal Server Error!")
        .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
        .build();
    }


  }


}
