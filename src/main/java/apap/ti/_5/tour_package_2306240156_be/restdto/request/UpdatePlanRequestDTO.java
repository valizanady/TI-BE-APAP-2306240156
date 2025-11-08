package apap.ti._5.tour_package_2306240156_be.restdto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePlanRequestDTO {
  @NotBlank
  private String planName;

  @NotNull
  private LocalDateTime startDate;

  @NotNull
  private LocalDateTime endDate;

  @NotBlank
  private String startLocation;

  @NotBlank
  private String endLocation;
}
