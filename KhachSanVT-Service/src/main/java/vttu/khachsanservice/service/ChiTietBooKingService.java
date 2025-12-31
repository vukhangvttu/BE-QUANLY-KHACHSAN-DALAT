package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.PhuThuDto;
import vttu.khachsanservice.dto.PhuThuKhacDto;
import vttu.khachsanservice.dto.response.ChiTietBooKingResponse;
import vttu.khachsanservice.dto.response.GiaPhongTheoNgayReponse;
import vttu.khachsanservice.entity.BooKing;
import vttu.khachsanservice.entity.ChiTietBooking;
import vttu.khachsanservice.entity.XepPhongBooKing;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.ChiTietBooKingMapper;
import vttu.khachsanservice.mapper.GiaPhongTheoNgayMapper;
import vttu.khachsanservice.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChiTietBooKingService {

    ChiTietBooKingRepository chiTietBooKingRepository;
    ChiTietBooKingMapper chiTietBooKingMapper;
    private final BooKingRepository booKingRepository;
    private final XepPhongService xepPhongService;
    private final XepPhongRepository xepPhongRepository;
    private final DanhSachPhongChiTietRepository danhSachPhongChiTietRepository;
    private final GiaPhongTheoNgayRepository giaPhongTheoNgayRepository;
    private final GiaPhongTheoNgayMapper giaPhongTheoNgayMapper;

    public List<ChiTietBooKingResponse> getChiTietBooKingByMaBooKing(String maBooKing) {

        return chiTietBooKingRepository.getChiTietBookingByMaBooking(maBooKing)
                .stream().map(chiTietBooKingMapper::toChiTietBooKingResponse).collect(Collectors.toList());

    }

    // tạo id mã string tự động tăng
    public synchronized String generateNewCTBookingId() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // Ví dụ: "20250303"
        Optional<String> latestId = chiTietBooKingRepository.findLatestMaCTBooKing();
        if (latestId.isPresent() && latestId.get().startsWith("BK" + datePrefix)) {
            int number = Integer.parseInt(latestId.get().substring(10)); // Sau "BK20250303"
            return String.format("CT-BK%s%03d", datePrefix, number + 1);
        }
        return "CT-BK" + datePrefix + "001"; // Ví dụ: "BK20250303001"
    }

    @Transactional
    public boolean deleteChiTietBooking(String ma_chitiet_booking, String ma_booking) {
        // Tìm kiếm chi tiết booking cần xóa
        ChiTietBooking chiTietBooking = chiTietBooKingRepository.findById(ma_chitiet_booking)
                .orElseThrow(() -> new AppException(ErrorCode.CHI_TIET_BOOKING_NOT_EMPTY));

        // Kiểm tra nếu phòng đã được xếp và check in
        List<Map<String, Object>> xepPhong = xepPhongRepository.DaXepPhong(ma_chitiet_booking);

        StringBuilder listPhong = new StringBuilder();
        for(Map<String, Object> xep : xepPhong) {
            listPhong.append(xep.get("ma_phong")).append(",");
        }

        if(!xepPhong.isEmpty()){
            throw new AppException(ErrorCode.CHITIETBOOKING_DA_XEP_PHONG, "Phòng đã check-in : "+listPhong.toString() + " không thể xóa chi tiết booking này.");

        }

        long soLuong = chiTietBooKingRepository.countChiTietBookingByMaBooking(ma_booking);
        if(soLuong == 1) {
            throw new AppException(ErrorCode.SO_LUONG_CHI_TIET_DAT_PHONG_ERROR);
        }

        BooKing booKing = chiTietBooking.getBooKing();

        try {

//            nếu đã xếp phòng chưa check-in thì được phép xóa

            xepPhongRepository.DeleteXepPhongTheoChiTiet(ma_chitiet_booking);

            // xoa trong danh sach phòng chi tiết
            danhSachPhongChiTietRepository.deleteByChiTietBooking_MaChiTietBooking(ma_chitiet_booking);

            // xóa trong giá phòng từng ngày
            giaPhongTheoNgayRepository.xoaGiaPhongTheoNgay(ma_chitiet_booking);

            // Thử xóa chi tiết booking
            chiTietBooKingRepository.deleteById(ma_chitiet_booking);

            // Kiểm tra lại xem chi tiết booking còn tồn tại không
            boolean isDeleted = chiTietBooKingRepository.existsById(ma_chitiet_booking);
            if (isDeleted) {
                System.err.println("❌ Lỗi: Không thể xóa chi tiết booking với mã: " + ma_chitiet_booking);
                return false;
            }

            System.out.println("✅ Đã xóa thành công mã: " + ma_chitiet_booking);

            // Tính toán lại các giá trị sau khi xóa
            int TongSL = booKing.getTongSoLuong() - chiTietBooking.getSoLuong();
            int TongNguoiLon = booKing.getSoNguoiLon() - chiTietBooking.getNguoiLon();
            int TongTreEm = booKing.getSoTreEm() - chiTietBooking.getTreEm();
            BigDecimal TongTien = booKing.getTongTien().subtract(BigDecimal.valueOf(chiTietBooking.getGia()));

            // Cập nhật lại booking
            booKingRepository.updateBooKingKhiXoa1ChiTietBooKing(TongSL, TongNguoiLon, TongTreEm, TongTien, booKing.getMaBooking());


            return true;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa chi tiết booking: " + e.getMessage());
            return false;
        }
    }

    public List<GiaPhongTheoNgayReponse> getGiaPhongTheoNgayByMaChiTietBooking(String maXepPhong) {

        XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(maXepPhong)
                .orElseThrow(() -> new AppException(ErrorCode.XEP_PHONG_IS_EMPTY));

        List<GiaPhongTheoNgayReponse> giaPhongTheoNgayResponses = giaPhongTheoNgayRepository.findByMaChiTietBooking(xepPhongBooKing.getChiTietBooking().getMaChiTietBooking())
                .stream().map(giaPhongTheoNgayMapper::toGiaPhongTheoNgayReponse).collect(Collectors.toList());

        if (giaPhongTheoNgayResponses.isEmpty()) {
            throw new AppException(ErrorCode.GIA_PHONG_THEO_NGAY_NOT_FOUND);
        }

        return giaPhongTheoNgayResponses;
    }

    public List<Map<String,Object>> getThongTinChiTietBooking(String mabooking) {
        booKingRepository.findById(mabooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));
        return chiTietBooKingRepository.getThongTinXuatChiTietBooking(mabooking);
    }

  public List<Map<String,Object>> getThongTinChiTietPhuThuBooking(String mabooking) {
    booKingRepository.findById(mabooking)
      .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));
    return chiTietBooKingRepository.getThongTinXuatChiTietPhuThuBooking(mabooking);
  }

    public List<Map<String,Object>> getPhuThuTreEmKhiCreateBooking(String mabooking){
        booKingRepository.findById(mabooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

      return chiTietBooKingRepository.getDanhSachPhuThuTreEmThanhToan(mabooking);
    }

}
