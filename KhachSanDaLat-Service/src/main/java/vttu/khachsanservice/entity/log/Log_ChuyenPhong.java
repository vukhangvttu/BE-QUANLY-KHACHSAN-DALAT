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
public class Log_ChuyenPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long Id;
    @NotEmpty(message = "Lý do không được trống")
    String lyDo;

    String maXepPhong;
    String noiDung;
    @NotEmpty(message = "Tên tài khoản không được trống")
    String userName;
    LocalDateTime thoiGian = LocalDateTime.now();
}
