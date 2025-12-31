package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.LoaiPhongTrongDTO;
import vttu.khachsanservice.dto.response.LoaiPhongResponse;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.LoaiPhongMapper;
import vttu.khachsanservice.repository.LoaiPhongRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoaiPhongService {

    LoaiPhongRepository loaiPhongRepository;
    LoaiPhongMapper loaiPhongMapper;

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

        // Kiểm tra ngày đến không phải trong quá khứ
//        LocalDate today = LocalDate.now();
//        if (ngayDen.isBefore(today)) {
//            throw new AppException(ErrorCode.ERROR_NGAYDEN, "không thể là ngày trong quá khứ!");
//        }

        // Gọi repository lấy danh sách phòng
        List<LoaiPhongTrongDTO> dsPhongTrong = loaiPhongRepository.xemDuBaoPhong(ngayDen, ngayDi);

        // Kiểm tra nếu danh sách trống
        if (dsPhongTrong.isEmpty()) {
            System.out.println("Không có loại phòng trống trong khoảng thời gian đã chọn.");
        }

        return dsPhongTrong;
    }

}
