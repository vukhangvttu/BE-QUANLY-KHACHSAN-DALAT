# Dockerfile cho khachsan-service
FROM eclipse-temurin:21-jre-jammy

# Cài các thư viện thiếu + curl cho healthcheck
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fontconfig \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Tạo thư mục logs và config, cấp quyền
RUN mkdir -p /logs/scheduled-task /app/config && \
    chmod -R 777 /logs /app/config

# Sao chép file JAR từ máy host vào container
COPY build/libs/*.war KhachSan-Service-0.0.1-SNAPSHOT.war

# Thiết lập lệnh mặc định để chạy ứng dụng khi container được khởi động
CMD ["java", "-jar", "KhachSan-Service-0.0.1-SNAPSHOT.war"]