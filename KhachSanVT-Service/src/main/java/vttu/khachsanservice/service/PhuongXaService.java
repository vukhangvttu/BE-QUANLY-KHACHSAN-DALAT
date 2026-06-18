package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.request.PhuongXaRequest;
import vttu.khachsanservice.dto.response.PhuongXaResponse;
import vttu.khachsanservice.entity.Huyen;
import vttu.khachsanservice.entity.PhuongXa;
import vttu.khachsanservice.mapper.PhuongXaMapper;
import vttu.khachsanservice.repository.HuyenRepository;
import vttu.khachsanservice.repository.PhuongXaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhuongXaService {

    PhuongXaRepository phuongXaRepository;
    PhuongXaMapper phuongXaMapper;
    private final HuyenRepository huyenRepository;

//    public List<PhuongXaResponse> getPhuongXaByMaHuyen(String maTinh) {
//        return phuongXaRepository.getPhuongXaByMaHuyen(maTinh)
//                .stream().map(phuongXaMapper::toPhuongXaRePhuongXa)
//                .collect(Collectors.toList());
//    }

    public List<PhuongXaResponse> getPhuongXaByMaTinh(String maTinh) {
        return phuongXaRepository.getPhuongXaByMaTinh(maTinh)
                .stream().map(phuongXaMapper::toPhuongXaRePhuongXa)
                .collect(Collectors.toList());
    }

    public boolean savePhuongXa(PhuongXaRequest phuongXaRequest) {
        if (phuongXaRequest == null) {
            return false;
        }
        try {
       Huyen huyen = huyenRepository.findById(phuongXaRequest.getHuyen().getMaHuyen())
                .orElseThrow(() -> new IllegalArgumentException("Huyện không tồn tại"));

        int maxMaPhuongXa = phuongXaRepository.getMaxMaPhuongXaInHuyen(phuongXaRequest.getHuyen().getMaHuyen());


        int biendem = maxMaPhuongXa + 1;

        String congchuoimaphuongxa = phuongXaRequest.getHuyen().getMaHuyen() + padWithZerosAndRightTwo(biendem);

        var authentiaction = SecurityContextHolder.getContext().getAuthentication();

        PhuongXa phuongXa = new PhuongXa();
        phuongXa.setMaPhuongXa(congchuoimaphuongxa);
        phuongXa.setTenPhuongXa(phuongXaRequest.getTenPhuongXa().trim());
        phuongXa.setHuyen(huyen);
        phuongXa.setNguoiTao(authentiaction.getName());
        phuongXa.setHieuLuc(true);
        phuongXa.setThoiGianTao(LocalDateTime.now());
        phuongXaRepository.save(phuongXa);

            return true;
        } catch (Exception e) {
            // Log nếu cần: e.printStackTrace() hoặc logger.error(...)
            return false;
        }
    }

    public static String padWithZerosAndRightTwo(int number) {
        // Chuyển đổi số thành chuỗi
        String numberStr = String.valueOf(number);

        // Thêm số 0 vào phía trước để đảm bảo độ dài ít nhất là 4
        String paddedNumberStr = "0000" + numberStr;

        // Lấy 2 ký tự cuối cùng
        return paddedNumberStr.substring(paddedNumberStr.length() - 2);
    }

    public List<PhuongXaResponse> getAllPhuongXaUserTuThem() {
        return phuongXaRepository.getListPhuongXaTuTao()
                .stream().map(phuongXaMapper::toPhuongXaRePhuongXa)
                .collect(Collectors.toList());
    }
}
