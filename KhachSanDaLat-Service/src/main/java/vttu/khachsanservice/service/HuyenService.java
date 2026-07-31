package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.response.HuyenReponse;
import vttu.khachsanservice.mapper.HuyenMapper;
import vttu.khachsanservice.repository.HuyenRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HuyenService {

    HuyenRepository huyenRepository;
    HuyenMapper huyenMapper;

    public List<HuyenReponse> getAllHuyenByMaTinh (String maTinh){
        return huyenRepository.getHuyenByTinh(maTinh).stream()
                .map(huyenMapper::toHuyenReponse).collect(Collectors.toList());
    }
}
