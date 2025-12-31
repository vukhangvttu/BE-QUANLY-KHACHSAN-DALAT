package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.GiaPhongTheoThoiGian;
import vttu.khachsanservice.dto.response.GiaPhongResponse;
import vttu.khachsanservice.dto.response.GiaPhongTheoNgayReponse;
import vttu.khachsanservice.entity.GiaPhong;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.GiaPhongMapper;
import vttu.khachsanservice.mapper.GiaPhongTheoNgayMapper;
import vttu.khachsanservice.repository.GiaPhongRepository;
import vttu.khachsanservice.repository.GiaPhongTheoNgayRepository;
import vttu.khachsanservice.repository.LoaiPhongRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GiaPhongService {

    GiaPhongRepository giaPhongRepository;
    private final LoaiPhongRepository loaiPhongRepository;
    GiaPhongMapper giaPhongMapper;
    private final GiaPhongTheoNgayRepository giaPhongTheoNgayRepository;
    GiaPhongTheoNgayMapper giaPhongTheoNgayMapper;

    public Map<String, Object> getGiaPhongExtra(String ma_loaiphong) {
        loaiPhongRepository.findById(ma_loaiphong)
                .orElseThrow(() -> new AppException(ErrorCode.LOAI_PHONG_NOT_FOUND));
        return giaPhongRepository.getGiaGiuongExtra(ma_loaiphong);
    }

    public List<GiaPhongTheoThoiGian> getAllGiaPhongTheoThoiGian() {
        return giaPhongRepository.getAllGiaTheoThoiGian();
    }

    public List<GiaPhongResponse> getGiaPhongByMaLoaiPhong(String maLoaiPhong) {
        return giaPhongRepository.findByMaLoaiPhong(maLoaiPhong).stream()
                .map(giaPhongMapper::toGiaPhongResponse)
                .toList();
    }

    public List<GiaPhongTheoNgayReponse> getGiaPhongTheoNgay(String maXepPhongBooking) {
        return giaPhongTheoNgayRepository.findByMaXepPhongBooking(maXepPhongBooking)
                .stream()
                .map(giaPhongTheoNgayMapper::toGiaPhongTheoNgayReponse)
                .toList();
    }
}
