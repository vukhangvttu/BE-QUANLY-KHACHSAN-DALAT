package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.TinhThanhResponse;
import vttu.khachsanservice.entity.TinhThanh;

@Mapper(componentModel =  "spring")
public interface TinhThanhMapper {

    TinhThanhResponse toTinhThanhResponse(TinhThanh tinhThanh);
}
