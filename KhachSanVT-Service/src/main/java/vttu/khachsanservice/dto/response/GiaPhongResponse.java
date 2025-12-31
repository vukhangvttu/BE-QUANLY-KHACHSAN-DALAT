package vttu.khachsanservice.dto.response;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.LoaiGia;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GiaPhongResponse {

    int maGiaPhong;
    Double gia;
    boolean coVAT;
    boolean coAnSang;
    Double tienAnSang;
    Double tienAnSangTreEm;
    String vat;
    boolean giaNgayThuong;
    boolean giaCuoiTuan;
    boolean giaNgayLe;


    LoaiPhongResponse loaiPhong;

}
