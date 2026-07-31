package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.LoaiTheRequest;
import vttu.khachsanservice.dto.response.LoaiTheResponse;
import vttu.khachsanservice.entity.LoaiThe;

@Mapper(componentModel =  "spring")
public interface LoaiTheMapper {
    LoaiThe toLoaiThe(LoaiTheRequest loaiThe);
    LoaiTheResponse toLoaiTheResponse(LoaiThe loaiThe);
}
