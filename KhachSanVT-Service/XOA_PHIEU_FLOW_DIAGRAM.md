# Flow Diagram: Xóa Phiếu Nhập Hàng

## 🔄 Sequence Diagram

```
Client          Controller          Service                 Repository              Database
  |                 |                   |                        |                      |
  |--DELETE-------->|                   |                        |                      |
  |  /api/phieu-    |                   |                        |                      |
  |  nhap-hang/     |                   |                        |                      |
  |  PNH001         |                   |                        |                      |
  |                 |                   |                        |                      |
  |                 |--xoaPhieuNhap---->|                        |                      |
  |                 |  NhapHang()       |                        |                      |
  |                 |                   |                        |                      |
  |                 |                   |--findById(PNH001)----->|                      |
  |                 |                   |                        |--SELECT------------>|
  |                 |                   |                        |   PhieuNhapHang     |
  |                 |                   |<------PhieuNhapHang----|<--------------------|
  |                 |                   |                        |                      |
  |                 |                   |--Check: daXoa?         |                      |
  |                 |                   |                        |                      |
  |                 |                   |--findByMaPhieu-------->|                      |
  |                 |                   |  NhapHang()            |--SELECT------------>|
  |                 |                   |                        |   NhapHang          |
  |                 |                   |<------List<NhapHang>---|   WHERE ma_phieu    |
  |                 |                   |                        |<--------------------|
  |                 |                   |                        |                      |
  |                 |                   |--LOOP: NhapHang        |                      |
  |                 |                   |  |                     |                      |
  |                 |                   |  |--getDichVuMienPhi   |                      |
  |                 |                   |  |                     |                      |
  |                 |                   |  |--findByDichVu------>|                      |
  |                 |                   |  |  MienPhi()          |--SELECT------------>|
  |                 |                   |  |                     |   TonKho            |
  |                 |                   |  |<------TonKho--------|<--------------------|
  |                 |                   |  |                     |                      |
  |                 |                   |  |--Check: soLuong >= soLuongCanTru?          |
  |                 |                   |  |                     |                      |
  |                 |                   |  |--Calculate:         |                      |
  |                 |                   |  |  soLuongMoi = soLuongHienTai - soLuongCanTru
  |                 |                   |  |                     |                      |
  |                 |                   |  |--save(TonKho)------>|                      |
  |                 |                   |  |                     |--UPDATE------------>|
  |                 |                   |  |                     |   TonKho            |
  |                 |                   |  |<--------------------|   SET so_luong      |
  |                 |                   |  |                     |<--------------------|
  |                 |                   |  |                     |                      |
  |                 |                   |  |--Log: "📦 Cập nhật tồn kho..."            |
  |                 |                   |  |                     |                      |
  |                 |                   |--END LOOP             |                      |
  |                 |                   |                        |                      |
  |                 |                   |--setDaXoa(true)        |                      |
  |                 |                   |--save(PhieuNhap)------>|                      |
  |                 |                   |                        |--UPDATE------------>|
  |                 |                   |                        |   PhieuNhapHang     |
  |                 |                   |                        |   SET da_xoa=true   |
  |                 |                   |<-----------------------|<--------------------|
  |                 |                   |                        |                      |
  |                 |                   |--Log: "🗑️ Xóa phiếu nhập hàng thành công..."  |
  |                 |                   |                        |                      |
  |                 |<--SUCCESS---------|                        |                      |
  |<--200 OK--------|                   |                        |                      |
  |                 |                   |                        |                      |
```

## 🎨 State Diagram

```
                         ┌─────────────────────────┐
                         │  Phiếu Nhập Hàng        │
                         │  daXoa = false          │
                         └──────────┬──────────────┘
                                    │
                                    │ Gọi xoaPhieuNhapHang()
                                    ↓
                         ┌─────────────────────────┐
                         │  Validate Phiếu         │
                         │  - Tồn tại?             │
                         │  - Đã xóa?              │
                         └──────────┬──────────────┘
                                    │
                    ┌───────────────┴────────────────┐
                    │                                │
              Không hợp lệ                      Hợp lệ
                    │                                │
                    ↓                                ↓
         ┌─────────────────────┐      ┌─────────────────────────┐
         │  Throw Exception    │      │  Lấy List<NhapHang>    │
         │  - KHONG_TON_TAI    │      │  theo maPhieuNhapHang   │
         │  - DA_XOA           │      └──────────┬──────────────┘
         └─────────────────────┘                 │
                                                  │ Loop qua từng NhapHang
                                                  ↓
                                   ┌─────────────────────────┐
                                   │  Tìm TonKho theo        │
                                   │  DichVuMienPhi          │
                                   └──────────┬──────────────┘
                                              │
                          ┌───────────────────┴────────────────┐
                          │                                    │
                   Không tìm thấy                   Tìm thấy TonKho
                          │                                    │
                          ↓                                    ↓
              ┌──────────────────────┐         ┌──────────────────────────┐
              │  Throw Exception     │         │  Kiểm tra số lượng       │
              │  KHONG_TON_TAI       │         │  soLuongHienTai >=       │
              └──────────────────────┘         │  soLuongCanTru?          │
                                               └──────────┬───────────────┘
                                                          │
                                       ┌──────────────────┴──────────────┐
                                       │                                 │
                                  Không đủ                           Đủ số lượng
                                       │                                 │
                                       ↓                                 ↓
                          ┌────────────────────┐        ┌────────────────────────┐
                          │  Throw Exception   │        │  Trừ số lượng:         │
                          │  TON_KHO_KHONG_DU  │        │  soLuongMoi =          │
                          └────────────────────┘        │  soLuongHienTai -      │
                                                         │  soLuongCanTru         │
                                                         └─────────┬──────────────┘
                                                                   │
                                                                   ↓
                                                         ┌────────────────────────┐
                                                         │  Save TonKho mới       │
                                                         │  Log chi tiết          │
                                                         └─────────┬──────────────┘
                                                                   │
                                                                   │ Tiếp tục loop
                                                                   ↓
                                                         ┌────────────────────────┐
                                                         │  Đã xử lý hết          │
                                                         │  List<NhapHang>        │
                                                         └─────────┬──────────────┘
                                                                   │
                                                                   ↓
                                                         ┌────────────────────────┐
                                                         │  setDaXoa(true)        │
                                                         │  Save PhieuNhapHang    │
                                                         └─────────┬──────────────┘
                                                                   │
                                                                   ↓
                                                         ┌────────────────────────┐
                                                         │  Phiếu Nhập Hàng       │
                                                         │  daXoa = true          │
                                                         │  ✅ SUCCESS            │
                                                         └────────────────────────┘
```

## 📊 Data Flow

```
INPUT: maPhieuNhapHang = "PNH001"
   │
   ↓
┌──────────────────────────────────────────────────────────────┐
│  DATABASE QUERY #1                                           │
│  SELECT * FROM phieu_nhap_hang WHERE ma_phieu_nhap_hang='PNH001' │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ↓
                  PhieuNhapHang
                  { maPhieuNhapHang: "PNH001",
                    daXoa: false }
                       │
                       ↓
┌──────────────────────────────────────────────────────────────┐
│  DATABASE QUERY #2                                           │
│  SELECT * FROM nhap_hang WHERE ma_phieu_nhap_hang='PNH001'   │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ↓
              List<NhapHang>
              [ { maDichVu: "DV001", soLuong: 100 },
                { maDichVu: "DV002", soLuong: 50 } ]
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ↓                             ↓
   NhapHang #1                   NhapHang #2
   DV001: 100                    DV002: 50
        │                             │
        ↓                             ↓
┌──────────────────┐          ┌──────────────────┐
│  Query TonKho    │          │  Query TonKho    │
│  WHERE ma_dv     │          │  WHERE ma_dv     │
│  = 'DV001'       │          │  = 'DV002'       │
└────────┬─────────┘          └────────┬─────────┘
         │                             │
         ↓                             ↓
    TonKho DV001                  TonKho DV002
    soLuong: 250                  soLuong: 80
         │                             │
         ↓                             ↓
    Check: 250 >= 100 ✅          Check: 80 >= 50 ✅
         │                             │
         ↓                             ↓
    250 - 100 = 150               80 - 50 = 30
         │                             │
         ↓                             ↓
┌──────────────────┐          ┌──────────────────┐
│  UPDATE ton_kho  │          │  UPDATE ton_kho  │
│  SET so_luong    │          │  SET so_luong    │
│  = 150           │          │  = 30            │
│  WHERE ma_dv     │          │  WHERE ma_dv     │
│  = 'DV001'       │          │  = 'DV002'       │
└────────┬─────────┘          └────────┬─────────┘
         │                             │
         └──────────────┬──────────────┘
                        │
                        ↓
         ┌──────────────────────────────┐
         │  UPDATE phieu_nhap_hang      │
         │  SET da_xoa = true           │
         │  WHERE ma_phieu = 'PNH001'   │
         └──────────────┬───────────────┘
                        │
                        ↓
                   ✅ SUCCESS
                        │
                        ↓
OUTPUT: void (Success) + Logs
   📦 Cập nhật tồn kho khi xóa - Mã DV: DV001, SL cũ: 250, SL trừ: 100, SL mới: 150
   📦 Cập nhật tồn kho khi xóa - Mã DV: DV002, SL cũ: 80, SL trừ: 50, SL mới: 30
   🗑️ Xóa phiếu nhập hàng thành công - Mã: PNH001, Người thực hiện: cvkhang
```

## 🔒 Transaction Flow

```
┌─────────────────────────────────────────────────────────┐
│  @Transactional BEGIN                                   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  1. SELECT PhieuNhapHang                        │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  2. SELECT List<NhapHang>                       │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  3. LOOP: Update TonKho                         │   │
│  │     - SELECT TonKho                              │   │
│  │     - Validate số lượng                          │   │
│  │     - UPDATE TonKho.so_luong                     │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  4. UPDATE PhieuNhapHang.da_xoa = true          │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  COMMIT ✅                                              │
│  (Tất cả thay đổi được lưu vào database)                │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Nếu có Exception:                                      │
├─────────────────────────────────────────────────────────┤
│  ROLLBACK ⏪                                            │
│  (Tất cả thay đổi bị hủy bỏ)                            │
│  - PhieuNhapHang không đổi                              │
│  - TonKho không đổi                                     │
│  - Database giữ nguyên trạng thái ban đầu               │
└─────────────────────────────────────────────────────────┘
```

---

✨ **Diagram minh họa luồng xử lý xóa phiếu nhập hàng hoàn chỉnh!**

