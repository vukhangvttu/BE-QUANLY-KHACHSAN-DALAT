package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.TrangThaiBooKingRequest;
import vttu.khachsanservice.dto.response.TrangThaiBooKingResponse;
import vttu.khachsanservice.entity.TrangThaiBooKing;

@Mapper(componentModel =  "spring")
public interface TrangThaiBooKingMapper {
    TrangThaiBooKing toTrangThaiBooKing(TrangThaiBooKingRequest trangThaiBooKingRequest);
    TrangThaiBooKingResponse toTrangThaiBooKingResponse(TrangThaiBooKing trangThaiBooKing);
}
