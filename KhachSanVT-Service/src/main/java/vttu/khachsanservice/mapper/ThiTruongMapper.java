package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.ThiTruongRequest;
import vttu.khachsanservice.dto.response.ThiTruongResponse;
import vttu.khachsanservice.entity.ThiTruong;

@Mapper(componentModel =  "spring")
public interface ThiTruongMapper {
    ThiTruong toThiTruong(ThiTruongRequest request);
    ThiTruongResponse toThiTruongResponse(ThiTruong thiTruong);
}
