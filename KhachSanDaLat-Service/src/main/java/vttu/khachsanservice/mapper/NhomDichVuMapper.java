package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.NhomDichVuReponse;
import vttu.khachsanservice.entity.NhomDichVu;

@Mapper(componentModel =  "spring")
public interface NhomDichVuMapper {

    NhomDichVuReponse toReposeNhomDichVuReponse(NhomDichVu nhomDichVu);
}
