package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.Log_ChuyenPhongRequest;
import vttu.khachsanservice.entity.log.Log_ChuyenPhong;

@Mapper(componentModel =  "spring")
public interface ChuyenPhongMapper {
    Log_ChuyenPhong toCreateLog_ChuyenPhong(Log_ChuyenPhongRequest logChuyenPhong);
}
