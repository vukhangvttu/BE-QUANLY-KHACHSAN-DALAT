# HƯỚNG DẪN SỬ DỤNG QUANTITY - API KIỂM TRA PHÒNG TRỐNG

## Tổng quan

API đã được nâng cấp để hỗ trợ kiểm tra số lượng phòng cụ thể. Bạn có thể yêu cầu kiểm tra xem có đủ N phòng trống trong khoảng thời gian hay không.

---

## 📋 Những gì đã thay đổi

### ✅ Request DTOs
- **BookingDetailRequest**: Thêm trường `quantity` (bắt buộc, >= 1)

### ✅ Response DTOs
- **RoomAvailabilityDetailResponse**: Thêm trường `requestedQuantity` để hiển thị số lượng yêu cầu

### ✅ Service Layer
- Logic kiểm tra đã được cập nhật: `availableRooms >= requestedQuantity`
- Status message chi tiết: "Còn đủ phòng (Còn 5/10)" hoặc "Không đủ phòng (Còn 3/10)"

### ✅ Controller
- **POST /check**: Hỗ trợ quantity trong request body
- **GET /check-range**: Thêm query parameter `quantity` (optional, default = 1)
- **GET /check-date**: Thêm query parameter `quantity` (optional, default = 1)

---

## 🚀 Cách sử dụng

### 1. POST API - Kiểm tra với Request Body

**Endpoint**: `POST /api/room-availability/check`

**Request Body MỚI**:
```json
{
  "bookingDetails": [
    {
      "roomTypeId": "CS-C-D",
      "quantity": 10,
      "listDate": [
        { "ngay": "2025-12-26" },
        { "ngay": "2025-12-27" },
        { "ngay": "2025-12-28" }
      ]
    }
  ]
}
```

**Response khi ĐỦ phòng**:
```json
{
  "code": 200,
  "message": "Tất cả các loại phòng đều đủ số lượng trong khoảng thời gian yêu cầu",
  "result": {
    "allAvailable": true,
    "message": "Tất cả các loại phòng đều đủ số lượng trong khoảng thời gian yêu cầu",
    "availabilityDetails": [
      {
        "roomTypeId": "CS-C-D",
        "roomTypeName": "Phòng Deluxe",
        "date": "2025-12-26",
        "totalRooms": 20,
        "availableRooms": 15,
        "bookedRooms": 5,
        "requestedQuantity": 10,
        "isAvailable": true,
        "status": "Còn đủ phòng (Còn 15/10)"
      },
      {
        "roomTypeId": "CS-C-D",
        "roomTypeName": "Phòng Deluxe",
        "date": "2025-12-27",
        "totalRooms": 20,
        "availableRooms": 12,
        "bookedRooms": 8,
        "requestedQuantity": 10,
        "isAvailable": true,
        "status": "Còn đủ phòng (Còn 12/10)"
      }
    ]
  }
}
```

**Response khi KHÔNG ĐỦ phòng**:
```json
{
  "code": 200,
  "message": "Có phòng không đủ số lượng yêu cầu",
  "result": {
    "allAvailable": false,
    "message": "Loại phòng Phòng Deluxe (CS-C-D) không đủ phòng vào ngày 2025-12-27. Yêu cầu: 10 phòng, Còn trống: 5 phòng.",
    "availabilityDetails": [
      {
        "roomTypeId": "CS-C-D",
        "roomTypeName": "Phòng Deluxe",
        "date": "2025-12-26",
        "totalRooms": 20,
        "availableRooms": 12,
        "bookedRooms": 8,
        "requestedQuantity": 10,
        "isAvailable": true,
        "status": "Còn đủ phòng (Còn 12/10)"
      },
      {
        "roomTypeId": "CS-C-D",
        "roomTypeName": "Phòng Deluxe",
        "date": "2025-12-27",
        "totalRooms": 20,
        "availableRooms": 5,
        "bookedRooms": 15,
        "requestedQuantity": 10,
        "isAvailable": false,
        "status": "Không đủ phòng (Còn 5/10)"
      }
    ]
  }
}
```

---

### 2. GET API - Check Range với Quantity

**Endpoint**: `GET /api/room-availability/check-range`

**Parameters**:
- `roomTypeId` (required): Mã loại phòng
- `startDate` (required): Ngày bắt đầu (yyyy-MM-dd)
- `endDate` (required): Ngày kết thúc (yyyy-MM-dd)
- `quantity` (optional, default = 1): Số lượng phòng yêu cầu

**Example với quantity**:
```
GET /api/room-availability/check-range?roomTypeId=CS-C-D&startDate=2025-12-26&endDate=2025-12-28&quantity=10
```

**Example không có quantity (mặc định = 1)**:
```
GET /api/room-availability/check-range?roomTypeId=CS-C-D&startDate=2025-12-26&endDate=2025-12-28
```

**cURL Example**:
```bash
curl "http://localhost:8310/khachsan-service/api/room-availability/check-range?roomTypeId=CS-C-D&startDate=2025-12-26&endDate=2025-12-28&quantity=10"
```

---

### 3. GET API - Check Date với Quantity

**Endpoint**: `GET /api/room-availability/check-date`

**Parameters**:
- `roomTypeId` (required): Mã loại phòng
- `date` (required): Ngày cần kiểm tra (yyyy-MM-dd)
- `quantity` (optional, default = 1): Số lượng phòng yêu cầu

**Example**:
```
GET /api/room-availability/check-date?roomTypeId=CS-C-D&date=2025-12-26&quantity=5
```

---

## 📊 So sánh Request CŨ vs MỚI

### ❌ Request CŨ (không có quantity):
```json
{
  "bookingDetails": [
    {
      "roomTypeId": "CS-C-D",
      "listDate": [
        { "ngay": "2025-12-26" }
      ]
    }
  ]
}
```
→ **Lỗi**: Missing required field `quantity`

### ✅ Request MỚI (có quantity):
```json
{
  "bookingDetails": [
    {
      "roomTypeId": "CS-C-D",
      "quantity": 10,
      "listDate": [
        { "ngay": "2025-12-26" }
      ]
    }
  ]
}
```
→ **OK**: Kiểm tra xem có đủ 10 phòng không

---

## 🎯 Use Cases

### Use Case 1: Booking cho nhóm khách lớn
```json
{
  "bookingDetails": [
    {
      "roomTypeId": "CS-C-D",
      "quantity": 15,
      "listDate": [
        { "ngay": "2025-12-26" },
        { "ngay": "2025-12-27" }
      ]
    }
  ]
}
```
→ Kiểm tra có đủ 15 phòng Deluxe cho 2 ngày không

### Use Case 2: Booking nhiều loại phòng
```json
{
  "bookingDetails": [
    {
      "roomTypeId": "CS-C-D",
      "quantity": 10,
      "listDate": [
        { "ngay": "2025-12-26" }
      ]
    },
    {
      "roomTypeId": "CS-C-S",
      "quantity": 5,
      "listDate": [
        { "ngay": "2025-12-26" }
      ]
    }
  ]
}
```
→ Kiểm tra: 10 phòng Deluxe + 5 phòng Suite

### Use Case 3: Kiểm tra nhanh 1 phòng
```bash
curl "http://localhost:8310/khachsan-service/api/room-availability/check-date?roomTypeId=CS-C-D&date=2025-12-26"
```
→ Quantity mặc định = 1 (kiểm tra có ít nhất 1 phòng trống)

---

## ✅ Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| `quantity` | NOT NULL | "Số lượng phòng không được rỗng" |
| `quantity` | >= 1 | "Số lượng phòng phải lớn hơn 0" |
| `roomTypeId` | NOT BLANK | "Mã loại phòng không được rỗng" |
| `listDate` | NOT EMPTY | "Danh sách ngày không được rỗng" |

---

## 📝 Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `requestedQuantity` | int | Số lượng phòng yêu cầu |
| `availableRooms` | int | Số phòng còn trống |
| `isAvailable` | boolean | `true` nếu `availableRooms >= requestedQuantity` |
| `status` | string | "Còn đủ phòng (Còn X/Y)" hoặc "Không đủ phòng (Còn X/Y)" |

---

## 🧪 Testing với Postman

### Import Collection
1. Import file: `postman_collection_with_quantity.json`
2. Chọn request cần test
3. Click "Send"

### Test Cases
1. ✅ **Test với quantity hợp lệ** (1, 5, 10)
2. ✅ **Test với quantity = 0** (expect error)
3. ✅ **Test với quantity âm** (expect error)
4. ✅ **Test với quantity rất lớn** (expect không đủ phòng)
5. ✅ **Test không truyền quantity** (expect default = 1)
6. ✅ **Test nhiều loại phòng với quantity khác nhau**

---

## 🔍 Kiểm tra Logs

Logs sẽ hiển thị thông tin chi tiết:

```
INFO: Kiểm tra loại phòng: CS-C-D - Phòng Deluxe với số lượng yêu cầu: 10
INFO: Loại phòng CS-C-D còn đủ 15 phòng trống vào ngày 2025-12-26 (Yêu cầu: 10)
WARN: Loại phòng CS-C-D không đủ phòng vào ngày 2025-12-27. Yêu cầu: 10, Còn: 5
INFO: Kết quả kiểm tra: Không đủ phòng - Loại phòng Phòng Deluxe (CS-C-D) không đủ phòng vào ngày 2025-12-27. Yêu cầu: 10 phòng, Còn trống: 5 phòng.
```

---

## 🚨 Error Handling

### Error 1: Quantity = 0
```json
{
  "code": 400,
  "message": "Số lượng phòng phải lớn hơn 0",
  "result": null
}
```

### Error 2: Quantity âm
```json
{
  "code": 400,
  "message": "Số lượng phòng phải lớn hơn 0",
  "result": null
}
```

### Error 3: Missing quantity
```json
{
  "code": 400,
  "message": "Số lượng phòng không được rỗng",
  "result": null
}
```

---

## 💡 Tips

1. **Mặc định quantity = 1** cho GET APIs
2. **Bắt buộc quantity** cho POST API
3. **Status message** rõ ràng: "Còn 15/10" = Còn 15 phòng, yêu cầu 10
4. **isAvailable** = `true` chỉ khi `availableRooms >= requestedQuantity`
5. **Thread-safe**: Đảm bảo không bị race condition khi nhiều user truy cập

---

## 📞 Support

Nếu gặp vấn đề:
1. Check logs trong `logs/khachsan-service.log`
2. Verify request body theo format mới
3. Kiểm tra validation errors
4. Test với Postman collection

---

**Last Updated**: 2025-12-27  
**Version**: 2.0.0 (với Quantity support)  
**Status**: ✅ Ready for Testing

