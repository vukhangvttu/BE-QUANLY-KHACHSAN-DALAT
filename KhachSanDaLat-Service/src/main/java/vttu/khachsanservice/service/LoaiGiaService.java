package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.LoaiGiaResponse;
import vttu.khachsanservice.mapper.LoaiGiaMapper;
import vttu.khachsanservice.repository.LoaiGiaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoaiGiaService {

    LoaiGiaRepository loaiGiaRepository;
    LoaiGiaMapper loaiGiaMapper;

    public List<LoaiGiaResponse> getAllLoaiGia() {
        return loaiGiaRepository.findAll()
                .stream().map(loaiGiaMapper::toLoaiGiaResponse).collect(Collectors.toList());
    }
}
