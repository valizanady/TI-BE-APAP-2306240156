package apap.ti._5.tour_package_2306240156_be.restdto.response;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletePackageCreateResponseDTO {
  private String packageId;
  private String packageName;
  private List<CreatedPlan> createdPlans;
  private Long totalPrice;
  private String status;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CreatedPlan {
    private String planId;
    private String planName;
    private String activityType;
    private String status;
    private Long planPrice;
    private int activitiesCount;
    private int totalOrderedQuantity;
  }
}
