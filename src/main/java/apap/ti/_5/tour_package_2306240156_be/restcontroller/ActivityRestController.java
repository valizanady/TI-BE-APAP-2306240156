package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
@RequestMapping("/activities")
public class ActivityRestController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityRestController.class);

    @Autowired
    private ActivityRepository activityRepository;

    /**
     * GET /activities
     * Get all activities with optional filters and search
     * 
     * Default: Shows ALL activities (no isDeleted filter)
     * Default sorting: startDate ascending
     * 
     * @param isDeleted Optional - filter by isDeleted status (null = show all, true = show deleted only, false = show active only)
     * @param activityType Optional - filter by activity type
     * @param startLocation Optional - filter by start location
     * @param endLocation Optional - filter by end location
     * @param startDate Optional - filter by start date
     * @param endDate Optional - filter by end date
     * @param search Optional - case-insensitive search by activity name or activity item
     * @return List of activities matching filters
     */
    @GetMapping
    public ResponseEntity<?> getAllActivities(
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) String startLocation,
            @RequestParam(required = false) String endLocation,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String search) {
        
        try {
            logger.info("🔍 GET /activities - isDeleted: {}, activityType: {}, startLocation: {}, endLocation: {}, search: '{}'", 
                       isDeleted, activityType, startLocation, endLocation, search);

            // 1. Fetch all activities
            List<Activity> activities = activityRepository.findAll();
            
            // 2. Apply filters
            List<Activity> filteredActivities = activities.stream()
                    .filter(activity -> {
                        // ✅ Filter: isDeleted (OPTIONAL)
                        // null (default) → show ALL activities (no filter)
                        // true → show ONLY deleted (isDeleted = true)
                        // false → show ONLY active (isDeleted = false)
                        if (isDeleted != null) {
                            // Filter by specified isDeleted value
                            if (!isDeleted.equals(activity.getIsDeleted())) {
                                return false;
                            }
                        }
                        
                        // Filter: activityType
                        if (activityType != null && !activityType.isEmpty()) {
                            if (activity.getActivityType() == null || 
                                !activity.getActivityType().equalsIgnoreCase(activityType)) {
                                return false;
                            }
                        }
                        
                        // Filter: startLocation
                        if (startLocation != null && !startLocation.isEmpty()) {
                            if (activity.getStartLocation() == null || 
                                !activity.getStartLocation().equalsIgnoreCase(startLocation)) {
                                return false;
                            }
                        }
                        
                        // Filter: endLocation
                        if (endLocation != null && !endLocation.isEmpty()) {
                            if (activity.getEndLocation() == null || 
                                !activity.getEndLocation().equalsIgnoreCase(endLocation)) {
                                return false;
                            }
                        }
                        
                        // Filter: startDate (activity startDate >= filter startDate)
                        if (startDate != null) {
                            if (activity.getStartDate() == null || 
                                activity.getStartDate().isBefore(startDate)) {
                                return false;
                            }
                        }
                        
                        // Filter: endDate (activity endDate <= filter endDate)
                        if (endDate != null) {
                            if (activity.getEndDate() == null || 
                                activity.getEndDate().isAfter(endDate)) {
                                return false;
                            }
                        }
                        
                        // Filter: search (case-insensitive search by activityName or activityItem)
                        if (search != null && !search.isEmpty()) {
                            String searchLower = search.toLowerCase();
                            boolean matchName = activity.getActivityName() != null && 
                                               activity.getActivityName().toLowerCase().contains(searchLower);
                            boolean matchItem = activity.getActivityItem() != null && 
                                               activity.getActivityItem().toLowerCase().contains(searchLower);
                            
                            if (!matchName && !matchItem) {
                                return false;
                            }
                        }
                        
                        return true;
                    })
                    // 3. Sort by startDate ascending (default)
                    .sorted(Comparator.comparing(
                        activity -> activity.getStartDate() != null ? activity.getStartDate() : LocalDateTime.MAX,
                        Comparator.nullsLast(Comparator.naturalOrder())
                    ))
                    .collect(Collectors.toList());
            
            logger.info("✅ Found {} activities after filtering", filteredActivities.size());
            
            return ResponseEntity.ok(java.util.Map.of(
                "status", 200,
                "message", "Activities retrieved successfully",
                "data", filteredActivities,
                "count", filteredActivities.size()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving activities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                        "status", 500,
                        "message", "Failed to retrieve activities: " + e.getMessage()
                    ));
        }
    }
}
