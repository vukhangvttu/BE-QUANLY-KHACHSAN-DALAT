package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.request.PhieuDichVuRequest;
import vttu.khachsanservice.dto.request.XepPhongBooKingRequest;
import vttu.khachsanservice.dto.response.PhieuDichVuReponse;
import vttu.khachsanservice.entity.*;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.PhieuDichVuMapper;
import vttu.khachsanservice.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhieuDichVuService {


    private final BooKingRepository booKingRepository;
    private final DichVuRepository dichVuRepository;
    PhieuDichVuMapper phieuDichVuMapper;
    private final PhieuDichVuRepository phieuDichVuRepository;
    private final PhongRepository phongRepository;
    private final XepPhongRepository xepPhongRepository;
    private final HoaDonRepository hoaDonRepository;

    @Transactional
    public boolean createPhieuDichVu(String ma_phong, String ma_xepphong_booking, List<PhieuDichVuRequest> request) {
        if (request == null || request.isEmpty()) {
            throw new AppException(ErrorCode.PHIEU_DICH_VU_NOT_FOUND);
        }

        // Kiểm tra booking có tồn tại không
     xepPhongRepository.findById(ma_xepphong_booking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));
      Phong phong =  phongRepository.findById(ma_phong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_PHONG_NOT_FOUND));

        // Lấy danh sách mã dịch vụ từ request
        List<String> maDichVus = request.stream()
                .map(r -> r.getDichVu().getMaDichVu())
                .collect(Collectors.toList());

        // Lấy tất cả dịch vụ trong database một lần duy nhất
        Map<String, DichVu> dichVuMap = dichVuRepository.findAllById(maDichVus).stream()
                .collect(Collectors.toMap(DichVu::getMaDichVu, Function.identity()));

        // Lấy danh sách các dịch vụ đã tồn tại trong phiếu dịch vụ của booking này
        Map<String, PhieuDichVu> existingPhieuDichVuMap = phieuDichVuRepository.getAllPhieuDichVuByMaBooKing(ma_xepphong_booking).stream()
                .collect(Collectors.toMap(p -> p.getDichVu().getMaDichVu(), Function.identity()));

        // Danh sách để lưu dữ liệu mới hoặc cập nhật dữ liệu cũ
        List<PhieuDichVu> phieuDichVuList = new ArrayList<>();

        var authentiaction = SecurityContextHolder.getContext().getAuthentication();

        for (PhieuDichVuRequest phieuDichVuRequest : request) {
            String maDichVu = phieuDichVuRequest.getDichVu().getMaDichVu();
            DichVu dichVu = dichVuMap.get(maDichVu);

            // Kiểm tra dịch vụ có tồn tại không
            if (dichVu == null) {
                throw new AppException(ErrorCode.DICH_VU_NOT_FOUND);
            }

            // Kiểm tra giá hợp lệ
            if (phieuDichVuRequest.getGia().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.GIA_PHONG_ERROR);
            }

            if (existingPhieuDichVuMap.containsKey(maDichVu)) {
                // Nếu đã tồn tại, cập nhật số lượng và tổng tiền
                PhieuDichVu existingPhieuDichVu = existingPhieuDichVuMap.get(maDichVu);
                existingPhieuDichVu.setSoLuong(existingPhieuDichVu.getSoLuong() + phieuDichVuRequest.getSoLuong());
                existingPhieuDichVu.setThanhTien(
                        existingPhieuDichVu.getGia().multiply(BigDecimal.valueOf(existingPhieuDichVu.getSoLuong()))
                );
                existingPhieuDichVu.setGhiChu(existingPhieuDichVu.getGhiChu() + ", " + phieuDichVuRequest.getGhiChu());
                phieuDichVuList.add(existingPhieuDichVu);
            } else {
                // Nếu chưa có, tạo mới
                PhieuDichVu newPhieuDichVu = phieuDichVuMapper.toCreatePhieuDichVu(phieuDichVuRequest);
                newPhieuDichVu.setNgaySuDung(LocalDate.now());
                newPhieuDichVu.setGioSuDung(LocalTime.now());
                newPhieuDichVu.setPhong(phong);
                newPhieuDichVu.setThanhTien(
                        BigDecimal.valueOf(phieuDichVuRequest.getSoLuong()).multiply(phieuDichVuRequest.getGia())
                );

                newPhieuDichVu.setNguoiTao(authentiaction.getName());
                phieuDichVuList.add(newPhieuDichVu);
            }
        }

        // Lưu danh sách vào database (cập nhật hoặc thêm mới)
        phieuDichVuRepository.saveAll(phieuDichVuList);

        return true;
    }


    public List<PhieuDichVuReponse> getAllPhieuDichVuByMaBooKing(String ma_xepphong_booking) {

        xepPhongRepository.findById(ma_xepphong_booking)
                .orElseThrow(()-> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND, ma_xepphong_booking));
        return phieuDichVuRepository.getAllPhieuDichVuByMaBooKing(ma_xepphong_booking)
                .stream().map(phieuDichVuMapper::toPhieuDichVuResponse).collect(Collectors.toList());
    }

    @Transactional
    public boolean updatePhieuDichVu(String ma_booking,String ma_phong,String ma_xepphong_booking,List<PhieuDichVuRequest> request) {

        booKingRepository.findById(ma_booking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

        int check = hoaDonRepository.checkHoaDonByMaBooking(ma_booking);
        if(check > 0)
            throw new AppException(ErrorCode.ERROR_HOA_DON_DA_THANH_TOAN);

        if (request == null || request.isEmpty()) {
            throw new AppException(ErrorCode.PHIEU_DICH_VU_NOT_FOUND);
        }
        // Kiểm tra booking tồn tại trong DB
        xepPhongRepository.findById(ma_xepphong_booking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_XEP_PHONG_BOOKING_NOT_FOUND));

        Phong phong =  phongRepository.findById(ma_phong)
                .orElseThrow(() -> new AppException(ErrorCode.MA_PHONG_NOT_FOUND));

        // Kiểm tra trùng lặp madichvu trong request
        Set<String> maDichVuSet = new HashSet<>();
        for (PhieuDichVuRequest req : request) {
            String maDichVu = req.getDichVu().getMaDichVu();
            if (!maDichVuSet.add(maDichVu)) {
                throw new AppException(ErrorCode.DUPLICATE_MA_DICH_VU, "Dịch vụ " + maDichVu + " bị trùng lặp trong request.");
            }
        }

        // Lấy danh sách các dịch vụ đã tồn tại trong phiếu dịch vụ của booking này
        Map<String, PhieuDichVu> existingPhieuDichVuMap = phieuDichVuRepository.getAllPhieuDichVuByMaBooKing(ma_xepphong_booking).stream()
                .collect(Collectors.toMap(p -> p.getDichVu().getMaDichVu(), Function.identity()));
        // Lấy danh sách mã dịch vụ từ request
        List<String> maDichVus = request.stream()
                .map(r -> r.getDichVu().getMaDichVu())
                .collect(Collectors.toList());

        // Lấy tất cả dịch vụ trong database một lần duy nhất
        Map<String, DichVu> dichVuMap = dichVuRepository.findAllById(maDichVus).stream()
                .collect(Collectors.toMap(DichVu::getMaDichVu, Function.identity()));

        // Danh sách để lưu dữ liệu mới hoặc cập nhật dữ liệu cũ
        List<PhieuDichVu> phieuDichVuList = new ArrayList<>();

        var authentiaction = SecurityContextHolder.getContext().getAuthentication();

        for (PhieuDichVuRequest phieuDichVuRequest : request) {
            String maDichVu = phieuDichVuRequest.getDichVu().getMaDichVu();
            DichVu dichVu = dichVuMap.get(maDichVu);

            // Kiểm tra dịch vụ có tồn tại không
            if (dichVu == null) {
                throw new AppException(ErrorCode.DICH_VU_NOT_FOUND);
            }

            // Kiểm tra giá hợp lệ
            if (phieuDichVuRequest.getGia().compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.GIA_PHONG_ERROR);
            }

            if (existingPhieuDichVuMap.containsKey(maDichVu)) {
                // Nếu đã tồn tại, cập nhật số lượng và tổng tiền
                PhieuDichVu existingPhieuDichVu = existingPhieuDichVuMap.get(maDichVu);
                existingPhieuDichVu.setSoLuong(phieuDichVuRequest.getSoLuong());
                existingPhieuDichVu.setThanhTien(
                        phieuDichVuRequest.getGia().multiply(BigDecimal.valueOf(phieuDichVuRequest.getSoLuong()))
                );
                existingPhieuDichVu.setGhiChu( phieuDichVuRequest.getGhiChu());
                phieuDichVuList.add(existingPhieuDichVu);
            }
        }

        // Lưu danh sách vào database
        phieuDichVuRepository.saveAll(phieuDichVuList);


        return true;
    }

    @Transactional
    public boolean deleteDichVuTrongPhieu(String maBooKing,String maPhieuDichVu) {
        phieuDichVuRepository.findById(maPhieuDichVu)
                .orElseThrow(() -> new AppException(ErrorCode.PHIEU_DICH_VU_NOT_FOUND));
        booKingRepository.findById(maBooKing)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));

        int check = hoaDonRepository.checkHoaDonByMaBooking(maBooKing);
        if(check > 0)
            throw new AppException(ErrorCode.ERROR_HOA_DON_DA_THANH_TOAN);

        try {
            phieuDichVuRepository.updateThoiGianXoa(maPhieuDichVu);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa dịch vụ không phiếu: " + e.getMessage());
            return false;
        }
    }
}
