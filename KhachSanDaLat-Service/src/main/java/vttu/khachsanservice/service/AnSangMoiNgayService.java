package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.entity.AnSangMoiNgay;
import vttu.khachsanservice.entity.XepPhongBooKing;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.AnSangMoiNgayRepository;
import vttu.khachsanservice.repository.KhachHangPhongRepository;
import vttu.khachsanservice.repository.XepPhongRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j

public class AnSangMoiNgayService {

    AnSangMoiNgayRepository anSangMoiNgayRepository;
    private final XepPhongRepository xepPhongRepository;
    private final KhachHangPhongRepository khachHangPhongRepository;

    @Transactional
    public boolean saveAnSangMoiNgay(String maXepPhong, String soLuong) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            // Kiểm tra và lấy thông tin xếp phòng
            XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(maXepPhong)
                    .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));

            // Kiểm tra đã cập nhật ăn sang theo ngày chưa
            LocalDate ngayHomNay = LocalDate.now();

            // Nếu là tất cả thì kiểm tra số khách trong phòng là bao nhiêu
            int soKhachDaAnSang = 0;
            if(soLuong.equals("all")) {
                soKhachDaAnSang = khachHangPhongRepository.getCountByXepPhongBooking(maXepPhong);
            }else {
                soKhachDaAnSang = Integer.parseInt(soLuong);
            }


            int kiemTra = anSangMoiNgayRepository.getCount(maXepPhong, ngayHomNay);

            if(kiemTra != 0){
                int update = anSangMoiNgayRepository.updateDaAnSang(soKhachDaAnSang,maXepPhong,ngayHomNay);

                if(update == 0){

                    throw new AppException(ErrorCode.ERROR_DA_AN_SANG);
                }
                return true;
            }else {
                // Tạo mới bản ghi ăn sáng
                AnSangMoiNgay anSangMoiNgay = new AnSangMoiNgay();
                anSangMoiNgay.setXepPhongBooKing(xepPhongBooKing);
                anSangMoiNgay.setDaAnSang(soKhachDaAnSang);
                anSangMoiNgay.setNgay(LocalDate.now());
                anSangMoiNgay.setNguoiTao(authentication.getName());
                anSangMoiNgay.setThoiGianTao(LocalDateTime.now());

                // Lưu vào database
                anSangMoiNgayRepository.save(anSangMoiNgay);// Sửa từ save(anSangMoiNgayRepository) thành save(anSangMoiNgay)
                return true;
            }

        } catch (AppException e) {
            // Xử lý exception từ orElseThrow
            log.error("Lỗi khi lưu thông tin ăn sáng: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Xử lý các exception khác
            log.error("Lỗi không xác định khi lưu thông tin ăn sáng: " + e.getMessage());
            return false;
        }
    }
}
