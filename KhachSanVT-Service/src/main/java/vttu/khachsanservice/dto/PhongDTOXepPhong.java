package vttu.khachsanservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.response.LoaiPhongResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class PhongDTOXepPhong {
    String maPhong;
    String tenPhong;
    LoaiPhongResponse loaiPhong;
    int soGiuongThem;
}
