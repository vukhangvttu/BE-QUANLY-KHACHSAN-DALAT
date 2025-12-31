package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.KhachHangPhongRequest;
import vttu.khachsanservice.dto.response.KhachHangPhongResponse;
import vttu.khachsanservice.entity.KhachHangPhong;

@Mapper(componentModel =  "spring")
public interface KhachHangPhongMapper {

    KhachHangPhong toCreateKhachHangPhong(KhachHangPhongRequest khachHangPhongRequest);

    KhachHangPhongResponse toKhachHangPhongResponse(KhachHangPhong khachHangPhong);
}
