package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vttu.khachsanservice.dto.request.ChiTietBooKingRequest;
import vttu.khachsanservice.dto.response.ChiTietBooKingResponse;
import vttu.khachsanservice.entity.ChiTietBooking;

@Mapper(componentModel =  "spring")
public interface ChiTietBooKingMapper {
    ChiTietBooking toCreChiTietDatPhong(ChiTietBooKingRequest chiTietBooKingRequest);
    ChiTietBooKingResponse toChiTietBooKingResponse(ChiTietBooking chiTietBooking);

    @Mapping(target = "booKing", ignore = true)
    void updateChiTietBooking(@MappingTarget ChiTietBooking entity, ChiTietBooKingRequest request);

}
