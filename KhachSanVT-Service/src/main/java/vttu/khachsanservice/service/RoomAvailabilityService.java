package vttu.khachsanservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vttu.khachsanservice.dto.RoomAvailabilityDTO;
import vttu.khachsanservice.dto.request.BookingDetailRequest;
import vttu.khachsanservice.dto.request.CheckRoomAvailabilityRequest;
import vttu.khachsanservice.dto.request.DateRequest;
import vttu.khachsanservice.dto.response.CheckRoomAvailabilityResponse;
import vttu.khachsanservice.dto.response.RoomAvailabilityDetailResponse;
import vttu.khachsanservice.entity.LoaiPhong;
import vttu.khachsanservice.repository.RoomAvailabilityRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomAvailabilityService {

    private final RoomAvailabilityRepository roomAvailabilityRepository;

    /**
     * Kiểm tra phòng trống với transaction isolation SERIALIZABLE
     * để đảm bảo thread-safe và không có race condition
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CheckRoomAvailabilityResponse checkRoomAvailability(CheckRoomAvailabilityRequest request) {
        log.info("Bắt đầu kiểm tra phòng trống cho {} loại phòng", request.getBookingDetails().size());

        List<RoomAvailabilityDetailResponse> allDetails = new ArrayList<>();
        boolean allAvailable = true;
        StringBuilder messageBuilder = new StringBuilder();

        for (BookingDetailRequest bookingDetail : request.getBookingDetails()) {
            String roomTypeId = bookingDetail.getRoomTypeId();
            Integer requestedQuantity = bookingDetail.getQuantity();

            // Kiểm tra loại phòng có tồn tại không
            LoaiPhong loaiPhong = roomAvailabilityRepository.findById(roomTypeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng: " + roomTypeId));

            log.info("Kiểm tra loại phòng: {} - {} với số lượng yêu cầu: {}",
                    roomTypeId, loaiPhong.getTenLoaiPhong(), requestedQuantity);

            // Kiểm tra từng ngày
            for (DateRequest dateRequest : bookingDetail.getListDate()) {
                LocalDate checkDate = dateRequest.getNgay();

                List<RoomAvailabilityDTO> availabilityList =
                        roomAvailabilityRepository.checkRoomAvailabilityByDate(roomTypeId, checkDate);

                if (!availabilityList.isEmpty()) {
                    RoomAvailabilityDTO availability = availabilityList.get(0);

                    // Kiểm tra xem số phòng trống có đủ cho số lượng yêu cầu không
                    boolean isAvailable = availability.getSoPhongTrong() >= requestedQuantity;
                    int availableRooms = availability.getSoPhongTrong();

                    RoomAvailabilityDetailResponse detail = RoomAvailabilityDetailResponse.builder()
                            .roomTypeId(availability.getMaLoaiPhong())
                            .roomTypeName(availability.getTenLoaiPhong())
                            .date(availability.getNgay())
                            .totalRooms(availability.getTongSoPhong())
                            .availableRooms(availableRooms)
                            .bookedRooms(availability.getSoPhongDaDat())
                            .requestedQuantity(requestedQuantity)
                            .isAvailable(isAvailable)
                            .status(isAvailable
                                    ? String.format("Còn đủ phòng (Còn %d/%d)", availableRooms, requestedQuantity)
                                    : String.format("Không đủ phòng (Còn %d/%d)", availableRooms, requestedQuantity))
                            .build();

                    allDetails.add(detail);

                    if (!isAvailable) {
                        allAvailable = false;
                        messageBuilder.append(String.format(
                                "Loại phòng %s (%s) không đủ phòng vào ngày %s. Yêu cầu: %d phòng, Còn trống: %d phòng. ",
                                availability.getTenLoaiPhong(),
                                availability.getMaLoaiPhong(),
                                availability.getNgay(),
                                requestedQuantity,
                                availableRooms
                        ));
                        log.warn("Loại phòng {} không đủ phòng vào ngày {}. Yêu cầu: {}, Còn: {}",
                                roomTypeId, checkDate, requestedQuantity, availableRooms);
                    } else {
                        log.info("Loại phòng {} còn đủ {} phòng trống vào ngày {} (Yêu cầu: {})",
                                roomTypeId, availableRooms, checkDate, requestedQuantity);
                    }
                } else {
                    // Không tìm thấy dữ liệu - coi như không có phòng
                    allAvailable = false;
                    RoomAvailabilityDetailResponse detail = RoomAvailabilityDetailResponse.builder()
                            .roomTypeId(roomTypeId)
                            .roomTypeName(loaiPhong.getTenLoaiPhong())
                            .date(checkDate)
                            .totalRooms(loaiPhong.getSoLuong())
                            .availableRooms(0)
                            .bookedRooms(loaiPhong.getSoLuong())
                            .requestedQuantity(requestedQuantity)
                            .isAvailable(false)
                            .status(String.format("Không đủ phòng (Còn 0/%d)", requestedQuantity))
                            .build();
                    allDetails.add(detail);

                    messageBuilder.append(String.format(
                            "Loại phòng %s (%s) đã hết phòng vào ngày %s. Yêu cầu: %d phòng. ",
                            loaiPhong.getTenLoaiPhong(),
                            roomTypeId,
                            checkDate,
                            requestedQuantity
                    ));
                }
            }
        }

        String finalMessage = allAvailable
                ? "Tất cả các loại phòng đều đủ số lượng trong khoảng thời gian yêu cầu"
                : messageBuilder.toString().trim();

        log.info("Kết quả kiểm tra: {} - {}", allAvailable ? "Đủ phòng" : "Không đủ phòng", finalMessage);

        return CheckRoomAvailabilityResponse.builder()
                .allAvailable(allAvailable)
                .message(finalMessage)
                .availabilityDetails(allDetails)
                .build();
    }

    /**
     * Kiểm tra phòng trống theo khoảng thời gian (tối ưu hơn cho nhiều ngày liên tiếp)
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public CheckRoomAvailabilityResponse checkRoomAvailabilityRange(
            String roomTypeId, LocalDate startDate, LocalDate endDate) {

        log.info("Kiểm tra phòng trống cho loại phòng {} từ {} đến {}",
                roomTypeId, startDate, endDate);

        // Kiểm tra loại phòng có tồn tại không
        LoaiPhong loaiPhong = roomAvailabilityRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng: " + roomTypeId));

        List<RoomAvailabilityDTO> availabilityList =
                roomAvailabilityRepository.checkRoomAvailabilityRange(roomTypeId, startDate, endDate);

        List<RoomAvailabilityDetailResponse> details = new ArrayList<>();
        boolean allAvailable = true;
        StringBuilder messageBuilder = new StringBuilder();

        // Group by date để xử lý
        Map<LocalDate, RoomAvailabilityDTO> availabilityMap = availabilityList.stream()
                .collect(Collectors.toMap(RoomAvailabilityDTO::getNgay, dto -> dto));

        // Kiểm tra từng ngày trong khoảng
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            RoomAvailabilityDTO availability = availabilityMap.get(currentDate);

            if (availability != null) {
                boolean isAvailable = availability.getSoPhongTrong() > 0;

                RoomAvailabilityDetailResponse detail = RoomAvailabilityDetailResponse.builder()
                        .roomTypeId(availability.getMaLoaiPhong())
                        .roomTypeName(availability.getTenLoaiPhong())
                        .date(availability.getNgay())
                        .totalRooms(availability.getTongSoPhong())
                        .availableRooms(availability.getSoPhongTrong())
                        .bookedRooms(availability.getSoPhongDaDat())
                        .isAvailable(isAvailable)
                        .status(isAvailable ? "Còn phòng" : "Hết phòng")
                        .build();

                details.add(detail);

                if (!isAvailable) {
                    allAvailable = false;
                    messageBuilder.append(String.format(
                            "Loại phòng %s đã hết phòng vào ngày %s. ",
                            availability.getTenLoaiPhong(),
                            availability.getNgay()
                    ));
                }
            } else {
                // Không có dữ liệu cho ngày này - coi như hết phòng
                allAvailable = false;
                RoomAvailabilityDetailResponse detail = RoomAvailabilityDetailResponse.builder()
                        .roomTypeId(roomTypeId)
                        .roomTypeName(loaiPhong.getTenLoaiPhong())
                        .date(currentDate)
                        .totalRooms(loaiPhong.getSoLuong())
                        .availableRooms(0)
                        .bookedRooms(loaiPhong.getSoLuong())
                        .isAvailable(false)
                        .status("Hết phòng")
                        .build();
                details.add(detail);

                messageBuilder.append(String.format(
                        "Loại phòng %s đã hết phòng vào ngày %s. ",
                        loaiPhong.getTenLoaiPhong(),
                        currentDate
                ));
            }

            currentDate = currentDate.plusDays(1);
        }

        String finalMessage = allAvailable
                ? "Tất cả các ngày đều còn phòng trống"
                : messageBuilder.toString().trim();

        return CheckRoomAvailabilityResponse.builder()
                .allAvailable(allAvailable)
                .message(finalMessage)
                .availabilityDetails(details)
                .build();
    }

    /**
     * Lock loại phòng để booking (dùng khi thực hiện booking thực tế)
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public LoaiPhong lockRoomTypeForBooking(String roomTypeId) {
        log.info("Lock loại phòng {} để booking", roomTypeId);
        return roomAvailabilityRepository.findByIdWithLock(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng: " + roomTypeId));
    }

    /**
     * Kiểm tra phòng trống theo khoảng thời gian với số lượng yêu cầu
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public CheckRoomAvailabilityResponse checkRoomAvailabilityRangeWithQuantity(
            String roomTypeId, LocalDate startDate, LocalDate endDate, Integer requestedQuantity) {

        log.info("Kiểm tra phòng trống cho loại phòng {} từ {} đến {} với số lượng: {}",
                roomTypeId, startDate, endDate, requestedQuantity);

        // Kiểm tra loại phòng có tồn tại không
        LoaiPhong loaiPhong = roomAvailabilityRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng: " + roomTypeId));

        List<RoomAvailabilityDTO> availabilityList =
                roomAvailabilityRepository.checkRoomAvailabilityRange(roomTypeId, startDate, endDate);

        List<RoomAvailabilityDetailResponse> details = new ArrayList<>();
        boolean allAvailable = true;
        StringBuilder messageBuilder = new StringBuilder();

        // Group by date để xử lý
        Map<LocalDate, RoomAvailabilityDTO> availabilityMap = availabilityList.stream()
                .collect(Collectors.toMap(RoomAvailabilityDTO::getNgay, dto -> dto));

        // Kiểm tra từng ngày trong khoảng
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            RoomAvailabilityDTO availability = availabilityMap.get(currentDate);

            if (availability != null) {
                int availableRooms = availability.getSoPhongTrong();
                boolean isAvailable = availableRooms >= requestedQuantity;

                RoomAvailabilityDetailResponse detail = RoomAvailabilityDetailResponse.builder()
                        .roomTypeId(availability.getMaLoaiPhong())
                        .roomTypeName(availability.getTenLoaiPhong())
                        .date(availability.getNgay())
                        .totalRooms(availability.getTongSoPhong())
                        .availableRooms(availableRooms)
                        .bookedRooms(availability.getSoPhongDaDat())
                        .requestedQuantity(requestedQuantity)
                        .isAvailable(isAvailable)
                        .status(isAvailable
                                ? String.format("Còn đủ phòng (Còn %d/%d)", availableRooms, requestedQuantity)
                                : String.format("Không đủ phòng (Còn %d/%d)", availableRooms, requestedQuantity))
                        .build();

                details.add(detail);

                if (!isAvailable) {
                    allAvailable = false;
                    messageBuilder.append(String.format(
                            "Loại phòng %s không đủ phòng vào ngày %s. Yêu cầu: %d, Còn: %d. ",
                            availability.getTenLoaiPhong(),
                            availability.getNgay(),
                            requestedQuantity,
                            availableRooms
                    ));
                }
            } else {
                // Không có dữ liệu cho ngày này - coi như hết phòng
                allAvailable = false;
                RoomAvailabilityDetailResponse detail = RoomAvailabilityDetailResponse.builder()
                        .roomTypeId(roomTypeId)
                        .roomTypeName(loaiPhong.getTenLoaiPhong())
                        .date(currentDate)
                        .totalRooms(loaiPhong.getSoLuong())
                        .availableRooms(0)
                        .bookedRooms(loaiPhong.getSoLuong())
                        .requestedQuantity(requestedQuantity)
                        .isAvailable(false)
                        .status(String.format("Không đủ phòng (Còn 0/%d)", requestedQuantity))
                        .build();
                details.add(detail);

                messageBuilder.append(String.format(
                        "Loại phòng %s đã hết phòng vào ngày %s. Yêu cầu: %d. ",
                        loaiPhong.getTenLoaiPhong(),
                        currentDate,
                        requestedQuantity
                ));
            }

            currentDate = currentDate.plusDays(1);
        }

        String finalMessage = allAvailable
                ? "Tất cả các ngày đều đủ phòng theo yêu cầu"
                : messageBuilder.toString().trim();

        return CheckRoomAvailabilityResponse.builder()
                .allAvailable(allAvailable)
                .message(finalMessage)
                .availabilityDetails(details)
                .build();
    }
}
