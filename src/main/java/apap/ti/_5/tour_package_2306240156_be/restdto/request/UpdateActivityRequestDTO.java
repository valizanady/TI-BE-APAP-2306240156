package apap.ti._5.tour_package_2306240156_be.restdto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateActivityRequestDTO {

    // All fields are OPTIONAL for PATCH-style update
    // Only fields that are provided will be updated
    
    private String activityName;

    private String activityItem;

    @Positive(message = "Capacity must be > 0")
    private Integer capacity; // Changed to Integer (nullable)

    @Min(value = 1, message = "Price must be > 0")
    private Long price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime endDate;

    private String startLocation;

    private String endLocation;
    
    // Note: ActivityType is NOT included - cannot be changed after creation
}
