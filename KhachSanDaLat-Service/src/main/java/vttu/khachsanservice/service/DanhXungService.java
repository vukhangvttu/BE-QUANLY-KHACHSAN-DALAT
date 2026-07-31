package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.DanhXungResponse;
import vttu.khachsanservice.mapper.DanhXungMapper;
import vttu.khachsanservice.repository.DanhXungRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class  DanhXungService {

    DanhXungRepository danhXungRepository;
    DanhXungMapper danhXungMapper;
    public List<DanhXungResponse> getAllDanhXung() {
        return danhXungRepository.findAll()
                .stream().map(danhXungMapper::toDanhXungResponse)
                .collect(Collectors.toList());
    }
}
