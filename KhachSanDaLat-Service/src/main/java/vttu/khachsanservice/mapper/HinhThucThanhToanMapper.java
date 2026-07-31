package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.HinhThucThanhToanRequest;
import vttu.khachsanservice.dto.response.HinhThucThanhToanResponse;
import vttu.khachsanservice.entity.HinhThucThanhToan;

@Mapper(componentModel =  "spring")
public interface HinhThucThanhToanMapper {
    HinhThucThanhToan toHinhThucThanhToan(HinhThucThanhToanRequest hinhThucThanhToanRequest);
    HinhThucThanhToanResponse toHinhThucThanhToanResponse(HinhThucThanhToan hinhThucThanhToan);
}
