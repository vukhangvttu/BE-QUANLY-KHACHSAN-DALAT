package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.DichVuVATCusTomeRequest;
import vttu.khachsanservice.dto.response.DichVuVATCusTomeReponse;
import vttu.khachsanservice.entity.DichVuVATCusTome;

@Mapper(componentModel =  "spring")
public interface DichVuVATCustomeMapper {
    DichVuVATCusTome toCreateDichVuVATCustome(DichVuVATCusTomeRequest dichVuVATCusTomeRequest);

    DichVuVATCusTomeReponse toResponse(DichVuVATCusTome dichVuVATCusTome);
}

