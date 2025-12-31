package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.AnSangMoiNgay;

import java.time.LocalDate;

public interface AnSangMoiNgayRepository extends JpaRepository<AnSangMoiNgay,Long> {

    // Kiểm tra đã ăn sáng của theo ngày
    @Query("SELECT COUNT (*) FROM AnSangMoiNgay asmn WHERE asmn.xepPhongBooKing.maXepPhongBooking=:ma_xepphong AND asmn.ngay=:ngayKiemTra")
    int getCount(String ma_xepphong, LocalDate ngayKiemTra);

    @Modifying
    @Query("UPDATE AnSangMoiNgay asmn SET asmn.daAnSang=:da_an_sang WHERE asmn.xepPhongBooKing.maXepPhongBooking=:ma_xepphong AND asmn.ngay=:ngay")
    int updateDaAnSang(int da_an_sang, String ma_xepphong, LocalDate ngay);
}
