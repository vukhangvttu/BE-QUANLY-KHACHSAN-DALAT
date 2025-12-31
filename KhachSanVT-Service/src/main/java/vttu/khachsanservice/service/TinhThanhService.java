package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.TinhThanhResponse;
import vttu.khachsanservice.mapper.TinhThanhMapper;
import vttu.khachsanservice.repository.TinhThanhRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TinhThanhService {

    TinhThanhRepository tinhThanhRepository;
    TinhThanhMapper tinhThanhMapper;

    public List<TinhThanhResponse> getAllTinhThanh() {
        return tinhThanhRepository.findAllTinhThanhHieuLuc().stream()
                .map(tinhThanhMapper::toTinhThanhResponse)
                .collect(Collectors.toList());
    }
}
