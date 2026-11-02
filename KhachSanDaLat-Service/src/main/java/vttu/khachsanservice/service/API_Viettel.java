package vttu.khachsanservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import vttu.khachsanservice.config.AES;
import vttu.khachsanservice.config.ReadFileWithBufferedReader;
import vttu.khachsanservice.dto.xuathoadon.InvoiceRequest;
import vttu.khachsanservice.entity.HoaDonVat;
import vttu.khachsanservice.entity.HoaDonVatPhatHanh;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.HoaDonVatNhapRepository;
import vttu.khachsanservice.repository.HoaDonVatPhatHanhRepository;
import vttu.khachsanservice.repository.HoaDonVatRepository;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
@Slf4j
public class API_Viettel {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final HoaDonVatNhapRepository hoaDonVatNhapRepository;
    private final HoaDonVatPhatHanhRepository hoaDonVatPhatHanhRepository;
    private final HoaDonVatRepository hoaDonVatRepository;

    private final String maSoThueLogin;
    private final String maSoThue;
    private final String user;
   // String maSoThue = "5801578218";
//    String mk = "2wsxCDE#"; // Mật khẩu xác thực, cần được bảo mật
  //  String user = "5801578218"; // Mã số thuế của doanh nghiệp
    // Mã số thuế và mật khẩu xác thực cần được bảo mật, không nên hardcode trong mã nguồn
    // Cập nhật thông tin xác thực tại đây hoặc sử dụng biến môi trường
    // hoặc cấu hình bên ngoài để bảo mật hơn
    // Ví dụ: sử dụng biến môi trường

//    String maSoThue = "0100109106-509";
//    String user = "0100109106-509";
//    String matKhau = "2wsxCDE#"; // Mật khẩu xác thực, cần được bảo mật
public API_Viettel(WebClient.Builder webClientBuilder,
                   ObjectMapper objectMapper,
                   HoaDonVatNhapRepository hoaDonVatNhapRepository,
                   HoaDonVatPhatHanhRepository hoaDonVatPhatHanhRepository,
                   HoaDonVatRepository hoaDonVatRepository,
                   @Value("${viettel.location}") String location,
                   @Value("${maSoThue.daLat}") String maSoThueDaLat,
                   @Value("${maSoThue.vungTau}") String maSoThueVungTau,
                   @Value("${mst.mkDaLat}") String mkDaLat,
                   @Value("${mst.mkVungTau}") String mkVungTau,
                    @Value("${maSoThueLogin.vungTau}") String maSoThueLoginVungTau,
                   @Value("${maSoThueLogin.daLat}") String maSoThueLoginDaLat)

    {

        String setKey = "Aa@123456";
        String salt = "khangkhongnoi";

        String selectMaSoThueLogin;
        String selectedMaSoThue;
        String selectedEncryptedPassword;

        if ("daLat".equalsIgnoreCase(location)) {
            selectMaSoThueLogin = maSoThueLoginDaLat;
            selectedMaSoThue = maSoThueDaLat;
            selectedEncryptedPassword = mkDaLat;
        } else if ("vungTau".equalsIgnoreCase(location)) {
            selectMaSoThueLogin = maSoThueLoginVungTau;
            selectedMaSoThue = maSoThueVungTau;
            selectedEncryptedPassword = mkVungTau;
        } else {
            throw new IllegalArgumentException("Địa điểm Viettel không hợp lệ: " + location);
        }
        this.maSoThueLogin = selectMaSoThueLogin;
        this.maSoThue = selectedMaSoThue;
        this.user = selectedMaSoThue;

        String decryptedPassword = AES.decrypt(selectedEncryptedPassword, setKey, salt);

        if (decryptedPassword == null) {
            throw new IllegalArgumentException("Không thể giải mã mật khẩu Viettel cho địa điểm: " + location);
        }
        this.webClient = webClientBuilder
                .baseUrl("https://api-vinvoice.viettel.vn")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(headers -> headers.setBasicAuth(maSoThueLogin, decryptedPassword)) // Cập nhật thông tin xác thực
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        this.objectMapper = objectMapper;
        this.hoaDonVatNhapRepository = hoaDonVatNhapRepository;
        this.hoaDonVatPhatHanhRepository = hoaDonVatPhatHanhRepository;
        this.hoaDonVatRepository = hoaDonVatRepository;
    }

    @Transactional
    public ResponseEntity<?> createAndDownloadInvoice(String ma_hoadon_vat, InvoiceRequest request) throws UnsupportedEncodingException {
        // Log payload để kiểm tra
        try {
            log.info("Payload JSON: {}", objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.error("Lỗi khi log payload: {}", e.getMessage());
        }

        HoaDonVat hd = hoaDonVatRepository.findById(ma_hoadon_vat)
                .orElseThrow(() -> new AppException(ErrorCode.CHI_TIET__HOA_DON));

        if (hd.isXacNhan())
            throw new AppException(ErrorCode.ERROR_HOA_DON_DA_XAC_NHAN);

        try {

            // Bước 1: Gọi API tạo hóa đơn nháp
            Map<String, Object> draftResponse = webClient.post()
                    .uri("/services/einvoiceapplication/api/InvoiceAPI/InvoiceWS/createOrUpdateInvoiceDraft/" + maSoThue)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> {
                                        try {
                                            Map<String, Object> errorResponse = objectMapper.readValue(body, Map.class);
                                            String message = (String) errorResponse.getOrDefault("message", "Lỗi không xác định");
                                            String data = (String) errorResponse.getOrDefault("data", "");
                                            return new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, message + ": " + data);
                                        } catch (Exception e) {
                                            return new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Lỗi client: " + body);
                                        }
                                    }))
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Lỗi server: " + body)))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            // Log phản hồi từ API tạo hóa đơn nháp
//            log.info("Phản hồi từ API createOrUpdateInvoiceDraft: {}", draftResponse);

            // Kiểm tra phản hồi
            if (draftResponse == null || !draftResponse.containsKey("errorCode") || !draftResponse.get("errorCode").equals("")) {
                log.error("Lỗi khi tạo hóa đơn nháp: {}", draftResponse);
                throw new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Không thể tạo hóa đơn nháp");
            }

            // Bước 2: Gọi API lấy file PDF
            Map<String, Object> previewResponse = webClient.post()
                    .uri("/services/einvoiceapplication/api/InvoiceAPI/InvoiceUtilsWS/createInvoiceDraftPreview/" + maSoThue)
                    .bodyValue(request) // Sử dụng cùng payload
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> {
                                        try {
                                            Map<String, Object> errorResponse = objectMapper.readValue(body, Map.class);
                                            String message = (String) errorResponse.getOrDefault("message", "Lỗi không xác định");
                                            String data = (String) errorResponse.getOrDefault("data", "");
                                            return new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, message + ": " + data);
                                        } catch (Exception e) {
                                            return new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Lỗi client: " + body);
                                        }
                                    }))
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Lỗi server: " + body)))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            // Log phản hồi từ API lấy file PDF
//            log.info("Phản hồi từ API createInvoiceDraftPreview: {}", previewResponse);

            // Kiểm tra phản hồi
            if (previewResponse == null || !previewResponse.containsKey("fileToBytes") || !previewResponse.containsKey("fileName")) {
                log.error("Phản hồi không chứa 'fileToBytes' hoặc 'fileName': {}", previewResponse);
                throw new AppException(ErrorCode.ERROR_NO_FILE_RETURNED);
            }

            // Giải mã file PDF từ Base64
            byte[] fileBytes = Base64.getDecoder().decode((String) previewResponse.get("fileToBytes"));
            String fileName = (String) previewResponse.get("fileName");

            // Mã hóa tên file
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_PDF);
            responseHeaders.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build());


            if (HttpStatus.OK.value() == 200) {

                hoaDonVatRepository.updateDaInNhapHoaDonVat(true, hd.getSoLuongInNhap() + 1, ma_hoadon_vat);
            }

            return new ResponseEntity<>(fileBytes, responseHeaders, HttpStatus.OK);

        } catch (AppException e) {
            // Log lỗi và ném ngoại lệ để xử lý ở tầng cao hơn
            log.error("Lỗi khi gọi API Viettel: {}", e.getMessage());
            throw new AppException(ErrorCode.ERROR_SYSTEM, "Lỗi khi gọi API Viettel: " + e.getMessage());
        }
    }

    @Transactional
    public boolean createInvoicePhatHanh(String ma_hoadon_vat, InvoiceRequest request) {

        HoaDonVat hoaDonVat = hoaDonVatRepository.findById(ma_hoadon_vat)
                .orElseThrow(() -> new AppException(ErrorCode.CHI_TIET__HOA_DON));

        if(hoaDonVat.isXacNhan())
            throw new AppException(ErrorCode.ERROR_HOA_DON_DA_XAC_NHAN);

        int hoaDonPhatHanh = hoaDonVatPhatHanhRepository.countByMaHoaDonVat(ma_hoadon_vat);

        if(hoaDonPhatHanh > 0)
            throw new AppException(ErrorCode.ERROR_HOA_DON_DA_PHAT_HANH);

        try {
            log.info("Payload JSON: {}", objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.error("Lỗi khi log payload: {}", e.getMessage());
        }

        if (request.getGeneralInvoiceInfo().getTransactionUuid() == null || request.getGeneralInvoiceInfo().getTransactionUuid().isEmpty()) {
            log.error("Transaction UUID không được để trống");
            return false;
        }

        try {
            // Bước 1: Gọi API tạo hóa đơn
            Map<String, Object> draftResponse = webClient.post()
                    .uri("/services/einvoiceapplication/api/InvoiceAPI/InvoiceWS/createInvoice/" + maSoThue)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> {
                                        try {
                                            Map<String, Object> errorResponse = objectMapper.readValue(body, Map.class);
                                            String message = (String) errorResponse.getOrDefault("message", "Lỗi không xác định");
                                            String data = (String) errorResponse.getOrDefault("data", "");
                                            log.error("Lỗi client khi tạo hóa đơn: {} - {}", message, data);
                                        } catch (Exception ex) {
                                            log.error("Lỗi client khi tạo hóa đơn (raw): {}", body);
                                        }
                                        return null;
                                    }))
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> {
                                        log.error("Lỗi server khi gọi API tạo hóa đơn: {}", body);
                                        return null;
                                    }))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            // Kiểm tra phản hồi
            if (draftResponse == null) {
                log.error("Lỗi khi tạo hóa đơn: {}", draftResponse);
                return false;
            }

            Map<String, Object> result = (Map<String, Object>) draftResponse.get("result");
            if (result == null) {
                log.error("Phản hồi không chứa trường 'result'");
                return false;
            }

            String supplierTaxCode = (String) result.get("supplierTaxCode");
            String invoiceNo = (String) result.get("invoiceNo");
            String transactionID = result.get("transactionID") != null ? result.get("transactionID").toString() : null;
            String reservationCode = result.get("reservationCode") != null ? result.get("reservationCode").toString() : null;

            if (invoiceNo == null || transactionID == null) {
                log.error("Thiếu invoiceNo hoặc transactionID trong phản hồi: {}", result);
                return false;
            }

            log.info("Lấy được invoiceNo: {}, transactionID: {}", invoiceNo, transactionID);

            HoaDonVatPhatHanh hoaDonVatPhatHanh = new HoaDonVatPhatHanh();
            hoaDonVatPhatHanh.setInvoiceNo(invoiceNo);
            hoaDonVatPhatHanh.setTransactionID(transactionID);
            hoaDonVatPhatHanh.setReservationCode(reservationCode);
            hoaDonVatPhatHanh.setTransactionUUID(request.getGeneralInvoiceInfo().getTransactionUuid());
            hoaDonVatPhatHanh.setSupplierTaxCode(supplierTaxCode);
            hoaDonVatPhatHanh.setHoaDonVat(hoaDonVat);
            hoaDonVatPhatHanh.setTemplateCode(hoaDonVat.getMauHoaDon());

            hoaDonVatPhatHanhRepository.save(hoaDonVatPhatHanh);

            // cập nhật hóa đơn vat sang phát hành
            int updatePhatHanhHoaDonVat = hoaDonVatRepository.updatePhatHanhHoaDonVat(true, ma_hoadon_vat);
            if (updatePhatHanhHoaDonVat == 0) {
                log.warn("Không thể cập nhật phát hành hóa đơn vat : {}", ma_hoadon_vat);
                throw new AppException(ErrorCode.ERROR_PHAT_HANH_HOA_DON);
            }

            return true;

        } catch (Exception e) {
            log.error("Lỗi khi gọi API hoặc lưu DB: {}", e.getMessage(), e);
            return false;
        }
    }


    // API lấy issueDate theo transactionUuid

    public ResponseEntity<?> xuatPDFHoaDonPhatHanh(String maHoaDonVat) throws UnsupportedEncodingException {
        // Tìm hóa đơn VAT
        HoaDonVat hoaDonVat = hoaDonVatRepository.findById(maHoaDonVat)
                .orElseThrow(() -> new AppException(ErrorCode.HOA_DON_VAT_NOT_FOUND));

        if (!hoaDonVat.isXacNhan())
            throw new AppException(ErrorCode.ERROR_HOA_DON_CHUA_PHAT_HANH);

        // Lấy thông tin hóa đơn phát hành
        HoaDonVatPhatHanh hoaDonVatPhatHanh = hoaDonVatPhatHanhRepository.getThongTinHoaDonTheoHoaDonVat(maHoaDonVat);
        if (hoaDonVatPhatHanh.getTransactionUUID() == null || hoaDonVatPhatHanh.getTransactionUUID().isEmpty()) {
            throw new AppException(ErrorCode.ERROR_NOT_EMPTY_TransactionUuid, "TransactionUUID không được rỗng");
        }

        try {
            // Bước 1: Gọi API tìm hóa đơn bằng transactionUuid
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("transactionUuid", hoaDonVatPhatHanh.getTransactionUUID());
            formData.add("supplierTaxCode", maSoThue);

            Map<String, Object> searchResponse = webClient.post()
                    .uri("/services/einvoiceapplication/api/InvoiceAPI/InvoiceWS/searchInvoiceByTransactionUuid")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> {
                                        try {
                                            Map<String, Object> errorResponse = objectMapper.readValue(body, Map.class);
                                            String message = (String) errorResponse.getOrDefault("message", "Lỗi không xác định");
                                            String data = (String) errorResponse.getOrDefault("data", "");
                                            return new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, message + ": " + data);
                                        } catch (Exception e) {
                                            return new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Lỗi client: " + body);
                                        }
                                    }))
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Lỗi server: " + body)))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            // Log phản hồi từ API tìm hóa đơn
            log.info("Phản hồi từ API searchInvoiceByTransactionUuid: {}", searchResponse);

            // Kiểm tra phản hồi
            if (searchResponse == null || searchResponse.get("errorCode") != null) {
                log.error("Lỗi khi tìm hóa đơn: {}", searchResponse);
                throw new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Không thể tìm hóa đơn theo transactionUuid");
            }

            // Lấy issueDate và invoiceNo từ result
            List<Map<String, Object>> result = (List<Map<String, Object>>) searchResponse.get("result");
            if (result == null || result.isEmpty()) {
                log.error("Phản hồi không chứa mảng 'result' hoặc mảng rỗng: {}", searchResponse);
                throw new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Không tìm thấy thông tin hóa đơn");
            }

            Map<String, Object> invoiceData = result.get(0);
            Long issueDateMillis = (Long) invoiceData.get("issueDate");
            String invoiceNo = (String) invoiceData.get("invoiceNo");
            if (issueDateMillis == null || invoiceNo == null) {
                log.error("Thiếu trường issueDate hoặc invoiceNo trong result: {}", invoiceData);
                throw new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Không tìm thấy issueDate hoặc invoiceNo");
            }

            log.info("Lấy được issueDate: {}, invoiceNo: {}", issueDateMillis, invoiceNo);

            // Bước 2: Gọi API lấy file PDF
            MultiValueMap<String, String> pdfFormData = new LinkedMultiValueMap<>();
            pdfFormData.add("supplierTaxCode", maSoThue);
            pdfFormData.add("invoiceNo", invoiceNo);
            pdfFormData.add("strIssueDate", String.valueOf(issueDateMillis));
            pdfFormData.add("exchangeUser", "Golden Era"); // Giả định, thay bằng người dùng thực tế nếu cần

            Map<String, Object> previewResponse = webClient.post()
                    .uri("/services/einvoiceapplication/api/InvoiceAPI/InvoiceWS/createExchangeInvoiceFile")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(pdfFormData)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> {
                                        try {
                                            Map<String, Object> errorResponse = objectMapper.readValue(body, Map.class);
                                            String message = (String) errorResponse.getOrDefault("message", "Lỗi không xác định");
                                            String data = (String) errorResponse.getOrDefault("data", "");
                                            return new AppException(ErrorCode.ERROR_NO_FILE_RETURNED, message + ": " + data);
                                        } catch (Exception e) {
                                            return new AppException(ErrorCode.ERROR_NO_FILE_RETURNED, "Lỗi client: " + body);
                                        }
                                    }))
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new AppException(ErrorCode.ERROR_NO_FILE_RETURNED, "Lỗi server: " + body)))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            // Log phản hồi từ API lấy file PDF
            log.info("Phản hồi từ API createExchangeInvoiceFile: {}", previewResponse);

            // Kiểm tra phản hồi
            if (previewResponse == null || !previewResponse.containsKey("fileToBytes") || !previewResponse.containsKey("fileName")) {
                log.error("Phản hồi không chứa 'fileToBytes' hoặc 'fileName': {}", previewResponse);
                throw new AppException(ErrorCode.ERROR_NO_FILE_RETURNED, "Không thể lấy file PDF");
            }

            // Giải mã file PDF từ Base64
            byte[] fileBytes = Base64.getDecoder().decode((String) previewResponse.get("fileToBytes"));
            String fileName = (String) previewResponse.get("fileName");

            // Mã hóa tên file
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_PDF);
            responseHeaders.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build());
            responseHeaders.setCacheControl("no-cache, no-store, must-revalidate");
            responseHeaders.setPragma("no-cache");
            responseHeaders.setExpires(0);

            return new ResponseEntity<>(fileBytes, responseHeaders, HttpStatus.OK);

        } catch (AppException e) {
            log.error("Lỗi khi gọi API Viettel: {}", e.getMessage());

            throw new AppException(ErrorCode.ERROR_SYSTEM, "Lỗi khi gọi API Viettel: " + e.getMessage());

        } catch (Exception e) {
            log.error("Lỗi hệ thống: {}", e.getMessage());
            throw new AppException(ErrorCode.ERROR_SYSTEM, "Lỗi khi xuất PDF hóa đơn: " + e.getMessage());
        }
    }


    public class DateConversion {
        public static String convertLocalDateToMillisString(LocalDate localDate) {
            // Convert LocalDate to LocalDateTime at the start of the day
            LocalDateTime localDateTime = localDate.atStartOfDay();

            // Convert LocalDateTime to Instant using the system's default time zone
            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();

            // Get the epoch milliseconds from the Instant and convert to String
            return String.valueOf(instant.toEpochMilli());
        }
    }




    @Transactional
    public ResponseEntity<?> cancelTransactionInvoice(String maHoaDonVAT,String vanBanThoaThuan, LocalDate ngayThoaThuan, String lyDo) {

        HoaDonVat hoaDonVat = hoaDonVatRepository.findById(maHoaDonVAT)
                .orElseThrow(() -> new AppException(ErrorCode.ERROR_MA_HOA_DON_VAT));

        if(hoaDonVat.isDaHuy())
            throw new AppException(ErrorCode.ERROR_HOA_DON_DA_HUY);

        if(vanBanThoaThuan.isEmpty())
            throw new AppException(ErrorCode.VAN_BAN_THOA_THUAN_EMPTY);

        if(ngayThoaThuan == null)
            throw new AppException(ErrorCode.NGAY_THOA_THUAN_EMPTY);
        if(lyDo.isEmpty())
            throw new AppException(ErrorCode.LY_DO_EMPTY);


        // Check if the invoice has been issued
        HoaDonVatPhatHanh hoaDonVatPhatHanh = hoaDonVatPhatHanhRepository.getThongTinHoaDonTheoHoaDonVat(maHoaDonVAT);
        if (hoaDonVatPhatHanh == null) {
            throw new AppException(ErrorCode.ERROR_HOA_DON_CHUA_PHAT_HANH);
        }

        // Bước 1: Gọi API tìm hóa đơn bằng transactionUuid
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("transactionUuid", hoaDonVatPhatHanh.getTransactionUUID());
        formData.add("supplierTaxCode", maSoThue);

        Map<String, Object> searchResponse = webClient.post()
                .uri("/services/einvoiceapplication/api/InvoiceAPI/InvoiceWS/searchInvoiceByTransactionUuid")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(body -> {
                                    try {
                                        Map<String, Object> errorResponse = objectMapper.readValue(body, Map.class);
                                        String message = (String) errorResponse.getOrDefault("message", "Lỗi không xác định");
                                        String data = (String) errorResponse.getOrDefault("data", "");
                                        return new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, message + ": " + data);
                                    } catch (Exception e) {
                                        return new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Lỗi client: " + body);
                                    }
                                }))
                .onStatus(status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(body -> new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Lỗi server: " + body)))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        // Log phản hồi từ API tìm hóa đơn
        log.info("Phản hồi từ API searchInvoiceByTransactionUuid: {}", searchResponse);

        // Kiểm tra phản hồi
        if (searchResponse == null || searchResponse.get("errorCode") != null) {
            log.error("Lỗi khi tìm hóa đơn: {}", searchResponse);
            throw new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Không thể tìm hóa đơn theo transactionUuid");
        }

        // Lấy issueDate và invoiceNo từ result
        List<Map<String, Object>> result = (List<Map<String, Object>>) searchResponse.get("result");
        if (result == null || result.isEmpty()) {
            log.error("Phản hồi không chứa mảng 'result' hoặc mảng rỗng: {}", searchResponse);
            throw new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Không tìm thấy thông tin hóa đơn");
        }

        Map<String, Object> invoiceData = result.get(0);
        Long issueDateMillis = (Long) invoiceData.get("issueDate");
        String invoiceNo = (String) invoiceData.get("invoiceNo");
        if (issueDateMillis == null || invoiceNo == null) {
            log.error("Thiếu trường issueDate hoặc invoiceNo trong result: {}", invoiceData);
            throw new AppException(ErrorCode.ERROR_GUI_API_TAO_HOA_DON_NHAP, "Không tìm thấy issueDate hoặc invoiceNo");
        }

        log.info("Lấy được issueDate: {}, invoiceNo: {}", issueDateMillis, invoiceNo);


        String supplierTaxCode = hoaDonVatPhatHanh.getSupplierTaxCode();
        String templateCode = hoaDonVatPhatHanh.getTemplateCode();

        try {
            // Prepare form data
            MultiValueMap<String, String> formDataHuy = new LinkedMultiValueMap<>();
            formDataHuy.add("supplierTaxCode", supplierTaxCode);
            formDataHuy.add("templateCode", templateCode);
            formDataHuy.add("invoiceNo", invoiceNo);
            formDataHuy.add("strIssueDate", issueDateMillis.toString());
            formDataHuy.add("additionalReferenceDesc", vanBanThoaThuan);
            formDataHuy.add("additionalReferenceDate", DateConversion.convertLocalDateToMillisString(ngayThoaThuan));
            formDataHuy.add("reasonDelete", lyDo);

            // Make the API call
            Map<String, Object> response = webClient.post()
                    .uri("/services/einvoiceapplication/api/InvoiceAPI/InvoiceWS/cancelTransactionInvoice")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formDataHuy)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> {
                                        try {
                                            Map<String, Object> errorResponse = objectMapper.readValue(body, Map.class);
                                            String message = (String) errorResponse.getOrDefault("message", "Lỗi không xác định");
                                            return new AppException(ErrorCode.ERROR_CANCEL_INVOICE, message);
                                        } catch (Exception e) {
                                            return new AppException(ErrorCode.ERROR_CANCEL_INVOICE, "Lỗi client: " + body);
                                        }
                                    }))
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new AppException(ErrorCode.ERROR_CANCEL_INVOICE, "Lỗi server: " + body)))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            // Log and check response
            log.info("Phản hồi từ API cancelTransactionInvoice: {}", response);

            if (response == null || response.get("errorCode") != null) {
                log.error("Lỗi khi hủy hóa đơn: {}", response);
                throw new AppException(ErrorCode.ERROR_CANCEL_INVOICE, "Không thể hủy hóa đơn");
            }

            String description = (String) response.get("description");
            log.info("Description: {}", description);

            if(description.equals("CANCEL TRANSACTION INVOICE SUCCESS"))
            {
                // Cập nhật trạng thái của hóa đơn phát hành thành đã hủy
                hoaDonVatPhatHanhRepository.updateDaHuyHoaDonVatPhatHanh(true, maHoaDonVAT);
              return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            else{
                throw new AppException(ErrorCode.ERROR_CANCEL_INVOICE, description);
            }

        } catch (AppException e) {
            log.error("Lỗi khi gọi API Viettel: {}", e.getMessage());
            throw new AppException(ErrorCode.ERROR_SYSTEM, "Lỗi khi gọi API Viettel: " + e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi hệ thống: {}", e.getMessage());
            throw new AppException(ErrorCode.ERROR_SYSTEM, "Lỗi khi hủy hóa đơn: " + e.getMessage());
        }
    }
}

