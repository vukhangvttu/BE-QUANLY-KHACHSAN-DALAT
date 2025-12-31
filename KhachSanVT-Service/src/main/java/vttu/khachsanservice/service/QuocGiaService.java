package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.QuocGiaResponse;
import vttu.khachsanservice.entity.QuocGia;
import vttu.khachsanservice.mapper.QuocGiaMapper;
import vttu.khachsanservice.repository.QuocGiaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuocGiaService {

    QuocGiaRepository quocGiaRepository;
    QuocGiaMapper quocGiaMapper;

    public List<QuocGiaResponse> getAllQuocGia() {
        return quocGiaRepository.findAll().stream()
                .map(quocGiaMapper::toQuocGiaResponse).collect(Collectors.toList());
    }
}
