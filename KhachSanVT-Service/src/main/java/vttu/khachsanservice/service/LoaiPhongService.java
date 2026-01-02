package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import vttu.khachsanservice.dto.BookingStatsDTO;
import vttu.khachsanservice.dto.LoaiPhongTrongDTO;
import vttu.khachsanservice.dto.LoaiPhongTrongDTOImpl;
import vttu.khachsanservice.dto.response.LoaiPhongResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.LoaiPhongMapper;
import vttu.khachsanservice.repository.LoaiPhongRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class LoaiPhongService {

    final LoaiPhongRepository loaiPhongRepository;
    final LoaiPhongMapper loaiPhongMapper;
    final RestTemplate restTemplate;

    @Value("${external.golden-era.base-url}")
    String goldenEraBaseUrl;

    public List<LoaiPhongResponse> getAllLoaiPhong() {
        return loaiPhongRepository.findAll()
                .stream().map(loaiPhongMapper::toLoaiPhongResponse).collect(Collectors.toList());
    }

    public List<LoaiPhongTrongDTO> getAllLoaiPhongTrong(LocalDate ngayDen, LocalDate ngayDi) {
        // Kiểm tra ngày không null
        if (ngayDen == null || ngayDi == null) {
            throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY);
        }

//        // Kiểm tra ngày đến không được sau ngày đi
//        if (ngayDen.isAfter(ngayDi)) {
//            throw new AppException(ErrorCode.ERROR_NGAYDEN, "không thể sau ngày đi!");
//        }
//
//        // Kiểm tra ngày đến không phải trong quá khứ
//        LocalDate today = LocalDate.now();
//        if (ngayDen.isBefore(today)) {
//            throw new AppException(ErrorCode.ERROR_NGAYDEN, "không thể là ngày trong quá khứ!");
//        }

        // Gọi repository lấy danh sách phòng
        List<LoaiPhongTrongDTO> dsPhongTrong = loaiPhongRepository.kiemTraAllLoaiPhongTrong(ngayDen, ngayDi, "all");

        // Kiểm tra nếu danh sách trống
        if (dsPhongTrong.isEmpty()) {
            System.out.println("Không có loại phòng trống trong khoảng thời gian đã chọn.");
        }

        return dsPhongTrong;
    }

    public List<LoaiPhongTrongDTO> xemDuBaoPhong(LocalDate ngayDen, LocalDate ngayDi) {
        // Kiểm tra ngày không null
        if (ngayDen == null || ngayDi == null) {
            throw new AppException(ErrorCode.NGAYDEN_NGAYDI_EMPTY);
        }

        // Kiểm tra ngày đến không được sau ngày đi
        if (ngayDen.isAfter(ngayDi)) {
            throw new AppException(ErrorCode.ERROR_NGAYDEN, "không thể sau ngày đi!");
        }

        // Gọi repository lấy danh sách phòng
        List<LoaiPhongTrongDTO> dsPhongTrong = loaiPhongRepository.xemDuBaoPhong(ngayDen, ngayDi);

        // Kiểm tra nếu danh sách trống
        if (dsPhongTrong.isEmpty()) {
            log.warn("Không có loại phòng trống trong khoảng thời gian đã chọn.");
            return dsPhongTrong;
        }

        // Gọi API external để lấy thống kê booking
        List<BookingStatsDTO> bookingStats = fetchBookingStats(ngayDen, ngayDi);

        // Nếu không lấy được dữ liệu từ API external, trả về dữ liệu gốc
        if (bookingStats == null || bookingStats.isEmpty()) {
            log.warn("Không lấy được dữ liệu từ Golden Era API, sử dụng dữ liệu nội bộ");
            return dsPhongTrong;
        }

        // Tạo map để tra cứu nhanh số phòng booking theo ngày và mã loại phòng
        Map<String, Map<String, Integer>> bookingMap = buildBookingMap(bookingStats);

        // Chuyển đổi và cập nhật số phòng trống
        List<LoaiPhongTrongDTO> result = dsPhongTrong.stream()
                .map(phong -> {
                    LoaiPhongTrongDTOImpl impl = new LoaiPhongTrongDTOImpl(
                            phong.getNgay(),
                            phong.getMaLoaiPhong(),
                            phong.getTenLoaiPhong(),
                            phong.getTongSoPhong(),
                            phong.getSoPhongTrong(),
                            phong.getTongSoGiuongThem(),
                            phong.getTongSoGiuongDaSuDung()
                    );

                    // Lấy số phòng booking từ API external
                    String ngay = phong.getNgay();
                    String maLoaiPhong = phong.getMaLoaiPhong();

                    if (bookingMap.containsKey(ngay) && bookingMap.get(ngay).containsKey(maLoaiPhong)) {
                        int totalRooms = bookingMap.get(ngay).get(maLoaiPhong);
                        int newSoPhongTrong = phong.getSoPhongTrong() + totalRooms;
                        impl.setSoPhongTrong(newSoPhongTrong);
                        log.debug("Updated room availability for {} on {}: {} + {} = {}",
                                maLoaiPhong, ngay, phong.getSoPhongTrong(), totalRooms, newSoPhongTrong);
                    }

                    return (LoaiPhongTrongDTO) impl;
                })
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Gọi API external để lấy thống kê booking
     */
    private List<BookingStatsDTO> fetchBookingStats(LocalDate checkInDate, LocalDate checkOutDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

            String url = UriComponentsBuilder
                    .fromHttpUrl(goldenEraBaseUrl + "/booking-stats/daily")
                    .queryParam("checkInDate", checkInDate.format(formatter))
                    .queryParam("checkOutDate", checkOutDate.format(formatter))
                    .queryParam("region", "VUNGTAU")
                    .queryParam("status", "CONFIRMED")
                    .build()
                    .toUriString();

            log.info("Calling Golden Era API: {}", url);

            ResponseEntity<List<BookingStatsDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BookingStatsDTO>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully fetched {} booking stats from Golden Era API", response.getBody().size());
                return response.getBody();
            } else {
                log.warn("Golden Era API returned non-successful status: {}", response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (Exception e) {
            log.error("Lỗi khi gọi Golden Era API: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION,
                    "Không thể kết nối đến Golden Era API: " + e.getMessage());
        }
    }

    /**
     * Tạo map để tra cứu nhanh số phòng booking theo ngày và mã loại phòng
     * Key1: ngày (stayDate)
     * Key2: mã loại phòng (roomTypeId)
     * Value: số phòng (totalRooms)
     */
    private Map<String, Map<String, Integer>> buildBookingMap(List<BookingStatsDTO> bookingStats) {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        for (BookingStatsDTO stats : bookingStats) {
            result.computeIfAbsent(stats.getStayDate(), k -> new HashMap<>())
                    .put(stats.getRoomTypeId(), stats.getTotalRooms());
        }

        return result;
    }

}
