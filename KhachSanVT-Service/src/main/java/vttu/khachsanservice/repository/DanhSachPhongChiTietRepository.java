package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.DanhSachPhongChiTiet;

public interface DanhSachPhongChiTietRepository extends JpaRepository<DanhSachPhongChiTiet, Integer> {

    @Modifying
    @Query("DELETE FROM DanhSachPhongChiTiet d WHERE d.chiTietBooking.maChiTietBooking= ?1")
    void deleteByChiTietBooking_MaChiTietBooking(String maChiTietBooking);


    @Query("SELECT ds FROM DanhSachPhongChiTiet ds WHERE ds.chiTietBooking.maChiTietBooking = ?1")
    DanhSachPhongChiTiet findByChiTietBooking_MaChiTietBooking(String maChiTietBooking);

  @Query("SELECT ds FROM DanhSachPhongChiTiet ds WHERE ds.chiTietBooking.maChiTietBooking = ?1 AND ds.maPhong = ?2")
    DanhSachPhongChiTiet findByChiTietBooking_MaChiTietBookingAndMaPhong(String maChiTietBooking, String maPhong);



}
