package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.YeuCauRequest;
import vttu.khachsanservice.dto.response.YeuCauResponse;
import vttu.khachsanservice.entity.YeuCau;

@Mapper(componentModel =  "spring")
public interface YeuCauMapper {
    YeuCau toYeuCau(YeuCauRequest yeuCauRequest);
    YeuCauResponse toYeuCauResponse(YeuCau yeuCau);
}
