package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.HuyenReponse;
import vttu.khachsanservice.entity.Huyen;


@Mapper(componentModel =  "spring")
public interface HuyenMapper {
    HuyenReponse toHuyenReponse(Huyen huyen);
}
