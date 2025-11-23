package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateActivityRequestDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * GET /activities/{id}
     * Get activity detail by Activity ID
     * 
     * Only shows details for activities with isDeleted = FALSE
     * 
     * @param id Activity ID (UUID string)
     * @return Activity detail or 404 if not found/deleted
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable String id) {
        try {
            logger.info("🔍 GET /activities/{} - Retrieving activity detail", id);

            // 1. Find activity by ID
            var activityOptional = activityRepository.findById(id);
            
            if (activityOptional.isEmpty()) {
                logger.warn("❌ Activity with ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of(
                            "status", 404,
                            "message", "Activity with ID " + id + " not found"
                        ));
            }

            Activity activity = activityOptional.get();

            // 2. Check if activity is deleted
            if (Boolean.TRUE.equals(activity.getIsDeleted())) {
                logger.warn("❌ Activity with ID {} is deleted (isDeleted = true)", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of(
                            "status", 404,
                            "message", "Activity with ID " + id + " not found"
                        ));
            }

            // 3. Return activity detail
            logger.info("✅ Activity with ID {} retrieved successfully", id);
            return ResponseEntity.ok(java.util.Map.of(
                "status", 200,
                "message", "Activity retrieved successfully",
                "data", activity
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving activity detail for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                        "status", 500,
                        "message", "Failed to retrieve activity: " + e.getMessage()
                    ));
        }
    }

    /**
     * POST /activities
     * Create a new activity
     * 
     * Vendor restrictions (TODO: Implement after RBAC):
     * - Tour Package vendor: can create all activity types
     * - Flight vendor: can only create Flight activities
     * - Accommodation vendor: can only create Accommodation activities
     * - Vehicle Rental vendor: can only create Vehicle Rental activities
     * 
     * Validations:
     * - All attributes are required (not null/empty)
     * - ActivityID auto-generated: ACT-{YYYYMMDD}-{XXX}
     * - startDate < endDate
     * - price > 0, capacity > 0
     * - startDate >= now (cannot create activity in the past)
     * 
     * @param request CreateActivityRequestDTO with activity details
     * @return Created activity or error response
     */
    @PostMapping
    public ResponseEntity<?> createActivity(
            @Valid @RequestBody CreateActivityRequestDTO request) {
        
        try {
            logger.info("📝 POST /activities - activityType: {}", request.getActivityType());

            // TODO: Uncomment when RBAC is implemented
            // String vendorType = request header or JWT claim
            // if (!isValidVendorForActivityType(vendorType, request.getActivityType())) {
            //     String errorMsg = String.format(
            //         "Vendor type '%s' is not authorized to create activity type '%s'", 
            //         vendorType, request.getActivityType()
            //     );
            //     logger.warn("❌ {}", errorMsg);
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN)
            //             .body(java.util.Map.of(
            //                 "status", 403,
            //                 "message", errorMsg
            //             ));
            // }

            // 1. Validate startDate < endDate
            if (!request.getStartDate().isBefore(request.getEndDate())) {
                logger.warn("❌ Start date must be before end date");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be before end date"
                        ));
            }

            // 2. Validate startDate >= now (not in the past)
            if (request.getStartDate().isBefore(LocalDateTime.now())) {
                logger.warn("❌ Cannot create activity in the past");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be in the present or future"
                        ));
            }

            // 3. Generate Activity ID: ACT-{YYYYMMDD}-{XXX}
            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String prefix = "ACT-" + dateStr + "-";
            
            long seq = activityRepository.countByIdPrefix(prefix) + 1;
            String activityId = prefix + String.format("%03d", seq);

            // 4. Build and save activity
            Activity activity = Activity.builder()
                    .id(activityId)
                    .activityName(request.getActivityName())
                    .activityItem(request.getActivityItem())
                    .activityType(request.getActivityType())
                    .capacity(request.getCapacity())
                    .price(request.getPrice())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .startLocation(request.getStartLocation())
                    .endLocation(request.getEndLocation())
                    .isDeleted(false)
                    .build();

            Activity savedActivity = activityRepository.save(activity);

            logger.info("✅ Activity created successfully with ID: {}", activityId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(java.util.Map.of(
                        "status", 201,
                        "message", "Activity created successfully",
                        "data", savedActivity
                    ));
            
        } catch (Exception e) {
            logger.error("❌ Error creating activity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                        "status", 500,
                        "message", "Failed to create activity: " + e.getMessage()
                    ));
        }
    }

    // TODO: Uncomment when RBAC is implemented
    /**
     * Validate if vendor type is allowed to create the specified activity type
     * 
     * Rules:
     * - Tour Package: can create all types
     * - Flight: can only create Flight
     * - Accommodation: can only create Accommodation
     * - Vehicle Rental: can only create Vehicle Rental
     */
    // private boolean isValidVendorForActivityType(String vendorType, String activityType) {
    //     // Normalize strings (trim and case-insensitive comparison)
    //     String normalizedVendor = vendorType.trim().toLowerCase();
    //     String normalizedActivityType = activityType.trim().toLowerCase();

    //     // Tour Package vendor can create all types
    //     if (normalizedVendor.equals("tour package")) {
    //         return true;
    //     }

    //     // Other vendors can only create their specific type
    //     return switch (normalizedVendor) {
    //         case "flight" -> normalizedActivityType.equals("flight");
    //         case "accommodation" -> normalizedActivityType.equals("accommodation");
    //         case "vehicle rental" -> normalizedActivityType.equals("vehicle rental");
    //         default -> false; // Unknown vendor type
    //     };
    // }
}
