package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.DichVuVATCusTome;

import java.util.List;

public interface DichVuVATCustomeRepository extends JpaRepository<DichVuVATCusTome, String> {

    @Query("SELECT dv FROM DichVuVATCusTome dv WHERE dv.hoaDonVat.maHoaDonVat= :maHoaDonVat")
    List<DichVuVATCusTome> getThongTinDichVuVAT(String maHoaDonVat);

    @Query("SELECT COUNT(*) FROM DichVuVATCusTome dv WHERE dv.hoaDonVat.maHoaDonVat= :maHoaDonVat")
    int countThongTinDichVuVAT(String maHoaDonVat);

    @Modifying
    @Query("DELETE FROM DichVuVATCusTome dv WHERE dv.hoaDonVat.maHoaDonVat=:maHoaDonVat")
    void deleteAllByMaHoaDonVat(String maHoaDonVat);

}
