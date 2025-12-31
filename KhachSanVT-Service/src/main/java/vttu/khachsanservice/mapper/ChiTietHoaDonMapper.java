package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.ChiTietHoaDonRequest;
import vttu.khachsanservice.entity.ChiTietHoaDon;

@Mapper(componentModel =  "spring")
public interface ChiTietHoaDonMapper {
    ChiTietHoaDon toCreateChiTietHoaDon(ChiTietHoaDonRequest chiTietHoaDonRequest);
}
