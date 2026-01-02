# Cập nhật chức năng xemDuBaoPhong - Tích hợp API Golden Era

## Tổng quan
Đã thiết kế lại phương thức `xemDuBaoPhong` để tích hợp với API Golden Era và cộng dồn số phòng từ hệ thống booking bên ngoài.

## Các thay đổi chính

### 1. File cấu hình: `application.yml`
Thêm cấu hình cho Golden Era API:
```yaml
external:
  golden-era:
    base-url: ${GOLDEN_ERA_BASE_URL:http://localhost:8010/api/v1/golden-era}
```
- Có thể thay đổi base URL thông qua biến môi trường `GOLDEN_ERA_BASE_URL`
- Mặc định: `http://localhost:8010/api/v1/golden-era`

### 2. DTO mới

#### `BookingStatsDTO.java`
DTO để nhận dữ liệu từ Golden Era API với 4 trường:
- `stayDate`: Ngày lưu trú (format: yyyy-MM-dd)
- `roomTypeId`: Mã loại phòng
- `roomTypeName`: Tên loại phòng
- `totalRooms`: Tổng số phòng

#### `LoaiPhongTrongDTOImpl.java`
Implementation của interface `LoaiPhongTrongDTO` để có thể cập nhật số phòng trống:
- Chứa tất cả các trường của `LoaiPhongTrongDTO`
- Có phương thức `setSoPhongTrong(int)` để cập nhật số phòng trống

### 3. Configuration: `RestTemplateConfig.java`
Tạo bean `RestTemplate` để gọi API external:
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 4. Cập nhật `LoaiPhongService.java`

#### Thêm dependencies
- `RestTemplate`: Để gọi API
- `@Value("${external.golden-era.base-url}")`: Inject base URL từ config

**Lưu ý về Dependency Injection:**
- Các dependency chính (`LoaiPhongRepository`, `LoaiPhongMapper`, `RestTemplate`) được inject qua constructor với `final`
- `goldenEraBaseUrl` sử dụng `@Value` và **không được đánh dấu `final`** để tránh conflict với `@RequiredArgsConstructor`
- Sử dụng `@FieldDefaults(level = AccessLevel.PRIVATE)` thay vì `makeFinal = true`

```java
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class LoaiPhongService {

    final LoaiPhongRepository loaiPhongRepository;
    final LoaiPhongMapper loaiPhongMapper;
    final RestTemplate restTemplate;

    @Value("${external.golden-era.base-url}")
    String goldenEraBaseUrl;
    
    // ...
}
```

#### Phương thức `xemDuBaoPhong`
Luồng xử lý:
1. Validate ngày đến và ngày đi
2. Lấy danh sách phòng trống từ repository nội bộ
3. Gọi API Golden Era để lấy thống kê booking
4. Nếu có lỗi API → log warning và trả về dữ liệu nội bộ
5. Tạo map tra cứu nhanh (ngày + mã loại phòng → số phòng)
6. Cập nhật số phòng trống theo công thức:
   ```
   Số phòng trống mới = Số phòng trống hiện tại + totalRooms từ API
   ```
7. Trả về danh sách `LoaiPhongTrongDTO` đã được cập nhật

#### Phương thức `fetchBookingStats` (private)
- Gọi API: `GET /booking-stats/daily`
- Query params:
  - `checkInDate`: Ngày đến (ISO format)
  - `checkOutDate`: Ngày đi (ISO format)
  - `region`: "VUNGTAU"
  - `status`: "CONFIRMED"
- Xử lý lỗi:
  - Log chi tiết lỗi
  - Throw `AppException` với message rõ ràng
  - Không làm crash ứng dụng

#### Phương thức `buildBookingMap` (private)
Tạo cấu trúc Map lồng nhau để tra cứu O(1):
```
Map<String, Map<String, Integer>>
├─ "2026-01-01"
│  ├─ "DLX-D" → 2
│  ├─ "DLX-T" → 1
│  └─ "SUP-S-D" → 3
├─ "2026-01-02"
│  └─ ...
```

## API Golden Era

### Endpoint
```
GET http://localhost:8010/api/v1/golden-era/booking-stats/daily
```

### Query Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| checkInDate | String (yyyy-MM-dd) | Yes | Ngày đến |
| checkOutDate | String (yyyy-MM-dd) | Yes | Ngày đi |
| region | String | Yes | Khu vực (VUNGTAU) |
| status | String | Yes | Trạng thái (CONFIRMED) |

### Response Format
```json
[
  {
    "stayDate": "2026-01-02",
    "roomTypeId": "DLX-T",
    "roomTypeName": "Deluxe Sea View Twin",
    "totalRooms": 1
  },
  ...
]
```

## Ví dụ sử dụng

### Request
```
GET /api/loai-phong/xem-du-bao?ngayDen=2026-01-01&ngayDi=2026-01-08
```

### Xử lý
1. Lấy dữ liệu nội bộ: DLX-T ngày 2026-01-02 có 5 phòng trống
2. Gọi Golden Era API: DLX-T ngày 2026-01-02 có 1 phòng booking
3. Tính toán: 5 + 1 = 6 phòng trống
4. Trả về: DLX-T ngày 2026-01-02 có 6 phòng trống

### Response
```json
[
  {
    "ngay": "2026-01-02",
    "maLoaiPhong": "DLX-T",
    "tenLoaiPhong": "Deluxe Sea View Twin",
    "tongSoPhong": 10,
    "soPhongTrong": 6,
    "tongSoGiuongThem": 5,
    "tongSoGiuongDaSuDung": 2
  },
  ...
]
```

## Xử lý lỗi

### API không khả dụng
- Log warning: "Không lấy được dữ liệu từ Golden Era API, sử dụng dữ liệu nội bộ"
- Trả về dữ liệu từ repository nội bộ (không có booking external)

### API trả về lỗi
- Log error với chi tiết lỗi
- Throw `AppException` với message: "Không thể kết nối đến Golden Era API: {error detail}"

### Không có dữ liệu
- Log warning
- Trả về danh sách trống hoặc dữ liệu nội bộ

## Lợi ích
1. ✅ Tích hợp linh hoạt với hệ thống external
2. ✅ Dễ dàng cấu hình URL qua environment variable
3. ✅ Xử lý lỗi rõ ràng, không làm crash ứng dụng
4. ✅ Performance tốt với Map lookup O(1)
5. ✅ Logging chi tiết để debug
6. ✅ Cấu trúc DTO không thay đổi (backward compatible)

## Test API
```bash
# Test Golden Era API
curl "http://localhost:8010/api/v1/golden-era/booking-stats/daily?checkInDate=2026-01-01&checkOutDate=2026-01-08&region=VUNGTAU&status=CONFIRMED"

# Test xemDuBaoPhong
curl "http://localhost:8310/khachsan-service/api/loai-phong/xem-du-bao?ngayDen=2026-01-01&ngayDi=2026-01-08"
```

## Chú ý
- Đảm bảo Golden Era API đang chạy trước khi test
- Có thể thay đổi base URL thông qua environment variable
- Cấu trúc response không thay đổi, chỉ giá trị `soPhongTrong` được cập nhật

## Troubleshooting

### Lỗi: "Parameter 3 of constructor required a bean of type 'java.lang.String'"

**Nguyên nhân:** `@Value` field được đánh dấu `final` khi sử dụng với `@RequiredArgsConstructor`

**Giải pháp:** Đã được fix bằng cách:
1. Thay đổi `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` 
   → `@FieldDefaults(level = AccessLevel.PRIVATE)`
2. Đánh dấu `final` riêng lẻ cho từng dependency cần inject qua constructor
3. Để `goldenEraBaseUrl` **không có `final`** để Spring có thể inject qua `@Value`

```java
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoaiPhongService {
    final LoaiPhongRepository loaiPhongRepository;  // ✅ Constructor injection
    final RestTemplate restTemplate;                 // ✅ Constructor injection
    
    @Value("${external.golden-era.base-url}")
    String goldenEraBaseUrl;                         // ✅ Field injection (not final)
}
```

