package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.HinhThucThanhToanResponse;
import vttu.khachsanservice.mapper.HinhThucThanhToanMapper;
import vttu.khachsanservice.repository.HinhThucThanhToanRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HinhThucThanhToanService {

    HinhThucThanhToanRepository hinhThucThanhToanRepository;
    HinhThucThanhToanMapper hinhThucThanhToanMapper;

    public List<HinhThucThanhToanResponse> getAllHinhThucThanhToan() {
            return hinhThucThanhToanRepository.findAll()
                    .stream().map(hinhThucThanhToanMapper::toHinhThucThanhToanResponse)
                    .collect(Collectors.toList());
    }

    public List<HinhThucThanhToanResponse> getHinhThucThanhToanByMa() {
        return hinhThucThanhToanRepository.getHinhThucThanhToan()
                .stream().map(hinhThucThanhToanMapper::toHinhThucThanhToanResponse)
                .collect(Collectors.toList());
    }

}
