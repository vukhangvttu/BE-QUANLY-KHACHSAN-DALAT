package vttu.khachsanservice.dto.xuathoadon;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MetaData {

    String keyTag;
    String stringValue;
    String valueType;
    String keyLabel;
    Boolean isRequired = false;
    Boolean isSeller = false;
}
