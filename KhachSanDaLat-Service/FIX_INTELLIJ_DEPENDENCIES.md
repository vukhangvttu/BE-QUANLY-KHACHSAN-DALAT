# Fix IntelliJ IDEA - Dependencies không được nhận diện

## Vấn đề
IntelliJ IDEA hiển thị lỗi "Cannot resolve symbol 'google'" hoặc "Cannot resolve symbol 'imgscalr'" mặc dù Gradle build thành công.

## Nguyên nhân
IntelliJ IDEA cần được refresh để nhận diện các dependencies mới được thêm vào build.gradle.

## Giải pháp

### Cách 1: Reload Gradle Project (Khuyến nghị)

1. **Mở cửa sổ Gradle:**
   - Nhấn `Ctrl + Shift + A` (hoặc `Cmd + Shift + A` trên Mac)
   - Gõ "Gradle" và chọn "Gradle"
   - Hoặc click vào tab "Gradle" ở cạnh phải IDE

2. **Reload Project:**
   - Click vào biểu tượng 🔄 "Reload All Gradle Projects" 
   - Hoặc chuột phải vào project > "Reload Gradle Project"

3. **Chờ IntelliJ indexing:**
   - Đợi IntelliJ IDEA tải lại dependencies (thanh progress ở dưới)
   - Khi xong, các lỗi đỏ sẽ biến mất

### Cách 2: Invalidate Caches (Nếu cách 1 không hoạt động)

1. **File > Invalidate Caches...**
2. Chọn cả hai:
   - ☑ Invalidate and Restart
   - ☑ Clear file system cache and Local History
3. Click **"Invalidate and Restart"**
4. Chờ IntelliJ khởi động lại và reindex

### Cách 3: Re-import Project

1. **File > Close Project**
2. **Open > Chọn thư mục project**
3. Chọn "Trust Project" khi được hỏi
4. Chờ Gradle import và indexing

### Cách 4: Clean và Rebuild (Terminal)

```bash
# Stop Gradle daemon
./gradlew --stop

# Clean build
./gradlew clean

# Build lại
./gradlew build -x test

# Trong IntelliJ: File > Invalidate Caches > Invalidate and Restart
```

### Cách 5: Kiểm tra JDK

1. **File > Project Structure > Project**
2. Đảm bảo JDK = 21 (hoặc compatible version)
3. **File > Project Structure > Modules**
4. Kiểm tra Language Level

## Xác nhận đã fix

Sau khi thực hiện một trong các cách trên, kiểm tra:

1. ✅ Không còn underline đỏ ở imports
2. ✅ Ctrl+Click vào `BufferedImageLuminanceSource` có thể jump to definition
3. ✅ No compilation errors trong "Problems" tab
4. ✅ Build project thành công trong IntelliJ

## Test Application

Sau khi fix xong, test application:

```bash
# Start application
./gradlew bootRun

# Hoặc trong IntelliJ: Run > Run 'KhachSanServiceApplication'
```

Test health endpoint:
```bash
curl http://localhost:8080/api/cccd/health
```

Expected response:
```json
{
  "code": 200,
  "message": "Service hoạt động bình thường",
  "result": "CCCD QR Code Service is running"
}
```

## Nếu vẫn còn lỗi

### Kiểm tra dependencies đã tải chưa:

```bash
./gradlew dependencies | findstr zxing
./gradlew dependencies | findstr imgscalr
```

Kết quả mong đợi:
```
|    \--- com.google.zxing:core:3.5.3
|    +--- com.google.zxing:javase:3.5.3
|    \--- org.imgscalr:imgscalr-lib:4.2
```

### Force download dependencies:

```bash
./gradlew clean build --refresh-dependencies -x test
```

### Xóa cache Gradle:

```bash
# Windows PowerShell
Remove-Item -Recurse -Force ~\.gradle\caches
./gradlew clean build -x test
```

## Quick Fix Script (PowerShell)

```powershell
# Script tự động fix
cd E:\GitHub\BE-QUANLY-KHACHSAN-VT\KhachSanVT-Service

# Stop daemon
.\gradlew --stop

# Clean
.\gradlew clean

# Refresh dependencies
.\gradlew build --refresh-dependencies -x test

Write-Host "Done! Now in IntelliJ: File > Invalidate Caches > Invalidate and Restart"
```

## Tóm tắt các bước nhanh nhất

1. **Gradle Tab (bên phải) > 🔄 Reload All Gradle Projects**
2. Chờ indexing xong
3. Nếu vẫn lỗi: **File > Invalidate Caches > Invalidate and Restart**
4. Done! ✅

---

**Note:** Lỗi này chỉ xuất hiện trong IntelliJ IDEA. Gradle build vẫn chạy bình thường và application có thể start được.

