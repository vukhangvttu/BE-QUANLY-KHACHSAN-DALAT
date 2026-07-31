package vttu.khachsanservice.dto.request;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.Phong;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NhanPhongRequest {

    Double gia;
    Phong phong;
    BooKing booKing;
}
