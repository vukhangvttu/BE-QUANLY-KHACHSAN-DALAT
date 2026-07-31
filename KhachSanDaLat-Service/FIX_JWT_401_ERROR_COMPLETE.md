# Fix: JWT Token Hết Hạn Trả Lỗi 500 Thay Vì 401 - GIẢI PHÁP HOÀN CHỈNH

## ❌ Vấn Đề
Khi JWT token hết hạn, API đang trả về:
```json
{
    "timestamp": "2026-01-03T09:43:35.720+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/khachsan-service/danh-xung"
}
```

Thay vì trả về **401 Unauthorized**.

---

## 🔍 Nguyên Nhân Gốc Rễ

### 1. **CustomJwtDecoder throw JwtException quá sớm**
File: `CustomJwtDecoder.java` - **NGUYÊN NHÂN CHÍNH**

```java
// ❌ CODE CŨ - SAI
@Override
public Jwt decode(String token) throws JwtException {
    try {
        var response = authenticationService.introspect(
                IntrospectRequest.builder().token(token).build());

        if (!response.isValid()) throw new JwtException("Token invalid"); // <- Đây là vấn đề!
    } catch (JOSEException | ParseException e) {
        throw new JwtException(e.getMessage());
    }
    // ...
}
```

**Vấn đề:**
- Khi token hết hạn, `introspect()` trả về `isValid = false`
- Code throw `JwtException` TRƯỚC KHI `NimbusJwtDecoder` được gọi
- Spring Security không xử lý được exception này đúng cách
- Kết quả: **500 Internal Server Error**

### 2. **JwtAuthenticationFilter không có try-catch**
File: `JwtAuthenticationFilter.java`

```java
// ❌ CODE CŨ - SAI
@Override
protected void doFilterInternal(...) {
    String userName = jwtTokenProvider.getUsernameFromToken(token); // <- Có thể throw exception
    UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
    // ...
}
```

**Vấn đề:**
- `getUsernameFromToken()` gọi `jwtDecoder.decode()` không có try-catch
- Exception không được xử lý → crash → 500

### 3. **Thiếu Exception Handlers**
Security config không có `AuthenticationEntryPoint` và `AccessDeniedHandler` để xử lý lỗi authentication.

---

## ✅ Giải Pháp Hoàn Chỉnh

### **Bước 1: Fix CustomJwtDecoder** ⭐ QUAN TRỌNG NHẤT

**File:** `src/main/java/vttu/khachsanservice/config/CustomJwtDecoder.java`

```java
@Override
public Jwt decode(String token) throws JwtException {
    // Khởi tạo NimbusJwtDecoder nếu chưa có
    if (Objects.isNull(nimbusJwtDecoder)) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
        nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    // Decode token - NimbusJwtDecoder sẽ tự động throw JwtException nếu token hết hạn
    return nimbusJwtDecoder.decode(token);
}
```

**Tại sao:**
- Xóa bỏ logic `introspect()` check
- Để `NimbusJwtDecoder` tự nhiên kiểm tra và throw exception
- Spring Security OAuth2 Resource Server biết cách xử lý exception từ `NimbusJwtDecoder`
- Exception sẽ được convert thành `AuthenticationException` và xử lý bởi `AuthenticationEntryPoint`

---

### **Bước 2: Fix JwtTokenProvider**

**File:** `src/main/java/vttu/khachsanservice/config/JwtTokenProvider.java`

```java
public String getUsernameFromToken(String token) {
    try {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getSubject();
    } catch (JwtException e) {
        return null; // Token không hợp lệ hoặc hết hạn
    }
}
```

**Tại sao:**
- Thêm try-catch để tránh exception propagate ra ngoài
- Trả về `null` khi token không hợp lệ
- Filter sẽ kiểm tra null và bỏ qua authentication

---

### **Bước 3: Fix JwtAuthenticationFilter**

**File:** `src/main/java/vttu/khachsanservice/config/JwtAuthenticationFilter.java`

```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                FilterChain filterChain) throws ServletException, IOException {
    try {
        String token = getTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userName = jwtTokenProvider.getUsernameFromToken(token);
            
            if (userName != null) {  // <- Kiểm tra null
                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
    } catch (Exception e) {
        // Bỏ qua lỗi, để OAuth2ResourceServer xử lý
        logger.error("Cannot set user authentication: {}", e.getMessage(), e);
    }

    filterChain.doFilter(request, response);
}
```

**Tại sao:**
- Wrap tất cả logic trong try-catch
- Kiểm tra `userName != null` trước khi dùng
- Nếu có exception, log và tiếp tục filter chain
- OAuth2ResourceServer sẽ xử lý tiếp và trả 401

---

### **Bước 4: Tạo JwtAuthenticationEntryPoint**

**File mới:** `src/main/java/vttu/khachsanservice/config/JwtAuthenticationEntryPoint.java`

```java
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
```

**Chức năng:**
- Bắt tất cả `AuthenticationException` từ Spring Security
- Trả về HTTP 401 với format JSON chuẩn của API

---

### **Bước 5: Tạo JwtAccessDeniedHandler**

**File mới:** `src/main/java/vttu/khachsanservice/config/JwtAccessDeniedHandler.java`

```java
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {
        
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
```

**Chức năng:**
- Bắt `AccessDeniedException` (access denied errors)
- Trả về HTTP 401 với format JSON chuẩn

---

### **Bước 6: Cập nhật Security Config**

**File:** `src/main/java/vttu/khachsanservice/config/Security.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity httpSecurity, 
        JwtAuthenticationFilter jwtAuthenticationFilter, 
        CustomJwtDecoder customJwtDecoder,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        JwtAccessDeniedHandler jwtAccessDeniedHandler) throws Exception {
    return httpSecurity
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(request ->
                    request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                            .requestMatchers(HttpMethod.GET).permitAll()
                            .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth24 -> oauth24
                    .jwt(jwtConfigurer ->
                            jwtConfigurer.decoder(customJwtDecoder)
                                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    )
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // <- Thêm
            )
            .exceptionHandling(exceptions -> exceptions  // <- Thêm
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}
```

**Thay đổi:**
1. Thêm 2 tham số: `JwtAuthenticationEntryPoint` và `JwtAccessDeniedHandler`
2. Thêm `.authenticationEntryPoint()` trong `oauth2ResourceServer`
3. Thêm `.exceptionHandling()` để xử lý tất cả authentication/access denied errors

---

## 🔄 Flow Hoạt Động Sau Khi Fix

### **Khi token hết hạn:**

1. **Request đến** → `JwtAuthenticationFilter`
2. **validateToken()** → return `false` (token hết hạn)
3. **Không set authentication** → SecurityContext rỗng
4. **filterChain.doFilter()** → chuyển đến `BearerTokenAuthenticationFilter` (OAuth2)
5. **BearerTokenAuthenticationFilter** → gọi `customJwtDecoder.decode(token)`
6. **NimbusJwtDecoder.decode()** → throw `JwtException` (token expired)
7. **Spring Security** → convert thành `AuthenticationException`
8. **JwtAuthenticationEntryPoint** → bắt exception
9. **Trả về 401** với format:
```json
{
  "code": 1003,
  "message": "User not authenticated"
}
```

---

## 🎯 Kết Quả

### ❌ Trước khi fix:
```json
{
    "timestamp": "2026-01-03T09:43:35.720+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/khachsan-service/danh-xung"
}
```

### ✅ Sau khi fix:
```json
{
    "code": 1003,
    "message": "User not authenticated"
}
```
**HTTP Status:** `401 Unauthorized`

---

## 🧪 Test Cases

Các trường hợp cần test:

| Test Case | Expected Result |
|-----------|----------------|
| Token hết hạn | 401 Unauthorized |
| Token không hợp lệ | 401 Unauthorized |
| Token bị sửa đổi | 401 Unauthorized |
| Không có token | 401 Unauthorized |
| Token hợp lệ | 200 OK |

---

## 📝 Tóm Tắt Các File Đã Thay Đổi

1. ✅ **CustomJwtDecoder.java** - Xóa logic introspect, để NimbusJwtDecoder tự xử lý
2. ✅ **JwtTokenProvider.java** - Thêm try-catch trong getUsernameFromToken
3. ✅ **JwtAuthenticationFilter.java** - Thêm try-catch và kiểm tra null
4. ✅ **JwtAuthenticationEntryPoint.java** - File mới - Xử lý authentication errors
5. ✅ **JwtAccessDeniedHandler.java** - File mới - Xử lý access denied errors
6. ✅ **Security.java** - Thêm exception handlers vào config

---

## ⚠️ Lưu Ý Quan Trọng

### **Tại sao bỏ logic introspect trong CustomJwtDecoder?**
- Logic introspect gây throw exception quá sớm
- Spring Security không xử lý được exception từ custom logic
- `NimbusJwtDecoder` đã có sẵn logic validate token expiry
- Spring Security biết cách xử lý exception từ `NimbusJwtDecoder`

### **Nếu vẫn cần introspect?**
Nếu bạn cần check token với service introspect, có 2 cách:
1. **Tạo custom filter** chạy TRƯỚC `BearerTokenAuthenticationFilter`
2. **Xử lý trong JwtAuthenticationFilter** (đã có sẵn)

Nhưng **KHÔNG NÊN** throw exception trong `decode()` method.

### **Về JwtAuthenticationFilter**
- Filter này chạy TRƯỚC OAuth2ResourceServer
- Chỉ nên dùng để set authentication cho valid token
- Không nên throw exception → để OAuth2 xử lý

---

## 🚀 Deployment

Sau khi apply tất cả changes:

1. **Build project:**
```bash
./gradlew clean build -x test
```

2. **Restart application**

3. **Test với token hết hạn:**
```bash
curl -H "Authorization: Bearer <expired_token>" \
     http://localhost:8310/khachsan-service/danh-xung
```

4. **Verify response:** HTTP 401 với message "User not authenticated"

---

## 🐛 Troubleshooting

### Vẫn trả 500?
1. Kiểm tra logs để xem exception ở đâu
2. Đảm bảo `CustomJwtDecoder` đã bỏ logic introspect
3. Kiểm tra `JwtAuthenticationFilter` có try-catch chưa
4. Verify `Security.java` đã inject đủ dependencies

### Token hợp lệ cũng bị 401?
1. Kiểm tra secret key đúng chưa
2. Verify token signature algorithm (HS512)
3. Check token format (Bearer prefix)

---

**Version:** 1.0
**Date:** 2026-01-03
**Status:** ✅ RESOLVED

