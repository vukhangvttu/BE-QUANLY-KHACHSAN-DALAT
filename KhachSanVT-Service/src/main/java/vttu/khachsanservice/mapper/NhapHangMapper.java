package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.response.NhapHangReponse;
import vttu.khachsanservice.entity.NhapHang;

@Mapper(componentModel =  "spring")
public interface NhapHangMapper {
    NhapHangReponse toNhapHangReponse(NhapHang nhapHang);
}
