package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.GiaPhongResponse;
import vttu.khachsanservice.entity.GiaPhong;

@Mapper(componentModel =  "spring")
public interface GiaPhongMapper {
    GiaPhongResponse toGiaPhongResponse(GiaPhong giaPhong);
}
