package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateOrderedActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.ActivityDTO;

import java.util.List;
import java.util.UUID;

public interface OrderedActivityRestService {
    

    List<ActivityDTO> getEligibleActivities(UUID planId);
    
    OrderedQuantity addActivityToPlan(UUID planId, CreateOrderedActivityRequestDTO request);
    
    OrderedQuantity updateOrderedActivity(UUID orderedActivityId, int newQuantity);
    

    void deleteOrderedActivity(UUID orderedActivityId);
}