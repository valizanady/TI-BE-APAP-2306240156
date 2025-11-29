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
     * POST /api/activities
     * Create a new activity
     * 
     * RBAC: Only vendors (Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor) can create activities
     * 
     * VendorId is automatically set from JWT token - frontend does NOT send userId!
     * 
     * Validations:
     * - All attributes are required (not null/empty)
     * - ActivityID auto-generated: ACT-{YYYYMMDD}-{XXX}
     * - startDate < endDate
     * - price > 0, capacity > 0
     * - startDate >= now (cannot create activity in the past)
     * 
     * @param user AuthenticatedUser from JWT token (auto-injected by Spring Security)
     * @param request CreateActivityRequestDTO with activity details
     * @return Created activity or error response
     */
    @PostMapping
    public ResponseEntity<?> createActivity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateActivityRequestDTO request) {
        
        try {
            logger.info("📝 POST /api/activities - activityType: {}", request.getActivityType());
            logger.info("👤 User: ID={}, Role={}, Email={}", user.getId(), user.getRole(), user.getEmail());
            
            // 1. Check if user has vendor role (RBAC)
            if (!user.isVendor() && !user.isSuperadmin()) {
                logger.warn("❌ Access denied: Role {} cannot create activities", user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of(
                            "status", 403,
                            "message", "Access denied: Only vendors can create activities"
                        ));
            }

            // 2. Validate startDate < endDate
            if (!request.getStartDate().isBefore(request.getEndDate())) {
                logger.warn("❌ Start date must be before end date");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be before end date"
                        ));
            }

            // 3. Validate startDate >= now (not in the past)
            if (request.getStartDate().isBefore(LocalDateTime.now())) {
                logger.warn("❌ Cannot create activity in the past");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be in the present or future"
                        ));
            }

            // 4. Generate Activity ID: ACT-{YYYYMMDD}-{XXX}
            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String prefix = "ACT-" + dateStr + "-";
            
            long seq = activityRepository.countByIdPrefix(prefix) + 1;
            String activityId = prefix + String.format("%03d", seq);

            // 5. Build and save activity
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
     * RBAC:
     * - Only Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor can update
     * - Vendor can only update activities they created (vendorId match)
     * - Superadmin can update all activities
     * 
     * Restrictions:
     * - Can only update activities with isDeleted = FALSE
     * - Cannot change activityType after creation
     * - Cannot update if activity has fulfilled orderedActivities
     * 
     * Validations:
     * - price > 0, capacity > 0
     * - startDate < endDate
     * - startDate >= now (cannot set activity in the past)
     * 
     * @param user AuthenticatedUser from JWT token (auto-injected)
     * @param id Activity ID
     * @param request UpdateActivityRequestDTO with new values
     * @return Updated activity or error response
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id,
            @Valid @RequestBody UpdateActivityRequestDTO request) {
        
        try {
            logger.info("🔄 PUT /api/activities/{} - Updating activity", id);
            logger.info("👤 User: ID={}, Role={}, Email={}", user.getId(), user.getRole(), user.getEmail());
            
            // 1. Check if user has vendor role
            if (!user.isVendor() && !user.isSuperadmin()) {
                logger.warn("❌ Access denied: Role {} cannot update activities", user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of(
                            "status", 403,
                            "message", "Access denied: Only vendors can update activities"
                        ));
            }

            // 2. Find activity
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
            
            // 3. Check if activity is deleted
            if (Boolean.TRUE.equals(activity.getIsDeleted())) {
                logger.warn("❌ Cannot update deleted activity (isDeleted = true)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Cannot update activity with isDeleted = TRUE"
                        ));
            }
            
            // 4. Authorization check: Vendor can only update own activities
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
            
            // 5. Check if activity has fulfilled orderedActivities
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

            // 6. Validate startDate < endDate (if both provided or either is being updated)
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

            // 7. Validate startDate >= now (not in the past) - only if startDate is being updated
            if (request.getStartDate() != null && request.getStartDate().isBefore(LocalDateTime.now())) {
                logger.warn("❌ Cannot set activity start date in the past");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Start date must be in the present or future"
                        ));
            }

            // 8. Update activity fields conditionally (PATCH-style - only update provided fields)
            // ActivityType is NOT updated
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
     * 
     * RBAC:
     * - Only Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor can delete
     * - Vendor can only delete activities they created (vendorId match)
     * - Superadmin can delete all activities
     * 
     * Restrictions:
     * - Activity cannot be deleted if it has unfulfilled orderedActivities
     * - Activity can be deleted if:
     *   1. It has NO orderedActivities
     *   2. It has ONLY fulfilled orderedActivities (Package status = "Processed")
     * 
     * Soft Delete Implications:
     * - Activities with isDeleted = TRUE cannot be updated
     * - Activities with isDeleted = TRUE cannot be used for new packages
     * - Activities with isDeleted = TRUE are filtered out from getAll() by default
     * 
     * @param user AuthenticatedUser from JWT token (auto-injected)
     * @param id Activity ID
     * @return Success message or error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id) {
        try {
            logger.info("🗑️ DELETE /api/activities/{} - Soft deleting activity", id);
            logger.info("👤 User: ID={}, Role={}, Email={}", user.getId(), user.getRole(), user.getEmail());
            
            // 1. Check if user has vendor role
            if (!user.isVendor() && !user.isSuperadmin()) {
                logger.warn("❌ Access denied: Role {} cannot delete activities", user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of(
                            "status", 403,
                            "message", "Access denied: Only vendors can delete activities"
                        ));
            }

            // 2. Find activity
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
            
            // 3. Check if activity is already deleted
            if (Boolean.TRUE.equals(activity.getIsDeleted())) {
                logger.warn("❌ Activity is already deleted (isDeleted = true)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "status", 400,
                            "message", "Activity is already deleted"
                        ));
            }
            
            // 4. Authorization check: Vendor can only delete own activities
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
            
            // 5. Check if activity has unfulfilled orderedActivities
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

            // 6. Soft delete: Set isDeleted = TRUE
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
