package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.ThiTruongResponse;
import vttu.khachsanservice.mapper.ThiTruongMapper;
import vttu.khachsanservice.repository.ThiTruongRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThiTruongService {

    ThiTruongRepository thiTruongRepository;
    ThiTruongMapper thiTruongMapper;

    public List<ThiTruongResponse> getAllThiTruong() {
        return thiTruongRepository.findAll()
                .stream().map(thiTruongMapper::toThiTruongResponse)
                .collect(Collectors.toList());
    }
}
