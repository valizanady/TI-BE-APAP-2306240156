package apap.ti._5.tour_package_2306240156_be.restdto.response;

import apap.ti._5.tour_package_2306240156_be.model.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponseDTO {
    private UUID id;
    private String planName;
    private String activityType;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String startLocation;
    private String endLocation;
    private Long price;
    private String packageId;
    private String packageName;
    private String packageStatus;
    private String packageUserId; // Add userId for frontend RBAC
    private String creatorRole; // Add creatorRole for tracking

    public static PlanResponseDTO fromEntity(Plan plan) {
        // Calculate total price from active (non-deleted) ordered quantities
        Long totalPrice = 0L;
        if (plan.getOrderedQuantities() != null && !plan.getOrderedQuantities().isEmpty()) {
            totalPrice = plan.getOrderedQuantities().stream()
                    .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                    .mapToLong(oq -> (long) oq.getOrderedQuota() * oq.getPrice())
                    .sum();
        }
        
        // Get package info if available
        String packageId = null;
        String packageName = null;
        String packageStatus = null;
        String packageUserId = null;
        String creatorRole = null;
        
        if (plan.getTourPackage() != null) {
            packageId = plan.getTourPackage().getId();
            packageName = plan.getTourPackage().getPackageName();
            packageStatus = plan.getTourPackage().getStatus();
            packageUserId = plan.getTourPackage().getUserId();
            creatorRole = plan.getTourPackage().getCreatorRole();
        }
     
        return PlanResponseDTO.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .activityType(plan.getActivityType())
                .status(plan.getStatus())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .startLocation(plan.getStartLocation())
                .endLocation(plan.getEndLocation())
                .price(totalPrice) // ✅ Total dari ordered quantities
                .packageId(packageId)
                .packageName(packageName)
                .packageStatus(packageStatus)
                .packageUserId(packageUserId) // ✅ For frontend RBAC
                .creatorRole(creatorRole) // ✅ For tracking
                .build();
    }
}
