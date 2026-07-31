package vttu.khachsanservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vttu.khachsanservice.dto.DichVuDto;
import vttu.khachsanservice.dto.PhuThuDto;
import vttu.khachsanservice.dto.ThongTinPhongDto;
import vttu.khachsanservice.dto.request.HoaDonRequest;
import vttu.khachsanservice.dto.request.HoaDonVatPDFRequest;
import vttu.khachsanservice.dto.request.HoaDonVatRequest;
import vttu.khachsanservice.dto.response.HoaDonVatResponse;
import vttu.khachsanservice.dto.xuathoadon.*;
import vttu.khachsanservice.entity.DichVuVATCusTome;
import vttu.khachsanservice.entity.HoaDonVat;
import vttu.khachsanservice.entity.HoaDonVatNhap;
import vttu.khachsanservice.entity.HoaDonVatPhatHanh;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.HoaDonVatMapper;
import vttu.khachsanservice.repository.*;
import vttu.khachsanservice.utils.InvoiceCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class HoaDonVatService {

    HoaDonVatRepository hoaDonVatRepository;
    private final ObjectMapper objectMapper;
    private final HoaDonVatNhapRepository hoaDonVatNhapRepository;
    private final DichVuVATCustomeRepository dichVuVATCustomeRepository;

    // tạo id mã string tự động tăng
    public synchronized String generateNewHoaDonVatId() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // Ví dụ: "20250303"
        Optional<String> latestId = hoaDonVatRepository.findLatestMaHoaDonVat();
        if (latestId.isPresent() && latestId.get().startsWith("HDVAT" + datePrefix)) {
            int number = Integer.parseInt(latestId.get().substring(14)); // Sau "BK20250303"
            return String.format("HDVAT%s%03d", datePrefix, number + 1);
        }
        return "HDVAT" + datePrefix + "001"; // Ví dụ: "BK20250303001"
    }

    private final BooKingRepository booKingRepository;
    HoaDonVatMapper hoaDonVatMapper;

    @Transactional
    public boolean createHoaDonVat(String maBooKing, HoaDonVatRequest request) {

        booKingRepository.findById(maBooKing)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

        if(request == null)
            throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
        var authentiaction = SecurityContextHolder.getContext().getAuthentication();

        HoaDonVat hoaDonVat = hoaDonVatMapper.toReactedHoaDonVat(request);
        hoaDonVat.setMaHoaDonVat(generateNewHoaDonVatId());
        hoaDonVat.setThoiGianTao(LocalDateTime.now());
        hoaDonVat.setNguoiTao(authentiaction.getName());

        if(hoaDonVat.getTenCongTy() == null || hoaDonVat.getTenCongTy().isEmpty()) {
           throw new AppException(ErrorCode.TEN_CONG_TY_NOT_FOUND);
        }

        if(!hoaDonVat.getLoaiGiayTo().equals("KHONG")) {
            if (hoaDonVat.getDiaChi() == null || hoaDonVat.getDiaChi().isEmpty()) {
                throw new AppException(ErrorCode.TEN_CONG_TY_NOT_FOUND);
            }

            if (hoaDonVat.getCodeVat().isEmpty()) {

                if (hoaDonVat.getLoaiGiayTo().equals("CCCD")) {
                    if (hoaDonVat.getCccd().length() != 12)
                        throw new AppException(ErrorCode.CCCD_NOT_VALID, "CCCD phải có 12 ký tự");
                } else {
                    if (hoaDonVat.getCccd().length() < 8)
                        throw new AppException(ErrorCode.CCCD_NOT_VALID, "Hộ chiếu phải có ít nhất 8 ký tự");
                }
            } else {
                if (hoaDonVat.getCodeVat().length() != 10 && hoaDonVat.getCodeVat().length() != 14)
                    throw new AppException(ErrorCode.CCCD_NOT_VALID, "Code Vat phải có 10 hoặc 14 ký tự");
            }
            if (!hoaDonVat.getCodeVat().isEmpty()) {
                if (hoaDonVat.getLoaiGiayTo().equals("CCCD")) {
                    if (hoaDonVat.getCccd().length() != 12)
                        throw new AppException(ErrorCode.CCCD_NOT_VALID, "CCCD phải có 12 ký tự");
                } else {
                    if (hoaDonVat.getCccd().length() < 8)
                        throw new AppException(ErrorCode.CCCD_NOT_VALID, "Hộ chiếu phải có ít nhất 8 ký tự");
                }
            }
        }

        hoaDonVatRepository.save(hoaDonVat);

        return true;
    }

    public List<HoaDonVatResponse> getHoaDonVatByMaBooKing(String maBooKing) {
        return hoaDonVatRepository.getHoaDonVatByMaBooKing(maBooKing)
                .stream().map(hoaDonVatMapper::toResponseHoaDonVat).collect(Collectors.toList());
    }

    @Transactional
    public boolean updateHoaDonVat(HoaDonVatRequest request) {

        HoaDonVat hoaDonVat = hoaDonVatRepository.findById(request.getMaHoaDonVat())
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

        if(request == null)
            throw new AppException(ErrorCode.CHI_TIET__HOA_DON);

        HoaDonVat hoaDonVatupdate = hoaDonVatMapper.toReactedHoaDonVat(request);
        hoaDonVatupdate.setMaHoaDonVat(hoaDonVat.getMaHoaDonVat());
        hoaDonVatupdate.setThoiGianTao(hoaDonVat.getThoiGianTao());
        hoaDonVatupdate.setNguoiTao(hoaDonVat.getNguoiTao());

      if(hoaDonVat.getTenCongTy() == null || hoaDonVat.getTenCongTy().isEmpty()) {
        throw new AppException(ErrorCode.TEN_CONG_TY_NOT_FOUND);
      }
        if(!hoaDonVat.getLoaiGiayTo().equals("KHONG")) {
            if (hoaDonVat.getDiaChi() == null || hoaDonVat.getDiaChi().isEmpty()) {
                throw new AppException(ErrorCode.TEN_CONG_TY_NOT_FOUND);
            }

            if (hoaDonVat.getCodeVat().isEmpty()) {

                if (hoaDonVat.getLoaiGiayTo().equals("CCCD")) {
                    if (hoaDonVat.getCccd().length() != 12)
                        throw new AppException(ErrorCode.CCCD_NOT_VALID, "CCCD phải có 12 ký tự");
                } else {
                    if (hoaDonVat.getCccd().length() < 8)
                        throw new AppException(ErrorCode.CCCD_NOT_VALID, "Hộ chiếu phải có ít nhất 8 ký tự");
                }
            } else {
                if (hoaDonVat.getCodeVat().length() != 10 && hoaDonVat.getCodeVat().length() != 14)
                    throw new AppException(ErrorCode.CCCD_NOT_VALID, "Code Vat phải có 10 hoặc 14 ký tự");
            }
        }

        hoaDonVatRepository.save(hoaDonVatupdate);

        return true;
    }

    // tổng hợp thông tin đẩy lên viettel theo mẫu để xuất hóa đơn nháp

    public InvoiceRequest getThongTinXuatHoaDonNhap(String maBooking, String maHoaDonVat) {

        booKingRepository.findById(maBooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

        HoaDonVat  hoaDonVatCheck =  hoaDonVatRepository.findById(maHoaDonVat)
                .orElseThrow(() -> new AppException(ErrorCode.CHI_TIET__HOA_DON));

        var authentiaction = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> rawData = hoaDonVatRepository.getThongTinXuatNhapHoaDonVat(maBooking,maHoaDonVat);

            try {

                GeneralInvoiceInfo invoiceInfo = new GeneralInvoiceInfo();
                invoiceInfo.setTransactionUuid(UUID.randomUUID().toString());
                invoiceInfo.setInvoiceType("");
                invoiceInfo.setTemplateCode("1/001");
                invoiceInfo.setInvoiceSeries("C26MHK");
//                invoiceInfo.setTemplateCode("1/679");
//                invoiceInfo.setInvoiceSeries("C25TZC");
                invoiceInfo.setCurrencyCode("VND");
                invoiceInfo.setAdjustmentType("1");
                invoiceInfo.setPaymentStatus(true);
                invoiceInfo.setPaymentType("TM/CK");
                invoiceInfo.setPaymentTypeName("TM/CK");
                invoiceInfo.setCusGetInvoiceRight(true);
                invoiceInfo.setUserName(authentiaction.getName());
                invoiceInfo.setInvoiceNote(hoaDonVatCheck.getGhiChu());

                // 03-07-2025 CAP NHAT CHON LOAI GIAY TO
                BuyerInfo buyerInfo = objectMapper.readValue(rawData.get("thong_tin_nguoi_mua").toString(), BuyerInfo.class);

                if (!hoaDonVatCheck.getLoaiGiayTo().equals("KHONG")) {
                    if (hoaDonVatCheck.getCodeVat() != null && !hoaDonVatCheck.getCodeVat().isEmpty()) {

                        if (hoaDonVatCheck.getCodeVat().length() != 10 && hoaDonVatCheck.getCodeVat().length() != 14) {
                            throw new AppException(ErrorCode.CCCD_NOT_VALID, "Mã số thuế phải có 10 hoặc 14 ký tự");
                        } else {
                            buyerInfo.setBuyerIdType("");
                        }

                    } else {
                        if (hoaDonVatCheck.getLoaiGiayTo().equals("CCCD")) {

                            if (hoaDonVatCheck.getCccd() != null && !hoaDonVatCheck.getCccd().isEmpty()) {
                                buyerInfo.setBuyerIdType("1");
                            } else {
                                throw new AppException(ErrorCode.CCCD_NOT_VALID, "CCCD không được để trống khi không có mã số thuế");
                            }
                        } else {
                            if (hoaDonVatCheck.getCccd() != null && !hoaDonVatCheck.getCccd().isEmpty()) {
                                buyerInfo.setBuyerIdType("2");
                            } else {
                                throw new AppException(ErrorCode.CCCD_NOT_VALID, "Hộ chiếu không được để trống khi không có mã số thuế");
                            }
                        }
                    }
                }

                SellerInfo sellerInfo = objectMapper.readValue(rawData.get("thong_tin_nguoi_ban").toString(), SellerInfo.class);

                InvoiceRequest results = new InvoiceRequest();

                int count = dichVuVATCustomeRepository.countThongTinDichVuVAT(maHoaDonVat);

                List<ItemInfo> danhSachDichVu = new ArrayList<>();

                if (count > 0) {
                    List<DichVuVATCusTome> danhSachDichVuVAT = dichVuVATCustomeRepository.getThongTinDichVuVAT(maHoaDonVat);

                    for (DichVuVATCusTome item : danhSachDichVuVAT) {
                        ItemInfo itemInfo = new ItemInfo();
                        itemInfo.setItemCode(item.getItemCode());
                        itemInfo.setItemName(item.getItemName());
                        itemInfo.setUnitName(item.getUnitName());
                        itemInfo.setQuantity(BigDecimal.valueOf(item.getQuantity()));
                        itemInfo.setUnitPrice(item.getUnitPrice());
                        itemInfo.setItemTotalAmountWithoutTax(
                                BigDecimal.valueOf(item.getQuantity()).multiply(item.getUnitPrice())
                        );

                        danhSachDichVu.add(itemInfo);
                    }
                    // Use danhSachDichVu as needed
                } else {
                     danhSachDichVu = objectMapper.readValue(
                            rawData.get("thong_tin_dich_vu").toString(),
                            new TypeReference<List<ItemInfo>>() {}
                    );
                    // Use danhSachDichVu as needed
                }


                // Chỉnh sửa thông tin dịch vụ khi thue là 8%

                if(danhSachDichVu.isEmpty()) {
                    throw new AppException(ErrorCode.CHI_TIET__HOA_DON, "Không có thông tin dịch vụ nào để xuất hóa đơn");
                }

                List<InvoiceCalculator.InvoiceItem> items = new ArrayList<>();


                // In chi tiết từng dòng
                for (InvoiceCalculator.InvoiceItem item : items) {
                    System.out.println(item);
                }

                for (ItemInfo item : danhSachDichVu) {
                    items.add(new InvoiceCalculator.InvoiceItem(item.getItemName(), new BigDecimal(String.valueOf(item.getQuantity())), new BigDecimal(String.valueOf(item.getUnitPrice()))));

                }

                // 10-07-2025 BO SUNG MATADATA TRUYEN NOTE
                List<MetaData> metaDataList = new ArrayList<>();
                MetaData metaData = new MetaData();
                metaData.setKeyTag("Note");
                metaData.setStringValue(hoaDonVatCheck.getGhiChu());
                metaData.setKeyLabel("Ghi chú");
                metaData.setValueType("text");
                metaDataList.add(metaData);

              // ✅ TÍNH TỔNG TIỀN GỐC (ĐÃ CÓ VAT) từ danh sách dịch vụ ban đầu
              BigDecimal totalPayment = danhSachDichVu.stream()
                .map(item -> item.getUnitPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);


              BigDecimal vatRate = new BigDecimal("8");
              BigDecimal sumWithoutVat = BigDecimal.ZERO;
              BigDecimal sumVat = BigDecimal.ZERO;
              BigDecimal sumWithVat = BigDecimal.ZERO;


                List<ItemInfo> updatedDanhSachDichVu = InvoiceCalculator.calculateWithoutVatFromItemInfo(danhSachDichVu, vatRate);

// In ra từng dòng để kiểm tra
                for (ItemInfo item : updatedDanhSachDichVu) {
                  System.out.println("Tên DV: " + item.getItemName());
                  System.out.println("SL: " + item.getQuantity());
                  System.out.println("Tổng có VAT: " + item.getItemTotalAmountWithoutTax().add(item.getTaxPercentage()));
                  System.out.println("Tổng chưa VAT: " + item.getItemTotalAmountWithoutTax());
                  System.out.println("Đơn giá chưa VAT: " + item.getUnitPrice());
                  System.out.println("VAT: " + item.getTaxPercentage());
                  System.out.println("------");

                  sumWithVat = sumWithVat.add(item.getItemTotalAmountWithoutTax().add(item.getTaxPercentage()));
                  sumWithoutVat = sumWithoutVat.add(item.getItemTotalAmountWithoutTax());
                  sumVat = sumVat.add(item.getTaxPercentage());
                }
              System.out.println("== Tổng cộng ==");
              System.out.println("Tổng chưa VAT: " + sumWithoutVat);
              System.out.println("Tổng VAT: " + sumVat);
              System.out.println("Tổng có VAT (tính lại): " + sumWithVat);
              log.info("Tổng payment gốc: " + totalPayment);

              if (sumWithVat.compareTo(totalPayment) == 0) {
                log.info("✅ Tổng tiền KHỚP sau khi tính lại VAT.");
              } else {
                log.error("❌ Tổng tiền KHÔNG khớp! Chênh lệch: " + sumWithVat.subtract(totalPayment));
              }

                List<Payment> danhSachThanhToan = objectMapper.readValue(
                        rawData.get("payments").toString(),
                        new TypeReference<List<Payment>>() {}
                );


              List<TaxBreakdown> danhSachThue = new ArrayList<>();

              TaxBreakdown taxBreakdown = new TaxBreakdown();
              taxBreakdown.setTaxPercentage(BigDecimal.valueOf(8)); // % VAT
              taxBreakdown.setTaxableAmount(sumWithoutVat);         // Tiền tính thuế (chưa VAT)
              taxBreakdown.setTaxAmount(sumVat);                    // Số tiền thuế

              danhSachThue.add(taxBreakdown);


              results.setGeneralInvoiceInfo(invoiceInfo);
                results.setBuyerInfo(buyerInfo);
                results.setSellerInfo(sellerInfo);
                results.setItemInfo(updatedDanhSachDichVu);
                results.setTaxBreakdowns(danhSachThue);
                results.setPayments(danhSachThanhToan);
                results.setMetadata(metaDataList);


                HoaDonVatNhap hoaDonVatNhap = new HoaDonVatNhap();
                hoaDonVatNhap.setNgayLap(LocalDateTime.now());
                hoaDonVatNhap.setTongThanhToan(sumWithVat);
                hoaDonVatNhap.setMaBooking(maBooking);
                hoaDonVatNhap.setNguoiTao(authentiaction.getName()); // hardcoded, có thể lấy từ Auth sau này
                hoaDonVatNhap.setHoaDonVat(hoaDonVatCheck);


                // Chuyển đổi dữ liệu sang JSON
                try {
                    hoaDonVatNhap.setThongTinDichVu(objectMapper.writeValueAsString(danhSachDichVu));
                    hoaDonVatNhap.setThongTinNguoiMua(objectMapper.writeValueAsString(buyerInfo));
                    hoaDonVatNhap.setThongTinNguoiBan(objectMapper.writeValueAsString(sellerInfo));
                    hoaDonVatNhap.setPayments(objectMapper.writeValueAsString(danhSachThanhToan));
                    hoaDonVatNhap.setTaxPercentage(objectMapper.writeValueAsString(danhSachThue));
                    hoaDonVatNhap.setInvoiceInfo(objectMapper.writeValueAsString(invoiceInfo));

                } catch (Exception e) {
                    throw new RuntimeException("Lỗi khi chuyển đổi dữ liệu sang JSON: " + e.getMessage(), e);
                }

                // Lưu vào DB
                try {
                    hoaDonVatNhapRepository.save(hoaDonVatNhap);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi khi lưu HoaDonVatNhap vào database: " + e.getMessage(), e);
                }


                return results;


            } catch (Exception e) {
                e.printStackTrace();
            }

        return null;

    }

    public InvoiceRequest getThongTinXuatHoaDonXacNhan(String maHoaDonVat) {

        hoaDonVatRepository.findById(maHoaDonVat)
                .orElseThrow(() -> new AppException(ErrorCode.CHI_TIET__HOA_DON));

        Map<String, Object> rawData = hoaDonVatNhapRepository.getHoaDonVatNhap(maHoaDonVat);

        try {

            GeneralInvoiceInfo invoiceInfo = objectMapper.readValue(rawData.get("thong_tin_hoa_don").toString(), GeneralInvoiceInfo.class);
            BuyerInfo buyerInfo = objectMapper.readValue(rawData.get("thong_tin_nguoi_mua").toString(), BuyerInfo.class);
            SellerInfo sellerInfo = objectMapper.readValue(rawData.get("thong_tin_nguoi_ban").toString(), SellerInfo.class);

            List<ItemInfo> danhSachDichVu = objectMapper.readValue(
                    rawData.get("thong_tin_dich_vu").toString(),
                    new TypeReference<List<ItemInfo>>() {}
            );
            List<TaxBreakdown> danhSachThue = objectMapper.readValue(
                    rawData.get("taxpercentage").toString(),
                    new TypeReference<List<TaxBreakdown>>() {}
            );
            List<Payment> danhSachThanhToan = objectMapper.readValue(
                    rawData.get("payments").toString(),
                    new TypeReference<List<Payment>>() {}
            );


            invoiceInfo.setTransactionUuid(UUID.randomUUID().toString());

            InvoiceRequest results = new InvoiceRequest();
            results.setGeneralInvoiceInfo(invoiceInfo);
            results.setBuyerInfo(buyerInfo);
            results.setSellerInfo(sellerInfo);
            results.setItemInfo(danhSachDichVu);
            results.setTaxBreakdowns(danhSachThue);
            results.setPayments(danhSachThanhToan);

            return results;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;



    }


    public List<Map<String, Object>> getListCodeVat() {
        return hoaDonVatRepository.getListCodeVat();
    }

    public  List<ItemInfo> getThongTinDichVuVAT(String ma_booking,String ma_hoadonvat) throws JsonProcessingException {

        Map<String, Object> rawData = hoaDonVatRepository.getThongTinDichVuVAT(ma_booking);

        List<ItemInfo> danhSachDichVu = objectMapper.readValue(
                rawData.get("get_thong_tin_dich_vu_vat").toString(),
                new TypeReference<List<ItemInfo>>() {}
        );

        return danhSachDichVu;
    }
}
