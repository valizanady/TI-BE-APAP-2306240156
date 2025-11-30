package apap.ti._5.tour_package_2306240156_be.restdto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PackageResponseDTO {
  private String id;
  private String userId;
  private String creatorRole;  // Role of creator (Customer, Superadmin, TourPackageVendor)
  private String packageName;
  private int quota;
  private Long price;
  private String status;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private List<PlanResponseDTO> plans;
  
  // Authorization metadata
  private Boolean canViewPlans;      // Can current user see plan details?
  private Boolean canProcess;        // Can current user process this package?
  private String accessMessage;      // Message about access restrictions
}
