package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.MucDichDenResponse;
import vttu.khachsanservice.mapper.MucDichDenMapper;
import vttu.khachsanservice.repository.MucDichDenRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MucDichDenService {
    MucDichDenRepository mucDichDenRepository;
    MucDichDenMapper mucDichDenMapper;

    public List<MucDichDenResponse> getAllMucDichDen() {
        return mucDichDenRepository.findAll()
                .stream().map(mucDichDenMapper::toMucDichDenResponse)
                .collect(Collectors.toList());
    }
}
