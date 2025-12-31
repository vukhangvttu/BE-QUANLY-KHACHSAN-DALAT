package vttu.khachsanservice.dto;

import java.time.LocalDate;

public interface RoomAvailabilityDTO {
    String getMaLoaiPhong();
    String getTenLoaiPhong();
    LocalDate getNgay();
    Integer getTongSoPhong();
    Integer getSoPhongDaDat();
    Integer getSoPhongTrong();
}

