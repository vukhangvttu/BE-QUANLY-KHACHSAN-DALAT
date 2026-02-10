# Hướng Dẫn Implement PhieuNhapHangService.savePhieuNhapHang()

## 📋 Tổng Quan
Service này xử lý việc lưu phiếu nhập hàng, validate dữ liệu, và tự động cập nhật tồn kho.

## 🔧 Các Thay Đổi Đã Thực Hiện

### 1. **TonKhoRepository** - Thêm Method Tìm Kiếm
```java
Optional<TonKho> findByDichVuMienPhi(DichVuMienPhi dichVuMienPhi);
```
- Cho phép tìm tồn kho theo dịch vụ miễn phí

### 2. **ErrorCode** - Thêm Các Mã Lỗi Mới
```java
NHAP_HANG_REQUEST_EMPTY(1134, "Danh sách nhập hàng không được để trống", HttpStatus.BAD_REQUEST)
MA_DICH_VU_EMPTY(1135, "Mã dịch vụ không được để trống", HttpStatus.BAD_REQUEST)
SO_LUONG_NHAP_HANG_INVALID(1136, "Số lượng nhập hàng phải lớn hơn 0", HttpStatus.BAD_REQUEST)
LOAI_NHAP_HANG_EMPTY(1137, "Loại nhập hàng không được để trống", HttpStatus.BAD_REQUEST)
DICH_VU_MIEN_PHI_KHONG_TON_TAI(1138, "Dịch vụ miễn phí không tồn tại", HttpStatus.NOT_FOUND)
DICH_VU_MIEN_PHI_BI_KHOA(1139, "Dịch vụ miễn phí đã bị khóa", HttpStatus.BAD_REQUEST)
```

### 3. **PhieuNhapHangService** - Implement Save Method

## 📝 Chi Tiết Luồng Xử Lý

### Method: `savePhieuNhapHang(List<NhapHangRequest> requests)`

#### **Bước 1: Kiểm Tra Danh Sách Không Rỗng**
```java
if (requests == null || requests.isEmpty()) {
    throw new AppException(ErrorCode.NHAP_HANG_REQUEST_EMPTY);
}
```

#### **Bước 2: Validate & Xây Dựng Danh Sách NhapHang**
Với mỗi `NhapHangRequest`:
1. **Validate dữ liệu đầu vào**
   - Mã dịch vụ không rỗng
   - Số lượng > 0
   - Loại không rỗng

2. **Kiểm tra DichVuMienPhi**
   - Tồn tại trong database
   - Trạng thái = `true` (còn hoạt động)

3. **Tạo entity NhapHang**
   - Gán dịch vụ miễn phí
   - Gán số lượng, loại, ghi chú
   - Tính tổng số lượng

#### **Bước 3: Tạo & Lưu PhieuNhapHang**
```java
PhieuNhapHang phieuNhapHang = new PhieuNhapHang();
phieuNhapHang.setTongSoLuong(tongSoLuong);
phieuNhapHang.setNgayNhap(LocalDate.now());
phieuNhapHang.setNguoiTao(SecurityUtil.getUsername());  // ✅ Lấy user đang đăng nhập
phieuNhapHang.setThoiGianTao(LocalDateTime.now());
```

#### **Bước 4: Liên Kết & Lưu Các NhapHang**
```java
for (NhapHang nhapHang : nhapHangList) {
    nhapHang.setPhieuNhapHang(savedPhieuNhapHang);
}
nhapHangRepository.saveAll(nhapHangList);
```

#### **Bước 5: Cập Nhật Tồn Kho**
Với mỗi `NhapHang`:
1. Tìm `TonKho` hiện tại theo `DichVuMienPhi`
2. Nếu chưa có → tạo mới với số lượng = 0
3. Cộng số lượng nhập vào tồn kho
4. Lưu vào database
5. Log chi tiết: Mã DV, SL cũ, SL nhập, SL mới

## 🔐 Security - Lấy Thông Tin User

### SecurityUtil.getUsername()
```java
public static String getUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
        return "ANONYMOUS";
    }
    return auth.getName();
}
```

## 📊 Logging với Username

### HibernateInterceptorWithUser
Tự động log username khi thực thi SQL:

```java
@Override
public String inspect(String sql) {
    String username = SecurityUtil.getUsername();
    log.info("👤 [username: {}]", username);
    return sql;
}
```

### Cấu Hình application.yml
```yaml
logging:
  level:
    vttu.khachsanservice.config.HibernateInterceptorWithUser: INFO
    org.hibernate.SQL: DEBUG
```

### Kết Quả Log
```
2025-01-15 10:30:45 INFO  [username: cvkhang]
Hibernate: 
    select 
        ttbk1_0.ma_trang_thai_boo_king,
        ttbk1_0.ten_trang_thai_boo_king,
        ttbk1_0.vitri 
    from 
        trang_thai_booking ttbk1_0 
    where 
        ttbk1_0.ma_trang_thai_boo_king in (1, 5, 3) 
    order by 
        ttbk1_0.vitri
```

## 📦 Ví Dụ Request

### JSON Request
```json
[
  {
    "maDichVu": "DV001",
    "soLuong": 100,
    "loai": "Nhập đầu kỳ",
    "ghiChu": "Nhập hàng tháng 1"
  },
  {
    "maDichVu": "DV002",
    "soLuong": 50,
    "loai": "Nhập bổ sung",
    "ghiChu": "Bổ sung do hết hàng"
  }
]
```

### Response Log
```
✅ Lưu phiếu nhập hàng thành công - Mã: abc-123-xyz, Tổng SL: 150.0, Người tạo: cvkhang
📦 Cập nhật tồn kho - Mã DV: DV001, SL cũ: 50, SL nhập: 100, SL mới: 150
📦 Cập nhật tồn kho - Mã DV: DV002, SL cũ: 0, SL nhập: 50, SL mới: 50
```

## ✅ Ưu Điểm Của Implementation

1. **Code Đơn Giản & Dễ Hiểu**
   - Chia thành các method nhỏ với chức năng rõ ràng
   - Comment đầy đủ bằng tiếng Việt
   - Tuân theo Single Responsibility Principle

2. **Validation Đầy Đủ**
   - Kiểm tra tất cả ràng buộc dữ liệu
   - Throw exception với message cụ thể
   - Validate cả tồn tại và trạng thái của dịch vụ

3. **Transaction Safety**
   - Sử dụng `@Transactional`
   - Nếu có lỗi → rollback toàn bộ
   - Đảm bảo tính nhất quán dữ liệu

4. **Tự Động Cập Nhật Tồn Kho**
   - Tự động tạo bản ghi TonKho nếu chưa có
   - Cộng dồn số lượng chính xác
   - Log chi tiết để audit

5. **Security Aware**
   - Tự động lấy username từ SecurityContext
   - Lưu người tạo phiếu nhập
   - Hỗ trợ audit trail

6. **Logging Chi Tiết**
   - Log kết quả thành công với đầy đủ thông tin
   - Log từng bước cập nhật tồn kho
   - Kết hợp với Hibernate interceptor để log SQL + username

## 🎯 Sử Dụng Trong Controller

```java
@PostMapping("/phieu-nhap-hang")
public ResponseEntity<PhieuNhapHang> createPhieuNhapHang(
        @RequestBody List<NhapHangRequest> requests) {
    PhieuNhapHang result = phieuNhapHangService.savePhieuNhapHang(requests);
    return ResponseEntity.ok(result);
}
```

## 🚀 Build & Deploy

### Compile
```bash
./gradlew compileJava
```

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew bootRun
```

---

✨ **Implementation hoàn thành và đã test compile thành công!**

