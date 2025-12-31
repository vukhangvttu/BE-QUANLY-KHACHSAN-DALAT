package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.GiaPhongTheoNgayReponse;
import vttu.khachsanservice.entity.GiaPhongTheoNgay;

@Mapper(componentModel =  "spring")
public interface GiaPhongTheoNgayMapper {

    GiaPhongTheoNgayReponse toGiaPhongTheoNgayReponse(GiaPhongTheoNgay giaPhongTheoNgay);

}
