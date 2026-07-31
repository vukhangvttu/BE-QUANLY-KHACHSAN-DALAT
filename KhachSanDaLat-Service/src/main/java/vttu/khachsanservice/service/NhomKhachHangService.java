package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.request.KhachHangBooKingRequest;
import vttu.khachsanservice.dto.response.KhachHangBooKingResponse;
import vttu.khachsanservice.dto.response.NhomKhachHangResponse;
import vttu.khachsanservice.entity.KhachHangBooKing;
import vttu.khachsanservice.entity.NhomKhachHang;
import vttu.khachsanservice.mapper.NhomKhachHangMapper;
import vttu.khachsanservice.repository.NhomKhachHangRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NhomKhachHangService {

    NhomKhachHangRepository NhomKhachHangRepository;
    NhomKhachHangMapper nhomKhachHangMapper;
    private final NhomKhachHangRepository nhomKhachHangRepository;


    public synchronized String generateNewId() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // Ví dụ: "20250303"
        Optional<String> latestId = NhomKhachHangRepository.findLatestNhomKhachHangId();
        if (latestId.isPresent() && latestId.get().startsWith("NKH" + datePrefix)) {
            int number = Integer.parseInt(latestId.get().substring(11)); // Sau "BK20250303"
            return String.format("NKH%s%02d", datePrefix, number + 1);
        }
        return "NKH" + datePrefix + "01"; // Ví dụ: "BK20250303001"
    }


    public List<NhomKhachHangResponse> getAllNhomKhachHang() {

        return nhomKhachHangRepository.findAll(Sort.by(Sort.Direction.DESC,"autoIncrementId"))
                .stream().map(nhomKhachHangMapper::toNhomKhachHangResponse )
                .collect(Collectors.toList());
    }
}
