package apap.ti._5.tour_package_2306240156_be.restdto.response;

import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDetailResponseDTO {
    private UUID id;
    private String planName;
    private String activityType;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String startLocation;
    private String endLocation;
    private Long price;
    
    // Package info
    private String packageId;
    private String packageName;
    private String packageStatus;
    
    // Ordered quantities
    private List<OrderedQuantityDTO> orderedQuantities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderedQuantityDTO {
        private UUID id;
        private String activityName;
        private String activityId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Long price;
        private int quota;
        private int orderedQuota;
        private Integer remaining; // Add remaining field
        private Long total;

        public static OrderedQuantityDTO fromEntity(OrderedQuantity oq) {
            // Get CURRENT activity capacity (may be reduced after processing)
            int currentCapacity = oq.getActivity() != null ? oq.getActivity().getCapacity() : 0;
            
            return OrderedQuantityDTO.builder()
                    .id(oq.getId())
                    .activityName(oq.getActivity() != null ? oq.getActivity().getActivityName() : "-")
                    .activityId(oq.getActivity() != null ? oq.getActivity().getId() : "-")
                    .startDate(oq.getStartDate())
                    .endDate(oq.getEndDate())
                    .price(oq.getPrice())
                    .quota(currentCapacity + oq.getOrderedQuota()) // Original capacity before this order
                    .orderedQuota(oq.getOrderedQuota())
                    .remaining(currentCapacity) // Remaining after this order
                    .total((long) oq.getOrderedQuota() * oq.getPrice())
                    .build();
        }
    }

    public static PlanDetailResponseDTO fromEntity(Plan plan) {
        // Calculate total price from ordered quantities
        Long totalPrice = 0L;
        List<OrderedQuantityDTO> orderedQuantitiesDTO = new ArrayList<>();
        
        if (plan.getOrderedQuantities() != null && !plan.getOrderedQuantities().isEmpty()) {
            // Filter out soft-deleted ordered quantities
            orderedQuantitiesDTO = plan.getOrderedQuantities().stream()
                    .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                    .map(OrderedQuantityDTO::fromEntity)
                    .collect(Collectors.toList());
            
            totalPrice = orderedQuantitiesDTO.stream()
                    .mapToLong(OrderedQuantityDTO::getTotal)
                    .sum();
        }

        return PlanDetailResponseDTO.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .activityType(plan.getActivityType())
                .status(plan.getStatus())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .startLocation(plan.getStartLocation())
                .endLocation(plan.getEndLocation())
                .price(totalPrice)
                .packageId(plan.getTourPackage() != null ? plan.getTourPackage().getId() : null)
                .packageName(plan.getTourPackage() != null ? plan.getTourPackage().getPackageName() : null)
                .packageStatus(plan.getTourPackage() != null ? plan.getTourPackage().getStatus() : null)
                .orderedQuantities(orderedQuantitiesDTO)
                .build();
    }
}
