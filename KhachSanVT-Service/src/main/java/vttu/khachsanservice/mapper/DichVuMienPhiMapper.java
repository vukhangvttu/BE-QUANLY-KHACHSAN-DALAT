package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.DichVuMienPhiResponse;
import vttu.khachsanservice.entity.DichVuMienPhi;

@Mapper(componentModel =  "spring")
public interface DichVuMienPhiMapper
{
    DichVuMienPhiResponse toDichVuMienPhiResponse(DichVuMienPhi dichVuMienPhi);
}
