package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.NguonKhachRequest;
import vttu.khachsanservice.dto.response.NguonKhachResponse;
import vttu.khachsanservice.entity.NguonKhach;

@Mapper(componentModel =  "spring")
public interface NguonKhachMapper {

    NguonKhach toNguonKhach(NguonKhachRequest request);
    NguonKhachResponse toNguonKhachResponse(NguonKhach khach);
}
