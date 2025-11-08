package apap.ti._5.tour_package_2306240156_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponseDTO {
    
    private Integer year;
    
    private Integer month; // null = all months
    
    private Map<String, Long> revenueByActivityType;
    
    private Long totalRevenue;
}