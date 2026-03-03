package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.TonKhoReponse;
import vttu.khachsanservice.entity.TonKho;

@Mapper(componentModel =  "spring")
public interface TonKhoMapper {
    TonKhoReponse toTonKhoReponse(TonKho tonKho);
}
