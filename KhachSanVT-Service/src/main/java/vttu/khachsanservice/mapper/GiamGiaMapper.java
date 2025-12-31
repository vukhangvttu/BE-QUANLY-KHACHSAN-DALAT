package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.GiamGiaResponse;
import vttu.khachsanservice.entity.GiamGia;

@Mapper(componentModel =  "spring")
public interface GiamGiaMapper {
    GiamGia  toGiamGia(GiamGia giamGia);
    GiamGiaResponse toGiamGiaResponse(GiamGia giamGia);
}
