package vttu.khachsanservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.dto.response.LoaiPhongResponse;
import vttu.khachsanservice.entity.LoaiPhong;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhongDTO {
     String maPhong;
     String tenPhong;
  //   LoaiPhongResponse loaiPhong;
     String maLoaiPhong;
}
