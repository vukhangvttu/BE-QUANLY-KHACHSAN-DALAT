package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.LoaiGiaResponse;
import vttu.khachsanservice.entity.LoaiGia;

@Mapper(componentModel =  "spring")
public interface LoaiGiaMapper {

    LoaiGiaResponse toLoaiGiaResponse(LoaiGia loaiGia);

}
