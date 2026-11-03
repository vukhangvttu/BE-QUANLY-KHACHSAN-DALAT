package vttu.khachsanservice.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vttu.khachsanservice.entity.BooKing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BooKingRepository extends JpaRepository<BooKing, String> {


    @Query("SELECT bk.maBooking  FROM BooKing bk ORDER BY bk.maBooking DESC LIMIT 1")
    Optional<String> findLatestMaBooKing();

    @EntityGraph(attributePaths = {"chiTietBookings"})
    List<BooKing> findAll();

    @Query(value = "UPDATE booking SET da_xep_phong = :daXepPhong  Where ma_booking = :ma_booking" , nativeQuery = true)
    @Modifying
    int updateDa_xep_phong(boolean daXepPhong, String ma_booking);


    @Modifying
    @Query("UPDATE BooKing  b SET b.tongSoLuong = :tongsoluong, b.soNguoiLon = :songuoilon, b.soTreEm = :sotreem, b.tongTien = :tongtien WHERE b.maBooking = :ma_booking")
    int updateBooKingKhiXoa1ChiTietBooKing(int tongsoluong, int songuoilon, int sotreem , BigDecimal tongtien, String ma_booking);

    // cập nhật ngày đi khi thanh toàn toàn bộ booking và trạng thái
    @Modifying
    @Query("UPDATE BooKing b set b.trangThaiBooKing.maTrangThaiBooKing = :maTrangThai, b.ngayDi =:ngayDi  WHERE b.maBooking = :maBooking")
    int updateTrangThaiBooKing(int maTrangThai , LocalDate ngayDi ,String maBooking);


    // danh sách booking
    @Query(value ="SELECT * FROM danh_sach_all_booking(:size)", nativeQuery = true)
    List<Map<String, Object>> danhSachBooKing(Integer size);

    // Update tổng tiền booking khi xóa phòng hội nghị

    @Modifying
    @Query("UPDATE BooKing b set b.tongTien = :tongTien WHERE b.maBooking = :maBooking")
    void updateTongTien(BigDecimal tongTien, String maBooking);

    // Update trạng thái booking
    @Modifying
    @Query("UPDATE BooKing b set b.trangThaiBooKing.maTrangThaiBooKing = :maTrangThai WHERE b.maBooking = :maBooking")
    int updateTrangThai(int maTrangThai, String maBooking);
}

