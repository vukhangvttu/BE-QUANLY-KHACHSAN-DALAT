package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.MucDichDenRequest;
import vttu.khachsanservice.dto.response.MucDichDenResponse;
import vttu.khachsanservice.dto.response.ThiTruongResponse;
import vttu.khachsanservice.entity.MucDichDen;

@Mapper(componentModel =  "spring")
public interface MucDichDenMapper {
    MucDichDen toMucDichDen(MucDichDenRequest mucDichDenRequest);
    MucDichDenResponse toMucDichDenResponse(MucDichDen mucDichDen);
}
