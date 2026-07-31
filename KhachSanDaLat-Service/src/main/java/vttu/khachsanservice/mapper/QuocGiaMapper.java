package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.QuocGiaRequest;
import vttu.khachsanservice.dto.response.QuocGiaResponse;
import vttu.khachsanservice.entity.QuocGia;

@Mapper(componentModel =  "spring")
public interface QuocGiaMapper {
    QuocGia toCreateQuocGia(QuocGiaRequest quocGia);
    QuocGiaResponse toQuocGiaResponse(QuocGia quocGia);
}
