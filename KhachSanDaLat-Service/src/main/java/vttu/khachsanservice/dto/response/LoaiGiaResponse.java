package vttu.khachsanservice.dto.response;

import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.GiaPhong;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoaiGiaResponse {

    int maLoaiGia;
    String tenLoaiGia;

    List<GiaPhongResponse> giaPhongs = new ArrayList<>();
}
