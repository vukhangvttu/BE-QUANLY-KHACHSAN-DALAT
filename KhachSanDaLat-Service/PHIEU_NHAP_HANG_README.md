# 📦 Quản Lý Phiếu Nhập Hàng - Complete Documentation

## 📚 Tài Liệu Tổng Hợp

### 1️⃣ **PHIEU_NHAP_HANG_IMPLEMENTATION.md**
- Hướng dẫn implement method `savePhieuNhapHang()`
- Chi tiết validation và business logic
- Cách sử dụng SecurityUtil để lấy username
- Logging với Hibernate Interceptor

### 2️⃣ **XOA_PHIEU_NHAP_HANG_GUIDE.md**
- Hướng dẫn chi tiết logic xóa phiếu nhập hàng
- So sánh logic cũ vs mới
- Các tình huống xử lý lỗi
- Ví dụ thực tế với data flow

### 3️⃣ **XOA_PHIEU_FLOW_DIAGRAM.md**
- Sequence diagram
- State diagram
- Data flow diagram
- Transaction flow

## 🎯 Quick Start

### Tạo Phiếu Nhập Hàng
```java
List<NhapHangRequest> requests = Arrays.asList(
    new NhapHangRequest("DV001", 100, "Nhập đầu kỳ", "Nhập tháng 1"),
    new NhapHangRequest("DV002", 50, "Nhập bổ sung", "Bổ sung kho")
);

PhieuNhapHang result = phieuNhapHangService.savePhieuNhapHang(requests);
```

**Kết quả:**
- ✅ Tạo PhieuNhapHang mới
- ✅ Lưu 2 bản ghi NhapHang
- ✅ Tự động cập nhật TonKho (+100 cho DV001, +50 cho DV002)
- ✅ Log username và chi tiết

### Xóa Phiếu Nhập Hàng
```java
phieuNhapHangService.xoaPhieuNhapHang("PNH001");
```

**Kết quả:**
- ✅ Kiểm tra phiếu tồn tại và chưa xóa
- ✅ Lấy danh sách NhapHang theo maPhieuNhapHang
- ✅ Trừ số lượng tồn kho cho từng dịch vụ
- ✅ Đánh dấu phiếu đã xóa (daXoa = true)
- ✅ Log chi tiết quá trình

## 📊 Database Schema

```sql
-- Phiếu Nhập Hàng
CREATE TABLE phieu_nhap_hang (
    ma_phieu_nhap_hang VARCHAR(36) PRIMARY KEY,
    tong_so_luong FLOAT,
    ngay_nhap DATE,
    nguoi_tao VARCHAR(50),
    thoi_gian_tao TIMESTAMP,
    da_xoa BOOLEAN DEFAULT false
);

-- Chi Tiết Nhập Hàng
CREATE TABLE nhap_hang (
    ma_nhap_hang BIGSERIAL PRIMARY KEY,
    ma_dich_vu_mien_phi VARCHAR(50),
    so_luong INTEGER NOT NULL,
    loai VARCHAR(50) NOT NULL,
    ghi_chu TEXT,
    ma_phieu_nhap_hang VARCHAR(36),
    FOREIGN KEY (ma_dich_vu_mien_phi) REFERENCES dich_vu_mien_phi(ma_dich_vu_mien_phi),
    FOREIGN KEY (ma_phieu_nhap_hang) REFERENCES phieu_nhap_hang(ma_phieu_nhap_hang)
);

-- Tồn Kho
CREATE TABLE ton_kho (
    ma_ton_kho BIGSERIAL PRIMARY KEY,
    ma_dich_vu_mien_phi VARCHAR(50),
    so_luong INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (ma_dich_vu_mien_phi) REFERENCES dich_vu_mien_phi(ma_dich_vu_mien_phi)
);
```

## 🔐 Security & Logging

### Lấy Username Đang Đăng Nhập
```java
// SecurityUtil.java
public static String getUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
        return "ANONYMOUS";
    }
    return auth.getName();
}
```

### Hibernate Interceptor (Log SQL + Username)
```java
// HibernateInterceptorWithUser.java
@Override
public String inspect(String sql) {
    String username = SecurityUtil.getUsername();
    log.info("👤 [username: {}]", username);
    return sql;
}
```

**Config trong application.yml:**
```yaml
logging:
  level:
    vttu.khachsanservice.config.HibernateInterceptorWithUser: INFO
    org.hibernate.SQL: DEBUG
```

**Output:**
```
INFO  👤 [username: cvkhang]
DEBUG Hibernate: select ... from nhap_hang ...
```

## 📈 Business Logic Flow

### Save PhieuNhapHang
```
Validate → Kiểm tra DichVu → Tạo PhieuNhapHang → Lưu NhapHang → Cập nhật TonKho (+)
```

### Xóa PhieuNhapHang
```
Kiểm tra tồn tại → Kiểm tra đã xóa → Lấy NhapHang → Cập nhật TonKho (-) → Đánh dấu xóa
```

## ⚠️ Error Codes

| Code | Error | Mô tả |
|------|-------|-------|
| 1134 | NHAP_HANG_REQUEST_EMPTY | Danh sách nhập hàng rỗng |
| 1135 | MA_DICH_VU_EMPTY | Mã dịch vụ trống |
| 1136 | SO_LUONG_NHAP_HANG_INVALID | Số lượng <= 0 |
| 1137 | LOAI_NHAP_HANG_EMPTY | Loại nhập hàng trống |
| 1138 | DICH_VU_MIEN_PHI_KHONG_TON_TAI | Dịch vụ không tồn tại |
| 1139 | DICH_VU_MIEN_PHI_BI_KHOA | Dịch vụ bị khóa |
| 1140 | PHIEU_NHAP_HANG_KHONG_TON_TAI | Phiếu không tồn tại |
| 1141 | PHIEU_NHAP_HANG_DA_XOA | Phiếu đã xóa |
| 1142 | TON_KHO_KHONG_DU | Tồn kho không đủ để xóa |

## 📝 Example Logs

### Save Success
```
✅ Lưu phiếu nhập hàng thành công - Mã: abc-123-xyz, Tổng SL: 150.0, Người tạo: cvkhang
📦 Cập nhật tồn kho - Mã DV: DV001, SL cũ: 50, SL nhập: 100, SL mới: 150
📦 Cập nhật tồn kho - Mã DV: DV002, SL cũ: 0, SL nhập: 50, SL mới: 50
```

### Delete Success
```
📦 Cập nhật tồn kho khi xóa - Mã DV: DV001, SL cũ: 250, SL trừ: 100, SL mới: 150
📦 Cập nhật tồn kho khi xóa - Mã DV: DV002, SL cũ: 80, SL trừ: 50, SL mới: 30
🗑️ Xóa phiếu nhập hàng thành công - Mã: PNH001, Người thực hiện: cvkhang
```

### Delete Error - Insufficient Stock
```
❌ AppException: TON_KHO_KHONG_DU
Mã DV: DV003 - Tồn kho hiện tại: 20, Cần trừ: 100
```

## 🧪 Testing

### Unit Test Example
```java
@Test
void testSavePhieuNhapHang_Success() {
    // Given
    List<NhapHangRequest> requests = Arrays.asList(
        new NhapHangRequest("DV001", 100, "Nhập đầu kỳ", null)
    );
    
    // When
    PhieuNhapHang result = service.savePhieuNhapHang(requests);
    
    // Then
    assertNotNull(result.getMaPhieuNhapHang());
    assertEquals(100f, result.getTongSoLuong());
    verify(tonKhoRepository).save(any());
}

@Test
void testXoaPhieuNhapHang_InsufficientStock() {
    // Given
    String maPhieu = "PNH001";
    // TonKho có 20, nhưng cần trừ 100
    
    // When & Then
    assertThrows(AppException.class, 
        () -> service.xoaPhieuNhapHang(maPhieu));
}
```

## 🚀 Build & Deploy

```bash
# Compile
./gradlew compileJava

# Run tests
./gradlew test

# Build
./gradlew build

# Run application
./gradlew bootRun
```

## 📂 Files Modified

1. ✅ `TonKhoRepository.java` - Added findByDichVuMienPhi
2. ✅ `ErrorCode.java` - Added 9 error codes
3. ✅ `PhieuNhapHangRepository.java` - Updated query filter
4. ✅ `PhieuNhapHangService.java` - Implemented save & delete logic

## 🎯 Features

- ✅ **Tạo phiếu nhập hàng** với validation đầy đủ
- ✅ **Tự động cập nhật tồn kho** khi tạo/xóa
- ✅ **Validate business logic** (dịch vụ tồn tại, số lượng hợp lệ, tồn kho đủ)
- ✅ **Transaction safety** với @Transactional
- ✅ **Audit trail** với username và timestamp
- ✅ **Comprehensive logging** với emoji
- ✅ **Error handling** rõ ràng và chi tiết

## 📞 Support

Nếu có vấn đề, vui lòng:
1. Kiểm tra logs
2. Xem error code trong ErrorCode.java
3. Đọc tài liệu chi tiết trong thư mục docs

---

✨ **Complete Implementation - Ready for Production!** ✨

