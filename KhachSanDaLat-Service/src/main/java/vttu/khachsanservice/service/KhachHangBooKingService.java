package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.request.KhachHangBooKingRequest;
import vttu.khachsanservice.dto.response.KhachHangBooKingResponse;
import vttu.khachsanservice.entity.KhachHangBooKing;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.KhachHangBooKingMapper;
import vttu.khachsanservice.repository.BooKingRepository;
import vttu.khachsanservice.repository.HoaDonVatRepository;
import vttu.khachsanservice.repository.KhachHangBooKingRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KhachHangBooKingService {

    KhachHangBooKingRepository khachHangBooKingRepository;
    KhachHangBooKingMapper khachHangBooKingMapper;
    private final BooKingRepository booKingRepository;


    public synchronized String generateNewId() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // Ví dụ: "20250303"
        Optional<String> latestId = khachHangBooKingRepository.findLatestKhachHangId();
        if (latestId.isPresent() && latestId.get().startsWith("KH-BK" + datePrefix)) {
            int number = Integer.parseInt(latestId.get().substring(13)); // Sau "BK20250303"
            return String.format("KH-BK%s%03d", datePrefix, number + 1);
        }
        return "KH-BK" + datePrefix + "001"; // Ví dụ: "BK20250303001"
    }


    public KhachHangBooKingResponse createKhachHangBooKing(KhachHangBooKingRequest request) {

        KhachHangBooKing khachHangBooKing = khachHangBooKingMapper.toCreateKhachHangBooKing(request);


        khachHangBooKing.setHoKhachHangBooking(generateNewId());


        return khachHangBooKingMapper.toKhachHangBooKingResponse(khachHangBooKingRepository.save(khachHangBooKing));
    }


    public List<KhachHangBooKingResponse> listKhachHangBooKing() {
        return khachHangBooKingRepository.findAll(Sort.by(Sort.Direction.DESC,"autoIncrementId"))
                .stream().map(khachHangBooKing -> khachHangBooKingMapper.toKhachHangBooKingResponse(khachHangBooKing))
                .collect(Collectors.toList());
    }

    public Map<String,Object> getThongTinKhachHangXuatVAT(String maBooking){

        booKingRepository.findById(maBooking)
                .orElseThrow(() -> new AppException(ErrorCode.MA_BOOKING_NOT_FOUND));
    	return khachHangBooKingRepository.getThongTinKhachHangXuatVAT(maBooking);
    }
}
