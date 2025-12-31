package vttu.khachsanservice.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vttu.khachsanservice.dto.request.BooKingRequest;
import vttu.khachsanservice.dto.request.ChiTietBooKingRequest;
import vttu.khachsanservice.dto.request.YeuCauRequest;
import vttu.khachsanservice.dto.response.BooKingResponse;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.ChiTietBooking;
import vttu.khachsanservice.entity.YeuCau;

import java.util.List;

@Mapper(componentModel =  "spring" , uses = {LoaiPhongMapper.class, LoaiGiaMapper.class,YeuCauMapper.class})
public interface BooKingMapper {
    BooKing toCreateBooKing(BooKingRequest request);

//    @Mapping(target = "chiTietBooKings", source = "chiTietBookings")
    BooKingResponse toBooKingResponse(BooKing booKing);

    // Ánh xạ ChiTietBooKingRequest sang ChiTietBooking


    // Ánh xạ danh sách ChiTietBooKingRequest
    List<ChiTietBooking> toChiTietBookingList(List<ChiTietBooKingRequest> requests, @Context BooKing booking);


    // Phương thức updateBooKing
    @Mapping(target = "khachHangBooKing", ignore = true) // Bỏ qua quan hệ, xử lý trong service
    @Mapping(target = "thongTinLienHeBooKing", ignore = true) // Bỏ qua quan hệ
    @Mapping(target = "yeuCaus", ignore = true) // Bỏ qua quan hệ
    void updateBooKing(@MappingTarget BooKing entity, BooKingRequest request);
}
