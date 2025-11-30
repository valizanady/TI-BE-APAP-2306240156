package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdateActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
@RequestMapping("/api/activities")
public class ActivityRestController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityRestController.class);

    @Autowired
    private ActivityRepository activityRepository;

    /**
     * GET /api/activities
     * Get all activities with optional filters and search
     * 
     * Default: Shows ALL activities (no isDeleted filter)
     * Default sorting: startDate ascending
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

            List<Activity> activities = activityRepository.findAll();

            List<Activity> filteredActivities = activities.stream()
                    .filter(activity -> {
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
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable String id) {
        try {
            logger.info("🔍 GET /activities/{} - Retrieving activity detail", id);
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
            if (Boolean.TRUE.equals(activity.getIsDeleted())) {
                logger.warn("❌ Activity with ID {} is deleted (isDeleted = true)", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of(
                            "status", 404,
                            "message", "Activity with ID " + id + " not found"
                        ));
            }

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
     * POST /api/activities
     * Create a new activity
     * 
     * RBAC: Only vendors (Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor) can create activities
     * 
     * VendorId is automatically set from JWT token - frontend does NOT send userId!
     * 
     */
    @PostMapping
    public ResponseEntity<?> createActivity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateActivityRequestDTO request) {
        
        try {
            logger.info("📝 POST /api/activities - activityType: {}", request.getActivityType());
            logger.info("👤 User: ID={}, Role={}, Email={}", user.getId(), user.getRole(), user.getEmail());
            if (!user.isVendor() && !user.isSuperadmin()) {
                logger.warn("❌ Access denied: Role {} cannot create activities", user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of(
                            "status", 403,
                            "message", "Access denied: Only vendors can create activities"
                        ));
            }
            if (!request.getStartDate().isBefore(request.getEndDate())) {
                logger.warn("❌ Start date must be before end date");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be before end date"
                        ));
            }
            if (request.getStartDate().isBefore(LocalDateTime.now())) {
                logger.warn("❌ Cannot create activity in the past");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be in the present or future"
                        ));
            }
            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String prefix = "ACT-" + dateStr + "-";
            
            long seq = activityRepository.countByIdPrefix(prefix) + 1;
            String activityId = prefix + String.format("%03d", seq);

            Activity activity = Activity.builder()
                    .id(activityId)
                    .vendorId(user.getVendorId()) // ✅ Automatically from JWT token!
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

    /**
     * PUT /api/activities/{id}
     * Update an existing activity
     * 
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id,
            @Valid @RequestBody UpdateActivityRequestDTO request) {
        
        try {
            logger.info("🔄 PUT /api/activities/{} - Updating activity", id);
            logger.info("👤 User: ID={}, Role={}, Email={}", user.getId(), user.getRole(), user.getEmail());
            if (!user.isVendor() && !user.isSuperadmin()) {
                logger.warn("❌ Access denied: Role {} cannot update activities", user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of(
                            "status", 403,
                            "message", "Access denied: Only vendors can update activities"
                        ));
            }

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
            if (Boolean.TRUE.equals(activity.getIsDeleted())) {
                logger.warn("❌ Cannot update deleted activity (isDeleted = true)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Cannot update activity with isDeleted = TRUE"
                        ));
            }

            if (!user.isSuperadmin()) {
                if (activity.getVendorId() == null || !activity.getVendorId().equals(user.getId())) {
                    logger.warn("❌ Access denied: Vendor {} cannot update activity created by {}", 
                                user.getId(), activity.getVendorId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(java.util.Map.of(
                                "status", 403,
                                "message", "Access denied: You can only update activities you created"
                            ));
                }
            }

            if (activity.getOrderedQuantities() != null && !activity.getOrderedQuantities().isEmpty()) {
                boolean hasFulfilledOrders = activity.getOrderedQuantities().stream()
                        .anyMatch(oq -> {
                            if (Boolean.TRUE.equals(oq.getIsDeleted())) {
                                return false; // Skip deleted orders
                            }
                            if (oq.getPlan() == null || oq.getPlan().getTourPackage() == null) {
                                return false;
                            }
                            String pkgStatus = oq.getPlan().getTourPackage().getStatus();
                            // Consider "Fulfilled" or "Processed" packages as fulfilled
                            return "Fulfilled".equalsIgnoreCase(pkgStatus) || 
                                   "Processed".equalsIgnoreCase(pkgStatus);
                        });
                
                if (hasFulfilledOrders) {
                    logger.warn("❌ Cannot update activity with fulfilled orderedActivities");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(java.util.Map.of(
                                "status", 400,
                                "message", "Cannot update activity that has fulfilled orderedActivities"
                            ));
                }
            }

            LocalDateTime newStartDate = request.getStartDate() != null ? request.getStartDate() : activity.getStartDate();
            LocalDateTime newEndDate = request.getEndDate() != null ? request.getEndDate() : activity.getEndDate();
            
            if (!newStartDate.isBefore(newEndDate)) {
                logger.warn("❌ Start date must be before end date");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be before end date"
                        ));
            }

            if (request.getStartDate() != null && request.getStartDate().isBefore(LocalDateTime.now())) {
                logger.warn("❌ Cannot set activity start date in the past");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be in the present or future"
                        ));
            }

            if (request.getActivityName() != null) {
                activity.setActivityName(request.getActivityName());
            }
            if (request.getActivityItem() != null) {
                activity.setActivityItem(request.getActivityItem());
            }
            if (request.getPrice() != null) {
                activity.setPrice(request.getPrice());
            }
            if (request.getCapacity() != null) {
                activity.setCapacity(request.getCapacity());
            }
            if (request.getStartDate() != null) {
                activity.setStartDate(request.getStartDate());
            }
            if (request.getEndDate() != null) {
                activity.setEndDate(request.getEndDate());
            }
            if (request.getStartLocation() != null) {
                activity.setStartLocation(request.getStartLocation());
            }
            if (request.getEndLocation() != null) {
                activity.setEndLocation(request.getEndLocation());
            }

            Activity updatedActivity = activityRepository.save(activity);

            logger.info("✅ Activity {} updated successfully", id);
            return ResponseEntity.ok()
                    .body(java.util.Map.of(
                        "status", 200,
                        "message", "Activity updated successfully",
                        "data", updatedActivity
                    ));
            
        } catch (Exception e) {
            logger.error("❌ Error updating activity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                        "status", 500,
                        "message", "Failed to update activity: " + e.getMessage()
                    ));
        }
    }

    /**
     * DELETE /api/activities/{id}
     * Soft delete an activity (set isDeleted = TRUE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id) {
        try {
            logger.info("🗑️ DELETE /api/activities/{} - Soft deleting activity", id);
            logger.info("👤 User: ID={}, Role={}, Email={}", user.getId(), user.getRole(), user.getEmail());

            if (!user.isVendor() && !user.isSuperadmin()) {
                logger.warn("❌ Access denied: Role {} cannot delete activities", user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of(
                            "status", 403,
                            "message", "Access denied: Only vendors can delete activities"
                        ));
            }

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
            if (Boolean.TRUE.equals(activity.getIsDeleted())) {
                logger.warn("❌ Activity is already deleted (isDeleted = true)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Activity is already deleted"
                        ));
            }
            if (!user.isSuperadmin()) {
                if (activity.getVendorId() == null || !activity.getVendorId().equals(user.getId())) {
                    logger.warn("❌ Access denied: Vendor {} cannot delete activity created by {}", 
                                user.getId(), activity.getVendorId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(java.util.Map.of(
                                "status", 403,
                                "message", "Access denied: You can only delete activities you created"
                            ));
                }
            }

            if (activity.getOrderedQuantities() != null && !activity.getOrderedQuantities().isEmpty()) {
                boolean hasUnfulfilledOrders = activity.getOrderedQuantities().stream()
                        .anyMatch(oq -> {
                            if (Boolean.TRUE.equals(oq.getIsDeleted())) {
                                return false; // Skip deleted orders
                            }
                            if (oq.getPlan() == null || oq.getPlan().getTourPackage() == null) {
                                return false;
                            }
                            String pkgStatus = oq.getPlan().getTourPackage().getStatus();
                            // Unfulfilled if status is NOT "Processed"
                            // Statuses: "Pending", "Fulfilled", "Processed"
                            // Only "Processed" means the order is completely fulfilled
                            return !"Processed".equalsIgnoreCase(pkgStatus);
                        });
                
                if (hasUnfulfilledOrders) {
                    logger.warn("❌ Cannot delete activity with unfulfilled orderedActivities");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(java.util.Map.of(
                                "status", 400,
                                "message", "Cannot delete activity that has unfulfilled orderedActivities. Only activities with no orders or only fulfilled orders can be deleted."
                            ));
                }
            }

            activity.setIsDeleted(true);
            activityRepository.save(activity);

            logger.info("✅ Activity {} soft deleted successfully (isDeleted = true)", id);
            return ResponseEntity.ok()
                    .body(java.util.Map.of(
                        "status", 200,
                        "message", "Activity deleted successfully (soft delete)",
                        "data", activity
                    ));
            
        } catch (Exception e) {
            logger.error("❌ Error deleting activity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                        "status", 500,
                        "message", "Failed to delete activity: " + e.getMessage()
                    ));
        }
    }
}
