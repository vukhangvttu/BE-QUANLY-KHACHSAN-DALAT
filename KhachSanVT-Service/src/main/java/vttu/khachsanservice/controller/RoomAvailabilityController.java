package vttu.khachsanservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.CheckRoomAvailabilityRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.CheckRoomAvailabilityResponse;
import vttu.khachsanservice.service.RoomAvailabilityService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/room-availability")
@RequiredArgsConstructor
@Slf4j

public class RoomAvailabilityController {

    private final RoomAvailabilityService roomAvailabilityService;

    /**
     * API kiểm tra phòng trống cho nhiều loại phòng và nhiều ngày với số lượng
     * POST /api/room-availability/check
     *
     * Request Body:
     * {
     *   "bookingDetails": [
     *     {
     *       "roomTypeId": "CS-C-D",
     *       "quantity": 10,
     *       "listDate": [
     *         { "ngay": "2025-12-26" },
     *         { "ngay": "2025-12-27" },
     *         { "ngay": "2025-12-28" }
     *       ]
     *     }
     *   ]
     * }
     */
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<CheckRoomAvailabilityResponse>> checkRoomAvailability(
            @Valid @RequestBody CheckRoomAvailabilityRequest request) {

        log.info("Nhận yêu cầu kiểm tra phòng trống: {}", request);

        try {
            CheckRoomAvailabilityResponse response = roomAvailabilityService.checkRoomAvailability(request);

            ApiResponse<CheckRoomAvailabilityResponse> apiResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message(response.isAllAvailable()
                            ? "Kiểm tra phòng trống thành công - Đủ phòng"
                            : "Có phòng không đủ số lượng yêu cầu")
                    .result(response)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            log.error("Lỗi khi kiểm tra phòng trống: {}", e.getMessage(), e);

            ApiResponse<CheckRoomAvailabilityResponse> errorResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi: " + e.getMessage())
                    .result(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * API kiểm tra phòng trống cho một loại phòng trong khoảng thời gian với số lượng
     * GET /api/room-availability/check-range?roomTypeId=CS-C-D&startDate=2025-12-26&endDate=2025-12-28&quantity=10
     */
    @GetMapping("/check-range")
    public ResponseEntity<ApiResponse<CheckRoomAvailabilityResponse>> checkRoomAvailabilityRange(
            @RequestParam String roomTypeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false, defaultValue = "1") Integer quantity) {

        log.info("Kiểm tra phòng trống cho loại phòng {} từ {} đến {} với số lượng {}",
                roomTypeId, startDate, endDate, quantity);

        try {
            // Validate dates
            if (startDate.isAfter(endDate)) {
                ApiResponse<CheckRoomAvailabilityResponse> errorResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("Ngày bắt đầu phải trước ngày kết thúc")
                        .result(null)
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate quantity
            if (quantity < 1) {
                ApiResponse<CheckRoomAvailabilityResponse> errorResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("Số lượng phòng phải lớn hơn 0")
                        .result(null)
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            CheckRoomAvailabilityResponse response =
                    roomAvailabilityService.checkRoomAvailabilityRangeWithQuantity(
                            roomTypeId, startDate, endDate, quantity);

            ApiResponse<CheckRoomAvailabilityResponse> apiResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message(response.isAllAvailable()
                            ? "Kiểm tra phòng trống thành công - Đủ phòng"
                            : "Có ngày không đủ phòng theo yêu cầu")
                    .result(response)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            log.error("Lỗi khi kiểm tra phòng trống: {}", e.getMessage(), e);

            ApiResponse<CheckRoomAvailabilityResponse> errorResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi: " + e.getMessage())
                    .result(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * API kiểm tra nhanh một ngày với số lượng
     * GET /api/room-availability/check-date?roomTypeId=CS-C-D&date=2025-12-26&quantity=5
     */
    @GetMapping("/check-date")
    public ResponseEntity<ApiResponse<CheckRoomAvailabilityResponse>> checkRoomAvailabilityDate(
            @RequestParam String roomTypeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false, defaultValue = "1") Integer quantity) {

        log.info("Kiểm tra phòng trống cho loại phòng {} vào ngày {} với số lượng {}",
                roomTypeId, date, quantity);

        try {
            // Validate quantity
            if (quantity < 1) {
                ApiResponse<CheckRoomAvailabilityResponse> errorResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("Số lượng phòng phải lớn hơn 0")
                        .result(null)
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            CheckRoomAvailabilityResponse response =
                    roomAvailabilityService.checkRoomAvailabilityRangeWithQuantity(
                            roomTypeId, date, date, quantity);

            ApiResponse<CheckRoomAvailabilityResponse> apiResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message(response.isAllAvailable()
                            ? "Còn đủ phòng"
                            : "Không đủ phòng")
                    .result(response)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            log.error("Lỗi khi kiểm tra phòng trống: {}", e.getMessage(), e);

            ApiResponse<CheckRoomAvailabilityResponse> errorResponse = ApiResponse.<CheckRoomAvailabilityResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi: " + e.getMessage())
                    .result(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

