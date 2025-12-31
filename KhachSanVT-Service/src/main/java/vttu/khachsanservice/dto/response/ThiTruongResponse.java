package vttu.khachsanservice.dto.response;

import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vttu.khachsanservice.entity.BooKing;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThiTruongResponse {
    int maThiTruong;
    String tenThiTruong;
    String tenTienAnh;

//    List<BooKing> booKings = new ArrayList<>();
}
