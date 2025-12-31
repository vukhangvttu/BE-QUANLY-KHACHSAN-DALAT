package vttu.khachsanservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import vttu.khachsanservice.dto.request.ThongTinLienHeBooKingRequest;
import vttu.khachsanservice.entity.ThongTinLienHeBooKing;

@Mapper(componentModel =  "spring")
public interface ThongTinLienHeBooKingMapper {

    ThongTinLienHeBooKing toThongTinLienHeBooKing(ThongTinLienHeBooKingRequest thongTinLienHeBooKing);

    void updateThongTinLienHeBooKing(@MappingTarget ThongTinLienHeBooKing entity, ThongTinLienHeBooKingRequest request);
}
