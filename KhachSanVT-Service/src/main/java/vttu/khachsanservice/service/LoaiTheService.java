package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.LoaiTheResponse;
import vttu.khachsanservice.mapper.LoaiTheMapper;
import vttu.khachsanservice.repository.LoaiTheRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoaiTheService {

    LoaiTheRepository loaiTheRepository;
    LoaiTheMapper loaiTheMapper;

    public List<LoaiTheResponse> getAllLoaiThe() {
        return loaiTheRepository.findAll()
                .stream().map(loaiTheMapper::toLoaiTheResponse)
                .collect(Collectors.toList());
    }
}
