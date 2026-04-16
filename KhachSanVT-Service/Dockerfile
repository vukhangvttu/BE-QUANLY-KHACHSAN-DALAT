FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache \
    fontconfig \
    freetype \
    curl

RUN mkdir -p /logs/scheduled-task /app/config && \
    chmod -R 777 /logs /app/config

COPY build/libs/*.war app.war

CMD ["java", "-jar", "app.war"]