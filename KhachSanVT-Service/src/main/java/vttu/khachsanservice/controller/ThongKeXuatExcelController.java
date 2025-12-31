package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vttu.khachsanservice.service.ThongKeXuatExcelService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/xuat-excel")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThongKeXuatExcelController {


    ThongKeXuatExcelService thongKeXuatExcelService;

    @GetMapping("/chi-tiet-dat-phong")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam  String ma_booking
    ) throws IOException {

        String templatePath = "templates/XuatPhieuXacNhanDatPhong.xlsx";

        ByteArrayInputStream inputStream = thongKeXuatExcelService.exportChiTietDatPhong(ma_booking, templatePath);

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
    @GetMapping("/chi-tiet-hoa-don")
    public ResponseEntity<InputStreamResource> exportExcelHoDon(
            @RequestParam  String ma_booking
    ) throws IOException {

        String templatePath = "templates/MauPhieuHoaDon.xlsx";

        ByteArrayInputStream inputStream = thongKeXuatExcelService.exportChiTietHoaDon(ma_booking, templatePath);

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
}
