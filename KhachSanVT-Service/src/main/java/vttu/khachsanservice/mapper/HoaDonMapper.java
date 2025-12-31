package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.HoaDonRequest;
import vttu.khachsanservice.dto.response.HoaDonReponse;
import vttu.khachsanservice.entity.HoaDon;

@Mapper(componentModel =  "spring")
public interface HoaDonMapper {
    HoaDon toCteateHoaDon(HoaDonRequest hoaDonRequest);

    HoaDonReponse toHoaDonReponse(HoaDon hoaDon) ;
}
