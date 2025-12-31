package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.repository.ChiTietBooKingRepository;
import vttu.khachsanservice.repository.HoaDonRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThongKeXuatExcelService {

    HoaDonRepository hoaDonRepository;
    private final ChiTietBooKingRepository chiTietBooKingRepository;
    private final HoaDonService hoaDonService;

    private void createCell(Row row, int column, String value, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

    private void createCellSO(Row row, int column, double value, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);       // ghi ra dạng số thực sự
        cell.setCellStyle(cellStyle);
    }

    public class DateUtils {
        // Hàm chuyển đổi ngày từ yyyy-MM-dd sang dd/MM/yyyy
        public static String convertDateFormat(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "";

            // Cắt bỏ phần microsecond thừa (giữ 3 chữ số, vd: .85152 -> .851)
            dateStr = dateStr.replaceAll("\\.(\\d{3})\\d*", ".$1");

            // Parse lại
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDateTime dateTime = LocalDateTime.parse(dateStr, inputFormatter);
            return dateTime.toLocalDate().format(outputFormatter);
        }

    }

  // Xuất excel thông kê chi tiết đặt phòng
    public ByteArrayInputStream exportChiTietDatPhong(String ma_booking, String templatePath) throws IOException {


        Workbook workbook;
        ClassPathResource classPathResource = new ClassPathResource(templatePath);
        try(InputStream templateInputStrean = classPathResource.getInputStream()) {
            if(templatePath.endsWith(".xlsx")){
                workbook = new XSSFWorkbook(templateInputStrean);
            }else if(templatePath.endsWith(".xls")){
                workbook = new HSSFWorkbook(templateInputStrean);
            }else {
                throw new IllegalArgumentException("The specified file is not Excel file");
            }

            Sheet sheet = workbook.getSheetAt(0);


            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);



            Map<String,Object> thongTinKhachHang = hoaDonService.getThongTinKhachHangBooking(ma_booking);
            List<Map<String, Object>> data = chiTietBooKingRepository.getThongTinXuatChiTietBooking(ma_booking);

            String content = "NEW BOOKING\nBK-ID: " + thongTinKhachHang.get("ma_booking").toString() + "\nNgày phát hành: " + DateUtils.convertDateFormat(thongTinKhachHang.get("thoi_gian_tao").toString());

            // Ghi content vào dòng 0, cột G (index = 6)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                headerRow = sheet.createRow(0);
            }
            Cell contentCell = headerRow.createCell(6); // Column G
            contentCell.setCellValue(content);


            Row rowNguoiLienHe = sheet.getRow(2);
            if (rowNguoiLienHe == null) {
                rowNguoiLienHe = sheet.createRow(2);
            }

            String tenKhachHang = thongTinKhachHang.get("tenkhachhang").toString() != null ? thongTinKhachHang.get("tenkhachhang").toString() : "";
            Cell cellNguoiLienHe = rowNguoiLienHe.createCell(2);
            cellNguoiLienHe.setCellValue(tenKhachHang);

            Row rowSDT = sheet.getRow(3);
            if (rowSDT == null) {
                rowSDT = sheet.createRow(3);
            }
            Cell cellSDT = rowSDT.createCell(3);
            cellSDT.setCellValue(thongTinKhachHang.get("sdt_booking").toString() != null ? thongTinKhachHang.get("sdt_booking").toString() : "");

            Row rowEmail = sheet.getRow(4);
            if (rowEmail == null) {
                rowEmail = sheet.createRow(4);
            }
            Cell cellEmail = rowEmail.createCell(4);
            cellEmail.setCellValue(thongTinKhachHang.get("email_booking").toString() != null ? thongTinKhachHang.get("email_booking").toString() : "");

            DataFormat format = workbook.createDataFormat();
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);
            numberStyle.setDataFormat(format.getFormat("#,##0"));

            // Thêm dữ liệu (từ dòng 6)
            int rowIndex = 6;
            int stt = 1;
            BigDecimal tongTien = BigDecimal.ZERO;
            for (Map<String, Object> item : data) {

                Row row = sheet.createRow(rowIndex++);

                row.setHeightInPoints(30);

                createCell(row, 0, String.valueOf(stt), cellStyle);
                createCell(row, 1, item.get("ngay_den") != null ? ThongKeService.DateUtils.convertDateFormat(item.get("ngayDen").toString()) : "", cellStyle);
                createCell(row, 2, item.get("ngay_di") != null ? ThongKeService.DateUtils.convertDateFormat(item.get("ngayDi").toString()) : "", cellStyle);
                createCell(row, 3, item.get("ten_loai_phong") != null ? item.get("ten_loai_phong").toString() : "", cellStyle);

                String content1 = "";

// Ghi chú
                String ghiChu = (String) item.getOrDefault("ghi_chu", "");

// Trường hợp "Đặt phòng": hiển thị số người lớn và trẻ em
                if ("Đặt phòng".equals(ghiChu)) {
                    int soNguoiLon = item.get("so_nguoi_lon") != null ? (int) item.get("so_nguoi_lon") : 0;
                    int soTreEm = item.get("so_tre_em") != null ? (int) item.get("so_tre_em") : 0;
                    content1 += "Người lớn: " + soNguoiLon + "\nTrẻ em (0-5 tuổi): " + soTreEm;
                }

// Nếu không phải "Đặt phòng" → kiểm tra các phụ thu
                if (!"Đặt phòng".equals(ghiChu)) {

                    // Extra bed
                    int soLuongExtraBed = item.get("so_luong_extra_bed") != null ? (int) item.get("so_luong_extra_bed") : 0;
                    double giaExtra = item.get("gia_extra") != null ? Double.parseDouble(item.get("gia_extra").toString()) : 0;
                    if (soLuongExtraBed > 0) {
                        double tongTienExtra = giaExtra * soLuongExtraBed;
                        content1 += "Phụ thu extra bed " + soLuongExtraBed + " giường\n" +
                                formatCurrency(giaExtra) + " x " + soLuongExtraBed + " = " + formatCurrency(tongTienExtra);
                    }

                    // Phụ thu người lớn
                    int soLuongPhuThuNguoiLon = item.get("so_luong_phu_thu_nguoi_lon") != null ? (int) item.get("so_luong_phu_thu_nguoi_lon") : 0;
                    double giaPhuThuNguoiLon = item.get("gia_phu_thu_nguoi_lon") != null ? Double.parseDouble(item.get("gia_phu_thu_nguoi_lon").toString()) : 0;
                    if (soLuongPhuThuNguoiLon > 0) {
                        double tongTienPhuThu = giaPhuThuNguoiLon * soLuongPhuThuNguoiLon;
                        content1 += "\nPhụ thu " + soLuongPhuThuNguoiLon + " người lớn " +
                                formatCurrency(giaPhuThuNguoiLon) + " x " + soLuongPhuThuNguoiLon + " = " + formatCurrency(tongTienPhuThu);
                    }

                    // Phụ thu trẻ em
                    int soLuongPhuThuTreEm = item.get("so_luong_phu_thu_tre_em") != null ? (int) item.get("so_luong_phu_thu_tre_em") : 0;
                    double giaPhuThuTreEm = item.get("gia_phu_thu_tre_em") != null ? Double.parseDouble(item.get("gia_phu_thu_tre_em").toString()) : 0;
                    if (soLuongPhuThuTreEm > 0) {
                        double tongTienPhuThu = giaPhuThuTreEm * soLuongPhuThuTreEm;
                        content1 += "\nPhụ thu " + soLuongPhuThuTreEm + " trẻ em " +
                                formatCurrency(giaPhuThuTreEm) + " x " + soLuongPhuThuTreEm + " = " + formatCurrency(tongTienPhuThu);
                    }
                }

// Ghi nội dung vào Excel
                createCell(row, 4, content1, cellStyle);


//                createCell(row, 4,"Người lớn:" + item.get("so_nguoi_lon").toString()  + "\n"+ "Trẻ em (0-5 tuổi):" + item.get("so_tre_em").toString(), cellStyle);

                createCellSO(row, 5,  item.get("so_dem") != null ? ((Number) item.get("so_dem")).doubleValue() : 0.0, cellStyle);

                BigDecimal donGia = (BigDecimal) item.get("gia");
                if (donGia != null) {
                    createCellSO(row, 6, donGia.doubleValue(), numberStyle);
                } else {
                    createCellSO(row, 6,0 ,numberStyle);
                }

                BigDecimal thanhTien = (BigDecimal) item.get("tong_tien");
                if (thanhTien != null) {
                    createCellSO(row, 7, thanhTien.doubleValue(), numberStyle);
                } else {
                    createCellSO(row, 7, 0, numberStyle);
                }

                tongTien = tongTien.add(thanhTien);
            }


            Row rowTongTien = sheet.getRow(16);
            if (rowTongTien == null) {
                rowTongTien = sheet.createRow(16);
            }
            createCellSO(rowTongTien, 7, tongTien.doubleValue(), numberStyle);

            Row rowTienCoc = sheet.getRow(17);
            if (rowTienCoc == null) {
                rowTienCoc = sheet.createRow(17);
            }
            
            BigDecimal tienCoc = (BigDecimal) thongTinKhachHang.get("tien_coc");
            createCellSO(rowTienCoc, 7, tienCoc.doubleValue(), numberStyle);

            Row rowTongTienCanLai = sheet.getRow(18);
            if (rowTongTienCanLai == null) {
                rowTongTienCanLai = sheet.createRow(18);
            }
            createCellSO(rowTongTienCanLai, 7, tongTien.doubleValue() - tienCoc.doubleValue(), numberStyle);

            // Xuất file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);


            return new ByteArrayInputStream(outputStream.toByteArray());
        }

    }

    private String formatCurrency(double value) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return currencyFormat.format(value);
    }


    // xuất excel thông kê chi tiết hóa đơn
    public ByteArrayInputStream exportChiTietHoaDon(String ma_booking, String templatePath) throws IOException {


        Workbook workbook;
        ClassPathResource classPathResource = new ClassPathResource(templatePath);
        try(InputStream templateInputStrean = classPathResource.getInputStream()) {
            if(templatePath.endsWith(".xlsx")){
                workbook = new XSSFWorkbook(templateInputStrean);
            }else if(templatePath.endsWith(".xls")){
                workbook = new HSSFWorkbook(templateInputStrean);
            }else {
                throw new IllegalArgumentException("The specified file is not Excel file");
            }

            Sheet sheet = workbook.getSheetAt(0);


            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);




            List<Map<String, Object>> data = hoaDonRepository.getChiTietHoaDon(ma_booking);

            String content = "NEW BOOKING\nBK-ID: " + data.getFirst().get("mabooking").toString() + "\nNgày phát hành: " + DateUtils.convertDateFormat(data.getFirst().get("thoigiantao").toString());

            // Ghi content vào dòng 0, cột G (index = 6)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                headerRow = sheet.createRow(0);
            }
            Cell contentCell = headerRow.createCell(6); // Column G
            contentCell.setCellValue(content);


            Row rowNguoiLienHe = sheet.getRow(2);
            if (rowNguoiLienHe == null) {
                rowNguoiLienHe = sheet.createRow(2);
            }
            String danhXung = data.getFirst().get("danhxung").toString() != null ? data.getFirst().get("danhxung").toString() + " " : "";
            String hoLot = data.getFirst().get("hokhachhang").toString() != null ? data.getFirst().get("hokhachhang").toString() + " " : "";
            String tenKhachHang = data.getFirst().get("tenkhachhang").toString() != null ? data.getFirst().get("tenkhachhang").toString() : "";
            Cell cellNguoiLienHe = rowNguoiLienHe.createCell(2);
            cellNguoiLienHe.setCellValue(danhXung + "." + hoLot + " " + tenKhachHang);

            Row rowSDT = sheet.getRow(3);
            if (rowSDT == null) {
                rowSDT = sheet.createRow(3);
            }
            Cell cellSDT = rowSDT.createCell(3);
            cellSDT.setCellValue(data.getFirst().get("sdtkhachhang").toString() != null ? data.getFirst().get("sdtkhachhang").toString() : "");

            Row rowEmail = sheet.getRow(4);
            if (rowEmail == null) {
                rowEmail = sheet.createRow(4);
            }
            Cell cellEmail = rowEmail.createCell(4);
            cellEmail.setCellValue(data.getFirst().get("emailkhachhang").toString() != null ? data.getFirst().get("emailkhachhang").toString() : "");

            // Thêm dữ liệu (từ dòng 6)
            int rowIndex = 6;

            BigDecimal tongTien = BigDecimal.ZERO;
            BigDecimal tongTienDichVu = BigDecimal.ZERO;

            for (Map<String, Object> item : data) {

                Row row = sheet.createRow(rowIndex++);

                row.setHeightInPoints(30);

                createCell(row, 0, item.get("stt") != null ? item.get("stt").toString() : "", cellStyle);
                createCell(row, 1, item.get("ngayden") != null ? ThongKeService.DateUtils.convertDateFormat(item.get("ngayden").toString()) : "", cellStyle);
                createCell(row, 2, item.get("ngaydi") != null ? ThongKeService.DateUtils.convertDateFormat(item.get("ngaydi").toString()) : "", cellStyle);
                createCell(row, 3, item.get("loaiphong") != null ? item.get("loaiphong").toString() : "", cellStyle);
                createCell(row, 4,  item.get("sodem") != null ? item.get("sodem").toString() : "", cellStyle);

                BigDecimal donGia = (BigDecimal) item.get("dongia");
                if (donGia != null) {
                    createCell(row, 5, String.valueOf(donGia.doubleValue()), cellStyle);
                } else {
                    createCell(row, 5, String.valueOf(0), cellStyle);
                }

                BigDecimal thanhTien = (BigDecimal) item.get("thanhtien");
                if (thanhTien != null) {
                    createCell(row, 6, String.valueOf(thanhTien.doubleValue()), cellStyle);
                } else {
                    createCell(row, 6, String.valueOf(0), cellStyle);
                }

                createCell(row, 7,  item.get("ghichu") != null ? item.get("ghichu").toString() : "", cellStyle);


                BigDecimal dichvu = (BigDecimal) item.get("tiendichvuphatsinh");

                tongTienDichVu = tongTienDichVu.add(dichvu);
                tongTien = tongTien.add(thanhTien);

            }

            Row rowTongTienDV = sheet.getRow(16);
            if (rowTongTienDV == null) {
                rowTongTienDV = sheet.createRow(16);
            }
            createCell(rowTongTienDV, 6, tongTienDichVu.toString(), cellStyle);

            Row rowTongTien = sheet.getRow(17);
            if (rowTongTien == null) {
                rowTongTien = sheet.createRow(6);
            }
            createCell(rowTongTien, 6, tongTien.toString(), cellStyle);

            Row rowTienCoc = sheet.getRow(18);
            if (rowTienCoc == null) {
                rowTienCoc = sheet.createRow(18);
            }

            BigDecimal tienCoc = (BigDecimal) data.getFirst().get("tiencoc");
            createCell(rowTienCoc, 6, tienCoc.toString(), cellStyle);

            Row rowTongTienCanLai = sheet.getRow(19);
            if (rowTongTienCanLai == null) {
                rowTongTienCanLai = sheet.createRow(19);
            }
            createCell(rowTongTienCanLai, 6, tongTien.subtract(tienCoc).add(tongTienDichVu).toString(), cellStyle);

            // Xuất file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);


            return new ByteArrayInputStream(outputStream.toByteArray());
        }

    }
}
