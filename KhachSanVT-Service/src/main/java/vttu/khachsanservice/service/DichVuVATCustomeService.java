package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.request.DichVuVATCusTomeRequest;
import vttu.khachsanservice.dto.response.DichVuVATCusTomeReponse;
import vttu.khachsanservice.entity.DichVuVATCusTome;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.DichVuVATCustomeMapper;
import vttu.khachsanservice.repository.DichVuVATCustomeRepository;
import vttu.khachsanservice.repository.HoaDonVatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DichVuVATCustomeService {

    DichVuVATCustomeRepository dichVuVATCustomeRepository;
    DichVuVATCustomeMapper dichVuVATCustomeMapper;
    private final HoaDonVatRepository hoaDonVatRepository;

    @Transactional
    public boolean createDichVuTuynChinhVAT(String ma_hoadon_vat,List<DichVuVATCusTomeRequest> request) {
        try {


        hoaDonVatRepository.findById(ma_hoadon_vat)
                .orElseThrow(() -> new AppException(ErrorCode.HOA_DON_VAT_NOT_FOUND));

            if(request == null || request.isEmpty()) {
                dichVuVATCustomeRepository.deleteAllByMaHoaDonVat(ma_hoadon_vat);

            }
            var authentiaction = SecurityContextHolder.getContext().getAuthentication();

        int count = dichVuVATCustomeRepository.countThongTinDichVuVAT(ma_hoadon_vat);
            if(count > 0){
                dichVuVATCustomeRepository.deleteAllByMaHoaDonVat(ma_hoadon_vat);
            }

            int stt = 1;
        for(DichVuVATCusTomeRequest dichVuVATCusTomeRequest : request){

            if(dichVuVATCusTomeRequest.getHoaDonVat().getMaHoaDonVat() == null || dichVuVATCusTomeRequest.getHoaDonVat().getMaHoaDonVat().isEmpty()) {
                throw new AppException(ErrorCode.MA_HOA_DON_VAT_NOT_FOUND);
            }
            if(dichVuVATCusTomeRequest.getItemCode() == null || dichVuVATCusTomeRequest.getItemCode().isEmpty()){
                throw new AppException(ErrorCode.ITEM_CODE_IS_EMPTY);
            }
            if(dichVuVATCusTomeRequest.getItemName() == null || dichVuVATCusTomeRequest.getItemName().isEmpty()){
                throw new AppException(ErrorCode.ITEM_NAME_IS_EMPTY);
            }
            if(dichVuVATCusTomeRequest.getUnitName() == null || dichVuVATCusTomeRequest.getUnitName().isEmpty()){
                throw new AppException(ErrorCode.UNIT_NAME_IS_EMPTY);
            }
            if(dichVuVATCusTomeRequest.getQuantity() <= 0){
                throw new AppException(ErrorCode.QUANTITY_ERROR);
            }
            if(dichVuVATCusTomeRequest.getUnitPrice() == null){
                throw new AppException(ErrorCode.UNIT_PRICE_ERROR);
            }

            DichVuVATCusTome dichVuVATCusTome = dichVuVATCustomeMapper.toCreateDichVuVATCustome(dichVuVATCusTomeRequest);
            dichVuVATCusTome.setThoiGianTao(LocalDateTime.now());
            dichVuVATCusTome.setNguoiTao(authentiaction.getName());
            dichVuVATCusTome.setLineNumber(String.valueOf(stt++));
            dichVuVATCustomeRepository.save(dichVuVATCusTome);

        }

        return true;
        } catch (Exception e) {
            // Log the exception if needed
            return false; // Return false if any exception occurs
        }
    }

    public List<DichVuVATCusTomeReponse> getDichVuVATCustome(String maHoaDonVat) {
        hoaDonVatRepository.findById(maHoaDonVat)
                .orElseThrow(() -> new AppException(ErrorCode.MA_HOA_DON_VAT_NOT_FOUND));
        return dichVuVATCustomeRepository.getThongTinDichVuVAT(maHoaDonVat).stream().map(dichVuVATCustomeMapper::toResponse).toList();
    }
}
