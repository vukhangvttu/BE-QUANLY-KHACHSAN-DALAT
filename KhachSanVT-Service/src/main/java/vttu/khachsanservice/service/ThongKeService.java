package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.HoaDonRepository;
import vttu.khachsanservice.repository.KhachHangPhongRepository;
import vttu.khachsanservice.repository.XepPhongRepository;
import vttu.khachsanservice.utils.DateConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThongKeService {

    HoaDonRepository hoaDonRepository;
    private final XepPhongRepository xepPhongRepository;
    private final KhachHangPhongRepository khachHangPhongRepository;
    // Lấy thống kê ngày hiện tại
//    public Map<String, Object> getTodayStatistics() {
//        return hoaDonRepository.getDailyStatistics(LocalDate.now());
//    }

    // Lấy thống kê sau 1 ngày (hôm qua)
    public Map<String, Object> getYesterdayStatistics(String ngay) {
        return hoaDonRepository.getDailyStatisticsWithInterval(ngay);
    }

    public Map<String, Object> getThongKeTheoKhoanThoiGian(LocalDate ngayDen, LocalDate ngayDi, String loaiPhong, String nhomKhachHang, String loaiKhachHang) {
        if(loaiPhong.isEmpty())
            return null;
        else {

            return hoaDonRepository.getThongKeLoaiPhongKhoanThoiGian(ngayDen, ngayDi,loaiPhong,nhomKhachHang,loaiKhachHang);
        }

    }
    public List<Map<String, Object>> getThongKeDoanThuTheoThang() {
        return hoaDonRepository.getThongKeDoanThuTheoThang();
    }


    public List<Map<String, Object>> getThongKeDoanThuBuongPhong(LocalDate ngayBD, LocalDate ngayKT) {
        return hoaDonRepository.getThongKePhongExcelKhoanThoiGianBuongPhong(ngayBD,ngayKT);
    }


    public ByteArrayInputStream exportThongKeDoanhThuKhoanThoiGianTheoMaBK(LocalDate ngayDen, LocalDate ngayDi, String loaiPhong,String nhomKhachHang, String loaiKhachHang, String templatePath) throws IOException {


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

            Sheet sheet = workbook.getSheetAt(1);


            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);


            // THÊM DỮ LIỆU DÒNG 1
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 14);

// Tạo CellStyle có font in đậm và căn giữa ngang + dọc
            CellStyle style = workbook.createCellStyle();
            style.setFont(boldFont);

            style.setAlignment(HorizontalAlignment.CENTER);       // Căn giữa ngang
            style.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc (nếu cần)

// Tạo nội dung báo cáo
            String content = "BÁO CÁO DOANH THU THEO BOOKING - ID\n" +
                    "Từ ngày " + DateConverter.converDate(ngayDen) +
                    " Đến ngày " + DateConverter.converDate(ngayDi);

// Ghi vào dòng 1, cột 0
            Row headerRow = sheet.getRow(1);
            if (headerRow == null) {
                headerRow = sheet.createRow(1);
            }
            Cell contentCell = headerRow.createCell(0);
            contentCell.setCellValue(content);
            contentCell.setCellStyle(style);

            List<Map<String, Object>> data = hoaDonRepository.getThongKeLoaiPhongExcelKhoanThoiGian(ngayDen,ngayDi,nhomKhachHang,loaiKhachHang,loaiPhong);

            BigDecimal sumTongTienPhong = BigDecimal.ZERO; // Tổng tiền phòng
            BigDecimal sumTongChiecKhau = BigDecimal.ZERO; // Tổng chiết khấu
            BigDecimal sumTongPhuThuGiuong = BigDecimal.ZERO;// Tổng phụ thu giường
            BigDecimal sumTongPhuThuAnSang = BigDecimal.ZERO;
            BigDecimal sumTongPhuThuTreEm = BigDecimal.ZERO;// Tổng phụ thu trẻ em
            BigDecimal sumTongPhuThuNguoiLon = BigDecimal.ZERO;// Tổng phụ thu người lớn
            BigDecimal sumTongTienDichVuMinibar = BigDecimal.ZERO;// Tổng tiền dịch vụ minibar
            BigDecimal sumTongTienDichVuGiatUi = BigDecimal.ZERO;// Tổng tiền dịch vụ giặt-ui
            BigDecimal sumTongTienDichVuKhac = BigDecimal.ZERO;// Tổng tiền dịch vụ khác
            BigDecimal sumTongCongThanhToan = BigDecimal.ZERO;// Tổng thanh toán
            BigDecimal sumTongTienCoc = BigDecimal.ZERO;// Tổng tiền cọc
            BigDecimal sumTongTienCuoi = BigDecimal.ZERO;// Tổng tiền cuối cùng

            // Bổ sung 27-10
            BigDecimal sunTongTienPhuThuCheckInSom = BigDecimal.ZERO;
            BigDecimal sunTongTienPhuThuCheckOutTre = BigDecimal.ZERO;
            // Thêm dữ liệu (từ dòng 4)
            DataFormat format = workbook.createDataFormat();
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);
            numberStyle.setDataFormat(format.getFormat("#,##0")); // không phần thập phân

            int rowIndex = 5;
            for (Map<String, Object> item : data) {

                Row row = sheet.createRow(rowIndex++);

                createCell(row, 0, item.get("stt") != null ? item.get("stt").toString() : "", cellStyle);
                createCell(row, 1, item.get("thoi_gian_tao") != null ? item.get("thoi_gian_tao").toString() : "", cellStyle);
                createCell(row, 2, item.get("nguoi_tao") != null ? item.get("nguoi_tao").toString() : "", cellStyle);
                createCell(row, 3, item.get("ma_booking") != null ? item.get("ma_booking").toString() : "", cellStyle);
                createCell(row, 4, item.get("ten_nhom_khach_hang") != null ? item.get("ten_nhom_khach_hang").toString() : "", cellStyle);
                createCell(row, 5,  item.get("loai_nguon_khach") != null ? item.get("loai_nguon_khach").toString() : "", cellStyle);
                createCell(row, 6,  item.get("danh_sach_phong") != null ? item.get("danh_sach_phong").toString() : "", cellStyle);
                createCell(row, 7,  item.get("danh_sach_loai_phong") != null ? item.get("danh_sach_loai_phong").toString() : "", cellStyle);
                createCell(row, 8, item.get("ngay_den") != null ? DateUtils.convertDateFormat(item.get("ngay_den").toString()) : "", cellStyle);
                createCell(row, 9, item.get("ngay_di") != null ? DateUtils.convertDateFormat(item.get("ngay_di").toString()) : "", cellStyle);



                BigDecimal giaPhong = (BigDecimal) item.get("gia_phong");
                if (giaPhong != null) {
                  createCellSO(row, 10, giaPhong.doubleValue(), numberStyle);
                    sumTongTienPhong = sumTongTienPhong.add(giaPhong);
                } else {
                  createCellSO(row, 10, 0, numberStyle);
                }


                BigDecimal tongGiaPhong = (BigDecimal) item.get("tong_gia_phong");

                if (tongGiaPhong != null) {
                  createCellSO(row, 11, tongGiaPhong.doubleValue(), numberStyle);
                  sumTongChiecKhau = sumTongChiecKhau.add(tongGiaPhong);

                } else {
                  createCellSO(row, 11, 0, numberStyle);
                }
                createCell(row, 12,  item.get("danh_sach_ghi_chu") != null ? item.get("danh_sach_ghi_chu").toString() : "", cellStyle);

                BigDecimal tongPhuThuGiuong = (BigDecimal) item.get("tong_phu_thu_giuong");
                if (tongPhuThuGiuong != null) {
                    createCellSO(row, 13, tongPhuThuGiuong.doubleValue(), numberStyle);
                    sumTongPhuThuGiuong = sumTongPhuThuGiuong.add(tongPhuThuGiuong);

                } else {
                    createCellSO(row, 13, 0, numberStyle);
                }

                BigDecimal tongPhuThuAnSang = (BigDecimal) item.get("tong_phu_thu_an_sang");
                if (tongPhuThuAnSang != null) {
                    createCellSO(row, 14, tongPhuThuAnSang.doubleValue(), numberStyle);
                    sumTongPhuThuAnSang = sumTongPhuThuAnSang.add(tongPhuThuAnSang);
                } else {
                    createCellSO(row, 14, 0, numberStyle);
                }

                BigDecimal tongPhuThuTreEm = (BigDecimal) item.get("tong_phu_thu_tre_em");
                if (tongPhuThuTreEm != null) {
                    createCellSO(row, 15, tongPhuThuTreEm.doubleValue(), numberStyle);
                    sumTongPhuThuTreEm = sumTongPhuThuTreEm.add(tongPhuThuTreEm);
                } else {
                    createCellSO(row, 15, 0, numberStyle);
                }

                BigDecimal tongPhuThuNguoiLon = (BigDecimal) item.get("tong_phu_thu_nguoi_lon");
                if (tongPhuThuNguoiLon != null) {
                    createCellSO(row, 16, tongPhuThuNguoiLon.doubleValue(), numberStyle);
                    sumTongPhuThuNguoiLon = sumTongPhuThuNguoiLon.add(tongPhuThuNguoiLon);
                } else {
                    createCellSO(row, 16, 0, numberStyle);
                }

                BigDecimal tongTienDichVuMinibar = (BigDecimal) item.get("tong_tien_dich_vu_minibar");
                if (tongTienDichVuMinibar != null) {
                    createCellSO(row, 17, tongTienDichVuMinibar.doubleValue(), numberStyle);
                    sumTongTienDichVuMinibar = sumTongTienDichVuMinibar.add(tongTienDichVuMinibar);
                } else {
                    createCellSO(row, 17, 0, numberStyle);
                }

                BigDecimal tongTienDichVuGiatUi = (BigDecimal) item.get("tong_tien_dich_vu_giatui");
                if (tongTienDichVuGiatUi != null) {
                    createCellSO(row, 18, tongTienDichVuGiatUi.doubleValue(), numberStyle);
                    sumTongTienDichVuGiatUi = sumTongTienDichVuGiatUi.add(tongTienDichVuGiatUi);
                } else {
                    createCellSO(row, 18, 0, numberStyle);
                }


                BigDecimal tienDichVuKhac = (BigDecimal) item.get("tong_tien_dich_vu_khac");
                if (tienDichVuKhac != null) {
                    createCellSO(row, 19, tienDichVuKhac.doubleValue(), numberStyle);
                    sumTongTienDichVuKhac = sumTongTienDichVuKhac.add(tienDichVuKhac);
                } else {
                    createCellSO(row, 19, 0, numberStyle);
                }

                createCellSO(row, 20,  0, numberStyle);

                BigDecimal tienPhuThuCheckInSom = (BigDecimal) item.get("tong_phu_thu_check_in_som");
                if (tienPhuThuCheckInSom != null) {
                    createCellSO(row, 21, tienPhuThuCheckInSom.doubleValue(), numberStyle);
                    sunTongTienPhuThuCheckInSom = sunTongTienPhuThuCheckInSom.add(tienPhuThuCheckInSom);
                } else {
                    createCellSO(row, 21, 0, numberStyle);
                }

                BigDecimal tienPhuThuCheckOutTre = (BigDecimal) item.get("tong_phu_thu_check_out_tre");
                if (tienPhuThuCheckOutTre != null) {
                    createCellSO(row, 22, tienPhuThuCheckOutTre.doubleValue(), numberStyle);
                    sunTongTienPhuThuCheckOutTre = sunTongTienPhuThuCheckOutTre.add(tienPhuThuCheckOutTre);
                } else {
                    createCellSO(row, 22, 0, numberStyle);
                }


                BigDecimal tongThanhToan = (BigDecimal) item.get("tong_tien");
                BigDecimal tienCoc = (BigDecimal) item.get("tien_coc");
                BigDecimal tongAddTienCoc = tongThanhToan.add(tienCoc);

                createCellSO(row, 23, tongAddTienCoc.doubleValue(), numberStyle);
                sumTongCongThanhToan = sumTongCongThanhToan.add(tongAddTienCoc);


                if (tienCoc != null) {
                    createCellSO(row, 24, tienCoc.doubleValue(), numberStyle);
                    sumTongTienCoc = sumTongTienCoc.add(tienCoc);
                } else {
                    createCellSO(row, 24, 0, numberStyle);
                }

                BigDecimal tongTienCuoi = tongAddTienCoc.subtract(tienCoc);
                createCellSO(row, 25,  tongTienCuoi.doubleValue(), numberStyle);
                sumTongTienCuoi = sumTongTienCuoi.add(tongTienCuoi);


            }

            // Tạo dòng hiển thị tổng (nằm ngay sau dòng dữ liệu cuối)
            Row totalRow1 = sheet.createRow(rowIndex);
            totalRow1.setHeightInPoints(25);
// Gộp ô từ cột 0 đến 9 để ghi chữ "Tổng tiền"
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 9));

            Cell totalLabelCell2 = totalRow1.createCell(0);


            // Style cho ô tiêu đề
            CellStyle style2 = workbook.createCellStyle();
            Font font2 = workbook.createFont();
            font2.setBold(true);
            font2.setFontHeightInPoints((short) 12);
            style2.setFont(font2);
            style2.setAlignment(HorizontalAlignment.CENTER);
            style2.setVerticalAlignment(VerticalAlignment.CENTER);
            style2.setBorderTop(BorderStyle.THIN);
            style2.setBorderBottom(BorderStyle.THIN);
            style2.setBorderLeft(BorderStyle.THIN);
            style2.setBorderRight(BorderStyle.THIN);

            totalLabelCell2.setCellStyle(style2);

            // Gán nội dung và style cho từng ô trong vùng gộp
            for (int col = 0; col <= 9; col++) {
                Cell cell = totalRow1.createCell(col);
                if (col == 0) {
                    cell.setCellValue("Tổng tiền"); // Gán text ở ô đầu tiên
                }
                cell.setCellStyle(style2); // Gán style có border cho tất cả ô
            }

            createCellSO(totalRow1, 10, sumTongTienPhong.doubleValue(), numberStyle);
            createCellSO(totalRow1, 11, sumTongChiecKhau.doubleValue(), numberStyle);
            createCell(totalRow1, 12, "" , cellStyle);
            createCellSO(totalRow1, 13, sumTongPhuThuGiuong.doubleValue(), numberStyle);
            createCellSO(totalRow1, 14, sumTongPhuThuAnSang.doubleValue(), numberStyle);
            createCellSO(totalRow1, 15, sumTongPhuThuTreEm.doubleValue(), numberStyle);
            createCellSO(totalRow1, 16, sumTongPhuThuNguoiLon.doubleValue(), numberStyle);
            createCellSO(totalRow1, 17, sumTongTienDichVuMinibar.doubleValue(), numberStyle);
            createCellSO(totalRow1, 18, sumTongTienDichVuGiatUi.doubleValue(), numberStyle);
            createCellSO(totalRow1, 19, sumTongTienDichVuKhac.doubleValue(), numberStyle);
            createCellSO(totalRow1, 20, 0, cellStyle);
            createCellSO(totalRow1, 21, sunTongTienPhuThuCheckInSom.doubleValue(), cellStyle);
            createCellSO(totalRow1, 22, sunTongTienPhuThuCheckOutTre.doubleValue(), cellStyle);
            createCellSO(totalRow1, 23, sumTongCongThanhToan.doubleValue(), numberStyle);
            createCellSO(totalRow1, 24, sumTongTienCoc.doubleValue(), numberStyle);
            createCellSO(totalRow1, 25, sumTongTienCuoi.doubleValue(), numberStyle);


            Sheet sheet0 = workbook.getSheetAt(0);

            // THÊM DỮ LIỆU DÒNG 1
// Tạo nội dung báo cáo
            String content1 = "BÁO CÁO DOANH THU THEO PHÒNG\n" +
                    "Từ ngày " + DateConverter.converDate(ngayDen) +
                    " Đến ngày " + DateConverter.converDate(ngayDi);

// Ghi vào dòng 1, cột 0
            Row headerRow1 = sheet0.getRow(1);
            if (headerRow1 == null) {
                headerRow1 = sheet0.createRow(1);
            }
            Cell contentCell1 = headerRow1.createCell(0);
            contentCell1.setCellValue(content1);
            contentCell1.setCellStyle(style);

            List<Map<String, Object>> data0 = hoaDonRepository.getThongKePhongExcelKhoanThoiGian(ngayDen,ngayDi,nhomKhachHang,loaiKhachHang,loaiPhong);

            BigDecimal sumTongTienPhong1 = BigDecimal.ZERO; // Tổng tiền phòng
            BigDecimal sumTongChiecKhau1 = BigDecimal.ZERO; // Tổng chiết khấu
            BigDecimal sumTongPhuThuGiuong1 = BigDecimal.ZERO;// Tổng phụ thu giường
            BigDecimal sumTongPhuThuAnSang1 = BigDecimal.ZERO;
            BigDecimal sumTongPhuThuTreEm1 = BigDecimal.ZERO;// Tổng phụ thu trẻ em
            BigDecimal sumTongTienDichVuMinibar1 = BigDecimal.ZERO;// Tổng tiền dịch vụ minibar
            BigDecimal sumTongTienDichVuGiatUi1 = BigDecimal.ZERO;// Tổng tiền dịch vụ giặt-ui
            BigDecimal sumTongTienDichVuKhac1 = BigDecimal.ZERO;// Tổng tiền dịch vụ khác
            BigDecimal sumTongCongThanhToan1 = BigDecimal.ZERO;// Tổng thanh toán
            BigDecimal sumTongPhuThuNguoiLon1 = BigDecimal.ZERO;// Tổng phụ thu người lớn

            // Bổ sung 27-10
            BigDecimal sunTongTienPhuThuCheckInSom1 = BigDecimal.ZERO;
            BigDecimal sunTongTienPhuThuCheckOutTre1 = BigDecimal.ZERO;

            // Thêm dữ liệu (từ dòng 4)
            int rowIndex0 = 5;
            for (Map<String, Object> item : data0) {

                Row row = sheet0.createRow(rowIndex0++);

                createCell(row, 0, item.get("stt") != null ? item.get("stt").toString() : "", cellStyle);
                createCell(row, 1, item.get("thoi_gian_tao") != null ? item.get("thoi_gian_tao").toString() : "", cellStyle);
                createCell(row, 2, item.get("nguoi_tao") != null ? item.get("nguoi_tao").toString() : "", cellStyle);
                createCell(row, 3, item.get("ma_booking") != null ? item.get("ma_booking").toString() : "", cellStyle);
                createCell(row, 4, item.get("ten_nhom_khach_hang") != null ? item.get("ten_nhom_khach_hang").toString() : "", cellStyle);
                createCell(row, 5,  item.get("loai_nguon_khach") != null ? item.get("loai_nguon_khach").toString() : "", cellStyle);
                createCell(row, 6,  item.get("ma_phong") != null ? item.get("ma_phong").toString() : "", cellStyle);
                createCell(row, 7,  item.get("ten_loai_phong") != null ? item.get("ten_loai_phong").toString() : "", cellStyle);
                createCell(row, 8, item.get("ngay_den") != null ? DateUtils.convertDateFormat(item.get("ngay_den").toString()) : "", cellStyle);
                createCell(row, 9, item.get("ngay_di") != null ? DateUtils.convertDateFormat(item.get("ngay_di").toString()) : "", cellStyle);

                BigDecimal giaPhong = (BigDecimal) item.get("gia_phong");
                if (giaPhong != null) {
                    createCellSO(row, 10, giaPhong.doubleValue(), numberStyle);
                    sumTongTienPhong1 = sumTongTienPhong1.add(giaPhong);
                } else {
                    createCellSO(row, 10, 0, numberStyle);
                }

                BigDecimal tienPhong = (BigDecimal) item.get("tong_tien_phong_bk");
                if (tienPhong != null) {
                    createCellSO(row, 11, tienPhong.doubleValue(), numberStyle);
                    sumTongChiecKhau1 = sumTongChiecKhau1.add(tienPhong);
                } else {
                    createCellSO(row, 11, 0, numberStyle);
                }

                createCell(row, 12,  item.get("ghi_chu") != null ? item.get("ghi_chu").toString() : "", cellStyle);

                BigDecimal tienPhuThuGiuong = (BigDecimal) item.get("phu_thu_giuong");
                if (tienPhuThuGiuong != null) {
                    createCellSO(row, 13, tienPhuThuGiuong.doubleValue(), numberStyle);
                    sumTongPhuThuGiuong1 = sumTongPhuThuGiuong1.add(tienPhuThuGiuong);
                } else {
                    createCellSO(row, 13, 0, numberStyle);
                }

                BigDecimal tienPhuThuAnSang = (BigDecimal) item.get("tong_phu_thu_an_sang");
                if (tienPhuThuAnSang != null) {
                    createCellSO(row, 14, tienPhuThuAnSang.doubleValue(), numberStyle);
                    sumTongPhuThuAnSang1 = sumTongPhuThuAnSang1.add(tienPhuThuAnSang);
                } else {
                    createCellSO(row, 14, 0, numberStyle);
                }

                BigDecimal tienPhuThuTreEm = (BigDecimal) item.get("phu_thu_tre_em");
                if (tienPhuThuTreEm != null) {
                    createCellSO(row, 15, tienPhuThuTreEm.doubleValue(), numberStyle);
                    sumTongPhuThuTreEm1 = sumTongPhuThuTreEm1.add(tienPhuThuTreEm);
                } else {
                    createCellSO(row, 15, 0, numberStyle);
                }

                BigDecimal tienPhuThuNguoiLon = (BigDecimal) item.get("phu_thu_nguoi_lon");
                if (tienPhuThuNguoiLon != null) {
                    createCellSO(row, 16, tienPhuThuNguoiLon.doubleValue(), numberStyle);
                    sumTongPhuThuTreEm1 = sumTongPhuThuTreEm1.add(tienPhuThuTreEm);
                } else {
                    createCellSO(row, 16, 0, numberStyle);
                }

                BigDecimal tienDichVuMinibar = (BigDecimal) item.get("tien_dich_vu_minibar");
                if (tienDichVuMinibar != null) {
                    createCellSO(row, 17, tienDichVuMinibar.doubleValue(), numberStyle);
                    sumTongTienDichVuMinibar1 = sumTongTienDichVuMinibar1.add(tienDichVuMinibar);
                } else {
                    createCellSO(row, 17, 0, numberStyle);
                }

                BigDecimal tienDicVuGiatUi = (BigDecimal) item.get("tien_dich_vu_giatui");
                if (tienDicVuGiatUi != null) {
                    createCellSO(row, 18, tienDicVuGiatUi.doubleValue(), numberStyle);
                    sumTongTienDichVuGiatUi1 = sumTongTienDichVuGiatUi1.add(tienDicVuGiatUi);
                } else {
                    createCellSO(row, 18, 0, numberStyle);
                }


                BigDecimal tienDichVu = (BigDecimal) item.get("tien_dich_vu_khac");
                if (tienDichVu != null) {
                    createCellSO(row, 19, tienDichVu.doubleValue(), numberStyle);
                    sumTongTienDichVuKhac1 = sumTongTienDichVuKhac1.add(tienDichVu);
                } else {
                    createCellSO(row, 19, 0, numberStyle);
                }

                createCell(row, 20,  "0", cellStyle);

                BigDecimal tienPhuThuChecInSom = (BigDecimal) item.get("phu_thu_check_in_som");
                if (tienDichVu != null) {
                    createCellSO(row, 21, tienPhuThuChecInSom.doubleValue(), numberStyle);
                    sunTongTienPhuThuCheckInSom1 = sunTongTienPhuThuCheckInSom1.add(tienPhuThuChecInSom);
                } else {
                    createCellSO(row, 21, 0, numberStyle);
                }

                BigDecimal tienPhuThuChecOutTre = (BigDecimal) item.get("phu_thu_check_out_tre");
                if (tienDichVu != null) {
                    createCellSO(row, 22, tienPhuThuChecOutTre.doubleValue(), numberStyle);
                    sunTongTienPhuThuCheckOutTre1 = sunTongTienPhuThuCheckOutTre1.add(tienPhuThuChecOutTre);
                } else {
                    createCellSO(row, 22, 0, numberStyle);
                }

                BigDecimal tongThanhToan = (BigDecimal) item.get("tong_thanh_toan");
                if (tongThanhToan != null) {
                    createCellSO(row, 23, tongThanhToan.doubleValue(), numberStyle);
                    sumTongCongThanhToan1 = sumTongCongThanhToan1.add(tongThanhToan);
                } else {
                    createCellSO(row, 23,0, numberStyle);
                }

            }


            // Tạo dòng hiển thị tổng (nằm ngay sau dòng dữ liệu cuối)
            Row totalRow = sheet0.createRow(rowIndex0);
            totalRow.setHeightInPoints(25);
// Gộp ô từ cột 0 đến 9 để ghi chữ "Tổng tiền"
            sheet0.addMergedRegion(new CellRangeAddress(rowIndex0, rowIndex0, 0, 9));

            Cell totalLabelCell = totalRow.createCell(0);


            // Style cho ô tiêu đề
            CellStyle style1 = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 12);
            style1.setFont(font);
            style1.setAlignment(HorizontalAlignment.CENTER);
            style1.setVerticalAlignment(VerticalAlignment.CENTER);
            style1.setBorderTop(BorderStyle.THIN);
            style1.setBorderBottom(BorderStyle.THIN);
            style1.setBorderLeft(BorderStyle.THIN);
            style1.setBorderRight(BorderStyle.THIN);

            totalLabelCell.setCellStyle(style1);

            // Gán nội dung và style cho từng ô trong vùng gộp
            for (int col = 0; col <= 9; col++) {
                Cell cell = totalRow.createCell(col);
                if (col == 0) {
                    cell.setCellValue("Tổng tiền"); // Gán text ở ô đầu tiên
                }
                cell.setCellStyle(style1); // Gán style có border cho tất cả ô
            }

            createCellSO(totalRow, 10, sumTongTienPhong1.doubleValue(), numberStyle);
            createCellSO(totalRow, 11, sumTongChiecKhau1.doubleValue(), numberStyle);
            createCell(totalRow, 12, "", cellStyle);
            createCellSO(totalRow, 13, sumTongPhuThuGiuong1.doubleValue(), numberStyle);
            createCellSO(totalRow, 14, sumTongPhuThuAnSang1.doubleValue(), numberStyle);
            createCellSO(totalRow, 15, sumTongPhuThuTreEm1.doubleValue(), numberStyle);
            createCellSO(totalRow, 16, sumTongPhuThuNguoiLon1.doubleValue(), numberStyle);
            createCellSO(totalRow, 17, sumTongTienDichVuMinibar1.doubleValue(), numberStyle);
            createCellSO(totalRow, 18, sumTongTienDichVuGiatUi1.doubleValue(), numberStyle);
            createCellSO(totalRow, 19, sumTongTienDichVuKhac1.doubleValue(), numberStyle);
            createCell(totalRow, 20, "0", cellStyle);
            createCellSO(totalRow, 21, sunTongTienPhuThuCheckOutTre1.doubleValue(), numberStyle);
            createCellSO(totalRow, 22, sunTongTienPhuThuCheckInSom1.doubleValue(), numberStyle);
            createCellSO(totalRow, 23, sumTongCongThanhToan1.doubleValue(), numberStyle);

            // Xuất file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);


            return new ByteArrayInputStream(outputStream.toByteArray());
        }

    }

    public ByteArrayInputStream exportThongKeDoanhThuKhoanThoiGianTheoBuongPhong(LocalDate ngayDen, LocalDate ngayDi, String templatePath) throws IOException {


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


            // THÊM DỮ LIỆU DÒNG 1
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 14);

// Tạo CellStyle có font in đậm và căn giữa ngang + dọc
            CellStyle style = workbook.createCellStyle();
            style.setFont(boldFont);

            style.setAlignment(HorizontalAlignment.CENTER);       // Căn giữa ngang
            style.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc (nếu cần)

// Tạo nội dung báo cáo
            String content = "BÁO CÁO DOANH THU MINI BAR VÀ PHỤ KHÁC\n" +
                    "Từ ngày " + DateConverter.converDate(ngayDen) +
                    " Đến ngày " + DateConverter.converDate(ngayDi);

// Ghi vào dòng 1, cột 0
            Row headerRow = sheet.getRow(1);
            if (headerRow == null) {
                headerRow = sheet.createRow(1);
            }
            Cell contentCell = headerRow.createCell(0);
            contentCell.setCellValue(content);
            contentCell.setCellStyle(style);

            List<Map<String, Object>> data = hoaDonRepository.getThongKePhongExcelKhoanThoiGianBuongPhong(ngayDen,ngayDi);

            BigDecimal sumTongTienNuocSuoi = BigDecimal.ZERO;
            BigDecimal sumTongTienNuocNgot = BigDecimal.ZERO;
            BigDecimal sumTongTienBia= BigDecimal.ZERO;//
            BigDecimal sumTongTienDichVuGiatUi = BigDecimal.ZERO;
            BigDecimal sumTongTienDichVuConLai = BigDecimal.ZERO;
            BigDecimal sumTongTienXuLyMui = BigDecimal.ZERO;
            BigDecimal sumTongTienXuLyDenBu = BigDecimal.ZERO;
            BigDecimal sumTongCong = BigDecimal.ZERO;// Tổng thanh toán

            // Thêm dữ liệu (từ dòng 4)
            DataFormat format = workbook.createDataFormat();
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);
            numberStyle.setDataFormat(format.getFormat("#,##0")); // không phần thập phân

            int rowIndex = 5;
            for (Map<String, Object> item : data) {

                Row row = sheet.createRow(rowIndex++);

                createCell(row, 0, item.get("stt") != null ? item.get("stt").toString() : "", cellStyle);
                createCell(row, 1, item.get("thoi_gian_tao") != null ? item.get("thoi_gian_tao").toString() : "", cellStyle);
                createCell(row, 2, item.get("nguoi_tao") != null ? item.get("nguoi_tao").toString() : "", cellStyle);
                createCell(row, 3, item.get("ma_booking") != null ? item.get("ma_booking").toString() : "", cellStyle);
                createCell(row, 4, item.get("ten_nhom_khach_hang") != null ? item.get("ten_nhom_khach_hang").toString() : "", cellStyle);
                createCell(row, 5,  item.get("loai_nguon_khach") != null ? item.get("loai_nguon_khach").toString() : "", cellStyle);
                createCell(row, 6,  item.get("ma_phong") != null ? item.get("ma_phong").toString() : "", cellStyle);
                createCell(row, 7,  item.get("ten_loai_phong") != null ? item.get("ten_loai_phong").toString() : "", cellStyle);

                createCell(row, 8, item.get("ngay_den") != null ? DateUtils.convertDateFormat(item.get("ngay_den").toString()) : "", cellStyle);
                createCell(row, 9, item.get("ngay_di") != null ? DateUtils.convertDateFormat(item.get("ngay_di").toString()) : "", cellStyle);



                BigDecimal tienDichVuNuocSuoi = (BigDecimal) item.get("tien_dich_vu_nuoc_suoi");
                if (tienDichVuNuocSuoi != null) {
                    createCellSO(row, 10, tienDichVuNuocSuoi.doubleValue(), numberStyle);
                    sumTongTienNuocSuoi = sumTongTienNuocSuoi.add(tienDichVuNuocSuoi);
                } else {
                    createCellSO(row, 10, 0, numberStyle);
                }


                BigDecimal tienDichVuNuocNgot = (BigDecimal) item.get("tien_dich_vu_nuoc_ngot");

                if (tienDichVuNuocNgot != null) {
                    createCellSO(row, 11, tienDichVuNuocNgot.doubleValue(), numberStyle);
                    sumTongTienNuocNgot = sumTongTienNuocNgot.add(tienDichVuNuocNgot);

                } else {
                    createCellSO(row, 11, 0, numberStyle);
                }

                BigDecimal tienDichVuBia = (BigDecimal) item.get("tien_dich_vu_bia");
                if (tienDichVuBia != null) {
                    createCellSO(row, 12, tienDichVuBia.doubleValue(), numberStyle);
                    sumTongTienBia = sumTongTienBia.add(tienDichVuBia);

                } else {
                    createCellSO(row, 12, 0, numberStyle);
                }


                BigDecimal tienDichVuConLai = (BigDecimal) item.get("tong_dich_vu_con_lai");
                if (tienDichVuConLai != null) {
                    createCellSO(row, 13, tienDichVuConLai.doubleValue(), numberStyle);
                    sumTongTienDichVuConLai = sumTongTienDichVuConLai.add(tienDichVuConLai);
                } else {
                    createCellSO(row, 13, 0, numberStyle);
                }

                BigDecimal tienDichVuGiatui = (BigDecimal) item.get("tien_dich_vu_giatui");
                if (tienDichVuGiatui != null) {
                    createCellSO(row, 14, tienDichVuGiatui.doubleValue(), numberStyle);
                    sumTongTienDichVuGiatUi = sumTongTienDichVuGiatUi.add(tienDichVuGiatui);
                } else {
                    createCellSO(row, 14, 0, numberStyle);
                }


                BigDecimal tienPhatSinhXuLyMui = (BigDecimal) item.get("tien_phat_sinh_xu_ly_mui");
                if (tienPhatSinhXuLyMui != null) {
                    createCellSO(row, 15, tienPhatSinhXuLyMui.doubleValue(), numberStyle);
                    sumTongTienXuLyMui = sumTongTienXuLyMui.add(tienPhatSinhXuLyMui);
                } else {
                    createCellSO(row, 15, 0, numberStyle);
                }



                BigDecimal tienPhatSinhXuLyDenBu = (BigDecimal) item.get("tien_phat_sinh_xu_ly_den_bu");

                if (tienPhatSinhXuLyDenBu != null) {
                    createCellSO(row, 16, tienPhatSinhXuLyDenBu.doubleValue(), numberStyle);
                    sumTongTienXuLyDenBu = sumTongTienXuLyDenBu.add(tienPhatSinhXuLyDenBu);
                } else {
                    createCellSO(row, 16, 0, numberStyle);
                }

                createCellSO(row, 17, 0, numberStyle);
                createCellSO(row, 18, 0, numberStyle);
                createCellSO(row, 19, 0, numberStyle);

                BigDecimal tongCong = (BigDecimal) item.get("tong_cong");

                if (tongCong != null) {
                    createCellSO(row, 20, tongCong.doubleValue(), numberStyle);
                    sumTongCong = sumTongCong.add(tongCong);
                } else {
                    createCellSO(row, 20, 0, numberStyle);
                }
            }

            Sheet sheet1 = workbook.getSheetAt(1);

            // THÊM DỮ LIỆU DÒNG 1
// Tạo nội dung báo cáo
            String content1 = "BÁO CÁO CÁC LOẠI PHÒNG ĐÃ SỬ DỤNG\n" +
                    "Từ ngày " + DateConverter.converDate(ngayDen) +
                    " Đến ngày " + DateConverter.converDate(ngayDi);

// Ghi vào dòng 1, cột 0
            Row headerRow1 = sheet1.getRow(1);
            if (headerRow1 == null) {
                headerRow1 = sheet1.createRow(1);
            }
            Cell contentCell1 = headerRow1.createCell(0);
            contentCell1.setCellValue(content1);
            contentCell1.setCellStyle(style);

            int rowIndex1 = 5;

            for (Map<String, Object> item : data) {

                Row row = sheet1.createRow(rowIndex1++);

                createCell(row, 0, item.get("stt") != null ? item.get("stt").toString() : "", cellStyle);
                createCell(row, 1, item.get("thoi_gian_tao") != null ? item.get("thoi_gian_tao").toString() : "", cellStyle);
                createCell(row, 2, item.get("nguoi_tao") != null ? item.get("nguoi_tao").toString() : "", cellStyle);
                createCell(row, 3, item.get("ma_booking") != null ? item.get("ma_booking").toString() : "", cellStyle);
                createCell(row, 4, item.get("ten_nhom_khach_hang") != null ? item.get("ten_nhom_khach_hang").toString() : "", cellStyle);
                createCell(row, 5,  item.get("loai_nguon_khach") != null ? item.get("loai_nguon_khach").toString() : "", cellStyle);
                createCell(row, 6,  item.get("ma_phong") != null ? item.get("ma_phong").toString() : "", cellStyle);
                createCell(row, 7,  item.get("ten_loai_phong") != null ? item.get("ten_loai_phong").toString() : "", cellStyle);
                createCell(row, 8, item.get("ngay_den") != null ? DateUtils.convertDateFormat(item.get("ngay_den").toString()) : "", cellStyle);
                createCell(row, 9, item.get("ngay_di") != null ? DateUtils.convertDateFormat(item.get("ngay_di").toString()) : "", cellStyle);

            }


            // Xuất file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);


            return new ByteArrayInputStream(outputStream.toByteArray());
        }

    }

    // xuất dữ liệu excel CCA
    public ByteArrayInputStream exportToExcelThongTinLuuTru(LocalDate ngayDen, LocalDate ngayDi,String templatePath) {
        Workbook workbook;
        ClassPathResource classPathResource = new ClassPathResource(templatePath);



        try (InputStream templateInputStream = classPathResource.getInputStream()) {

            if (templatePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(templateInputStream);
            } else if (templatePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(templateInputStream);
            } else {
                throw new IllegalArgumentException("The specified file is not Excel file");
            }


            Sheet sheet = workbook.getSheetAt(0);


            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            List<Map<String, Object>> data =  hoaDonRepository.getThongTinLuuTruKhoanThoiGian(ngayDen, ngayDi);

            int rowIndex = 3;
            for (Map<String, Object> item : data) {

                Row row = sheet.createRow(rowIndex++);

                createCell(row, 0, item.get("stt") != null ? item.get("stt").toString() : "", cellStyle);
                createCell(row, 1, item.get("ten_khach_hang") != null ? item.get("ten_khach_hang").toString() : "", cellStyle);
                createCell(row, 2, item.get("ngay_sinh") != null ? DateUtils.convertDateFormat(item.get("ngay_sinh").toString()) : "", cellStyle);
                createCell(row, 3, item.get("gioi_tinh") != null ? item.get("gioi_tinh").toString() : "", cellStyle);
                createCell(row, 4, item.get("ma_loai_giay_to") != null ? item.get("ma_loai_giay_to").toString() : "", cellStyle);
                createCell(row, 5,  "", cellStyle);
                createCell(row, 6, item.get("so_giay_to") != null ? item.get("so_giay_to").toString() : "", cellStyle);
                createCell(row, 7, item.get("sdt") != null ? item.get("sdt").toString() : "", cellStyle);
                createCell(row, 8,  "THƯỜNG TRÚ", cellStyle);
                createCell(row, 9, item.get("ten_tinh") != null ? item.get("ten_tinh").toString() : "", cellStyle);
                createCell(row, 10, item.get("ten_huyen") != null ? item.get("ten_huyen").toString() : "", cellStyle);
                createCell(row, 11, item.get("ten_phuong_xa") != null ? item.get("ten_phuong_xa").toString() : "", cellStyle);
                createCell(row, 12, item.get("dia_chi") != null ? item.get("dia_chi").toString() : "", cellStyle);
                createCell(row, 13, item.get("ngay_den") != null ? DateUtils.convertDateFormat(item.get("ngay_den").toString() ): "", cellStyle);
                createCell(row, 14, item.get("ngay_di") != null ? DateUtils.convertDateFormat(item.get("ngay_di").toString() ): "", cellStyle);
                createCell(row, 15, item.get("ten_muc_dich") != null ? item.get("ten_muc_dich").toString() : "", cellStyle);
                createCell(row, 16, item.get("ma_phong") != null ? item.get("ma_phong").toString() : "", cellStyle);



            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            // Chuyển đổi dữ liệu thành ByteArrayInputStream để trả về
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public class DateUtils {
        // Hàm chuyển đổi ngày từ yyyy-MM-dd sang dd/MM/yyyy
        public static String convertDateFormat(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) {
                return ""; // Trả về chuỗi rỗng nếu đầu vào không hợp lệ
            }
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDate date = LocalDate.parse(dateStr, inputFormatter);
            return date.format(outputFormatter);
        }
    }



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


  // Lấy danh sách an sang theo ngay
    public List<Map<String,Object>> getPhongAnSang(LocalDate ngayBD, LocalDate ngayKT){
        if(ngayBD == null || ngayKT == null){
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }
        //        // Kiểm tra ngày đến không được sau ngày đi
        if (ngayKT.isAfter(ngayBD)) {
            throw new AppException(ErrorCode.ERROR_NGAYDEN, "không thể sau ngày bắt đầu!");
        }
        return xepPhongRepository.getDanhSachPhongAnSang(ngayBD, ngayKT);

    }

    // Thống kê ăn sáng
    public List<Map<String,Object>> getThongKeAnSang(LocalDate ngayBD, LocalDate ngayKT){
        if(ngayBD == null || ngayKT == null){
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }
        //        // Kiểm tra ngày đến không được sau ngày đi
        if (ngayKT.isAfter(ngayBD)) {
            throw new AppException(ErrorCode.ERROR_NGAYDEN, "không thể sau ngày bắt đầu!");
        }
        return xepPhongRepository.getThongKeAnSang(ngayBD, ngayKT);

    }

    // Thống kê lưu trú theo quy
    public List<Map<String,Object>> getThongKeKhachLuuTruQuy(){
        return khachHangPhongRepository.getThongTinLuuTruQuocGiaQuy();
    }

    // Thống kế khách hàng theo ngày
    public List<Map<String,Object>> getThongKeKhachHangTheoNgay(LocalDate ngayBD, LocalDate ngayKT) {
        if (ngayBD == null || ngayKT == null) {
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }
        //        // Kiểm tra ngày đến không được sau ngày đi
        if (ngayKT.isBefore(ngayBD)) {
            throw new AppException(ErrorCode.ERROR_NGAYDEN, "không thể sau ngày bắt đầu!");
        }
        return xepPhongRepository.getThongKeKhachHangTheoNgay(ngayBD, ngayKT);
    }


}

