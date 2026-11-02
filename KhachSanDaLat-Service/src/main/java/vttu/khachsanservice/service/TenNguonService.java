package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.request.TenNguonRequest;
import vttu.khachsanservice.dto.response.TenNguonResponse;
import vttu.khachsanservice.entity.TenNguon;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.TenNguonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TenNguonService {

    TenNguonRepository tenNguonRepository;

    public TenNguonResponse createTenNguon(TenNguonRequest tenNguonRequest) {

        long kiemTraTrungTen = tenNguonRepository.countByTenNguon(tenNguonRequest.getTenNguon());
        if(kiemTraTrungTen > 0) {
            throw new AppException(ErrorCode.TEN_NGUON_TRUNG);
        }
        TenNguon tenNguon = new TenNguon();
        tenNguon.setTenNguon(tenNguonRequest.getTenNguon().trim());
        tenNguon.setDiaChi(tenNguonRequest.getDiaChi().trim());
        tenNguon.setEmail(tenNguonRequest.getEmail().trim());
        tenNguon.setCodeVAT(tenNguonRequest.getCodeVAT().trim());
        tenNguon.setTrangThai(true);
        tenNguon = tenNguonRepository.save(tenNguon);
        return TenNguonResponse.builder().maTenNguon(tenNguon.getMaTenNguon()).tenNguon(tenNguon.getTenNguon()).diaChi(tenNguon.getDiaChi()).email(tenNguon.getEmail()).codeVAT(tenNguon.getCodeVAT()).build();
    }

   public List<TenNguon> getAllTenNguon() {
        return tenNguonRepository.findByTrangThai(true);
   }

    public TenNguonResponse updateTenNguon(TenNguonRequest tenNguonRequest) {

        boolean isExist = tenNguonRepository.existsByTenNguonAndMaTenNguonNot(tenNguonRequest.getTenNguon(), tenNguonRequest.getMaTenNguon());

        if(isExist) {
            throw new AppException(ErrorCode.TEN_NGUON_TRUNG);
        }
        if(tenNguonRequest.getMaTenNguon() == 0 )
        {
            throw new AppException(ErrorCode.valueOf("MA_TEN_NGUON_NOT_EMPTY"));
        }
        TenNguon tenNguon = new TenNguon();
        tenNguon.setMaTenNguon(tenNguonRequest.getMaTenNguon());
        tenNguon.setTenNguon(tenNguonRequest.getTenNguon().trim());
        tenNguon.setDiaChi(tenNguonRequest.getDiaChi().trim());
        tenNguon.setEmail(tenNguonRequest.getEmail().trim());
        tenNguon.setCodeVAT(tenNguonRequest.getCodeVAT().trim());
        tenNguon.setTrangThai(true);
        tenNguon = tenNguonRepository.save(tenNguon);
        return TenNguonResponse.builder().maTenNguon(tenNguon.getMaTenNguon()).tenNguon(tenNguon.getTenNguon()).diaChi(tenNguon.getDiaChi()).email(tenNguon.getEmail()).codeVAT(tenNguon.getCodeVAT()).build();
    }

    public TenNguonResponse getTenNguonById(Integer maTenNguon) {
        TenNguon entity = tenNguonRepository.findById(maTenNguon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tên nguồn với mã: " + maTenNguon));

        TenNguonResponse response = new TenNguonResponse();
        response.setMaTenNguon(entity.getMaTenNguon());
        response.setTenNguon(entity.getTenNguon());
        response.setDiaChi(entity.getDiaChi());
        response.setEmail(entity.getEmail());
        response.setCodeVAT(entity.getCodeVAT());

        return response;
    }
}
