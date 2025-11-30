package apap.ti._5.tour_package_2306240156_be.restdto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreatePackageRequestDTO {

    @NotBlank(message = "Package name is required")
    private String packageName;

    @Positive(message = "Quota must be > 0")
    private int quota;

    @NotNull(message = "Start date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
}
