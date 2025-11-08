package apap.ti._5.tour_package_2306240156_be.restdto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsRequestDTO {
    
    private Integer year;
    
    private Integer month; // null = all months
}