package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.DichVuResponse;
import vttu.khachsanservice.mapper.DichVuMapper;
import vttu.khachsanservice.repository.DichVuRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DichVuService {
    DichVuRepository dichVuRepository;
    DichVuMapper dichVuMapper;
    public List<DichVuResponse> getAllDichVu() {
        return dichVuRepository.findAll()
                .stream().map(dichVuMapper::toDichVuResponse).collect(Collectors.toList());
    }
}
