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
        System.out.println("📊 Calculating revenue for year: " + year + ", month: " + month);
        
        // 1. Fetch all OrderedQuantities
        List<OrderedQuantity> allOrderedQuantities = orderedQuantityRepository.findAll();
        
        if (month != null) {
            // ========== CASE 1: Specific Month - Return revenue with breakdown per activityType ==========
            return calculateRevenueForSpecificMonth(allOrderedQuantities, year, month);
        } else {
            // ========== CASE 2: Whole Year - Return revenue per month with breakdown per activityType ==========
            return calculateRevenuePerMonthInYear(allOrderedQuantities, year);
        }
    }
    
    /**
     * Calculate revenue for a specific month with breakdown per activityType
     */
    private StatisticsResponseDTO calculateRevenueForSpecificMonth(
            List<OrderedQuantity> allOrderedQuantities, Integer year, Integer month) {
        
        System.out.println("📅 Calculating for specific month: " + year + "-" + String.format("%02d", month));
        
        // Filter by year, month, and business rules
        List<OrderedQuantity> filteredOrderedQuantities = allOrderedQuantities.stream()
                .filter(oq -> isValidOrderedQuantity(oq, year, month))
                .toList();
        
        System.out.println("✅ Found " + filteredOrderedQuantities.size() + " ordered quantities");
        
        // Calculate revenue by activity type
        Map<String, Long> revenueByActivityType = new HashMap<>();
        
        for (OrderedQuantity oq : filteredOrderedQuantities) {
            if (oq.getActivity() == null) continue;
            
            String activityType = oq.getActivity().getActivityType();
            Long revenue = oq.getPrice();
            
            revenueByActivityType.merge(activityType, revenue, Long::sum);
        }
        
        // Calculate total revenue
        Long totalRevenue = revenueByActivityType.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        
        // Convert Map<String, Long> to Map<String, Object> for breakdown
        Map<String, Object> breakdown = new HashMap<>(revenueByActivityType);
        
        String period = year + "-" + String.format("%02d", month);
        
        System.out.println("💰 Total Revenue: Rp " + totalRevenue);
        System.out.println("📈 Revenue by Activity Type: " + revenueByActivityType);
        
        return StatisticsResponseDTO.builder()
                .period(period)
                .totalRevenue(totalRevenue)
                .breakdown(breakdown)
                .build();
    }
    
    /**
     * Calculate revenue per month in a year with breakdown per activityType for each month
     */
    private StatisticsResponseDTO calculateRevenuePerMonthInYear(
            List<OrderedQuantity> allOrderedQuantities, Integer year) {
        
        System.out.println("📅 Calculating for whole year: " + year);
        
        // Breakdown: {monthName: {activityType: revenue, totalRevenue: xxx}}
        Map<String, Object> breakdown = new HashMap<>();
        long yearTotalRevenue = 0L;
        
        String[] monthNames = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        
        for (int monthNum = 1; monthNum <= 12; monthNum++) {
            final int currentMonth = monthNum;
            
            // Filter for this specific month
            List<OrderedQuantity> monthOrderedQuantities = allOrderedQuantities.stream()
                    .filter(oq -> isValidOrderedQuantity(oq, year, currentMonth))
                    .toList();
            
            // Calculate revenue by activity type for this month
            Map<String, Long> monthRevenueByActivityType = new HashMap<>();
            
            for (OrderedQuantity oq : monthOrderedQuantities) {
                if (oq.getActivity() == null) continue;
                
                String activityType = oq.getActivity().getActivityType();
                Long revenue = oq.getPrice();
                
                monthRevenueByActivityType.merge(activityType, revenue, Long::sum);
            }
            
            // Calculate total for this month
            Long monthTotal = monthRevenueByActivityType.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            
            yearTotalRevenue += monthTotal;
            
            // Build month data
            Map<String, Object> monthData = new HashMap<>(monthRevenueByActivityType);
            monthData.put("totalRevenue", monthTotal);
            
            breakdown.put(monthNames[monthNum - 1], monthData);
            
            System.out.println(monthNames[monthNum - 1] + ": Rp " + monthTotal);
        }
        
        String period = String.valueOf(year);
        
        System.out.println("� Year Total Revenue: Rp " + yearTotalRevenue);
        
        return StatisticsResponseDTO.builder()
                .period(period)
                .totalRevenue(yearTotalRevenue)
                .breakdown(breakdown)
                .build();
    }
    
    /**
     * Validate if OrderedQuantity should be included in revenue calculation
     */
    private boolean isValidOrderedQuantity(OrderedQuantity oq, Integer year, Integer month) {
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
    }
}