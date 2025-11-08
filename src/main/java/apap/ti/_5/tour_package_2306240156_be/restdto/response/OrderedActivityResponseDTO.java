package apap.ti._5.tour_package_2306240156_be.restdto.response;

import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderedActivityResponseDTO {
    private UUID id;
    private String activityId;
    private String activityName;
    private int quota;
    private int orderedQuota;
    private Long price;
    private Long total;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID planId;
    private String planName;

    public static OrderedActivityResponseDTO fromEntity(OrderedQuantity oq) {
        return OrderedActivityResponseDTO.builder()
                .id(oq.getId())
                .activityId(oq.getActivity() != null ? oq.getActivity().getId() : null)
                .activityName(oq.getActivity() != null ? oq.getActivity().getActivityName() : null)
                .quota(oq.getQuota())
                .orderedQuota(oq.getOrderedQuota())
                .price(oq.getPrice())
                .total((long) oq.getOrderedQuota() * oq.getPrice())
                .startDate(oq.getStartDate())
                .endDate(oq.getEndDate())
                .planId(oq.getPlan() != null ? oq.getPlan().getId() : null)
                .planName(oq.getPlan() != null ? oq.getPlan().getPlanName() : null)
                .build();
    }
}
