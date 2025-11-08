package apap.ti._5.tour_package_2306240156_be.restdto.response;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibleActivitiesResponseDTO {
    private List<Activity> activities;
}
