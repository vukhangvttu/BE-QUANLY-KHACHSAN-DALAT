package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.request.ChiTietHoaDonBanLeRequest;
import vttu.khachsanservice.dto.request.ChiTietHoaDonRequest;
import vttu.khachsanservice.dto.request.HoaDonBanLeRequest;
import vttu.khachsanservice.dto.response.HoaDonBanLeResponse;
import vttu.khachsanservice.entity.ChiTietHoaDon;
import vttu.khachsanservice.entity.ChiTietHoaDonBanLe;
import vttu.khachsanservice.entity.HoaDonBanLe;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.ChiTietHoaDonBanLeMapper;
import vttu.khachsanservice.mapper.ChiTietHoaDonMapper;
import vttu.khachsanservice.mapper.HoaDonBanLeMapper;
import vttu.khachsanservice.repository.ChiTietHoaDonBanLeRepository;
import vttu.khachsanservice.repository.HoaDonBanLeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HoaDonBanLeService {

    HoaDonBanLeRepository hoaDonBanLeRepository;
    HoaDonBanLeMapper hoaDonBanLeMapper;
    ChiTietHoaDonBanLeMapper chiTietHoaDonBanLeMapper;
    ChiTietHoaDonBanLeRepository chiTietHoaDonBanLeRepository;


    // tạo id mã string tự động tăng
    public synchronized String generateNewHoaDonBanLe() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // Ví dụ: "20250303"
        Optional<String> latestId = hoaDonBanLeRepository.findLatestMaHoaDonBanLe();
        if (latestId.isPresent() && latestId.get().startsWith("HD-NH" + datePrefix)) {
            int number = Integer.parseInt(latestId.get().substring(13)); // Sau "BK20250303"
            return String.format("HD-NH%s%03d", datePrefix, number + 1);
        }
        return "HD-NH" + datePrefix + "001"; // Ví dụ: "BK20250303001"
    }


    public boolean createHoaDonBanLe(HoaDonBanLeRequest hoaDonBanLeRequest) {
        try {
            if (hoaDonBanLeRequest == null) {
                throw new AppException(ErrorCode.CHI_TIET__HOA_DON);
            }

            // Tạo hóa đơn bán lẻ
            HoaDonBanLe hoaDonBanLe = hoaDonBanLeMapper.toCreateHoaDonBanLe(hoaDonBanLeRequest);
            hoaDonBanLe.setMaHoaDonBanLe(generateNewHoaDonBanLe());
            List<ChiTietHoaDonBanLe> danhSachChiTietHoaDonBanLes = new ArrayList<>();

            for (ChiTietHoaDonBanLeRequest chiTietHoaDonRequest : hoaDonBanLeRequest.getChiTietHoaDonBanLe()) {
                ChiTietHoaDonBanLe chiTietHoaDonBanLe = chiTietHoaDonBanLeMapper.toCteateChiTietHoaDonBanLe(chiTietHoaDonRequest);
                chiTietHoaDonBanLe.setHoaDonBanLe(hoaDonBanLe);
                danhSachChiTietHoaDonBanLes.add(chiTietHoaDonBanLe);
            }

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            hoaDonBanLe.setNgayLap(LocalDateTime.now());
            hoaDonBanLe.setNguoiTao(authentication.getName());

            // Sử dụng transactional để đảm bảo toàn vẹn dữ liệu
            try {
                hoaDonBanLeRepository.save(hoaDonBanLe);
                chiTietHoaDonBanLeRepository.saveAll(danhSachChiTietHoaDonBanLes);
                return true;
            } catch (Exception e) {

                return false;
            }

        } catch (Exception ex) {

            return false;
        }
    }

    // xem lich sử hóa đơn bán lẽ

    public List<HoaDonBanLeResponse> getAllHoaDonBanLe() {
        return hoaDonBanLeRepository.findAll(Sort.by(Sort.Direction.DESC, "autoIncrementId")).stream()
                .map(hoaDonBanLeMapper::toResponseHoaDonBanLe)
                .collect(Collectors.toList());
    }
}
