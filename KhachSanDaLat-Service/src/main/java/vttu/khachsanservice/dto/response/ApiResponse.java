package vttu.khachsanservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private  int code = 1000;
    private  String message;
    private  T result;

    public static class ApiResponseBuilder<T> {
        private int code = 1000;
        private String message;
        private T result;

        public ApiResponseBuilder<T> code(int code) {
            this.code = code;
            return this;
        }

        public ApiResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }
        public ApiResponseBuilder<T> result(T result) {
            this.result = result;
            return this;
        }
        public ApiResponse<T> build(){
            ApiResponse<T> response = new ApiResponse<>();
            response.setCode(this.code);
            response.setMessage(this.message);
            response.setResult(this.result);
            return response;
        }
    }
}
