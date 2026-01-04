package vttu.khachsanservice.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vttu.khachsanservice.dto.request.BooKingOnlineRequest;
import vttu.khachsanservice.dto.request.BooKingRequest;
import vttu.khachsanservice.dto.request.ChiTietBooKingRequest;
import vttu.khachsanservice.dto.response.BooKingResponse;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.ChiTietBooking;

import java.util.List;

@Mapper(componentModel =  "spring" )
public interface BooKingOnlineMapper {
    BooKing toCreateBooKing(BooKingOnlineRequest request);

//    @Mapping(target = "chiTietBooKings", source = "chiTietBookings")
    BooKingResponse toBooKingResponse(BooKing booKing);

    // Ánh xạ ChiTietBooKingRequest sang ChiTietBooking


    // Ánh xạ danh sách ChiTietBooKingRequest
    List<ChiTietBooking> toChiTietBookingList(List<ChiTietBooKingRequest> requests, @Context BooKing booking);

}
