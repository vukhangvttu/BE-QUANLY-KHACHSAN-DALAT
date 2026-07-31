package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.PhongHoiNghiRequest;
import vttu.khachsanservice.entity.PhongHoiNghi;

@Mapper(componentModel =  "spring")
public interface PhongHoiNghiMapper {
    PhongHoiNghi toCreateHoiNghi(PhongHoiNghiRequest request);
}
