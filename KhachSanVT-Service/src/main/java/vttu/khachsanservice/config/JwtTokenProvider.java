package vttu.khachsanservice.config;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenProvider {
    private final JwtDecoder jwtDecoder;

    public JwtTokenProvider(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public boolean validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            // Kiểm tra token đã hết hạn chưa
            Instant expiration = jwt.getExpiresAt();
            if (expiration != null && expiration.isBefore(Instant.now())) {
                return false; // Token đã hết hạn
            }

            return true;
        } catch (JwtException e) {
            return false; // Token không hợp lệ
        }
    }

    public String getUsernameFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getSubject(); // Lấy username từ token
    }
}
