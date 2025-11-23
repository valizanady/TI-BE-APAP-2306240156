package apap.ti._5.tour_package_2306240156_be.restdto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateActivityRequestDTO {

    @NotBlank(message = "Activity name is required")
    private String activityName;

    @NotBlank(message = "Activity item is required")
    private String activityItem;

    @NotBlank(message = "Activity type is required")
    private String activityType;

    @Positive(message = "Capacity must be > 0")
    private int capacity;

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be > 0")
    private Long price;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    @NotBlank(message = "Start location is required")
    private String startLocation;

    @NotBlank(message = "End location is required")
    private String endLocation;
}
