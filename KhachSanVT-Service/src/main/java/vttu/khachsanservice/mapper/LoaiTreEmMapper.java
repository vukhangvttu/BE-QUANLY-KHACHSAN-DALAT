package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.LoaiTreEmResponse;
import vttu.khachsanservice.entity.LoaiTreEm;

@Mapper(componentModel =  "spring")
public interface LoaiTreEmMapper {

    LoaiTreEmResponse toCreateLoaiTreEm(LoaiTreEm loaiTreEm);
}
