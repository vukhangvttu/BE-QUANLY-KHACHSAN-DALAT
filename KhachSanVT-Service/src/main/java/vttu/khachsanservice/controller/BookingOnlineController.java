package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vttu.khachsanservice.dto.request.BooKingOnlineRequest;
import vttu.khachsanservice.dto.request.BooKingRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.BooKingResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.BooKingOnlineService;


@RestController
@RequestMapping("/booking-online")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingOnlineController {

    BooKingOnlineService booKingService;

    @PostMapping
    ApiResponse<BooKingResponse> create(@RequestBody BooKingOnlineRequest booKingRequest) {

        try {

            return ApiResponse.<BooKingResponse>builder()
                    .result(booKingService.createBooKing(booKingRequest))
                    .message("Booking được tạo thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<BooKingResponse>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<BooKingResponse>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(BooKingController.class).error("Error tạo booking", e);

            return ApiResponse.<BooKingResponse>builder()
                    .message("Thêm không thành công.Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
