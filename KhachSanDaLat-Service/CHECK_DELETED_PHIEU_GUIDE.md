# Cập Nhật: Kiểm Tra Phiếu Nhập Hàng Đã Xóa

## 🎯 Yêu Cầu
Thêm cơ chế kiểm tra nếu phiếu nhập hàng (`PhieuNhapHang`) có `da_xoa = true` thì không cho phép update nhập hàng (`NhapHang`).

## ✅ Đã Hoàn Thành

### 1. **ErrorCode** - Thêm Error Code Mới
```java
NHAP_HANG_DA_XOA(1145, "Không thể cập nhật nhập hàng đã bị xóa", HttpStatus.BAD_REQUEST)
```

### 2. **PhieuNhapHangService.updateNhapHang()** - Thêm Validation

#### Code Đã Thêm:
```java
// Kiểm tra phiếu nhập hàng có bị xóa không
if (nhapHang.getPhieuNhapHang() != null) {
    PhieuNhapHang phieuNhapHang = nhapHang.getPhieuNhapHang();
    if (phieuNhapHang.getDaXoa() != null && phieuNhapHang.getDaXoa()) {
        throw new AppException(ErrorCode.NHAP_HANG_DA_XOA,
                String.format("Mã nhập hàng: %d - Phiếu nhập hàng %s đã bị xóa",
                        maNhapHang, phieuNhapHang.getMaPhieuNhapHang()));
    }
}
```

## 🔄 Luồng Xử Lý

```
updateOrCreateNhapHang(request)
         │
         ↓
    maNhapHang có giá trị?
         │
         ↓ YES (UPDATE MODE)
    updateNhapHang(request)
         │
         ↓
    1. Parse maNhapHang
         │
         ↓
    2. Tìm NhapHang
         │
         ↓
    3. ✅ KIỂM TRA MỚI: PhieuNhapHang có daXoa = true?
         │
    ┌────┴────┐
    │         │
   YES       NO
    │         │
    ↓         ↓
  ❌ Throw  ✅ Tiếp tục
  NHAP_      update
  HANG_
  DA_XOA
```

## 💡 Ví Dụ Cụ Thể

### Scenario 1: Phiếu Chưa Xóa (Cho Phép Update)

**Database:**
```sql
-- Phiếu nhập hàng
PhieuNhapHang:
  ma_phieu_nhap_hang: "PNH001"
  da_xoa: false ✅

-- Nhập hàng
NhapHang:
  ma_nhap_hang: 123
  ma_phieu_nhap_hang: "PNH001"
  so_luong: 100
```

**Request:**
```json
{
  "maNhapHang": "123",
  "maDichVu": "DV001",
  "soLuong": 150,
  "loai": "Điều chỉnh"
}
```

**Result:**
```
✅ SUCCESS
→ Phiếu chưa xóa (daXoa = false)
→ Cho phép update
→ NhapHang #123: soLuong = 150
```

**Log:**
```
🔄 Cập nhật nhập hàng - Mã: 123, SL cũ: 100, SL mới: 150, Người cập nhật: cvkhang
📦 Cập nhật tồn kho (update) - Mã DV: DV001, SL cũ: 250, Delta: +50, SL mới: 300
```

### Scenario 2: Phiếu Đã Xóa (Không Cho Phép Update)

**Database:**
```sql
-- Phiếu nhập hàng
PhieuNhapHang:
  ma_phieu_nhap_hang: "PNH001"
  da_xoa: true ❌  -- ĐÃ XÓA!

-- Nhập hàng
NhapHang:
  ma_nhap_hang: 123
  ma_phieu_nhap_hang: "PNH001"
  so_luong: 100
```

**Request:**
```json
{
  "maNhapHang": "123",
  "maDichVu": "DV001",
  "soLuong": 150,
  "loai": "Điều chỉnh"
}
```

**Result:**
```
❌ EXCEPTION
→ Phát hiện phiếu đã xóa (daXoa = true)
→ Throw AppException: NHAP_HANG_DA_XOA
→ Không cho phép update
```

**Error Response:**
```json
{
  "code": 1145,
  "message": "Không thể cập nhật nhập hàng đã bị xóa - Mã nhập hàng: 123 - Phiếu nhập hàng PNH001 đã bị xóa"
}
```

### Scenario 3: Nhập Hàng Không Thuộc Phiếu Nào

**Database:**
```sql
NhapHang:
  ma_nhap_hang: 123
  ma_phieu_nhap_hang: null  -- Không thuộc phiếu nào
  so_luong: 100
```

**Request:**
```json
{
  "maNhapHang": "123",
  "soLuong": 150
}
```

**Result:**
```
✅ SUCCESS
→ Không có PhieuNhapHang liên kết
→ Bỏ qua kiểm tra daXoa
→ Cho phép update
```

## 📊 Flow Chart Chi Tiết

```
┌────────────────────────────────────────────────────┐
│  updateNhapHang(request)                           │
└────────────────┬───────────────────────────────────┘
                 │
                 ↓
      ┌──────────────────────┐
      │  Parse maNhapHang    │
      └──────────┬───────────┘
                 │
                 ↓
      ┌──────────────────────┐
      │  Tìm NhapHang        │
      │  findById()          │
      └──────────┬───────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
 Không tìm thấy          Tìm thấy
    │                         │
    ↓                         ↓
┌─────────────┐    ┌──────────────────────┐
│ Throw       │    │ nhapHang.            │
│ NHAP_HANG_  │    │ getPhieuNhapHang()   │
│ KHONG_      │    └──────────┬───────────┘
│ TON_TAI     │               │
└─────────────┘    ┌──────────┴───────────┐
                   │                      │
                 null                  not null
                   │                      │
                   │                      ↓
                   │          ┌──────────────────────┐
                   │          │ phieuNhapHang.       │
                   │          │ getDaXoa()           │
                   │          └──────────┬───────────┘
                   │                     │
                   │          ┌──────────┴──────────┐
                   │          │                     │
                   │        null              true/false
                   │          │                     │
                   │          │          ┌──────────┴──────────┐
                   │          │          │                     │
                   │          │        true                 false
                   │          │          │                     │
                   │          │          ↓                     │
                   │          │    ┌─────────────┐            │
                   │          │    │ Throw       │            │
                   │          │    │ NHAP_HANG_  │            │
                   │          │    │ DA_XOA      │            │
                   │          │    └─────────────┘            │
                   │          │                                │
                   └──────────┴────────────────────────────────┘
                              │
                              ↓
                   ┌──────────────────────┐
                   │ ✅ Tiếp tục update   │
                   │ - Lưu soLuongCu      │
                   │ - Check đổi DV       │
                   │ - Update TonKho      │
                   │ - Save NhapHang      │
                   └──────────────────────┘
```

## 🛡️ Business Logic

### Tại Sao Cần Kiểm Tra?

1. **Data Integrity**: Phiếu đã xóa = đã hủy giao dịch nhập hàng
2. **Tồn Kho Chính Xác**: Khi xóa phiếu, tồn kho đã được điều chỉnh (trừ đi)
3. **Audit Trail**: Không cho phép thay đổi dữ liệu đã hủy
4. **Business Rule**: Dữ liệu đã xóa không nên được chỉnh sửa

### Khi Nào Throw Error?

```java
if (phieuNhapHang != null && 
    phieuNhapHang.getDaXoa() != null && 
    phieuNhapHang.getDaXoa() == true) {
    // ❌ Không cho phép update
    throw NHAP_HANG_DA_XOA;
}
```

### Khi Nào Cho Phép Update?

- ✅ `phieuNhapHang == null` (Nhập hàng đơn lẻ, không thuộc phiếu)
- ✅ `phieuNhapHang.getDaXoa() == null` (Chưa set trạng thái)
- ✅ `phieuNhapHang.getDaXoa() == false` (Phiếu còn hoạt động)

## 📝 Error Message Format

```java
String.format("Mã nhập hàng: %d - Phiếu nhập hàng %s đã bị xóa",
              maNhapHang, 
              phieuNhapHang.getMaPhieuNhapHang())
```

**Example:**
```
"Mã nhập hàng: 123 - Phiếu nhập hàng PNH001 đã bị xóa"
```

## 🎯 Use Cases

### Use Case 1: User Cố Gắng Update Sau Khi Xóa Phiếu

**Timeline:**
```
1. 10:00 - Tạo PhieuNhapHang "PNH001" với NhapHang #123
2. 10:30 - Xóa PhieuNhapHang "PNH001" (daXoa = true)
           → TonKho đã được trừ
3. 11:00 - User cố gắng update NhapHang #123
           ❌ BLOCKED: "Phiếu nhập hàng PNH001 đã bị xóa"
```

**Lý do block:**
- Phiếu đã xóa → tồn kho đã điều chỉnh
- Nếu cho update → tồn kho sẽ sai
- Phải tạo phiếu mới thay vì sửa phiếu cũ

### Use Case 2: Update Nhập Hàng Đơn Lẻ

**Scenario:**
```
NhapHang #456 (không thuộc phiếu nào)
→ ma_phieu_nhap_hang = null
→ ✅ Cho phép update (không có ràng buộc phiếu)
```

## 🔄 Integration với Các Method Khác

### Method Flow:
```
updateOrCreateNhapHang()
    │
    ├─ maNhapHang = null → createNhapHang()
    │                      (không cần check daXoa)
    │
    └─ maNhapHang có giá trị → updateNhapHang()
                                │
                                ✅ CHECK daXoa ở đây!
```

### Không Ảnh Hưởng:
- ✅ `createNhapHang()` - Không cần check (tạo mới)
- ✅ `savePhieuNhapHang()` - Tạo phiếu mới (không check)
- ✅ `xoaPhieuNhapHang()` - Đã có check riêng

## ✅ Testing Scenarios

### Test Case 1: Update Nhập Hàng - Phiếu Active
```java
@Test
void testUpdateNhapHang_PhieuActive_Success() {
    // Given
    NhapHang nhapHang = new NhapHang();
    nhapHang.setMaNhapHang(123);
    
    PhieuNhapHang phieu = new PhieuNhapHang();
    phieu.setDaXoa(false);  // ✅ Chưa xóa
    
    nhapHang.setPhieuNhapHang(phieu);
    
    // When
    NhapHangRequest request = new NhapHangRequest();
    request.setMaNhapHang("123");
    request.setSoLuong(150);
    
    // Then
    assertDoesNotThrow(() -> service.updateOrCreateNhapHang(request));
}
```

### Test Case 2: Update Nhập Hàng - Phiếu Đã Xóa
```java
@Test
void testUpdateNhapHang_PhieuDaXoa_ThrowError() {
    // Given
    NhapHang nhapHang = new NhapHang();
    nhapHang.setMaNhapHang(123);
    
    PhieuNhapHang phieu = new PhieuNhapHang();
    phieu.setMaPhieuNhapHang("PNH001");
    phieu.setDaXoa(true);  // ❌ Đã xóa
    
    nhapHang.setPhieuNhapHang(phieu);
    
    // When
    NhapHangRequest request = new NhapHangRequest();
    request.setMaNhapHang("123");
    
    // Then
    AppException exception = assertThrows(AppException.class,
        () -> service.updateOrCreateNhapHang(request));
    
    assertEquals(ErrorCode.NHAP_HANG_DA_XOA, exception.getErrorCode());
    assertTrue(exception.getMessage().contains("PNH001"));
}
```

### Test Case 3: Update Nhập Hàng - Không Thuộc Phiếu
```java
@Test
void testUpdateNhapHang_KhongThuocPhieu_Success() {
    // Given
    NhapHang nhapHang = new NhapHang();
    nhapHang.setMaNhapHang(123);
    nhapHang.setPhieuNhapHang(null);  // ✅ Không thuộc phiếu
    
    // When & Then
    assertDoesNotThrow(() -> service.updateOrCreateNhapHang(request));
}
```

## 📊 Database State Examples

### Before Check:
```sql
-- Phiếu nhập hàng
SELECT * FROM phieu_nhap_hang WHERE ma_phieu_nhap_hang = 'PNH001';
-- Result: ma_phieu_nhap_hang | da_xoa
--         PNH001             | true

-- Nhập hàng
SELECT * FROM nhap_hang WHERE ma_nhap_hang = 123;
-- Result: ma_nhap_hang | ma_phieu_nhap_hang | so_luong
--         123          | PNH001             | 100

-- Tồn kho (đã được điều chỉnh khi xóa phiếu)
SELECT * FROM ton_kho WHERE ma_dich_vu_mien_phi = 'DV001';
-- Result: so_luong = 150 (đã trừ 100 khi xóa phiếu)
```

### After Block Update:
```sql
-- ❌ Update bị chặn, dữ liệu không thay đổi
-- NhapHang vẫn: so_luong = 100
-- TonKho vẫn: so_luong = 150
-- → Data integrity được bảo toàn ✅
```

## 🚀 Build Status

```bash
./gradlew compileJava
BUILD SUCCESSFUL in 11s ✅

No errors found ✅
```

## 📂 Files Modified

1. ✅ `ErrorCode.java` - Added `NHAP_HANG_DA_XOA (1145)`
2. ✅ `PhieuNhapHangService.java` - Added validation in `updateNhapHang()`

## 🎯 Summary

### Đã Implement:
- ✅ Kiểm tra `PhieuNhapHang.daXoa` trước khi update
- ✅ Throw error nếu phiếu đã xóa
- ✅ Message error chi tiết (mã nhập hàng + mã phiếu)
- ✅ Không ảnh hưởng create mode
- ✅ Xử lý đúng khi không thuộc phiếu nào

### Benefits:
1. ✅ **Data Integrity** - Không cho sửa dữ liệu đã hủy
2. ✅ **Tồn Kho Chính Xác** - Tránh điều chỉnh tồn kho 2 lần
3. ✅ **Business Logic Clear** - Quy trình rõ ràng
4. ✅ **User Experience** - Message error dễ hiểu
5. ✅ **Audit Trail** - Lịch sử dữ liệu không bị sửa đổi

---

✨ **Implementation hoàn tất! Cơ chế kiểm tra phiếu đã xóa đã được thêm vào!**

