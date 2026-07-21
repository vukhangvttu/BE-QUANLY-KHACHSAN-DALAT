package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.RolePermissionResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.*;
import vttu.khachsanservice.utils.DateConverter;
import vttu.khachsanservice.utils.SecurityUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThongKeService {

    HoaDonRepository hoaDonRepository;
    private final XepPhongRepository xepPhongRepository;
    private final KhachHangPhongRepository khachHangPhongRepository;
    private final ThongKeRepository thongKeRepository;
    private final AccountRepository accountRepository;
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

            String userName = SecurityUtil.getUsername();
            if ("ANONYMOUS".equalsIgnoreCase(userName)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            boolean xuatFull = false;
            List<RolePermissionResponse> danhSachPhanQuyen = accountRepository.findPermissionsByUsername(userName);
            for(RolePermissionResponse rolePermissionResponse : danhSachPhanQuyen){
                if(rolePermissionResponse.getPermissionName().equals("XUATEXCELDOANHSOKPINHANVIEN")){
                    xuatFull = true;
                }
            }

            return hoaDonRepository.getThongKeLoaiPhongKhoanThoiGian(ngayDen, ngayDi,loaiPhong,nhomKhachHang,loaiKhachHang,xuatFull,userName);
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


            // Xuất excel theo phân quyền
            var authentiaction = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentiaction.getName();

            List<RolePermissionResponse> danhSachPhanQuyen = accountRepository.findPermissionsByUsername(userName);
            boolean xuatFull = false;

            for(RolePermissionResponse rolePermissionResponse : danhSachPhanQuyen){
                if(rolePermissionResponse.getPermissionName().equals("XUATEXCELDOANHSOKPINHANVIEN")){
                    xuatFull = true;
                }
            }


            List<Map<String, Object>> data = hoaDonRepository.getThongKeLoaiPhongExcelKhoanThoiGian(ngayDen,ngayDi,nhomKhachHang,loaiKhachHang,loaiPhong, xuatFull, userName);

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
                BigDecimal tongAddTienCoc = tongThanhToan.subtract(tienCoc);




                if (tienCoc != null) {
                    createCellSO(row, 24, tienCoc.doubleValue(), numberStyle);
                    sumTongTienCoc = sumTongTienCoc.add(tienCoc);
                } else {
                    createCellSO(row, 24, 0, numberStyle);
                }

//                BigDecimal tongTienCuoi = tongAddTienCoc.subtract(tienCoc);
                createCellSO(row, 23,  tongThanhToan.doubleValue(), numberStyle);
                sumTongTienCuoi = sumTongTienCuoi.add(tongThanhToan);


                createCellSO(row, 25, tongAddTienCoc.doubleValue(), numberStyle);
                sumTongCongThanhToan = sumTongCongThanhToan.add(tongAddTienCoc);

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

            List<Map<String, Object>> data0 = hoaDonRepository.getThongKePhongExcelKhoanThoiGian(ngayDen,ngayDi,nhomKhachHang,loaiKhachHang,loaiPhong , xuatFull, userName);

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


            Map<String, String> lyDoMap = Map.of(
                    "DULICH", "1 - Du lịch",
                    "CONGTAC", "2 - Công tác",
                    "HOCTAP", "3 - Học tập",
                    "HOINGHI", "5 - Hội nghị",
                    "KETHON", "9 - Kết hôn",
                    "MUCDICHKHAC", "2 - Mục đích khác"
            );

            Map<String, String> loaiGiayTo = Map.of(
                    "HOCHIEU", "4 - Hộ chiếu",
                    "CCCD", "1 - Thẻ CCCD",
                    "CMND", "2 - Thẻ CMND",
                    "GIAYPHEPLAIXE", "3 - Giấy phép lái xe",
                    "GIAYKHAISINH", "5 - Giấy khai sinh",
                    "THEBAOHIEM", "6 - Thẻ BHYT",
                    "KHAC", "9 - Giấy Tờ Khác",
                    "KHONG", "9 - Giấy Tờ Khác"
            );

            int rowIndex = 3;
            for (Map<String, Object> item : data) {

                Row row = sheet.createRow(rowIndex++);

                createCell(row, 0, item.get("stt") != null ? item.get("stt").toString() : "", cellStyle);
                createCell(row, 1, item.get("ten_khach_hang") != null ? item.get("ten_khach_hang").toString() : "", cellStyle);
                createCell(row, 2, item.get("ngay_sinh") != null ? DateUtils.convertDateFormat(item.get("ngay_sinh").toString()) : "", cellStyle);
                createCell(row, 3, item.get("gioi_tinh") != null ? item.get("gioi_tinh").toString() : "", cellStyle);
                createCell(row, 4, item.get("quoc_gia") != null ? item.get("quoc_gia").toString() : "", cellStyle);

                String maLoaiGiayTo = item.get("ma_loai_giay_to") == null
                        ? ""
                        : loaiGiayTo.getOrDefault(
                        item.get("ma_loai_giay_to").toString(),
                        item.get("ma_loai_giay_to").toString()
                );
                createCell(row, 5, maLoaiGiayTo, cellStyle);
                createCell(row, 6,  "", cellStyle);
                createCell(row, 7, item.get("so_giay_to") != null ? item.get("so_giay_to").toString() : "", cellStyle);
                createCell(row, 8, item.get("sdt") != null ? item.get("sdt").toString() : "", cellStyle);
                createCell(row, 9,  "1 - Thường trú", cellStyle);
                createCell(row, 10, item.get("ten_tinh") != null ? item.get("ten_tinh").toString() : "", cellStyle);
//                createCell(row, 10, item.get("ten_huyen") != null ? item.get("ten_huyen").toString() : "", cellStyle);
                createCell(row, 11, item.get("ten_phuong_xa") != null ? item.get("ten_phuong_xa").toString() : "", cellStyle);
                createCell(row, 12, item.get("dia_chi") != null ? item.get("dia_chi").toString() : "", cellStyle);
                createCell(row, 13, item.get("ngay_den") != null ? DateUtils.convertDateFormat(item.get("ngay_den").toString() ): "", cellStyle);
                createCell(row, 14, item.get("ngay_di") != null ? DateUtils.convertDateFormat(item.get("ngay_di").toString() ): "", cellStyle);
                createCell(row, 15, item.get("ma_phong") != null ? item.get("ma_phong").toString() : "", cellStyle);
                String lyDoLuuTru = item.get("ly_do_luu_tru") == null
                        ? ""
                        : lyDoMap.getOrDefault(
                        item.get("ly_do_luu_tru").toString(),
                        item.get("ly_do_luu_tru").toString()
                );

                createCell(row, 16, lyDoLuuTru, cellStyle);
                createCell(row, 17,  "", cellStyle);
                createCell(row, 18,  item.get("ghi_chu") != null ? item.get("ghi_chu").toString() : "", cellStyle);

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


    public List<Map<String,Object>> thongKePhongDaBanTheoKhoanThoiGianService (int thangBD, int namBD, int thangKT, int namKT) {
        if (thangBD < 1 || thangBD > 12 || thangKT < 1 || thangKT > 12) {
            throw new AppException(ErrorCode.THANG_INVALID);
        }

        if (namBD > namKT) {
            throw new AppException(
                    ErrorCode.NAM_INVALID,
                    "Năm bắt đầu không được lớn hơn năm kết thúc"
            );
        }

//        int namHienTai = java.time.Year.now().getValue();
//        if (namBD < 2025 || namBD > namHienTai || namKT > namHienTai) {
//            throw new AppException(ErrorCode.NAM_INVALID, " Năm phải từ 2025 đến " + namHienTai);
//        }

        List<Map<String,Object>> map = thongKeRepository.thongKePhongDaBanTheoKhoanThoiGian(thangBD,namBD, thangKT, namKT);
        if(map.isEmpty())
            throw new AppException(ErrorCode.DATA_IS_EMPTY);

        return  map;

    }



    public ByteArrayInputStream exportThongKeDoanhThuKPINhanVienKhoanThoiGian(LocalDate ngayDen, LocalDate ngayDi, String loaiPhong, String nhomKhachHang, String loaiKhachHang, String templatePath) throws IOException {

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

            // ===================== STYLE CHUNG =====================
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            DataFormat format = workbook.createDataFormat();
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);
            numberStyle.setDataFormat(format.getFormat("#,##0"));

            // Style tiêu đề báo cáo (dòng 1)
            Font boldFont14 = workbook.createFont();
            boldFont14.setBold(true);
            boldFont14.setFontHeightInPoints((short) 14);
            boldFont14.setColor(IndexedColors.RED.getIndex());
            CellStyle headerTitleStyle = workbook.createCellStyle();
            headerTitleStyle.setFont(boldFont14);
            headerTitleStyle.setAlignment(HorizontalAlignment.CENTER);
            headerTitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerTitleStyle.setWrapText(true);

            // Style dòng tổng từng nhân viên
            Font boldFont12 = workbook.createFont();
            boldFont12.setBold(true);
            boldFont12.setFontHeightInPoints((short) 12);

            CellStyle subtotalLabelStyle = workbook.createCellStyle();
            subtotalLabelStyle.setFont(boldFont12);
            subtotalLabelStyle.setAlignment(HorizontalAlignment.CENTER);
            subtotalLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            subtotalLabelStyle.setBorderTop(BorderStyle.THIN);
            subtotalLabelStyle.setBorderBottom(BorderStyle.THIN);
            subtotalLabelStyle.setBorderLeft(BorderStyle.THIN);
            subtotalLabelStyle.setBorderRight(BorderStyle.THIN);
            subtotalLabelStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            subtotalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle subtotalNumberStyle = workbook.createCellStyle();
            subtotalNumberStyle.setFont(boldFont12);
            subtotalNumberStyle.setBorderTop(BorderStyle.THIN);
            subtotalNumberStyle.setBorderBottom(BorderStyle.THIN);
            subtotalNumberStyle.setBorderLeft(BorderStyle.THIN);
            subtotalNumberStyle.setBorderRight(BorderStyle.THIN);
            subtotalNumberStyle.setDataFormat(format.getFormat("#,##0"));
            subtotalNumberStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            subtotalNumberStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Style dòng tổng cuối cùng
            CellStyle totalLabelStyle = workbook.createCellStyle();
            totalLabelStyle.setFont(boldFont12);
            totalLabelStyle.setAlignment(HorizontalAlignment.CENTER);
            totalLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            totalLabelStyle.setBorderTop(BorderStyle.THIN);
            totalLabelStyle.setBorderBottom(BorderStyle.THIN);
            totalLabelStyle.setBorderLeft(BorderStyle.THIN);
            totalLabelStyle.setBorderRight(BorderStyle.THIN);

            CellStyle totalNumberStyle = workbook.createCellStyle();
            totalNumberStyle.setFont(boldFont12);
            totalNumberStyle.setBorderTop(BorderStyle.THIN);
            totalNumberStyle.setBorderBottom(BorderStyle.THIN);
            totalNumberStyle.setBorderLeft(BorderStyle.THIN);
            totalNumberStyle.setBorderRight(BorderStyle.THIN);
            totalNumberStyle.setDataFormat(format.getFormat("#,##0"));

            // Xuất excel theo phân quyền
            var authentiaction = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentiaction.getName();

            List<RolePermissionResponse> danhSachPhanQuyen = accountRepository.findPermissionsByUsername(userName);
            boolean xuatFull = false;

            for(RolePermissionResponse rolePermissionResponse : danhSachPhanQuyen){
                 if(rolePermissionResponse.getPermissionName().equals("XUATEXCELDOANHSOKPINHANVIEN")){
                     xuatFull = true;
                 }
            }

            // ===================== SHEET 1 (index=1): BÁO CÁO THEO NHÂN VIÊN =====================
            Sheet sheet1 = workbook.getSheetAt(1);

            // Tiêu đề dòng 1
            String content1 = "BÁO CÁO DOANH THU THEO NHÂN VIÊN \n"
                    + "Từ ngày " + DateConverter.converDate(ngayDen)
                    + " Đến ngày " + DateConverter.converDate(ngayDi);
            Row headerRow1 = sheet1.getRow(1);
            if (headerRow1 == null) headerRow1 = sheet1.createRow(1);
            Cell contentCell1 = headerRow1.createCell(0);
            contentCell1.setCellValue(content1);
            contentCell1.setCellStyle(headerTitleStyle);

            // Lấy dữ liệu sheet 1
            List<Map<String, Object>> data1 = hoaDonRepository.getThongKeLoaiPhongExcelKhoanThoiGian(ngayDen, ngayDi, nhomKhachHang, loaiKhachHang, loaiPhong, xuatFull, userName);

            // Nhóm theo nguoi_tao (giữ thứ tự)
            Map<String, List<Map<String, Object>>> groupedByNguoiTao1 = new LinkedHashMap<>();
            for (Map<String, Object> item : data1) {
                String nguoiTao = item.get("nguoi_tao") != null ? item.get("nguoi_tao").toString() : "";
                groupedByNguoiTao1.computeIfAbsent(nguoiTao, k -> new ArrayList<>()).add(item);
            }

            // Tổng toàn bộ sheet 1
            BigDecimal grandSumTienPhong1 = BigDecimal.ZERO;
            BigDecimal grandSumChietKhau1 = BigDecimal.ZERO;
            BigDecimal grandSumPhuThuGiuong1 = BigDecimal.ZERO;
            BigDecimal grandSumPhuThuAnSang1 = BigDecimal.ZERO;
            BigDecimal grandSumPhuThuTreEm1 = BigDecimal.ZERO;
            BigDecimal grandSumPhuThuNguoiLon1 = BigDecimal.ZERO;
            BigDecimal grandSumDichVuMinibar1 = BigDecimal.ZERO;
            BigDecimal grandSumDichVuGiatUi1 = BigDecimal.ZERO;
            BigDecimal grandSumDichVuKhac1 = BigDecimal.ZERO;
            BigDecimal grandSumCheckInSom1 = BigDecimal.ZERO;
            BigDecimal grandSumCheckOutTre1 = BigDecimal.ZERO;
            BigDecimal grandSumCongThanhToan1 = BigDecimal.ZERO;
            BigDecimal grandSumTienCoc1 = BigDecimal.ZERO;
            BigDecimal grandSumTienCuoi1 = BigDecimal.ZERO;
            BigDecimal grandSumDichVuSetMonAn1 = BigDecimal.ZERO;
            BigDecimal grandSumDenBu1 = BigDecimal.ZERO;
            BigDecimal grandSumHoiNghi1 = BigDecimal.ZERO;

            int rowIndex1 = 5;
            int stt1 = 1;

            for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByNguoiTao1.entrySet()) {
                String nguoiTao = entry.getKey();
                List<Map<String, Object>> items = entry.getValue();

                // Tổng từng nhân viên
                BigDecimal subSumTienPhong = BigDecimal.ZERO;
                BigDecimal subSumChietKhau = BigDecimal.ZERO;
                BigDecimal subSumPhuThuGiuong = BigDecimal.ZERO;
                BigDecimal subSumPhuThuAnSang = BigDecimal.ZERO;
                BigDecimal subSumPhuThuTreEm = BigDecimal.ZERO;
                BigDecimal subSumPhuThuNguoiLon = BigDecimal.ZERO;
                BigDecimal subSumDichVuMinibar = BigDecimal.ZERO;
                BigDecimal subSumDichVuGiatUi = BigDecimal.ZERO;
                BigDecimal subSumDichVuKhac = BigDecimal.ZERO;
                BigDecimal subSumDenBu = BigDecimal.ZERO;
                BigDecimal subSumHoiGhi = BigDecimal.ZERO;
                BigDecimal subSumCheckInSom = BigDecimal.ZERO;
                BigDecimal subSumCheckOutTre = BigDecimal.ZERO;
                BigDecimal subSumCongThanhToan = BigDecimal.ZERO;
                BigDecimal subSumTienCoc = BigDecimal.ZERO;
                BigDecimal subSumTienCuoi = BigDecimal.ZERO;
                BigDecimal subSumDichVuSetMonAn = BigDecimal.ZERO;

                for (Map<String, Object> item : items) {
                    Row row = sheet1.createRow(rowIndex1++);

                    createCell(row, 0, String.valueOf(stt1++), cellStyle);
                    createCell(row, 1, item.get("thoi_gian_tao") != null ? item.get("thoi_gian_tao").toString() : "", cellStyle);
                    createCell(row, 2, nguoiTao, cellStyle);
                    createCell(row, 3, item.get("ma_booking") != null ? item.get("ma_booking").toString() : "", cellStyle);
                    createCell(row, 4, item.get("ten_nhom_khach_hang") != null ? item.get("ten_nhom_khach_hang").toString() : "", cellStyle);
                    createCell(row, 5, item.get("loai_nguon_khach") != null ? item.get("loai_nguon_khach").toString() : "", cellStyle);
                    createCell(row, 6, item.get("danh_sach_phong") != null ? item.get("danh_sach_phong").toString() : "", cellStyle);
                    createCell(row, 7, item.get("danh_sach_loai_phong") != null ? item.get("danh_sach_loai_phong").toString() : "", cellStyle);
                    createCell(row, 8, item.get("ngay_den") != null ? DateUtils.convertDateFormat(item.get("ngay_den").toString()) : "", cellStyle);
                    createCell(row, 9, item.get("ngay_di") != null ? DateUtils.convertDateFormat(item.get("ngay_di").toString()) : "", cellStyle);

                    BigDecimal giaPhong = (BigDecimal) item.get("gia_phong");
                    if (giaPhong != null) {
                        createCellSO(row, 10, giaPhong.doubleValue(), numberStyle);
                        subSumTienPhong = subSumTienPhong.add(giaPhong);
                    } else createCellSO(row, 10, 0, numberStyle);

                    BigDecimal tongGiaPhong = (BigDecimal) item.get("tong_gia_phong");
                    if (tongGiaPhong != null) {
                        createCellSO(row, 11, tongGiaPhong.doubleValue(), numberStyle);
                        subSumChietKhau = subSumChietKhau.add(tongGiaPhong);
                    } else createCellSO(row, 11, 0, numberStyle);

                    createCell(row, 12, item.get("danh_sach_ghi_chu") != null ? item.get("danh_sach_ghi_chu").toString() : "", cellStyle);

                    BigDecimal tongPhuThuGiuong = (BigDecimal) item.get("tong_phu_thu_giuong");
                    if (tongPhuThuGiuong != null) {
                        createCellSO(row, 13, tongPhuThuGiuong.doubleValue(), numberStyle);
                        subSumPhuThuGiuong = subSumPhuThuGiuong.add(tongPhuThuGiuong);
                    } else createCellSO(row, 13, 0, numberStyle);

                    BigDecimal tongPhuThuAnSang = (BigDecimal) item.get("tong_phu_thu_an_sang");
                    if (tongPhuThuAnSang != null) {
                        createCellSO(row, 14, tongPhuThuAnSang.doubleValue(), numberStyle);
                        subSumPhuThuAnSang = subSumPhuThuAnSang.add(tongPhuThuAnSang);
                    } else createCellSO(row, 14, 0, numberStyle);

                    BigDecimal tongPhuThuTreEm = (BigDecimal) item.get("tong_phu_thu_tre_em");
                    if (tongPhuThuTreEm != null) {
                        createCellSO(row, 15, tongPhuThuTreEm.doubleValue(), numberStyle);
                        subSumPhuThuTreEm = subSumPhuThuTreEm.add(tongPhuThuTreEm);
                    } else createCellSO(row, 15, 0, numberStyle);

                    BigDecimal tongPhuThuNguoiLon = (BigDecimal) item.get("tong_phu_thu_nguoi_lon");
                    if (tongPhuThuNguoiLon != null) {
                        createCellSO(row, 16, tongPhuThuNguoiLon.doubleValue(), numberStyle);
                        subSumPhuThuNguoiLon = subSumPhuThuNguoiLon.add(tongPhuThuNguoiLon);
                    } else createCellSO(row, 16, 0, numberStyle);

                    BigDecimal tongTienMinibar = (BigDecimal) item.get("tong_tien_dich_vu_minibar");
                    if (tongTienMinibar != null) {
                        createCellSO(row, 17, tongTienMinibar.doubleValue(), numberStyle);
                        subSumDichVuMinibar = subSumDichVuMinibar.add(tongTienMinibar);
                    } else createCellSO(row, 17, 0, numberStyle);

                    BigDecimal tongTienGiatUi = (BigDecimal) item.get("tong_tien_dich_vu_giatui");
                    if (tongTienGiatUi != null) {
                        createCellSO(row, 18, tongTienGiatUi.doubleValue(), numberStyle);
                        subSumDichVuGiatUi = subSumDichVuGiatUi.add(tongTienGiatUi);
                    } else createCellSO(row, 18, 0, numberStyle);

                    BigDecimal tienDichVuKhac = (BigDecimal) item.get("tong_tien_dich_vu_khac");
                    if (tienDichVuKhac != null) {
                        createCellSO(row, 19, tienDichVuKhac.doubleValue(), numberStyle);
                        subSumDichVuKhac = subSumDichVuKhac.add(tienDichVuKhac);
                    } else createCellSO(row, 19, 0, numberStyle);

                    BigDecimal tienDenBu = (BigDecimal) item.get("tong_tien_den_bu");
                    if (tienDenBu != null) {
                        createCellSO(row, 20, tienDenBu.doubleValue(), numberStyle);
                        subSumDenBu = subSumDenBu.add(tienDenBu);
                    } else createCellSO(row, 20, 0, numberStyle);

                    BigDecimal tienHoiNghi = (BigDecimal) item.get("tong_tien_hoi_nghi");
                    if (tienHoiNghi != null) {
                        createCellSO(row, 21, tienHoiNghi.doubleValue(), numberStyle);
                        subSumHoiGhi = subSumHoiGhi.add(tienHoiNghi);
                    } else createCellSO(row, 21, 0, numberStyle);

                    BigDecimal tienCheckInSom = (BigDecimal) item.get("tong_phu_thu_check_in_som");
                    if (tienCheckInSom != null) {
                        createCellSO(row, 22, tienCheckInSom.doubleValue(), numberStyle);
                        subSumCheckInSom = subSumCheckInSom.add(tienCheckInSom);
                    } else createCellSO(row, 22, 0, numberStyle);

                    BigDecimal tienCheckOutTre = (BigDecimal) item.get("tong_phu_thu_check_out_tre");
                    if (tienCheckOutTre != null) {
                        createCellSO(row, 23, tienCheckOutTre.doubleValue(), numberStyle);
                        subSumCheckOutTre = subSumCheckOutTre.add(tienCheckOutTre);
                    } else createCellSO(row, 23, 0, numberStyle);

                    BigDecimal tienDichVuSetMonAn = (BigDecimal) item.get("tong_tien_set_mon_an");
                    if (tienDichVuSetMonAn != null) {
                        createCellSO(row, 24, tienDichVuSetMonAn.doubleValue(), numberStyle);
                        subSumDichVuSetMonAn = subSumDichVuSetMonAn.add(tienDichVuSetMonAn);
                    } else createCellSO(row, 24, 0, numberStyle);


                    BigDecimal tongThanhToan = (BigDecimal) item.get("tong_tien");
                    BigDecimal tienCoc = item.get("tien_coc") != null ? (BigDecimal) item.get("tien_coc") : BigDecimal.ZERO;
                    BigDecimal tongAddTienCoc = tongThanhToan != null ? tongThanhToan.subtract(tienCoc) : tienCoc;


                    createCellSO(row, 26, tienCoc.doubleValue(), numberStyle);
                    subSumTienCoc = subSumTienCoc.add(tienCoc);

//                    BigDecimal tongTienCuoi = tongAddTienCoc.subtract(tienCoc);
                    createCellSO(row, 25, tongThanhToan.doubleValue(), numberStyle);
                    subSumTienCuoi = subSumTienCuoi.add(tongThanhToan);

                    createCellSO(row, 27, tongAddTienCoc.doubleValue(), numberStyle);
                    subSumCongThanhToan = subSumCongThanhToan.add(tongAddTienCoc);
                }

                // ---- Dòng tổng phụ (subtotal) của từng nguoi_tao ----
                int subtotalRowIdx = rowIndex1++;
                Row subtotalRow = sheet1.createRow(subtotalRowIdx);
                subtotalRow.setHeightInPoints(22);

                sheet1.addMergedRegion(new CellRangeAddress(subtotalRowIdx, subtotalRowIdx, 0, 9));
                for (int col = 0; col <= 9; col++) {
                    Cell cell = subtotalRow.createCell(col);
                    if (col == 0) cell.setCellValue("Tổng " + nguoiTao);
                    cell.setCellStyle(subtotalLabelStyle);
                }

                createCellSO(subtotalRow, 10, subSumTienPhong.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 11, subSumChietKhau.doubleValue(), subtotalNumberStyle);
                createCell(subtotalRow, 12, "", subtotalLabelStyle);
                createCellSO(subtotalRow, 13, subSumPhuThuGiuong.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 14, subSumPhuThuAnSang.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 15, subSumPhuThuTreEm.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 16, subSumPhuThuNguoiLon.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 17, subSumDichVuMinibar.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 18, subSumDichVuGiatUi.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 19, subSumDichVuKhac.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 20, subSumDenBu.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 21, subSumHoiGhi.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 22, subSumCheckInSom.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 23, subSumCheckOutTre.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 24, subSumDichVuSetMonAn.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 25, subSumCongThanhToan.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 26, subSumTienCoc.doubleValue(), subtotalNumberStyle);
                createCellSO(subtotalRow, 27, subSumTienCuoi.doubleValue(), subtotalNumberStyle);

                // Cộng vào grand total
                grandSumTienPhong1 = grandSumTienPhong1.add(subSumTienPhong);
                grandSumChietKhau1 = grandSumChietKhau1.add(subSumChietKhau);
                grandSumPhuThuGiuong1 = grandSumPhuThuGiuong1.add(subSumPhuThuGiuong);
                grandSumPhuThuAnSang1 = grandSumPhuThuAnSang1.add(subSumPhuThuAnSang);
                grandSumPhuThuTreEm1 = grandSumPhuThuTreEm1.add(subSumPhuThuTreEm);
                grandSumPhuThuNguoiLon1 = grandSumPhuThuNguoiLon1.add(subSumPhuThuNguoiLon);
                grandSumDichVuMinibar1 = grandSumDichVuMinibar1.add(subSumDichVuMinibar);
                grandSumDichVuGiatUi1 = grandSumDichVuGiatUi1.add(subSumDichVuGiatUi);
                grandSumDichVuKhac1 = grandSumDichVuKhac1.add(subSumDichVuKhac);
                grandSumCheckInSom1 = grandSumCheckInSom1.add(subSumCheckInSom);
                grandSumCheckOutTre1 = grandSumCheckOutTre1.add(subSumCheckOutTre);
                grandSumDichVuSetMonAn1 = grandSumDichVuSetMonAn1.add(subSumDichVuSetMonAn);
                grandSumDenBu1 = grandSumDenBu1.add(subSumDenBu);
                grandSumHoiNghi1 = grandSumHoiNghi1.add(subSumHoiGhi);
                grandSumCongThanhToan1 = grandSumCongThanhToan1.add(subSumCongThanhToan);
                grandSumTienCoc1 = grandSumTienCoc1.add(subSumTienCoc);
                grandSumTienCuoi1 = grandSumTienCuoi1.add(subSumTienCuoi);
            }

            // ---- Dòng tổng cuối cùng sheet 1 ----
            int grandTotalIdx1 = rowIndex1;
            Row grandTotalRow1 = sheet1.createRow(grandTotalIdx1);
            grandTotalRow1.setHeightInPoints(25);

            sheet1.addMergedRegion(new CellRangeAddress(grandTotalIdx1, grandTotalIdx1, 0, 9));
            for (int col = 0; col <= 9; col++) {
                Cell cell = grandTotalRow1.createCell(col);
                if (col == 0) cell.setCellValue("Tổng tiền");
                cell.setCellStyle(totalLabelStyle);
            }
            createCellSO(grandTotalRow1, 10, grandSumTienPhong1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 11, grandSumChietKhau1.doubleValue(), totalNumberStyle);
            createCell(grandTotalRow1, 12, "", totalLabelStyle);
            createCellSO(grandTotalRow1, 13, grandSumPhuThuGiuong1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 14, grandSumPhuThuAnSang1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 15, grandSumPhuThuTreEm1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 16, grandSumPhuThuNguoiLon1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 17, grandSumDichVuMinibar1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 18, grandSumDichVuGiatUi1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 19, grandSumDichVuKhac1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 20, grandSumDenBu1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 21, grandSumHoiNghi1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 22, grandSumCheckInSom1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 23, grandSumCheckOutTre1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 24, grandSumDichVuSetMonAn1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 25, grandSumCongThanhToan1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 26, grandSumTienCoc1.doubleValue(), totalNumberStyle);
            createCellSO(grandTotalRow1, 27, grandSumTienCuoi1.doubleValue(), totalNumberStyle);


            // ===================== SHEET 0 (index=0): BÁO CÁO DOANH SỐ KPI NHÂN VIÊN =====================
            Sheet sheet0 = workbook.getSheetAt(0);

            String content0 = "BÁO CÁO DOANH SỐ KPI NHÂN VIÊN\n"
                    + "Từ ngày " + DateConverter.converDate(ngayDen)
                    + " Đến ngày " + DateConverter.converDate(ngayDi);
            Row headerRow0 = sheet0.getRow(1);
            if (headerRow0 == null) headerRow0 = sheet0.createRow(1);
            Cell contentCell0 = headerRow0.createCell(0);
            contentCell0.setCellValue(content0);
            contentCell0.setCellStyle(headerTitleStyle);

            List<Map<String, Object>> data0 = thongKeRepository.getThongKeDanhSoKPINhanVienExcelKhoanThoiGian(ngayDen, ngayDi, xuatFull, userName);

// Tổng cuối trang
            BigDecimal grandSumPhuThuGiuong0      = BigDecimal.ZERO;
            BigDecimal grandSumPhuThuTreEm0       = BigDecimal.ZERO;
            BigDecimal grandSumPhuThuNguoiLon0    = BigDecimal.ZERO;
            BigDecimal grandSumCheckInSom0        = BigDecimal.ZERO;
            BigDecimal grandSumCheckOutTre0       = BigDecimal.ZERO;
            BigDecimal grandSumPhuThuAnSang0      = BigDecimal.ZERO;
            BigDecimal grandSumMinibar0           = BigDecimal.ZERO;
            BigDecimal grandSumGiatUi0            = BigDecimal.ZERO;
            BigDecimal grandSumDichVuKhac0        = BigDecimal.ZERO;
            BigDecimal grandSumSetMonAn0          = BigDecimal.ZERO;
            BigDecimal grandSumThanhToan0         = BigDecimal.ZERO;
            BigDecimal grandSumTienNone0          = BigDecimal.ZERO;
            BigDecimal grandSumTienTA0            = BigDecimal.ZERO;
            BigDecimal grandSumTienOTA0           = BigDecimal.ZERO;
            BigDecimal grandSumTienWalkIn0        = BigDecimal.ZERO;
            BigDecimal grandSumTienDoanGala0      = BigDecimal.ZERO;
            BigDecimal grandSumTienDoanLe0        = BigDecimal.ZERO;
            BigDecimal grandSumHoiNghi0             = BigDecimal.ZERO;
            BigDecimal grandSumDenBu0             = BigDecimal.ZERO;

            int rowIndex0 = 5; // bắt đầu từ dòng 5 (index 5, tức dòng thứ 6 trong Excel)

            for (Map<String, Object> item : data0) {
                Row row = sheet0.createRow(rowIndex0++);

                // Cột 0: STT
                createCell(row, 0, item.get("stt") != null ? item.get("stt").toString() : "", cellStyle);

                // Cột 1: Nhân viên (nguoi_tao)
                createCell(row, 1, item.get("nguoi_tao") != null ? item.get("nguoi_tao").toString() : "", cellStyle);

                // Cột 2: Tổng số booking
                createCell(row, 2, item.get("ten_nhan_vien") != null ?  item.get("ten_nhan_vien").toString() : "", cellStyle);;


                // Cột 3: TA
                BigDecimal tienTA = toBD(item.get("tong_tien_ta"));
                createCellSO(row, 3, tienTA.doubleValue(), numberStyle);
                grandSumTienTA0 = grandSumTienTA0.add(tienTA);

                // Cột 4: OTA
                BigDecimal tienOTA = toBD(item.get("tong_tien_ota"));
                createCellSO(row, 4, tienOTA.doubleValue(), numberStyle);
                grandSumTienOTA0 = grandSumTienOTA0.add(tienOTA);

                // Cột 5: Walk-in
                BigDecimal tienWalkIn = toBD(item.get("tong_tien_walkin"));
                createCellSO(row, 5, tienWalkIn.doubleValue(), numberStyle);
                grandSumTienWalkIn0 = grandSumTienWalkIn0.add(tienWalkIn);

                // Cột 6: Đoàn lẻ
                BigDecimal tienDoanLe = toBD(item.get("tong_tien_doan_le"));
                createCellSO(row, 6, tienDoanLe.doubleValue(), numberStyle);
                grandSumTienDoanLe0 = grandSumTienDoanLe0.add(tienDoanLe);

                // Cột 7: Đoàn Gala
                BigDecimal tienDoanGala = toBD(item.get("tong_tien_doan_gala"));
                createCellSO(row, 7, tienDoanGala.doubleValue(), numberStyle);
                grandSumTienDoanGala0 = grandSumTienDoanGala0.add(tienDoanGala);

                // Cột 8: NONE
                BigDecimal tienNone = toBD(item.get("tong_tien_none"));
                createCellSO(row, 8, tienNone.doubleValue(), numberStyle);
                grandSumTienNone0 = grandSumTienNone0.add(tienNone);

                // Cột 9 Phụ thu giường
                BigDecimal phuThuGiuong = toBD(item.get("tong_phu_thu_giuong"));
                createCellSO(row, 9, phuThuGiuong.doubleValue(), numberStyle);
                grandSumPhuThuGiuong0 = grandSumPhuThuGiuong0.add(phuThuGiuong);

                // Cột 10: Phụ thu ăn sáng
                BigDecimal phuThuAnSang = toBD(item.get("tong_phu_thu_an_sang"));
                createCellSO(row, 10, phuThuAnSang.doubleValue(), numberStyle);
                grandSumPhuThuAnSang0 = grandSumPhuThuAnSang0.add(phuThuAnSang);

                // Cột 11: Phụ thu trẻ em
                BigDecimal phuThuTreEm = toBD(item.get("tong_phu_thu_tre_em"));
                createCellSO(row, 11, phuThuTreEm.doubleValue(), numberStyle);
                grandSumPhuThuTreEm0 = grandSumPhuThuTreEm0.add(phuThuTreEm);

                // Cột 12: Phụ thu người lớn
                BigDecimal phuThuNguoiLon = toBD(item.get("tong_phu_thu_nguoi_lon"));
                createCellSO(row, 12, phuThuNguoiLon.doubleValue(), numberStyle);
                grandSumPhuThuNguoiLon0 = grandSumPhuThuNguoiLon0.add(phuThuNguoiLon);

                // Cột 13: Set món ăn
                BigDecimal setMonAn = toBD(item.get("tong_tien_setmonan"));
                createCellSO(row, 13, setMonAn.doubleValue(), numberStyle);
                grandSumSetMonAn0 = grandSumSetMonAn0.add(setMonAn);

                // Cột 14: Minibar
                BigDecimal minibar = toBD(item.get("tong_tien_minibar"));
                createCellSO(row, 14, minibar.doubleValue(), numberStyle);
                grandSumMinibar0 = grandSumMinibar0.add(minibar);

                // Cột 15: Giặt ủi
                BigDecimal giatUi = toBD(item.get("tong_tien_giatui"));
                createCellSO(row, 15, giatUi.doubleValue(), numberStyle);
                grandSumGiatUi0 = grandSumGiatUi0.add(giatUi);

                // Cột 16: Dịch vụ khác
                BigDecimal dichVuKhac = toBD(item.get("tong_tien_dich_vu_khac"));
                createCellSO(row, 16, dichVuKhac.doubleValue(), numberStyle);
                grandSumDichVuKhac0 = grandSumDichVuKhac0.add(dichVuKhac);

                // Cột 17: Đền bù
                BigDecimal denBu = toBD(item.get("tong_tien_denbu"));
                createCellSO(row, 17, denBu.doubleValue(), numberStyle);
                grandSumDenBu0 = grandSumDenBu0.add(denBu);

                // Cột 18: Hội nghị
                BigDecimal hoiNghi = toBD(item.get("tong_tien_hoi_nghi"));
                createCellSO(row, 18, hoiNghi.doubleValue(), numberStyle);
                grandSumHoiNghi0 = grandSumHoiNghi0.add(hoiNghi);


                // Cột 19: Phụ thu check-in sớm
                BigDecimal checkInSom = toBD(item.get("tong_phu_thu_check_in_som"));
                createCellSO(row, 19, checkInSom.doubleValue(), numberStyle);
                grandSumCheckInSom0 = grandSumCheckInSom0.add(checkInSom);

                // Cột 20: Phụ thu check-out trễ
                BigDecimal checkOutTre = toBD(item.get("tong_phu_thu_check_out_tre"));
                createCellSO(row, 20, checkOutTre.doubleValue(), numberStyle);
                grandSumCheckOutTre0 = grandSumCheckOutTre0.add(checkOutTre);

                // Cột 16: Tổng thanh toán
                BigDecimal thanhToan = toBD(item.get("tong_thanh_toan"));
                createCellSO(row, 21, thanhToan.doubleValue(), numberStyle);
                grandSumThanhToan0 = grandSumThanhToan0.add(thanhToan);

            }

            // ---- Dòng tổng cuối cùng sheet 0 ----
            int grandTotalIdx0 = rowIndex0;
            Row grandTotalRow0 = sheet0.createRow(grandTotalIdx0);
            grandTotalRow0.setHeightInPoints(25);

            // Gộp ô cột 0-2 để ghi "Tổng tiền"
            sheet0.addMergedRegion(new CellRangeAddress(grandTotalIdx0, grandTotalIdx0, 0, 8));
            for (int col = 0; col <= 8; col++) {
                Cell cell = grandTotalRow0.createCell(col);
                if (col == 0) cell.setCellValue("Tổng tiền");
                cell.setCellStyle(totalLabelStyle);
            }

            createCellSO(grandTotalRow0,  9, grandSumPhuThuGiuong0.doubleValue(),   totalNumberStyle);
            createCellSO(grandTotalRow0, 10, grandSumPhuThuAnSang0.doubleValue(),        totalNumberStyle);
            createCellSO(grandTotalRow0, 11, grandSumPhuThuTreEm0.doubleValue(),         totalNumberStyle);
            createCellSO(grandTotalRow0, 12, grandSumPhuThuNguoiLon0.doubleValue(),     totalNumberStyle);
            createCellSO(grandTotalRow0, 13, grandSumSetMonAn0.doubleValue(),       totalNumberStyle);
            createCellSO(grandTotalRow0, 14, grandSumMinibar0.doubleValue(),     totalNumberStyle);
            createCellSO(grandTotalRow0, 15, grandSumGiatUi0.doubleValue(),  totalNumberStyle);
            createCellSO(grandTotalRow0, 16, grandSumDichVuKhac0.doubleValue(),      totalNumberStyle);
            createCellSO(grandTotalRow0, 17, grandSumDenBu0.doubleValue(),      totalNumberStyle);
            createCellSO(grandTotalRow0, 18, grandSumHoiNghi0.doubleValue(),       totalNumberStyle);
            createCellSO(grandTotalRow0, 19, grandSumCheckInSom0.doubleValue(),         totalNumberStyle);
            createCellSO(grandTotalRow0, 20, grandSumCheckOutTre0.doubleValue(),        totalNumberStyle);
            createCellSO(grandTotalRow0, 21, grandSumThanhToan0.doubleValue(),     totalNumberStyle);

            // Xuất file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    private BigDecimal toBD(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return new BigDecimal(value.toString());
    }



    public List<Map<String,Object>> thongKeAllKPINhanVienService (LocalDate ngayBatDau, LocalDate ngayKetThuc) throws Exception {

        if (ngayBatDau == null || ngayKetThuc == null) {
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }

        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new AppException(
                    ErrorCode.ERROR_NGAYDEN,
                    "Ngày kết thúc không được trước ngày bắt đầu"
            );
        }

        String userName = SecurityUtil.getUsername();
        if ("ANONYMOUS".equalsIgnoreCase(userName)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        List<RolePermissionResponse> danhSachPhanQuyen = accountRepository.findPermissionsByUsername(userName);
        boolean xuatFull = false;

        for(RolePermissionResponse rolePermissionResponse : danhSachPhanQuyen){
            if(rolePermissionResponse.getPermissionName().equals("XUATEXCELDOANHSOKPINHANVIEN")){
                xuatFull = true;
            }
        }

        List<Map<String, Object>> data = thongKeRepository.getThongKeDanhSoKPINhanVienExcelKhoanThoiGian(ngayBatDau, ngayKetThuc, xuatFull, userName);

        if (data == null || data.isEmpty()) {
            throw new AppException(ErrorCode.DATA_IS_EMPTY);
        }

        return  data;

    }

    public List<Map<String,Object>> thongKeChiTietKPINhanVienService (LocalDate ngayBatDau, LocalDate ngayKetThuc) throws Exception {

        if (ngayBatDau == null || ngayKetThuc == null) {
            throw new AppException(ErrorCode.ERROR_NGAYDI);
        }

        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new AppException(
                    ErrorCode.ERROR_NGAYDEN,
                    "Ngày kết thúc không được trước ngày bắt đầu"
            );
        }

        String userName = SecurityUtil.getUsername();
        if ("ANONYMOUS".equalsIgnoreCase(userName)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        List<RolePermissionResponse> danhSachPhanQuyen = accountRepository.findPermissionsByUsername(userName);
        boolean xuatFull = false;

        for(RolePermissionResponse rolePermissionResponse : danhSachPhanQuyen){
            if(rolePermissionResponse.getPermissionName().equals("XUATEXCELDOANHSOKPINHANVIEN")){
                xuatFull = true;
            }
        }

        List<Map<String, Object>> data = hoaDonRepository.getThongKeLoaiPhongExcelKhoanThoiGian(ngayBatDau, ngayKetThuc, "0", "", "all", xuatFull, userName);

        if (data == null || data.isEmpty()) {
            throw new AppException(ErrorCode.DATA_IS_EMPTY);
        }

        return  data;

    }
}


