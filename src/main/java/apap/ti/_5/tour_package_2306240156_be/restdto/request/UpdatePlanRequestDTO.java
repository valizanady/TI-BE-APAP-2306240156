package apap.ti._5.tour_package_2306240156_be.restdto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePlanRequestDTO {
  // ✅ All fields are optional for partial update
  // Only fields that are provided will be updated
  
  private String planName;
  
  private LocalDateTime startDate;
  
  private LocalDateTime endDate;
  
  @Min(value = 1, message = "Price must be greater than 0")
  private Long price;
  
  private String startLocation;
  
  private String endLocation;
}
