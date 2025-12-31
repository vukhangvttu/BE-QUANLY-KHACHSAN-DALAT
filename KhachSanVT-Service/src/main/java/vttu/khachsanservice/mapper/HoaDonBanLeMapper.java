package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.HoaDonBanLeRequest;
import vttu.khachsanservice.dto.response.HoaDonBanLeResponse;
import vttu.khachsanservice.entity.HoaDon;
import vttu.khachsanservice.entity.HoaDonBanLe;

@Mapper(componentModel =  "spring")
public interface HoaDonBanLeMapper {
    HoaDonBanLe toCreateHoaDonBanLe(HoaDonBanLeRequest hoaDonBanLeRequest);
    HoaDonBanLeResponse toResponseHoaDonBanLe(HoaDonBanLe hoaDonBanLe);
}
