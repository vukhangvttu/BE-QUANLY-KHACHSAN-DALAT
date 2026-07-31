package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.PhieuDichVuRequest;
import vttu.khachsanservice.dto.response.PhieuDichVuReponse;
import vttu.khachsanservice.entity.PhieuDichVu;

@Mapper(componentModel =  "spring")
public interface PhieuDichVuMapper {
    PhieuDichVu toCreatePhieuDichVu(PhieuDichVuRequest phieuDichVuRequest);
    PhieuDichVuReponse toPhieuDichVuResponse(PhieuDichVu phieuDichVu);
}
