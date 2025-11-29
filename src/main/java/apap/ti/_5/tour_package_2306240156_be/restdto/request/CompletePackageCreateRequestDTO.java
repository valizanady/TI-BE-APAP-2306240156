package apap.ti._5.tour_package_2306240156_be.restdto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import apap.ti._5.tour_package_2306240156_be.restdto.response.OrderedActivityResponseDTO;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletePackageCreateRequestDTO {
    
    private String packageName;
    private String userId;
    private int quota;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private List<PlanRequest> plans;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanRequest {
        private String planName;
        private String activityType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String startLocation;
        private String endLocation;

        private List<OrderedActivityRequest> orderedActivities;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderedActivityRequest {
        private String activityId;
        private Integer orderedQuantity;
    }
        
}
