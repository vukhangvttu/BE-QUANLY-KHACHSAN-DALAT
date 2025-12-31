package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.NguonKhachResponse;
import vttu.khachsanservice.entity.NguonKhach;
import vttu.khachsanservice.mapper.NguonKhachMapper;
import vttu.khachsanservice.repository.NguonKhachRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NguonKhachService {

    NguonKhachRepository nguonKhachRepository;
    NguonKhachMapper nguonKhachMapper;

    public List<NguonKhachResponse> getAllNguonKhach() {
        return nguonKhachRepository.findAll(Sort.by(Sort.Direction.ASC, "maNguonKhach"))
                .stream().map(nguonKhach -> nguonKhachMapper.toNguonKhachResponse(nguonKhach))
                .collect(Collectors.toList());
    }
}
