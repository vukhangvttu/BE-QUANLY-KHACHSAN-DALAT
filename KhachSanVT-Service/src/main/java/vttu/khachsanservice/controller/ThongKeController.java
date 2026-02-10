package vttu.khachsanservice.controller;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.exception.AppException;

import vttu.khachsanservice.service.ThongKeService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/thong-ke")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThongKeController {

    ThongKeService thongKeService;

    @GetMapping("/theo-ngay")
    ApiResponse<Map<String,Object>> allThonTinKhachHangTT(@RequestParam String ngay) {
        try {

            return ApiResponse.<Map<String,Object>>builder()
                    .result(thongKeService.getYesterdayStatistics(ngay))

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
    @GetMapping("/theo-khoan-thoi-gian")
    ApiResponse<Map<String,Object>> getThongKeTheoKhoanThoiGian(@RequestParam LocalDate ngayDen, @RequestParam LocalDate ngayDi, @RequestParam String maLoaiPhong,
                                                                @RequestParam String maNhomKhachHang, @RequestParam String maNguonKhach) {
        try {

            return ApiResponse.<Map<String,Object>>builder()
                    .result(thongKeService.getThongKeTheoKhoanThoiGian(ngayDen, ngayDi,maLoaiPhong,maNhomKhachHang,maNguonKhach))

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
            LoggerFactory.getLogger(ThongKeController.class).error("Erro load thông tin khách hàng thanh toán", e);

            return ApiResponse.<Map<String,Object>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }

    @GetMapping("/doanh-thu-theo-thang")
    ApiResponse<List<Map<String,Object>>> getThongKeDoanThuTheoThang() {
        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(thongKeService.getThongKeDoanThuTheoThang())

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
            LoggerFactory.getLogger(ThongKeController.class).error("Erro load Thống kê doanh thu theo tháng", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }

    // Sử dụng Apache POI
    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam LocalDate ngayDen, @RequestParam LocalDate ngayDi,  @RequestParam String maLoaiPhong,
            @RequestParam String maNhomKhachHang, @RequestParam String tenNhomKhachHang
    ) throws IOException {

        String templatePath = "templates/MauXuatThongKe.xlsx";

        ByteArrayInputStream inputStream = thongKeService.exportThongKeDoanhThuKhoanThoiGianTheoMaBK(ngayDen, ngayDi,maLoaiPhong,maNhomKhachHang,tenNhomKhachHang, templatePath);

        HttpHeaders headers = new HttpHeaders();
        // Lấy ngày giờ hiện tại
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        headers.add("Content-Disposition", "attachment; filename=thongke" + timestamp + ".xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }



    @GetMapping("/buong-phong")
    ApiResponse<List<Map<String,Object>>> getThongKeDoanThuBuongPhong(
            @RequestParam LocalDate ngayBatDau, @RequestParam LocalDate ngayKetThuc
    ) {
        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(thongKeService.getThongKeDoanThuBuongPhong(ngayBatDau, ngayKetThuc))

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
            LoggerFactory.getLogger(ThongKeController.class).error("Erro load Thống kê doanh thu theo tháng", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }
    @GetMapping("/export/excel/buong-phong")
    public ResponseEntity<InputStreamResource> exportExcelThongKeBuongPhong(
            @RequestParam LocalDate ngayBatDau, @RequestParam LocalDate ngayKetThuc
    ) throws IOException {

        String templatePath = "templates/XuatBaoCaoDoanhThu-Buong-NhaHang.xlsx";

        ByteArrayInputStream inputStream = thongKeService.exportThongKeDoanhThuKhoanThoiGianTheoBuongPhong(ngayBatDau, ngayKetThuc, templatePath);

        HttpHeaders headers = new HttpHeaders();
        // Lấy ngày giờ hiện tại
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        headers.add("Content-Disposition", "attachment; filename=thongke" + timestamp + ".xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }


    @GetMapping("/export/excel/thong-tin-luu-tru")
    public ResponseEntity<InputStreamResource> exportExcelKhachLuuTruKhoanThoiGian(
            @RequestParam LocalDate ngayDen, @RequestParam LocalDate ngayDi
    ) throws IOException {

        String templatePath = "templates/MauXuatCA.xlsx";

        ByteArrayInputStream inputStream = thongKeService.exportToExcelThongTinLuuTru(ngayDen, ngayDi, templatePath);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Content-Disposition", "attachment; filename=thongtinluutru.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    private LocalDate tinhNgayThongKe() {
        LocalTime now = LocalTime.now();

        // Sau 15h, 18h, 21h, 24h => +1 ngày
        if (now.isAfter(LocalTime.of(15, 0)) ||
                now.isAfter(LocalTime.of(18, 0)) ||
                now.isAfter(LocalTime.of(21, 0)) ||
                now.equals(LocalTime.MIDNIGHT)) {

            return LocalDate.now().plusDays(1);
        }

        return LocalDate.now();
    }

//    private LocalDate tinhNgayThongKe() {
//        return LocalTime.now().isBefore(LocalTime.of(15, 0))
//                ? LocalDate.now().plusDays(1)
//                : LocalDate.now();
//    }


    @GetMapping("/thong-ke-an-sang")
    ApiResponse<List<Map<String,Object>>> getThongKeAnSang() {
        try {
            LocalDate ngayThongKe = tinhNgayThongKe();

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(thongKeService.getThongKeAnSang(ngayThongKe, ngayThongKe))
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
            LoggerFactory.getLogger(ThongKeController.class).error("Error thống kê ăn sáng", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }


    }

    @GetMapping("/danh-sach-phong-an-sang")
    ApiResponse<List<Map<String,Object>>> getDanhSachPhongAnSang() {
        try {

            LocalDate ngayThongKe = tinhNgayThongKe();

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(thongKeService.getPhongAnSang(ngayThongKe, ngayThongKe))

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
            LoggerFactory.getLogger(ThongKeController.class).error("Error thống kê ăn sáng", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/khach-luu-tru-theo-quy")
    ApiResponse<List<Map<String,Object>>> getThongKeKhachLuuTruTheoQuy() {
        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(thongKeService.getThongKeKhachLuuTruQuy())
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
            LoggerFactory.getLogger(ThongKeController.class).error("Error thống kê khách lưu trú", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }

    @GetMapping("/khach-hang-theo-ngay")
    ApiResponse<List<Map<String,Object>>> getThongKeKhachHangTheoNgay(@RequestParam LocalDate ngayBD, @RequestParam LocalDate ngayKT) {
        try {

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .result(thongKeService.getThongKeKhachHangTheoNgay(ngayBD, ngayKT))
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
            LoggerFactory.getLogger(ThongKeController.class).error("Error thống kê khách theo ngày", e);

            return ApiResponse.<List<Map<String,Object>>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
