package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.response.StatisticsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticsRestServiceImpl implements StatisticsRestService {

    private final OrderedQuantityRepository orderedQuantityRepository;

    @Override
    public StatisticsResponseDTO calculatePotentialRevenue(Integer year, Integer month) {
        System.out.println("📊 Calculating potential revenue for year: " + year + ", month: " + month);
        
        // 1. Fetch all OrderedQuantities
        List<OrderedQuantity> allOrderedQuantities = orderedQuantityRepository.findAll();
        
        // 2. Filter by year, month, and business rules
        List<OrderedQuantity> filteredOrderedQuantities = allOrderedQuantities.stream()
                .filter(oq -> {
                    // ✅ Skip soft-deleted OrderedQuantity
                    if (Boolean.TRUE.equals(oq.getIsDeleted())) {
                        return false;
                    }
                    
                    // ✅ Skip if Plan is soft-deleted
                    if (oq.getPlan() != null && Boolean.TRUE.equals(oq.getPlan().getIsDeleted())) {
                        return false;
                    }
                    
                    // ✅ Only count OrderedQuantity from "Processed" packages
                    if (oq.getPlan() != null && oq.getPlan().getTourPackage() != null) {
                        String packageStatus = oq.getPlan().getTourPackage().getStatus();
                        if (!"Processed".equalsIgnoreCase(packageStatus)) {
                            return false;
                        }
                    } else {
                        return false; // Skip if no package relationship
                    }
                    
                    if (oq.getStartDate() == null) return false;
                    
                    int oqYear = oq.getStartDate().getYear();
                    int oqMonth = oq.getStartDate().getMonthValue();
                    
                    // Filter by year
                    if (year != null && oqYear != year) {
                        return false;
                    }
                    
                    // Filter by month (if specified)
                    if (month != null && oqMonth != month) {
                        return false;
                    }
                    
                    return true;
                })
                .toList();
        
        System.out.println("✅ Found " + filteredOrderedQuantities.size() + " ordered quantities after filtering");
        
        // 3. Calculate revenue by activity type
        Map<String, Long> revenueByActivityType = new HashMap<>();
        
        for (OrderedQuantity oq : filteredOrderedQuantities) {
            if (oq.getActivity() == null) continue;
            
            String activityType = oq.getActivity().getActivityType();
            Long revenue = (long) oq.getOrderedQuota() * oq.getPrice();
            
            revenueByActivityType.merge(activityType, revenue, Long::sum);
        }
        
        // 4. Calculate total revenue
        Long totalRevenue = revenueByActivityType.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        
        System.out.println("💰 Total Revenue: Rp " + totalRevenue);
        System.out.println("📈 Revenue by Activity Type: " + revenueByActivityType);
        
        return StatisticsResponseDTO.builder()
                .year(year)
                .month(month)
                .revenueByActivityType(revenueByActivityType)
                .totalRevenue(totalRevenue)
                .build();
    }
}