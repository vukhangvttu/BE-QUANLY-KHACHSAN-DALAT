package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.YeuCauRequest;
import vttu.khachsanservice.entity.YeuCau;

@Mapper(componentModel =  "spring")
public interface YeuCauBooKingMapper {

    YeuCau toYeuCau(YeuCauRequest yeuCauRequest);
}
