# Hướng Dẫn: Update/Create Nhập Hàng

## 📋 Tổng Quan
Method `updateOrCreateNhapHang()` thực hiện:
- **Nếu `maNhapHang` rỗng/null** → Thêm mới (tăng tồn kho)
- **Nếu `maNhapHang` có giá trị** → Update (điều chỉnh tồn kho)

## 🎯 Signature

```java
@Transactional
public NhapHangReponse updateOrCreateNhapHang(NhapHangRequest request)
```

### Input: `NhapHangRequest`
```java
{
    "maNhapHang": "123",  // Nếu rỗng → Create, Nếu có → Update
    "maDichVu": "DV001",
    "soLuong": 100,
    "loai": "Nhập đầu kỳ",
    "ghiChu": "Ghi chú"
}
```

### Output: `NhapHangReponse`
```java
{
    "maNhapHang": 123,
    "dichVuMienPhi": { ... },
    "soLuong": 100,
    "loai": "Nhập đầu kỳ",
    "ghiChu": "Ghi chú",
    "daXoa": false
}
```

## 🔄 Luồng Xử Lý

```
┌─────────────────────────────────────────────────────────┐
│  updateOrCreateNhapHang(request)                        │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
          ┌─────────────────────┐
          │  Validate Request   │
          │  - maDichVu         │
          │  - soLuong > 0      │
          │  - loai not empty   │
          └──────────┬──────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
   maNhapHang                maNhapHang
   rỗng/null?               có giá trị?
        │                         │
        ↓                         ↓
┌───────────────┐         ┌───────────────┐
│ CREATE MODE   │         │ UPDATE MODE   │
└───────┬───────┘         └───────┬───────┘
        │                         │
        ↓                         ↓
```

## 🆕 CREATE MODE (maNhapHang rỗng/null)

### Luồng Xử Lý:
```
1. Validate dịch vụ tồn tại & active
   ↓
2. Tạo NhapHang entity mới
   ↓
3. Save NhapHang
   ↓
4. Tìm TonKho (tạo mới nếu chưa có)
   ↓
5. Cộng số lượng: soLuongMoi = soLuongCu + soLuongThem
   ↓
6. Save TonKho
   ↓
7. Log & Return
```

### Ví Dụ:

**Input:**
```json
{
    "maNhapHang": null,
    "maDichVu": "DV001",
    "soLuong": 100,
    "loai": "Nhập đầu kỳ",
    "ghiChu": "Nhập tháng 1"
}
```

**Database - Before:**
```
TonKho DV001: 50
```

**Process:**
```java
createNhapHang(request)
→ Tạo NhapHang mới (id: 123)
→ TonKho: 50 + 100 = 150
```

**Database - After:**
```
NhapHang #123: DV001, SL=100
TonKho DV001: 150 ✅
```

**Log:**
```
✅ Thêm mới nhập hàng - Mã: 123, Dịch vụ: DV001, SL: 100, Người tạo: cvkhang
📦 Cập nhật tồn kho (thêm mới) - Mã DV: DV001, SL cũ: 50, SL thêm: 100, SL mới: 150
```

## 🔄 UPDATE MODE (maNhapHang có giá trị)

### Luồng Xử Lý:
```
1. Parse & validate maNhapHang
   ↓
2. Tìm NhapHang hiện tại (throw nếu không tồn tại)
   ↓
3. Lưu thông tin cũ (soLuongCu, dichVuCu)
   ↓
4. Kiểm tra có thay đổi dịch vụ?
   ├─ YES → Trừ tồn kho cũ, cộng tồn kho mới
   └─ NO  → Điều chỉnh delta số lượng
   ↓
5. Update NhapHang
   ↓
6. Save & Log
```

### Tình Huống 1: Thay Đổi Số Lượng (Cùng Dịch Vụ)

**Input:**
```json
{
    "maNhapHang": "123",
    "maDichVu": "DV001",
    "soLuong": 150,  // Tăng từ 100 → 150
    "loai": "Nhập bổ sung",
    "ghiChu": "Điều chỉnh"
}
```

**Database - Before:**
```
NhapHang #123: DV001, SL=100
TonKho DV001: 250
```

**Process:**
```java
updateNhapHang(request)
→ soLuongCu = 100, soLuongMoi = 150
→ delta = 150 - 100 = +50
→ TonKho: 250 + 50 = 300
```

**Database - After:**
```
NhapHang #123: DV001, SL=150 ✅
TonKho DV001: 300 ✅
```

**Log:**
```
🔄 Cập nhật nhập hàng - Mã: 123, SL cũ: 100, SL mới: 150, Người cập nhật: cvkhang
📦 Cập nhật tồn kho (update) - Mã DV: DV001, SL cũ: 250, Delta: +50, SL mới: 300
```

### Tình Huống 2: Giảm Số Lượng

**Input:**
```json
{
    "maNhapHang": "123",
    "maDichVu": "DV001",
    "soLuong": 50,  // Giảm từ 100 → 50
    "loai": "Điều chỉnh giảm",
    "ghiChu": "Điều chỉnh"
}
```

**Database - Before:**
```
NhapHang #123: DV001, SL=100
TonKho DV001: 250
```

**Process:**
```java
updateNhapHang(request)
→ soLuongCu = 100, soLuongMoi = 50
→ delta = 50 - 100 = -50
→ Check: 250 >= 50 ✅
→ TonKho: 250 - 50 = 200
```

**Database - After:**
```
NhapHang #123: DV001, SL=50 ✅
TonKho DV001: 200 ✅
```

**Log:**
```
🔄 Cập nhật nhập hàng - Mã: 123, SL cũ: 100, SL mới: 50, Người cập nhật: cvkhang
📦 Cập nhật tồn kho (update) - Mã DV: DV001, SL cũ: 250, Delta: -50, SL mới: 200
```

### Tình Huống 3: Thay Đổi Dịch Vụ

**Input:**
```json
{
    "maNhapHang": "123",
    "maDichVu": "DV002",  // Đổi từ DV001 → DV002
    "soLuong": 80,
    "loai": "Thay đổi dịch vụ",
    "ghiChu": "Chuyển sang DV khác"
}
```

**Database - Before:**
```
NhapHang #123: DV001, SL=100
TonKho DV001: 250
TonKho DV002: 100
```

**Process:**
```java
updateNhapHang(request)
→ Phát hiện thay đổi dịch vụ: DV001 → DV002
→ Trừ TonKho DV001: 250 - 100 = 150
→ Cộng TonKho DV002: 100 + 80 = 180
```

**Database - After:**
```
NhapHang #123: DV002, SL=80 ✅
TonKho DV001: 150 ✅
TonKho DV002: 180 ✅
```

**Log:**
```
📦 Cập nhật tồn kho (update) - Mã DV: DV001, SL cũ: 250, Delta: -100, SL mới: 150
📦 Cập nhật tồn kho (update) - Mã DV: DV002, SL cũ: 100, Delta: +80, SL mới: 180
🔄 Cập nhật nhập hàng - Mã: 123, SL cũ: 100, SL mới: 80, Người cập nhật: cvkhang
```

## ⚠️ Error Handling

### Error 1: Tồn Kho Không Đủ Khi Giảm

**Scenario:**
```
NhapHang #123: DV001, SL=100
TonKho DV001: 30 ⚠️

Update: soLuong từ 100 → 50
→ Cần trừ: 100 - 50 = 50
→ Tồn kho chỉ có: 30 < 50 ❌
```

**Response:**
```json
{
    "code": 1142,
    "message": "Số lượng tồn kho không đủ để xóa phiếu nhập hàng - Mã DV: DV001 - Tồn kho: 30, Cần trừ: 50"
}
```

### Error 2: Nhập Hàng Không Tồn Tại

**Input:**
```json
{
    "maNhapHang": "999",
    "maDichVu": "DV001",
    "soLuong": 100
}
```

**Response:**
```json
{
    "code": 1143,
    "message": "Nhập hàng không tồn tại - Mã: 999"
}
```

### Error 3: Dịch Vụ Bị Khóa

**Response:**
```json
{
    "code": 1139,
    "message": "Dịch vụ miễn phí đã bị khóa - Mã: DV001"
}
```

## 📊 So Sánh Create vs Update

| Tiêu Chí | CREATE | UPDATE |
|----------|--------|--------|
| Điều kiện | maNhapHang = null/empty | maNhapHang có giá trị |
| Tạo record mới | ✅ YES | ❌ NO |
| Tìm record cũ | ❌ NO | ✅ YES |
| Cập nhật TonKho | Cộng số lượng | Điều chỉnh delta |
| Validate tồn tại | Không cần | Phải tồn tại |
| Kiểm tra tồn kho đủ | Không cần | Nếu giảm SL |

## 🎯 Use Cases

### Use Case 1: Nhập Hàng Mới Đầu Kỳ
```java
NhapHangRequest request = new NhapHangRequest();
request.setMaNhapHang(null);  // CREATE
request.setMaDichVu("DV001");
request.setSoLuong(1000);
request.setLoai("Nhập đầu kỳ");

service.updateOrCreateNhapHang(request);
// → Tạo mới + Tăng tồn kho 1000
```

### Use Case 2: Điều Chỉnh Số Lượng Sau Kiểm Kê
```java
NhapHangRequest request = new NhapHangRequest();
request.setMaNhapHang("123");  // UPDATE
request.setMaDichVu("DV001");
request.setSoLuong(950);  // Điều chỉnh từ 1000 → 950
request.setLoai("Điều chỉnh kiểm kê");

service.updateOrCreateNhapHang(request);
// → Update - Giảm tồn kho 50
```

### Use Case 3: Sửa Lỗi Nhập Nhầm Dịch Vụ
```java
NhapHangRequest request = new NhapHangRequest();
request.setMaNhapHang("123");  // UPDATE
request.setMaDichVu("DV002");  // Đổi từ DV001 → DV002
request.setSoLuong(100);

service.updateOrCreateNhapHang(request);
// → Trừ tồn DV001, Cộng tồn DV002
```

## 🔐 Security & Audit

```java
// Tự động capture username
String username = SecurityUtil.getUsername();

// Log chi tiết
log.info("✅ Thêm mới nhập hàng - ..., Người tạo: {}", username);
log.info("🔄 Cập nhật nhập hàng - ..., Người cập nhật: {}", username);
```

## 📱 API Example

### Controller
```java
@PutMapping("/nhap-hang")
public ResponseEntity<NhapHangReponse> updateOrCreate(
        @RequestBody NhapHangRequest request) {
    NhapHangReponse response = service.updateOrCreateNhapHang(request);
    return ResponseEntity.ok(response);
}
```

### Request - Create
```http
PUT /api/nhap-hang
Content-Type: application/json

{
    "maDichVu": "DV001",
    "soLuong": 100,
    "loai": "Nhập mới",
    "ghiChu": "Nhập hàng tháng 2"
}
```

### Request - Update
```http
PUT /api/nhap-hang
Content-Type: application/json

{
    "maNhapHang": "123",
    "maDichVu": "DV001",
    "soLuong": 150,
    "loai": "Điều chỉnh",
    "ghiChu": "Tăng số lượng"
}
```

## ✅ Validation Checklist

### Create Mode:
- ✅ maDichVu không rỗng
- ✅ soLuong > 0
- ✅ loai không rỗng
- ✅ Dịch vụ tồn tại
- ✅ Dịch vụ còn hoạt động

### Update Mode:
- ✅ maNhapHang hợp lệ & tồn tại
- ✅ Tất cả validation của Create
- ✅ Nếu giảm SL → Tồn kho đủ
- ✅ Nếu đổi DV → DV mới hợp lệ

## 🎯 Transaction Flow

```
┌────────────────────────────────────────┐
│  @Transactional BEGIN                  │
├────────────────────────────────────────┤
│  1. Validate request                   │
│  2. Check CREATE or UPDATE             │
│  3. Process (create/update entity)     │
│  4. Update TonKho                      │
│  5. Save all changes                   │
├────────────────────────────────────────┤
│  COMMIT ✅                             │
│  (hoặc ROLLBACK nếu có lỗi) ⏪         │
└────────────────────────────────────────┘
```

---

✨ **Implementation hoàn tất! Method update/create nhập hàng đã sẵn sàng sử dụng!**

