package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import vttu.khachsanservice.dto.request.KhucVucRequest;
import vttu.khachsanservice.dto.response.KhuVucResponse;
import vttu.khachsanservice.entity.KhuVuc;

@Mapper(componentModel =  "spring")
public interface KhuVucMapper {
    KhuVuc toKhuVuc(KhucVucRequest request);
    KhuVucResponse toKhuVucResponse(KhuVuc khuVuc);
}
