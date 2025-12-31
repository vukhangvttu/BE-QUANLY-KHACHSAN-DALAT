package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.LoaiGiayToRequest;
import vttu.khachsanservice.dto.response.LoaiGiayToResponse;
import vttu.khachsanservice.entity.LoaiGiayTo;

@Mapper(componentModel =  "spring")
public interface LoaiGiayToMapper {
    LoaiGiayTo toCreateLoaiGiayTo(LoaiGiayToRequest loaiGiayToRequest);
    LoaiGiayToResponse toLoaiGiayResponse(LoaiGiayTo loaiGiayTo);
}
