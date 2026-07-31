package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.DichVuResponse;
import vttu.khachsanservice.entity.DichVu;

@Mapper(componentModel =  "spring")
public interface DichVuMapper {

    DichVuResponse toDichVuResponse(DichVu dichVu);
}
