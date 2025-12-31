package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.NhomKhachHangRequest;
import vttu.khachsanservice.dto.response.NhomKhachHangResponse;
import vttu.khachsanservice.entity.NhomKhachHang;

@Mapper(componentModel =  "spring")
public interface NhomKhachHangMapper {
    NhomKhachHang toCreateNhomKhachHang(NhomKhachHangRequest nhomKhachHangRequest);
    NhomKhachHangResponse toNhomKhachHangResponse(NhomKhachHang nhomKhachHang);
}
