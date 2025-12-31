package vttu.khachsanservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.DichVu;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class NhomDichVuReponse {
    String maNhomDichVu;
    String tenNhomDichVu;
    List<DichVuResponse> dichVus;
}
