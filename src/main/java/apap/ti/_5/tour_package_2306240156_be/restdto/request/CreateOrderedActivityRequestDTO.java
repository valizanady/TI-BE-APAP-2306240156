package apap.ti._5.tour_package_2306240156_be.restdto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderedActivityRequestDTO {
    private String activityId;
    private Integer orderedQuantity;
}