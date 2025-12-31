package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.GiamGiaResponse;
import vttu.khachsanservice.mapper.GiamGiaMapper;
import vttu.khachsanservice.repository.GiamGiaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GiamGiaService {

    GiamGiaRepository giamGiaRepository;
    GiamGiaMapper giamGiaMapper;

    public List<GiamGiaResponse> getAllGiamGia() {
        return giamGiaRepository.findAll()
                .stream().map(giamGiaMapper::toGiamGiaResponse)
                .collect(Collectors.toList());
    }
}
