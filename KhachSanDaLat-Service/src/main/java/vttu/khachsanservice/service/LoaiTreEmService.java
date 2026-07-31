package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.LoaiTheResponse;
import vttu.khachsanservice.dto.response.LoaiTreEmResponse;
import vttu.khachsanservice.mapper.LoaiTreEmMapper;
import vttu.khachsanservice.repository.LoaiTreEmRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoaiTreEmService {

    LoaiTreEmRepository loaiTreEmRepository;
    LoaiTreEmMapper loaiTreEmMapper;

    public List<LoaiTreEmResponse> getAllLoaiTreEm() {
        return loaiTreEmRepository.findAll(Sort.by(Sort.Direction.ASC, "maLoaiTreEm"))
                .stream().map(loaiTreEmMapper::toCreateLoaiTreEm)
                .collect(Collectors.toList());
    }

}
