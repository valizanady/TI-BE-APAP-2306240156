package apap.ti._5.tour_package_2306240156_be.restdto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePlanRequestDTO {
  @NotBlank(message = "Plan name is required")
  private String planName;

  @NotNull(message = "Start date is required")
  private LocalDateTime startDate;

  @NotNull(message = "End date is required")
  private LocalDateTime endDate;

  @NotNull(message = "Price is required")
  @Min(value = 1, message = "Price must be greater than 0")
  private Long price;

  @NotBlank(message = "Start location is required")
  private String startLocation;

  @NotBlank(message = "End location is required")
  private String endLocation;
}
