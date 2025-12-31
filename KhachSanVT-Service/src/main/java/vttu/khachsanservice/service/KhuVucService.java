package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.KhuVucResponse;
import vttu.khachsanservice.entity.KhuVuc;
import vttu.khachsanservice.mapper.KhuVucMapper;
import vttu.khachsanservice.repository.KhuVucRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KhuVucService {

    KhuVucRepository khuVucRepository;
    KhuVucMapper khuVucMapper;

    public List<KhuVucResponse> getAllKhuVuc() {
        return khuVucRepository.findAll()
                .stream().map(khuVuc -> khuVucMapper.toKhuVucResponse(khuVuc))
                .collect(Collectors.toList());
    }
}
