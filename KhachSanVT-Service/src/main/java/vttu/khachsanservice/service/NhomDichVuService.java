package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.NhomDichVuReponse;
import vttu.khachsanservice.mapper.NhomDichVuMapper;
import vttu.khachsanservice.repository.NhomDichVuRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NhomDichVuService {

    NhomDichVuRepository nhomDichVuRepository;
    NhomDichVuMapper nhomDichVuMapper;

    public List<NhomDichVuReponse> getAllNhomDichVu() {
        return nhomDichVuRepository.findAll(Sort.by(Sort.Direction.ASC, "thuTu")).stream()
                .map(nhomDichVuMapper::toReposeNhomDichVuReponse)
                .collect(Collectors.toList());
    }
}
