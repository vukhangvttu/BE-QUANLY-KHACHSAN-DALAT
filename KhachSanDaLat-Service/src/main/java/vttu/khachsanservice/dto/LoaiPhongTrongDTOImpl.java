package vttu.khachsanservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoaiPhongTrongDTOImpl implements LoaiPhongTrongDTO {
    private String ngay;
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private int tongSoPhong;
    private int soPhongTrong;
    private long tongSoGiuongThem;
    private long tongSoGiuongDaSuDung;

    @Override
    public String getNgay() {
        return ngay;
    }

    @Override
    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    @Override
    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    @Override
    public int getTongSoPhong() {
        return tongSoPhong;
    }

    @Override
    public int getSoPhongTrong() {
        return soPhongTrong;
    }

    @Override
    public long getTongSoGiuongThem() {
        return tongSoGiuongThem;
    }

    @Override
    public long getTongSoGiuongDaSuDung() {
        return tongSoGiuongDaSuDung;
    }

    public void setSoPhongTrong(int soPhongTrong) {
        this.soPhongTrong = soPhongTrong;
    }
}

