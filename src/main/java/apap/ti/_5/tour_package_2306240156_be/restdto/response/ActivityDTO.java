package apap.ti._5.tour_package_2306240156_be.restdto.response;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDTO {
    private String id;
    private String activityName;
    private String activityItem;
    private int capacity;
    private Long price;
    private String activityType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String startLocation;
    private String endLocation;

    public static ActivityDTO fromEntity(Activity activity) {
        return ActivityDTO.builder()
                .id(activity.getId())
                .activityName(activity.getActivityName())
                .activityItem(activity.getActivityItem())
                .capacity(activity.getCapacity())
                .price(activity.getPrice())
                .activityType(activity.getActivityType())
                .startDate(activity.getStartDate())
                .endDate(activity.getEndDate())
                .startLocation(activity.getStartLocation())
                .endLocation(activity.getEndLocation())
                .build();
    }
}
