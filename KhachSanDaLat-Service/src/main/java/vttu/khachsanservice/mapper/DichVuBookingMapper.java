package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vttu.khachsanservice.dto.request.DichVuBookingRequest;
import vttu.khachsanservice.entity.DichVuBooking;

@Mapper(componentModel =  "spring")
public interface DichVuBookingMapper {
    @Mapping(target = "autoIncrementId", ignore = true)
    @Mapping(target = "booKing", ignore = true)
    DichVuBooking toDichVuBooking(DichVuBookingRequest dichVuBookingRequest);
}
