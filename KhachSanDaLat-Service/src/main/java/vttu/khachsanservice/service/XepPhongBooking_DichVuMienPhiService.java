package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.request.XepPhongBooking_DichVuMienPhiRequest;
import vttu.khachsanservice.dto.response.XepPhongBooking_DichVuMienPhiResponse;
import vttu.khachsanservice.entity.DichVuMienPhi;
import vttu.khachsanservice.entity.XepPhongBooKing;
import vttu.khachsanservice.entity.XepPhongBooking_DichVuMienPhi;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.XepPhongBooking_DichVuMienPhiMapper;
import vttu.khachsanservice.repository.DichVuMienPhiRepository;
import vttu.khachsanservice.repository.XepPhongBooking_DichVuMienPhiRepository;
import vttu.khachsanservice.repository.XepPhongRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class XepPhongBooking_DichVuMienPhiService {

    private final XepPhongRepository xepPhongRepository;

    XepPhongBooking_DichVuMienPhiRepository xepPhongBooking_dichVuMienPhiRepository;

    @Qualifier("xepPhongBooking_DichVuMienPhiMapperImpl")
    XepPhongBooking_DichVuMienPhiMapper xepPhongBookingDichVuMienPhiMapper;
    private final DichVuMienPhiRepository dichVuMienPhiRepository;

    @Transactional
    public boolean createdDichVuMienPhi(String ma_xepphong, List<XepPhongBooking_DichVuMienPhiRequest> requests, LocalDate ngayBoSung) {

        XepPhongBooKing xepPhongBooKing = xepPhongRepository.findById(ma_xepphong)
                .orElseThrow(() -> new AppException(ErrorCode.XEP_PHONG_IS_EMPTY));

        if (requests.isEmpty()) {
            throw new AppException(ErrorCode.DICH_VU_MIEN_PHI_IS_EMPTY);
        }

        if(ngayBoSung == null){
            throw new AppException(ErrorCode.ERROR_NGAY_BO_SUNG_DICH_VU_MIEN_PHI);
        }

        long soLuongTheoNgay = xepPhongBooking_dichVuMienPhiRepository.countByMaXepPhongBooking(ma_xepphong, ngayBoSung);
        if(soLuongTheoNgay > 0){
            xepPhongBooking_dichVuMienPhiRepository.deleteByMaXepPhongBookingAndNgayBoSung(ma_xepphong, ngayBoSung);
        }

        //xepPhongBooking_dichVuMienPhiRepository.deleteByMaXepPhongBooking(ma_xepphong, ngayBoSung);

        List<XepPhongBooking_DichVuMienPhi> dichVuMienPhis = new ArrayList<>();
        for (XepPhongBooking_DichVuMienPhiRequest request : requests) {

            DichVuMienPhi ktdichVuMienPhi = dichVuMienPhiRepository.findById(request.getDichVuMienPhi().getMaDichVuMienPhi())
                    .orElseThrow(() -> new AppException(ErrorCode.DICH_VU_MIEN_PHI_NOT_FOUND));

            if (request.getDichVuMienPhi().getMaDichVuMienPhi() == null || request.getDichVuMienPhi().getMaDichVuMienPhi().isEmpty()) {
                throw new AppException(ErrorCode.DICH_VU_MIEN_PHI_IS_EMPTY);
            }

            if (request.getSoLuong() < 0) {
                throw new AppException(ErrorCode.ERROR_SO_LUONGDICH_VU_MIEN_PHI);
            }

            if (request.getNgayBoSung() == null) {
                throw new AppException(ErrorCode.ERROR_NGAY_BO_SUNG_DICH_VU_MIEN_PHI);
            }

            XepPhongBooking_DichVuMienPhi dichVuMienPhi = xepPhongBookingDichVuMienPhiMapper.toCreate(request);
            dichVuMienPhi.setMaXepPhongBooking(xepPhongBooKing);
            dichVuMienPhi.setMaDichVuMienPhi(ktdichVuMienPhi);
            dichVuMienPhi.setThoiGianTao(LocalDateTime.now());
            dichVuMienPhi.setNgayBoSung(ngayBoSung);
            dichVuMienPhi.setNguoiTao(SecurityContextHolder.getContext().getAuthentication().getName());
            dichVuMienPhi.setDaXoa(false);
            dichVuMienPhis.add(dichVuMienPhi);
        }

        try {
            xepPhongBooking_dichVuMienPhiRepository.saveAll(dichVuMienPhis);
            return true;
        } catch (Exception e) {
            // Ghi log nếu cần: log.error("Lỗi khi lưu dịch vụ miễn phí", e);
            return false;
        }
    }

    public List<XepPhongBooking_DichVuMienPhiResponse> xepPhongBookingDichVuMienPhiResponses (String ma_xepphong) {

        List<XepPhongBooking_DichVuMienPhi>  xepPhongBookingDichVuMienPhi = xepPhongBooking_dichVuMienPhiRepository.findByMaXepPhongBooking(ma_xepphong);


        return  xepPhongBooking_dichVuMienPhiRepository.findByMaXepPhongBooking(ma_xepphong)
                .stream()
                .map(xepPhongBookingDichVuMienPhiMapper::toXepPhongBooking_DichVuMienPhiResponse)
                .toList();

    }

}
