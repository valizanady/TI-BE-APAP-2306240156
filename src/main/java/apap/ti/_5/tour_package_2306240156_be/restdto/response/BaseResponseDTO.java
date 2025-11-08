package apap.ti._5.tour_package_2306240156_be.restdto.response ;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BaseResponseDTO<T> {
    private int status;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Jakarta")
    private Date timestamp;

    private T data;
}
