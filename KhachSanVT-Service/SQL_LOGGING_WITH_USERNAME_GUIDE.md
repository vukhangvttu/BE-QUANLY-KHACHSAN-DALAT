# Hướng dẫn Log SQL với Username

## 📝 Tổng quan

Hệ thống đã được cấu hình để tự động log tất cả câu SQL của Hibernate kèm theo **username** của người dùng đang thực hiện thao tác.

## 🔧 Các file đã tạo/cập nhật

### 1. **SecurityUtil.java** (Utility class)
- **Đường dẫn**: `src/main/java/vttu/khachsanservice/utils/SecurityUtil.java`
- **Chức năng**: Lấy username từ SecurityContext
- **Phương thức**: `SecurityUtil.getUsername()`

```java
String username = SecurityUtil.getUsername();
// Trả về username hoặc "ANONYMOUS" nếu chưa đăng nhập
```

### 2. **HibernateInterceptorWithUser.java** (StatementInspector)
- **Đường dẫn**: `src/main/java/vttu/khachsanservice/config/HibernateInterceptorWithUser.java`
- **Chức năng**: Intercept tất cả SQL queries và log kèm username
- **Implements**: `StatementInspector`

### 3. **HibernateConfig.java** (Configuration)
- **Đường dẫn**: `src/main/java/vttu/khachsanservice/config/HibernateConfig.java`
- **Chức năng**: Đăng ký StatementInspector với Hibernate

### 4. **application.yml** (Configuration file)
- Đã thêm logging level cho HibernateInterceptorWithUser
- Tắt `show-sql: false` để tránh log trùng lặp

## 📊 Format Log

### Trước khi cấu hình:
```
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

### Sau khi cấu hình:
```
2026-02-07 10:30:45.123 INFO  --- [username: cvkhang] - Hibernate: select ttbk1_0.ma_trang_thai_boo_king, ttbk1_0.ten_trang_thai_boo_king, ttbk1_0.vitri from trang_thai_booking ttbk1_0 where ttbk1_0.ma_trang_thai_boo_king in (1, 5, 3) order by ttbk1_0.vitri
```

## 🎯 Ví dụ sử dụng

### 1. Khi user đã đăng nhập (cvkhang)
```
👤 [username: cvkhang] - Hibernate: select * from booking where id = 1
👤 [username: cvkhang] - Hibernate: insert into khach_hang (ten, email) values ('Nguyen Van A', 'a@email.com')
👤 [username: cvkhang] - Hibernate: update phong set trang_thai = 'TRONG' where ma_phong = 'P101'
```

### 2. Khi user chưa đăng nhập (Anonymous)
```
👤 [username: ANONYMOUS] - Hibernate: select * from loai_phong
```

## 🔍 Log Level Configuration

Trong `application.yml`:

```yaml
logging:
  level:
    vttu.khachsanservice.config.HibernateInterceptorWithUser: INFO
```

- **INFO**: Hiển thị tất cả SQL queries kèm username (mặc định)
- **DEBUG**: Thêm thông tin chi tiết hơn
- **WARN**: Chỉ hiển thị warnings và errors
- **OFF**: Tắt hoàn toàn

## 💡 Sử dụng SecurityUtil trong code

Bạn có thể sử dụng `SecurityUtil.getUsername()` trong bất kỳ service nào:

```java
import vttu.khachsanservice.utils.SecurityUtil;

public void createBooking(BookingRequest request) {
    String currentUser = SecurityUtil.getUsername();
    log.info("📝 [User: {}] đang tạo booking", currentUser);
    
    // Logic tạo booking...
    booking.setNguoiTao(currentUser);
    booking.setThoiGianTao(LocalDateTime.now());
}
```

## 🎨 Icon trong log

- 👤 - User info
- 📝 - Create/Insert operation
- ✏️ - Update operation
- 🗑️ - Delete operation
- 🔍 - Select/Query operation

## ⚙️ Tùy chỉnh format log

Nếu muốn thay đổi format log, chỉnh sửa file `HibernateInterceptorWithUser.java`:

```java
@Override
public String inspect(String sql) {
    String username = SecurityUtil.getUsername();
    
    // Tùy chỉnh format tại đây
    log.info("👤 [username: {}] - Hibernate: {}", username, sql);
    
    return sql;
}
```

## 🚀 Testing

1. Khởi động ứng dụng
2. Đăng nhập với user `cvkhang`
3. Thực hiện bất kỳ thao tác nào (tạo booking, xem danh sách, etc.)
4. Kiểm tra log file hoặc console để xem username kèm SQL

## 📌 Lưu ý

- Username được lấy từ `SecurityContext` của Spring Security
- Nếu user chưa đăng nhập, username sẽ là **"ANONYMOUS"**
- Log sẽ hiển thị trong console và file log (nếu đã cấu hình)
- Format SQL có thể khác nhau tùy theo cấu hình `hibernate.format_sql`

## 🐛 Troubleshooting

### Không thấy username trong log?
- Kiểm tra log level: `logging.level.vttu.khachsanservice.config.HibernateInterceptorWithUser=INFO`
- Kiểm tra user đã đăng nhập chưa

### SQL không được log?
- Kiểm tra `spring.jpa.show-sql=false` trong application.yml
- Kiểm tra logging level của Hibernate

### Username luôn là ANONYMOUS?
- Kiểm tra JWT token có hợp lệ không
- Kiểm tra SecurityContext đã được set chưa

## ✅ Hoàn thành!

Giờ đây mọi câu SQL đều sẽ được log kèm theo username của người thực hiện, giúp việc debug và audit dễ dàng hơn! 🎉

