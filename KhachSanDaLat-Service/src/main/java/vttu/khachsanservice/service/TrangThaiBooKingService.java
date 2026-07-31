package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.TrangThaiBooKingResponse;
import vttu.khachsanservice.entity.TrangThaiBooKing;
import vttu.khachsanservice.mapper.TrangThaiBooKingMapper;
import vttu.khachsanservice.repository.TrangThaiBooKingRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrangThaiBooKingService {

    TrangThaiBooKingRepository trangThaiBooKingRepository;
    TrangThaiBooKingMapper trangThaiBooKingMapper;

    public List<TrangThaiBooKingResponse> getAllTrangThaiBooKings() {
        return  trangThaiBooKingRepository.getTrangThaiBooKing()
                .stream().map(trangThaiBooKingMapper::toTrangThaiBooKingResponse)
                .collect(Collectors.toList());
    }
}
