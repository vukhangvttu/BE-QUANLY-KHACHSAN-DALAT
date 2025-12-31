package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.HoaDonVatRequest;
import vttu.khachsanservice.dto.response.HoaDonVatResponse;
import vttu.khachsanservice.entity.HoaDonVat;

@Mapper(componentModel =  "spring")
public interface HoaDonVatMapper {

    HoaDonVat toReactedHoaDonVat(HoaDonVatRequest hd);
    HoaDonVatResponse toResponseHoaDonVat(HoaDonVat hd);
}
