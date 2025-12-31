package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.XepPhongBooKingRequest;
import vttu.khachsanservice.dto.response.XepPhongBooKingResponse;
import vttu.khachsanservice.entity.XepPhongBooKing;

@Mapper(componentModel =  "spring")
public interface XepPhongBooKingMapper {

    XepPhongBooKing toceateXepPhongBooKing(XepPhongBooKingRequest xepPhongBooKingRequest);
    XepPhongBooKingResponse toXepPhongBooKingResponse(XepPhongBooKing xepPhongBooKing);
}
