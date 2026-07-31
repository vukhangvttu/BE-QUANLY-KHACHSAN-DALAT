package vttu.khachsanservice.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import vttu.khachsanservice.dto.request.KhachHangBooKingRequest;
import vttu.khachsanservice.dto.response.KhachHangBooKingResponse;
import vttu.khachsanservice.entity.DanhXung;
import vttu.khachsanservice.entity.KhachHangBooKing;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.DanhXungRepository;

@Mapper(componentModel =  "spring")
public interface KhachHangBooKingMapper {

    KhachHangBooKing toCreateKhachHangBooKing(KhachHangBooKingRequest request);

    KhachHangBooKingResponse toKhachHangBooKingResponse(KhachHangBooKing khachHangBooKing);

    void updateKhachHangBooKing(@MappingTarget KhachHangBooKing entity, KhachHangBooKingRequest request);

    @Named("mapDanhXung")
    default DanhXung mapDanhXung(String tenDanhXung, @Context DanhXungRepository danhXungRepository) {
        if (tenDanhXung == null) {
            return null;
        }
        return danhXungRepository.findById(tenDanhXung)
                .orElseThrow(() -> new AppException(ErrorCode.DANH_XUNG_NOT_FOUND, "" + tenDanhXung));
    }
}
