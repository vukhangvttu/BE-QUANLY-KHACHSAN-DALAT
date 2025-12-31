package vttu.khachsanservice.entity.log;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Log_BooKing {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long Id;

    @NotEmpty(message = "Nội dung không được trống")
    String NoiDung;
    @NotEmpty(message = "Tên tài khoản không được trống")
    String UserName;
    LocalDateTime thoigian = LocalDateTime.now();
}
