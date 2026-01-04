package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vttu.khachsanservice.dto.request.ChiTietBooKingOnlineRequest;
import vttu.khachsanservice.dto.request.ChiTietBooKingRequest;
import vttu.khachsanservice.dto.response.ChiTietBooKingResponse;
import vttu.khachsanservice.entity.ChiTietBooking;

@Mapper(componentModel =  "spring")
public interface ChiTietBooKingOnlineMapper {
    ChiTietBooking toCreChiTietDatPhong(ChiTietBooKingOnlineRequest chiTietBooKingRequest);
    ChiTietBooKingResponse toChiTietBooKingResponse(ChiTietBooking chiTietBooking);

}
