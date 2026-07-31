package vttu.khachsanservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import vttu.khachsanservice.dto.request.XepPhongBooking_DichVuMienPhiRequest;
import vttu.khachsanservice.dto.response.ApiResponse;
import vttu.khachsanservice.dto.response.XepPhongBooking_DichVuMienPhiResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.service.XepPhongBooking_DichVuMienPhiService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/chi-tiet-booking-dich-vu-mien-phi")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XepPhongBooking_DichVuMienPhiController {

    XepPhongBooking_DichVuMienPhiService xepPhongBookingDichVuMienPhiService;

    @PostMapping("/{ma_xepphong}")
    ApiResponse<Boolean> saveDichVuMienPhi(@PathVariable String ma_xepphong, @RequestBody List<XepPhongBooking_DichVuMienPhiRequest> request, @RequestParam LocalDate ngay) {


        try {

            return ApiResponse.<Boolean>builder()
                    .result(xepPhongBookingDichVuMienPhiService.createdDichVuMienPhi(ma_xepphong, request, ngay))
                    .message("Cập nhât dịch vụ miễn phí thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<Boolean>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<Boolean>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(ChuyenPhongController.class).error("Error chuyển phòng", e);

            return ApiResponse.<Boolean>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
    @GetMapping("/{ma_xepphong}")
    ApiResponse<List<XepPhongBooking_DichVuMienPhiResponse>> getLichSuDichVuMienPhi(@PathVariable String ma_xepphong) {
        try {

            return ApiResponse.<List<XepPhongBooking_DichVuMienPhiResponse>>builder()
                    .result(xepPhongBookingDichVuMienPhiService.xepPhongBookingDichVuMienPhiResponses(ma_xepphong))
                    .message("Cập nhât dịch vụ miễn phí thành công")
                    .code(HttpStatus.OK.value())
                    .build();
        } catch (AccessDeniedException e) {
            return ApiResponse.<List<XepPhongBooking_DichVuMienPhiResponse>>builder()
                    .message("Bạn không có quyền thực hiện thao tác này")
                    .code(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .build();
        } catch (AppException e) {
            // Handle custom exceptions (e.g., validation or business logic errors)
            return ApiResponse.<List<XepPhongBooking_DichVuMienPhiResponse>>builder()
                    .message(e.getMessage())
                    .code(HttpStatus.BAD_REQUEST.value()) // For client-side errors
                    .build();
        } catch (Exception e) {
            // Log unexpected errors
            LoggerFactory.getLogger(ChuyenPhongController.class).error("Error chuyển phòng", e);

            return ApiResponse.<List<XepPhongBooking_DichVuMienPhiResponse>>builder()
                    .message("Internal Server Error!")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // For server-side errors
                    .build();
        }
    }
}
