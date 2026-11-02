package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.LoaiGia;

import java.util.List;

public interface LoaiGiaRepository extends JpaRepository<LoaiGia, Integer> {

    @Query(value = """
    select loai_gia.* from loai_gia
    join gia_phong on loai_gia.ma_loai_gia = gia_phong.maloaigia
    where gia_phong.maloaiphong = :maLoaiPhong order by ma_loai_gia asc
""", nativeQuery = true)
    List<LoaiGia> getLoaiGiaByMaLoaiPhong(String maLoaiPhong);

}
