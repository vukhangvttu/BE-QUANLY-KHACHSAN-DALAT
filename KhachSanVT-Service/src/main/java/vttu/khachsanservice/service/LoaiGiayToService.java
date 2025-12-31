package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.LoaiGiayToResponse;
import vttu.khachsanservice.mapper.LoaiGiayToMapper;
import vttu.khachsanservice.repository.LoaiGiayToRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoaiGiayToService {

    LoaiGiayToRepository loaiGiayToRepository;
    LoaiGiayToMapper loaiGiayToMapper;

    public List<LoaiGiayToResponse> getAllLoaiGiayTo() {
        return loaiGiayToRepository.findAll().stream()
                .map(loaiGiayToMapper::toLoaiGiayResponse).collect(Collectors.toList());
    }
}
