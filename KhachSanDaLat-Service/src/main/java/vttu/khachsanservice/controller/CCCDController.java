package vttu.khachsanservice.controller;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vttu.khachsanservice.dto.response.CCCDInfoResponse;
import vttu.khachsanservice.entity.TaiKhoanOcr;
import vttu.khachsanservice.service.TaiKhoanOcrService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/cccd")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CCCDController {

    TaiKhoanOcrService taiKhoanOcrService;
    @PostMapping
    public ResponseEntity<?> extractCCCD(@RequestParam("image") MultipartFile file) {

        if (file.isEmpty()) {
            System.out.println("Uploaded file is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Vui lòng tải lên một tệp hình ảnh.");
        }

        // === Kiểm tra tài khoản OCR còn hạn mức ===
        TaiKhoanOcr ocrAccount = taiKhoanOcrService.getAvailableAccount();
        if (ocrAccount == null) {
            System.out.println("❌ Không còn tài khoản OCR nào khả dụng (đã hết hạn mức tháng)");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Đã hết hạn mức OCR trong tháng này. Vui lòng thử lại vào tháng sau.");
        }

        String fileName = ocrAccount.getDuongDanCredentials();
        // Lấy thư mục config từ biến môi trường CONFIG_DIR (Docker: /app/config, local: thư mục hiện tại)
        String configDir = System.getenv("CONFIG_DIR");
        String credentialsPath;
        if (configDir != null && !configDir.isEmpty()) {
            credentialsPath = configDir + "/" + fileName;
        } else {
            credentialsPath = fileName; // Local dev: đọc từ thư mục gốc project
        }
        System.out.println("🔑 Sử dụng tài khoản OCR: " + ocrAccount.getTenTaiKhoan()
                + " (" + ocrAccount.getSoLuotDaDung() + "/" + ocrAccount.getHanMucThang() + ")"
                + " | credentials: " + credentialsPath);

        try {
            System.out.println("Bắt đầu xử lý yêu cầu CCCD OCR...");

            File fileCheck = new File(credentialsPath);
            if (!fileCheck.exists() || !fileCheck.canRead()) {
                System.err.println("❌ Không tìm thấy file credentials: " + credentialsPath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Lỗi cấu hình tài khoản OCR. Vui lòng liên hệ quản trị viên.");
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");

            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            byte[] imageBytes = file.getBytes();

            Image image = Image.newBuilder()
                    .setContent(ByteString.copyFrom(imageBytes))
                    .build();

            Feature feature = Feature.newBuilder()
                    .setType(Feature.Type.TEXT_DETECTION)
                    .build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create(settings)) {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(java.util.Arrays.asList(request));

                // === Gọi API thành công → tăng số lượt đã dùng ===
                taiKhoanOcrService.incrementUsage(ocrAccount);

                String text = response.getResponses(0).getFullTextAnnotation().getText();

                CCCDInfoResponse cccdInfo = parseTextToCCCD(text);

                if (cccdInfo != null && cccdInfo.getSoCccd() != null) {
                    System.out.println("Returning CCCD info...");
                    return ResponseEntity.ok(cccdInfo);
                } else {
                    System.out.println("No CCCD info found in image");
                    return ResponseEntity.ok("Không tìm thấy thông tin CCCD trong hình ảnh.");
                }
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đọc file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("General exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi gọi Google Vision API: " + e.getMessage());
        }
    }

    private CCCDInfoResponse parseTextToCCCD(String text) {
        CCCDInfoResponse info = new CCCDInfoResponse();
        String[] lines = text.split("\n");

        System.out.println("=== START PARSING CCCD ===");
        System.out.println("Total lines: " + lines.length);

        // Trim tất cả các dòng
        String[] trimmedLines = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            trimmedLines[i] = lines[i].trim();
        }

        // === PASS 1: Tìm số CCCD ===
        for (int i = 0; i < trimmedLines.length; i++) {
            String line = trimmedLines[i];
            if (line.isEmpty()) continue;
            // Tìm 12 chữ số trên cùng dòng với label hoặc dòng riêng
            Matcher m = Pattern.compile("(\\d{12})").matcher(line);
            if (m.find()) {
                // Kiểm tra dòng này hoặc dòng trước có chứa keyword CCCD không
                boolean hasLabel = line.matches("(?i).*(Số|No\\.?|S[o&]l?|định danh|identification|Identity).*");
                if (!hasLabel && i > 0) {
                    hasLabel = trimmedLines[i - 1].matches("(?i).*(Số|No\\.?|S[o&]l?|định danh|identification|Identity).*");
                }
                if (hasLabel || line.matches("^\\d{12}$")) {
                    info.setSoCccd(m.group(1));
                    System.out.println("✓ Found soCccd: " + m.group(1));
                    break;
                }
            }
        }

        // === PASS 2: Tìm họ tên ===
        // Tìm dòng chứa label "Họ" / "Full name"
        Pattern hoTenLabelP = Pattern.compile("(?i)(Họ.*tên|Full\\s*name)");
        Pattern namePartPattern = Pattern.compile("^[A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ][A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ\\s]*$");
        for (int i = 0; i < trimmedLines.length; i++) {
            String line = trimmedLines[i];
            if (!hoTenLabelP.matcher(line).find()) continue;

            // TH1: Tên cùng dòng với label - lấy phần sau dấu ":"
            Matcher m = Pattern.compile("(?i)(?:Họ.*tên|Full\\s*name)[^:]*:\\s*(.+?)\\s*$").matcher(line);
            if (m.find()) {
                String name = m.group(1).replaceAll("^[^A-ZÀ-Ỹa-zà-ỹ]+", "").trim();
                if (!name.isEmpty() && name.length() > 2) {
                    info.setHoVaTen(name);
                    System.out.println("✓ Found hoVaTen (same line): " + name);
                    break;
                }
            }

            // TH2: Tên ở dòng tiếp theo (có thể bị tách thành nhiều dòng)
            if (i + 1 < trimmedLines.length) {
                String nextLine = trimmedLines[i + 1];
                if (namePartPattern.matcher(nextLine).matches() && nextLine.length() > 1) {
                    StringBuilder fullName = new StringBuilder(nextLine);
                    // Kiểm tra các dòng tiếp theo có phải là phần tiếp của tên không
                    // (tên bị OCR tách ra nhiều dòng, ví dụ "PHẠM CÔNG" + "UẨN")
                    for (int j = i + 2; j < trimmedLines.length; j++) {
                        String part = trimmedLines[j];
                        // Phần tiếp của tên: chỉ chữ cái viết hoa, ngắn, không phải label
                        if (namePartPattern.matcher(part).matches()
                                && part.length() <= 20
                                && !part.matches("(?i).*(Ngày|Date|Giới|Sex|Quốc|Nation|Quê|Place|Nơi|CĂN|CỘNG|Độc|SOCIALIST).*")) {
                            fullName.append(" ").append(part);
                        } else {
                            break;
                        }
                    }
                    info.setHoVaTen(fullName.toString().trim());
                    System.out.println("✓ Found hoVaTen (multi-line): " + info.getHoVaTen());
                    break;
                }
            }
        }

        // === PASS 3: Tìm ngày sinh ===
        // Chiến lược: tìm ngày tháng gần label "Ngày sinh" / "Date of birth"
        Pattern dobLabelP = Pattern.compile("(?i)(Ngày.*sinh|Date\\s*of\\s*birth)");
        Pattern dateP = Pattern.compile("(\\d{1,2}[-/]\\d{1,2}[-/]\\d{4})");
        int dobLabelLine = -1;
        for (int i = 0; i < trimmedLines.length; i++) {
            if (dobLabelP.matcher(trimmedLines[i]).find()) {
                dobLabelLine = i;
                // TH1: Ngày sinh cùng dòng với label
                Matcher dm = dateP.matcher(trimmedLines[i]);
                if (dm.find()) {
                    info.setNgaySinh(dm.group(1));
                    System.out.println("✓ Found ngaySinh (same line " + i + "): " + info.getNgaySinh());
                }
                break;
            }
        }
        // TH2: Ngày sinh không cùng dòng - tìm trong phạm vi ±5 dòng từ label
        if (info.getNgaySinh() == null && dobLabelLine >= 0) {
            for (int i = dobLabelLine + 1; i < Math.min(dobLabelLine + 6, trimmedLines.length); i++) {
                Matcher dm = dateP.matcher(trimmedLines[i]);
                if (dm.find()) {
                    // Bỏ qua nếu dòng này là "Có giá trị" / "Date of expiry"
                    if (!trimmedLines[i].matches("(?i).*(expiry|giá trị|hạn).*")
                            && (i == 0 || !trimmedLines[i - 1].matches("(?i).*(expiry|giá trị|hạn).*"))) {
                        info.setNgaySinh(dm.group(1));
                        System.out.println("✓ Found ngaySinh (nearby line " + i + "): " + info.getNgaySinh());
                        break;
                    }
                }
            }
        }

        // === PASS 4: Tìm giới tính ===
        // Hỗ trợ nhiều format: "Sex: Nam", "Sex Nam", "Sex:\nNam"
        Pattern genderValueP = Pattern.compile("(?i)^\\s*(Nam|Nữ|Male|Female)\\s*$");
        for (int i = 0; i < trimmedLines.length; i++) {
            String line = trimmedLines[i];
            if (!line.matches("(?i).*(Giới\\s*tính|Sex).*")) continue;

            // TH1: "Giới tính / Sex: Nam" hoặc "Giới tính / Sex Nam" (có hoặc không có dấu :)
            Matcher m = Pattern.compile("(?i)(?:Giới\\s*tính|Sex)[^A-Za-zÀ-ỹ]*(Nam|Nữ|Male|Female)", Pattern.CASE_INSENSITIVE).matcher(line);
            if (m.find()) {
                info.setGioiTinh(normalizeGender(m.group(1)));
                System.out.println("✓ Found gioiTinh (same line " + i + "): " + info.getGioiTinh());
                break;
            }

            // TH2: Giới tính ở dòng tiếp theo
            if (i + 1 < trimmedLines.length) {
                Matcher gm = genderValueP.matcher(trimmedLines[i + 1]);
                if (gm.find()) {
                    info.setGioiTinh(normalizeGender(gm.group(1)));
                    System.out.println("✓ Found gioiTinh (next line " + (i + 1) + "): " + info.getGioiTinh());
                    break;
                }
            }
        }

        // === PASS 5: Tìm quốc tịch ===
        // Hỗ trợ: "Nationality: Việt Nam", "Nationalty Việt Nam", "Quốc tịch Việt Nam"
        // Lưu ý: OCR có thể bỏ dấu -> "Quoc tich", và có thể cùng dòng "Giới tính / Sex Nam Quốc tịch Nationalty Việt Nam"
        Pattern natLabelP = Pattern.compile("(?i)(Qu[oố]c\\s*t[iị]ch|Nationality|Nationalty|Nation[a-z]*)");
        for (int i = 0; i < trimmedLines.length; i++) {
            String line = trimmedLines[i];
            Matcher labelM = natLabelP.matcher(line);
            if (!labelM.find()) continue;

            // Tìm vị trí label cuối cùng trên dòng (ưu tiên Nationality/Nationalty sau cùng)
            // vì dòng có thể chứa "Quốc tịch Nationalty Việt Nam"
            int lastLabelEnd = labelM.end();
            while (labelM.find()) {
                lastLabelEnd = labelM.end();
            }

            // Lấy phần sau label quốc tịch cuối cùng
            String afterLabel = line.substring(lastLabelEnd).replaceAll("^[^A-ZÀ-Ỹa-zà-ỹ]+", "").trim();
            if (afterLabel.length() > 1 && !afterLabel.matches(".*\\d.*")) {
                info.setQuocTich(afterLabel);
                System.out.println("✓ Found quocTich (same line " + i + "): " + info.getQuocTich());
                break;
            }

            // TH2: Quốc tịch ở dòng tiếp theo
            if (i + 1 < trimmedLines.length) {
                String nextLine = trimmedLines[i + 1];
                if (nextLine.length() > 1 && !nextLine.matches(".*\\d.*")
                        && !nextLine.matches("(?i).*(Quê|Place|Nơi|Ngày|Date|Giới|Sex).*")) {
                    info.setQuocTich(nextLine.trim());
                    System.out.println("✓ Found quocTich (next line " + (i + 1) + "): " + info.getQuocTich());
                    break;
                }
            }
        }

        // === PASS 6: Tìm quê quán ===
        // Lưu ý: dòng có thể chứa cả 2 label "Quê quán" và "Place of origin"
        // VD: "Quê quán I Place of origin:" hoặc "Qué quán / Place of origin"
        // => cần tìm label CUỐI CÙNG rồi lấy text phía sau
        Pattern queQuanLabelP = Pattern.compile("(?i)(Qu[eêếéèẹẻẽ]\\s*qu[aáàạảã]n|Place\\s*of\\s*origin)");
        for (int i = 0; i < trimmedLines.length; i++) {
            String line = trimmedLines[i];
            Matcher labelM = queQuanLabelP.matcher(line);
            if (!labelM.find()) continue;

            // Tìm vị trí label cuối cùng trên dòng
            int lastLabelEnd = labelM.end();
            while (labelM.find()) {
                lastLabelEnd = labelM.end();
            }

            // TH1: Quê quán cùng dòng với label (sau dấu : của label cuối cùng)
            String afterLabel = line.substring(lastLabelEnd).replaceAll("^[^A-ZÀ-Ỹa-zà-ỹ0-9]+", "").trim();
            if (afterLabel.length() > 2) {
                info.setQueQuan(afterLabel);
                System.out.println("✓ Found queQuan (same line " + i + "): " + info.getQueQuan());
                break;
            }

            // TH2: Quê quán ở dòng tiếp theo
            if (i + 1 < trimmedLines.length) {
                String nextLine = trimmedLines[i + 1];
                if (nextLine.length() > 2
                        && !nextLine.matches("(?i).*(Place of residence|Nơi thường trú|Date|Ngày|Sex|Giới|Quốc|Nation).*")) {
                    info.setQueQuan(nextLine.trim());
                    System.out.println("✓ Found queQuan (next line " + (i + 1) + "): " + info.getQueQuan());
                    break;
                }
            }
        }

        // === FALLBACK: Nếu vẫn chưa tìm được ngày sinh, quét toàn bộ tìm ngày hợp lệ ===
        if (info.getNgaySinh() == null) {
            for (int i = 0; i < trimmedLines.length; i++) {
                String line = trimmedLines[i];
                // Bỏ qua dòng có "expiry" / "giá trị" / "hạn" / "Co " (Có giá trị)
                if (line.matches("(?i).*(expiry|giá trị|hạn|^Co\\s).*")) continue;
                if (i > 0 && trimmedLines[i - 1].matches("(?i).*(expiry|giá trị|hạn|^Co\\s).*")) continue;

                Matcher dm = dateP.matcher(line);
                if (dm.find()) {
                    String dateStr = dm.group(1);
                    // Chỉ lấy nếu ngày hợp lệ (năm sinh từ 1900-2025)
                    Matcher yearM = Pattern.compile("(\\d{4})$").matcher(dateStr);
                    if (yearM.find()) {
                        int year = Integer.parseInt(yearM.group(1));
                        if (year >= 1900 && year <= 2026) {
                            info.setNgaySinh(dateStr);
                            System.out.println("✓ Found ngaySinh (fallback scan line " + i + "): " + info.getNgaySinh());
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("=== PARSING COMPLETE ===");
        System.out.println("Final result - soCccd: " + info.getSoCccd());
        System.out.println("Final result - hoVaTen: " + info.getHoVaTen());
        System.out.println("Final result - ngaySinh: " + info.getNgaySinh());
        System.out.println("Final result - gioiTinh: " + info.getGioiTinh());
        System.out.println("Final result - quocTich: " + info.getQuocTich());
        System.out.println("Final result - queQuan: " + info.getQueQuan());

        return info;
    }

    private String normalizeGender(String gender) {
        if (gender == null) return null;
        if (gender.matches("(?i)(male|nam)")) return "Nam";
        if (gender.matches("(?i)(female|nữ)")) return "Nữ";
        return gender;
    }
}
