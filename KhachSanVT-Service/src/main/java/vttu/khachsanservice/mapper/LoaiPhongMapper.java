package vttu.khachsanservice.mapper;


import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.LoaiPhongResponse;
import vttu.khachsanservice.entity.LoaiPhong;

@Mapper(componentModel =  "spring")
public interface LoaiPhongMapper {

    LoaiPhongResponse toLoaiPhongResponse(LoaiPhong loaiPhong);
}
