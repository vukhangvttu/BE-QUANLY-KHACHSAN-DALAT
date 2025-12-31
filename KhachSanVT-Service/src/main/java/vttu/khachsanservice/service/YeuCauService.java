package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.YeuCauResponse;
import vttu.khachsanservice.mapper.YeuCauMapper;
import vttu.khachsanservice.repository.YeuCauRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class YeuCauService {

    YeuCauRepository yeuCauRepository;
    YeuCauMapper yeuCauMapper;

    public List<YeuCauResponse> getAllYeuCau() {
        return yeuCauRepository.findAll().stream().map(yeuCauMapper::toYeuCauResponse).collect(Collectors.toList());
    }
}
