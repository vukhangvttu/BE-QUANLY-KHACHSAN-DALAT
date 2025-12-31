package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.XepPhongBooking_DichVuMienPhiRequest;
import vttu.khachsanservice.dto.response.XepPhongBooking_DichVuMienPhiResponse;
import vttu.khachsanservice.entity.XepPhongBooking_DichVuMienPhi;

@Mapper(componentModel =  "spring")
public interface XepPhongBooking_DichVuMienPhiMapper {
    XepPhongBooking_DichVuMienPhi toCreate(XepPhongBooking_DichVuMienPhiRequest request);

    XepPhongBooking_DichVuMienPhiResponse toXepPhongBooking_DichVuMienPhiResponse(XepPhongBooking_DichVuMienPhi xepPhongBooking_dichVuMienPhi);
}
