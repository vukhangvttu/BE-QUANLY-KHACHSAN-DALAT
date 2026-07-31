# Hướng Dẫn: Logic Xóa Phiếu Nhập Hàng (Phức Tạp)

## 📋 Tổng Quan
Method `xoaPhieuNhapHang()` đã được cập nhật với logic phức tạp hơn để xử lý việc xóa phiếu nhập hàng và tự động cập nhật tồn kho một cách chính xác.

## 🔧 Các Thay Đổi

### 1. **ErrorCode** - Thêm Error Codes Mới
```java
PHIEU_NHAP_HANG_KHONG_TON_TAI(1140, "Phiếu nhập hàng không tồn tại", HttpStatus.NOT_FOUND)
PHIEU_NHAP_HANG_DA_XOA(1141, "Phiếu nhập hàng đã được xóa trước đó", HttpStatus.BAD_REQUEST)
TON_KHO_KHONG_DU(1142, "Số lượng tồn kho không đủ để xóa phiếu nhập hàng", HttpStatus.BAD_REQUEST)
```

### 2. **PhieuNhapHangRepository** - Cập Nhật Query
```java
@Query("SELECT p FROM PhieuNhapHang p WHERE p.daXoa = false OR p.daXoa IS NULL ORDER BY p.thoiGianTao DESC")
List<PhieuNhapHang> findAllActivePhieuNhapHang();
```
- Chỉ lấy các phiếu nhập hàng chưa xóa

### 3. **PhieuNhapHangService.xoaPhieuNhapHang()** - Logic Phức Tạp

## 📝 Chi Tiết Luồng Xử Lý

### Method: `xoaPhieuNhapHang(String maPhieuNhapHang)`

```
┌─────────────────────────────────────────────────────────────┐
│              LUỒNG XỬ LÝ XÓA PHIẾU NHẬP HÀNG                │
└─────────────────────────────────────────────────────────────┘

   BƯỚC 1: Kiểm Tra Phiếu Nhập Hàng Tồn Tại
      ↓
   ❌ Không tồn tại → Throw PHIEU_NHAP_HANG_KHONG_TON_TAI
      ↓
   ✅ Tồn tại
      ↓
   BƯỚC 2: Kiểm Tra Phiếu Đã Xóa Chưa
      ↓
   ❌ Đã xóa (daXoa = true) → Throw PHIEU_NHAP_HANG_DA_XOA
      ↓
   ✅ Chưa xóa
      ↓
   BƯỚC 3: Lấy Danh Sách NhapHang
      ↓
   Query: SELECT * FROM nhap_hang 
          WHERE ma_phieu_nhap_hang = :maPhieuNhapHang
      ↓
   [NhapHang 1, NhapHang 2, NhapHang 3, ...]
      ↓
   BƯỚC 4: CẬP NHẬT TỒN KHO (LOOP)
      ↓
   ┌─────────────────────────────────────────────┐
   │ Với mỗi NhapHang:                           │
   │                                             │
   │ 4.1 Lấy DichVuMienPhi từ NhapHang          │
   │     ↓                                       │
   │ 4.2 Tìm TonKho theo DichVuMienPhi          │
   │     ↓                                       │
   │ ❌ Không tìm thấy                           │
   │     → Throw DICH_VU_MIEN_PHI_KHONG_TON_TAI │
   │     ↓                                       │
   │ ✅ Tìm thấy TonKho                          │
   │     ↓                                       │
   │ 4.3 So Sánh Số Lượng                       │
   │     soLuongHienTai = tonKho.getSoLuong()   │
   │     soLuongCanTru = nhapHang.getSoLuong()  │
   │     ↓                                       │
   │ ❌ soLuongHienTai < soLuongCanTru           │
   │     → Throw TON_KHO_KHONG_DU                │
   │     ↓                                       │
   │ ✅ Đủ số lượng để trừ                       │
   │     ↓                                       │
   │ 4.4 Tính Số Lượng Mới                      │
   │     soLuongMoi = soLuongHienTai - soLuongCanTru │
   │     ↓                                       │
   │ 4.5 Cập Nhật TonKho                        │
   │     tonKho.setSoLuong(soLuongMoi)          │
   │     tonKhoRepository.save(tonKho)          │
   │     ↓                                       │
   │ 4.6 Log Chi Tiết                           │
   └─────────────────────────────────────────────┘
      ↓
   BƯỚC 5: Đánh Dấu Phiếu Đã Xóa
      ↓
   phieuNhapHang.setDaXoa(true)
   phieuNhapHangRepository.save(phieuNhapHang)
      ↓
   Log Kết Quả Thành Công
      ↓
   ✅ HOÀN THÀNH
```

## 💡 Ví Dụ Cụ Thể

### Tình Huống 1: Xóa Phiếu Thành Công

**Dữ liệu ban đầu:**
```
PhieuNhapHang:
- maPhieuNhapHang: "PNH001"
- tongSoLuong: 150
- daXoa: false

NhapHang:
1. maDichVu: "DV001", soLuong: 100
2. maDichVu: "DV002", soLuong: 50

TonKho (trước khi xóa):
1. DV001: soLuong = 250
2. DV002: soLuong = 80
```

**Xử lý:**
```java
xoaPhieuNhapHang("PNH001");
```

**Luồng xử lý:**
```
1. ✅ Tìm thấy PhieuNhapHang "PNH001"
2. ✅ daXoa = false (chưa xóa)
3. ✅ Lấy được 2 NhapHang

4. Xử lý NhapHang #1 (DV001):
   - Tìm TonKho: soLuongHienTai = 250
   - soLuongCanTru = 100
   - ✅ 250 >= 100 (đủ để trừ)
   - soLuongMoi = 250 - 100 = 150
   - Cập nhật TonKho DV001 = 150
   - Log: "📦 Cập nhật tồn kho khi xóa - Mã DV: DV001, SL cũ: 250, SL trừ: 100, SL mới: 150"

5. Xử lý NhapHang #2 (DV002):
   - Tìm TonKho: soLuongHienTai = 80
   - soLuongCanTru = 50
   - ✅ 80 >= 50 (đủ để trừ)
   - soLuongMoi = 80 - 50 = 30
   - Cập nhật TonKho DV002 = 30
   - Log: "📦 Cập nhật tồn kho khi xóa - Mã DV: DV002, SL cũ: 80, SL trừ: 50, SL mới: 30"

6. Đánh dấu daXoa = true
7. Log: "🗑️ Xóa phiếu nhập hàng thành công - Mã: PNH001, Người thực hiện: cvkhang"
```

**Kết quả:**
```
PhieuNhapHang:
- maPhieuNhapHang: "PNH001"
- daXoa: true ✅

TonKho (sau khi xóa):
1. DV001: 150 (đã trừ 100) ✅
2. DV002: 30 (đã trừ 50) ✅
```

### Tình Huống 2: Lỗi - Tồn Kho Không Đủ

**Dữ liệu ban đầu:**
```
PhieuNhapHang:
- maPhieuNhapHang: "PNH002"

NhapHang:
1. maDichVu: "DV003", soLuong: 100

TonKho:
1. DV003: soLuong = 50 ⚠️ (Không đủ!)
```

**Xử lý:**
```java
xoaPhieuNhapHang("PNH002");
```

**Kết quả:**
```
❌ AppException: TON_KHO_KHONG_DU
Message: "Mã DV: DV003 - Tồn kho hiện tại: 50, Cần trừ: 100"

→ Không xóa phiếu
→ Không cập nhật tồn kho
→ Transaction rollback ✅
```

### Tình Huống 3: Lỗi - Phiếu Đã Xóa

**Dữ liệu:**
```
PhieuNhapHang:
- maPhieuNhapHang: "PNH003"
- daXoa: true ⚠️
```

**Xử lý:**
```java
xoaPhieuNhapHang("PNH003");
```

**Kết quả:**
```
❌ AppException: PHIEU_NHAP_HANG_DA_XOA
Message: "Phiếu nhập hàng đã được xóa trước đó - Mã: PNH003"
```

## 🔍 So Sánh Logic Cũ vs Mới

### ❌ Logic Cũ (Đơn Giản)
```java
public void xoaPhieuNhapHang(String maPhieuNhapHang) {
    PhieuNhapHang phieuNhapHang = phieuNhapHangRepository.findById(maPhieuNhapHang)
            .orElseThrow(() -> new AppException(ErrorCode.PHIEU_NHAP_HANG_KHONG_TON_TAI));
    
    phieuNhapHang.setDaXoa(true);
    phieuNhapHangRepository.save(phieuNhapHang);
}
```

**Vấn đề:**
- ❌ Không cập nhật tồn kho
- ❌ Không kiểm tra đã xóa chưa
- ❌ Không log chi tiết
- ❌ Dữ liệu tồn kho sai

### ✅ Logic Mới (Phức Tạp & Chính Xác)
```java
@Transactional
public void xoaPhieuNhapHang(String maPhieuNhapHang) {
    // 1. Kiểm tra tồn tại
    // 2. Kiểm tra đã xóa chưa
    // 3. Lấy danh sách NhapHang
    // 4. Cập nhật tồn kho (trừ số lượng)
    // 5. Đánh dấu đã xóa
}
```

**Ưu điểm:**
- ✅ Tự động cập nhật tồn kho
- ✅ Validate đầy đủ
- ✅ Log chi tiết từng bước
- ✅ Transaction safety
- ✅ Dữ liệu chính xác

## 🔐 Security & Audit

```java
log.info("🗑️ Xóa phiếu nhập hàng thành công - Mã: {}, Người thực hiện: {}",
        maPhieuNhapHang, SecurityUtil.getUsername());
```

**Log Output:**
```
🗑️ Xóa phiếu nhập hàng thành công - Mã: PNH001, Người thực hiện: cvkhang
📦 Cập nhật tồn kho khi xóa - Mã DV: DV001, SL cũ: 250, SL trừ: 100, SL mới: 150
📦 Cập nhật tồn kho khi xóa - Mã DV: DV002, SL cũ: 80, SL trừ: 50, SL mới: 30
```

## 📊 Database Queries

### Query 1: Lấy PhieuNhapHang
```sql
SELECT * FROM phieu_nhap_hang 
WHERE ma_phieu_nhap_hang = 'PNH001';
```

### Query 2: Lấy Danh Sách NhapHang
```sql
SELECT h.* 
FROM nhap_hang h 
WHERE h.ma_phieu_nhap_hang = 'PNH001';
```

### Query 3: Lấy TonKho
```sql
SELECT * FROM ton_kho 
WHERE ma_dich_vu_mien_phi = 'DV001';
```

### Query 4: Cập Nhật TonKho
```sql
UPDATE ton_kho 
SET so_luong = so_luong - :soLuongTru 
WHERE ma_ton_kho = :maTonKho;
```

### Query 5: Đánh Dấu Đã Xóa
```sql
UPDATE phieu_nhap_hang 
SET da_xoa = true 
WHERE ma_phieu_nhap_hang = 'PNH001';
```

## ✅ Validation Checklist

Trước khi xóa phiếu nhập hàng, hệ thống kiểm tra:

- ✅ Phiếu nhập hàng có tồn tại không?
- ✅ Phiếu đã bị xóa trước đó chưa?
- ✅ Có danh sách chi tiết nhập hàng không?
- ✅ Mỗi dịch vụ có tồn kho không?
- ✅ Số lượng tồn kho có đủ để trừ không?

## 🎯 Use Case Thực Tế

### Kịch Bản 1: Nhập Nhầm Phiếu
```
1. Nhân viên nhập phiếu PNH001 (100 chai nước)
2. Tồn kho tự động tăng: 50 → 150
3. Phát hiện nhập nhầm
4. Xóa phiếu PNH001
5. Tồn kho tự động giảm: 150 → 50 ✅
```

### Kịch Bản 2: Đã Sử Dụng Hàng
```
1. Nhập phiếu PNH002 (100 khăn tắm)
2. Tồn kho: 0 → 100
3. Sử dụng 80 khăn tắm
4. Tồn kho: 100 → 20
5. Muốn xóa phiếu PNH002 (100 khăn)
6. ❌ Lỗi: Tồn kho chỉ còn 20, không đủ trừ 100
7. → Không cho xóa ✅ (Đảm bảo dữ liệu đúng!)
```

## 📱 API Response Example

### Success Response
```json
{
  "code": 200,
  "message": "Xóa phiếu nhập hàng thành công"
}
```

### Error Response - Tồn Kho Không Đủ
```json
{
  "code": 1142,
  "message": "Số lượng tồn kho không đủ để xóa phiếu nhập hàng - Mã DV: DV003 - Tồn kho hiện tại: 50, Cần trừ: 100"
}
```

### Error Response - Đã Xóa
```json
{
  "code": 1141,
  "message": "Phiếu nhập hàng đã được xóa trước đó - Mã: PNH001"
}
```

---

✨ **Implementation hoàn tất! Logic xóa phiếu nhập hàng đã phức tạp hơn và chính xác hơn!**

