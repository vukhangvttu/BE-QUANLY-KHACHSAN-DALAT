package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.ChiTietHoaDonBanLeRequest;
import vttu.khachsanservice.entity.ChiTietHoaDonBanLe;

@Mapper(componentModel =  "spring")
public interface ChiTietHoaDonBanLeMapper {
    ChiTietHoaDonBanLe toCteateChiTietHoaDonBanLe(ChiTietHoaDonBanLeRequest chiTietHoaDonBanLeRequest);
}
