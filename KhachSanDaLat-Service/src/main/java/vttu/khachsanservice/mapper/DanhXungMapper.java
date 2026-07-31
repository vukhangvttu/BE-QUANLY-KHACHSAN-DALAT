package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.DanhXungRequest;
import vttu.khachsanservice.dto.response.DanhXungResponse;
import vttu.khachsanservice.entity.DanhXung;

@Mapper(componentModel =  "spring")
public interface DanhXungMapper {
    DanhXung toCreateDanhXung(DanhXungRequest danhXungRequest);
    DanhXungResponse toDanhXungResponse(DanhXung danhXung);
}
