package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.PhuongXaResponse;
import vttu.khachsanservice.entity.PhuongXa;

@Mapper(componentModel =  "spring")
public interface PhuongXaMapper {

    PhuongXaResponse toPhuongXaRePhuongXa(PhuongXa phuongXa);
}
