package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.DichVuMienPhiResponse;
import vttu.khachsanservice.mapper.DichVuMienPhiMapper;
import vttu.khachsanservice.repository.DichVuMienPhiRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DichVuMienPhiService {

    private final DichVuMienPhiRepository dichVuMienPhiRepository;
    private final DichVuMienPhiMapper dichVuMienPhiMapper;

    public List<DichVuMienPhiResponse> getAllDichVuMienPhi(){

        return dichVuMienPhiRepository.findAllActive().stream().map(dichVuMienPhiMapper::toDichVuMienPhiResponse).toList();
    }
}
