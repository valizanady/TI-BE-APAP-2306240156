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

    public static PlanResponseDTO fromEntity(Plan plan) {
        // Calculate total price from active (non-deleted) ordered quantities
        Long totalPrice = 0L;
        if (plan.getOrderedQuantities() != null && !plan.getOrderedQuantities().isEmpty()) {
            totalPrice = plan.getOrderedQuantities().stream()
                    .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                    .mapToLong(oq -> (long) oq.getOrderedQuota() * oq.getPrice())
                    .sum();
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
                .packageId(
                    plan.getTourPackage() != null ? plan.getTourPackage().getId() : null
                )
                .build();
    }
}
