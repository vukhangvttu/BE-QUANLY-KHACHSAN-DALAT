package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.dto.LoaiPhongTrongDTO;
import vttu.khachsanservice.entity.LoaiPhong;

import java.time.LocalDate;
import java.util.List;

public interface LoaiPhongRepository extends JpaRepository<LoaiPhong, String> {

    // check kiểm tra số lượng loại phòng đang còn trống trong khoảng thời gian
    // sử dụng khi tạo booking
    @Query(value = "SELECT * FROM  thong_ke_phong_trong_theo_loai(:ngay_den,:ngay_di,:maLoaiPhong)", nativeQuery = true)
    List<LoaiPhongTrongDTO> kiemTraAllLoaiPhongTrong(LocalDate ngay_den, LocalDate ngay_di, String maLoaiPhong);


    @Query(value = "SELECT * FROM  thong_ke_phong_trong_theo_loai(:ngay_den,:ngay_di,:maLoaiPhong)", nativeQuery = true)
   LoaiPhongTrongDTO kiemTraLoaiPhongTrongMaLoaiPhong(LocalDate ngay_den, LocalDate ngay_di,String maLoaiPhong);

    @Query(value = "SELECT * FROM get_room_availability(:ngay_den,:ngay_di)", nativeQuery = true)
List<LoaiPhongTrongDTO> xemDuBaoPhong(LocalDate ngay_den, LocalDate ngay_di);

}