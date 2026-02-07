package vttu.khachsanservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

public class HangHoa {
    @Id
    @Column( nullable = false)
    String maHangHoa;
    String tenHangHoa;
    String dvt;
    boolean trangThai;
}
