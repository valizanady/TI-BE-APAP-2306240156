package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateOrderedActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.EligibleActivitiesResponseDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/ordered-activities")
public class OrderedActivityRestController {

    private static final Logger logger = LoggerFactory.getLogger(OrderedActivityRestController.class);

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private OrderedQuantityRepository orderedQuantityRepository;

    @GetMapping("/eligible")
    public ResponseEntity<?> getEligibleActivities(@RequestParam("planId") String planId) {
        try {
            logger.info("🔍 Fetching eligible activities for planId: {}", planId);

            Plan plan = planRepository.findById(UUID.fromString(planId))
                    .orElseThrow(() -> new RuntimeException("Plan not found with id: " + planId));

            logger.info("📋 Plan found: activityType={}, startDate={}, endDate={}, startLocation={}, endLocation={}",
                    plan.getActivityType(), plan.getStartDate(), plan.getEndDate(),
                    plan.getStartLocation(), plan.getEndLocation());

            if (plan.getActivityType() == null || plan.getStartDate() == null ||
                    plan.getEndDate() == null || plan.getStartLocation() == null ||
                    plan.getEndLocation() == null) {
                logger.error("❌ Plan has null fields");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Plan has incomplete data");
            }

            List<Activity> eligibleActivities = activityRepository.findEligibleActivitiesForPlan(
                    plan.getActivityType(),
                    plan.getStartDate(),
                    plan.getEndDate(),
                    plan.getStartLocation(),
                    plan.getEndLocation()
            );

            logger.info("✅ Found {} eligible activities", eligibleActivities.size());
            
            EligibleActivitiesResponseDTO response = new EligibleActivitiesResponseDTO(eligibleActivities);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid plan ID format: {}", planId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid plan ID format: " + planId);
        } catch (RuntimeException e) {
            logger.error("❌ Plan not found: {}", planId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error fetching eligible activities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrderedActivity(
            @RequestParam("planId") String planId,
            @RequestBody CreateOrderedActivityRequestDTO request) {
        try {
            logger.info("🔍 Creating ordered activity for planId: {}, activityId: {}, quantity: {}", 
                    planId, request.getActivityId(), request.getOrderedQuantity());

            Plan plan = planRepository.findById(UUID.fromString(planId))
                    .orElseThrow(() -> new RuntimeException("Plan not found with id: " + planId));

            // Validasi: Package status harus "Pending"
            if (plan.getTourPackage() == null) {
                logger.error("❌ Package not found for this plan");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Package Not Found",
                            "message", "Package not found for this plan"
                        ));
            }
            
            if (!"Pending".equals(plan.getTourPackage().getStatus())) {
                logger.error("❌ Cannot create ordered activity. Package status is not Pending: {}", 
                           plan.getTourPackage().getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Invalid Package Status",
                            "message", "Cannot create ordered activity. Package status must be 'Pending', current status: " + 
                                      plan.getTourPackage().getStatus()
                        ));
            }

            Activity activity = activityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new RuntimeException("Activity not found with id: " + request.getActivityId()));

            // ✅ Validasi 1: Activity quota > 0
            if (activity.getCapacity() <= 0) {
                logger.error("❌ Activity quota/capacity must be greater than 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Invalid Quota",
                            "message", "Activity quota must be greater than 0, current: " + activity.getCapacity()
                        ));
            }

            // ✅ Validasi 2: Activity price > 0
            if (activity.getPrice() == null || activity.getPrice() <= 0) {
                logger.error("❌ Activity price must be greater than 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Invalid Price",
                            "message", "Activity price must be greater than 0, current: " + activity.getPrice()
                        ));
            }

            // ✅ Validasi 3: orderedQuota ≥ 0
            if (request.getOrderedQuantity() < 0) {
                logger.error("❌ Ordered quantity cannot be negative");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Invalid Ordered Quantity",
                            "message", "Ordered quantity must be greater than or equal to 0, received: " + request.getOrderedQuantity()
                        ));
            }

            // ✅ Validasi 4: orderedQuota ≤ quota (capacity)
            if (request.getOrderedQuantity() > activity.getCapacity()) {
                logger.error("❌ Ordered quantity exceeds activity capacity");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Capacity Exceeded",
                            "message", "Ordered quantity (" + request.getOrderedQuantity() + 
                                      ") exceeds available capacity (" + activity.getCapacity() + ")"
                        ));
            }

            // ✅ Validasi 5: startDate < endDate
            if (activity.getStartDate() != null && activity.getEndDate() != null) {
                if (!activity.getEndDate().isAfter(activity.getStartDate())) {
                    logger.error("❌ Activity end date must be after start date");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(java.util.Map.of(
                                "error", "Invalid Date Range",
                                "message", "Activity end date must be after start date. Start: " + 
                                          activity.getStartDate() + ", End: " + activity.getEndDate()
                            ));
                }
            }

            // Validasi 2: Total ordered quantity dari PLAN INI tidak melebihi package quota
            // Hitung current total untuk plan ini saja
            int currentPlanTotal = orderedQuantityRepository.findAll().stream()
                    .filter(oq -> oq.getPlan() != null && 
                                  oq.getPlan().getId().equals(plan.getId()) &&
                                  (oq.getIsDeleted() == null || !oq.getIsDeleted()))
                    .mapToInt(OrderedQuantity::getOrderedQuota)
                    .sum();
            
            int newPlanTotal = currentPlanTotal + request.getOrderedQuantity();
            int packageQuota = plan.getTourPackage().getQuota();
            
            logger.info("📊 Plan {} - Current: {}, Adding: {}, New total: {}, Package quota: {}", 
                    plan.getPlanName(), currentPlanTotal, request.getOrderedQuantity(), newPlanTotal, packageQuota);
            
            // Plan bisa punya total OQ sampai = package quota (KASUS B)
            if (newPlanTotal > packageQuota) {
                logger.error("❌ Total ordered quantity for this plan exceeds package quota");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Quota Exceeded",
                            "message", "Total ordered quantity (" + newPlanTotal + 
                                      ") exceeds package quota (" + packageQuota + ")",
                            "currentTotal", currentPlanTotal,
                            "requestedQuantity", request.getOrderedQuantity(),
                            "packageQuota", packageQuota
                        ));
            }

            // ✅ NEW: Check if activity already exists in this plan
            Optional<OrderedQuantity> existingOQ = plan.getOrderedQuantities().stream()
                    .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()) &&
                                 oq.getActivity().getId().equals(request.getActivityId()))
                    .findFirst();

            OrderedQuantity savedOq;
            
            if (existingOQ.isPresent()) {
                // UPDATE existing OrderedQuantity
                OrderedQuantity oq = existingOQ.get();
                int newQuantity = oq.getOrderedQuota() + request.getOrderedQuantity();
                
                // Re-validate with new quantity
                int currentPlanTotalExcludingThis = orderedQuantityRepository.findAll().stream()
                        .filter(o -> o.getPlan() != null && 
                                     o.getPlan().getId().equals(plan.getId()) &&
                                     !o.getId().equals(oq.getId()) &&
                                     (o.getIsDeleted() == null || !o.getIsDeleted()))
                        .mapToInt(OrderedQuantity::getOrderedQuota)
                        .sum();
                
                int updatedPlanTotal = currentPlanTotalExcludingThis + newQuantity;
                
                if (newQuantity > activity.getCapacity()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(java.util.Map.of(
                                "error", "Capacity Exceeded",
                                "message", "Total quantity (" + newQuantity + 
                                          ") exceeds capacity (" + activity.getCapacity() + ")"
                            ));
                }
                
                if (updatedPlanTotal > packageQuota) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(java.util.Map.of(
                                "error", "Quota Exceeded",
                                "message", "Total ordered quantity (" + updatedPlanTotal + 
                                          ") exceeds package quota (" + packageQuota + ")"
                            ));
                }
                
                oq.setOrderedQuota(newQuantity);
                oq.setPrice(activity.getPrice() * newQuantity);
                savedOq = orderedQuantityRepository.save(oq);
                
                logger.info("✅ Updated existing ordered activity, new quantity: {}", newQuantity);
            } else {
                // CREATE new OrderedQuantity (existing logic)
                // ...existing validation code...
                
                if (newPlanTotal > packageQuota) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(java.util.Map.of(
                                "error", "Quota Exceeded",
                                "message", "Total ordered quantity (" + newPlanTotal + 
                                          ") exceeds package quota (" + packageQuota + ")"
                            ));
                }

                OrderedQuantity orderedQuantity = OrderedQuantity.builder()
                        .orderedQuota(request.getOrderedQuantity())
                        .quota(activity.getCapacity())
                        .price(activity.getPrice() * request.getOrderedQuantity())
                        .startDate(activity.getStartDate())
                        .endDate(activity.getEndDate())
                        .isDeleted(false)
                        .plan(plan)
                        .activity(activity)
                        .build();
                
                savedOq = orderedQuantityRepository.save(orderedQuantity);
                logger.info("✅ Created new ordered activity with ID: {}", savedOq.getId());
            }

            // Update plan status
            int finalTotal = orderedQuantityRepository.findAll().stream()
                    .filter(oq -> oq.getPlan() != null && 
                                  oq.getPlan().getId().equals(plan.getId()) &&
                                  (oq.getIsDeleted() == null || !oq.getIsDeleted()))
                    .mapToInt(OrderedQuantity::getOrderedQuota)
                    .sum();
            
            if (finalTotal == packageQuota) {
                plan.setStatus("Fulfilled");
            } else {
                plan.setStatus("Unfulfilled");
            }
            planRepository.save(plan);

            // Return response
            var response = new java.util.HashMap<String, Object>();
            response.put("id", savedOq.getId());
            response.put("orderedQuota", savedOq.getOrderedQuota());
            response.put("quota", savedOq.getActivity().getCapacity());
            response.put("price", savedOq.getPrice());
            response.put("startDate", savedOq.getStartDate());
            response.put("endDate", savedOq.getEndDate());
            response.put("activityId", savedOq.getActivity().getId());
            response.put("activityName", savedOq.getActivity().getActivityName());
            response.put("planId", savedOq.getPlan().getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid ID format", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid ID format");
        } catch (RuntimeException e) {
            logger.error("❌ Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error creating ordered activity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{orderedActivityId}")
    public ResponseEntity<?> updateOrderedActivity(
            @PathVariable("orderedActivityId") String orderedActivityId,
            @RequestParam("quantity") Integer newQuantity) {
        try {
            logger.info("🔄 Updating ordered activity: {}, new quantity: {}", orderedActivityId, newQuantity);

            OrderedQuantity orderedQuantity = orderedQuantityRepository.findById(UUID.fromString(orderedActivityId))
                    .orElseThrow(() -> new RuntimeException("Ordered activity not found"));

            // Validasi: OrderedQuantity belum di-soft delete
            if (Boolean.TRUE.equals(orderedQuantity.getIsDeleted())) {
                logger.error("❌ Cannot update deleted ordered activity");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Already Deleted",
                            "message", "Cannot update deleted ordered activity"
                        ));
            }

            Plan plan = orderedQuantity.getPlan();
            
            // Validasi: Package status harus "Pending"
            if (plan.getTourPackage() == null) {
                logger.error("❌ Package not found for this plan");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Package Not Found",
                            "message", "Package not found for this plan"
                        ));
            }
            
            if (!"Pending".equals(plan.getTourPackage().getStatus())) {
                logger.error("❌ Cannot update ordered activity. Package status is not Pending: {}", 
                           plan.getTourPackage().getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Invalid Package Status",
                            "message", "Cannot update ordered activity. Package status must be 'Pending', current status: " + 
                                      plan.getTourPackage().getStatus()
                        ));
            }
            
            Activity activity = orderedQuantity.getActivity();

            // ✅ Validasi 1: orderedQuota ≥ 0
            if (newQuantity == null || newQuantity < 0) {
                logger.error("❌ New quantity cannot be null or negative");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Invalid Quantity",
                            "message", "New quantity must be greater than or equal to 0, received: " + newQuantity
                        ));
            }

            // ✅ Validasi 2: orderedQuota ≤ quota (capacity)
            if (newQuantity > activity.getCapacity()) {
                logger.error("❌ New quantity exceeds activity capacity");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Capacity Exceeded",
                            "message", "New quantity (" + newQuantity + 
                                      ") exceeds activity capacity (" + activity.getCapacity() + ")"
                        ));
            }

            // Validasi quota (exclude current OQ)
            int currentPlanTotal = orderedQuantityRepository.findAll().stream()
                    .filter(oq -> oq.getPlan() != null && 
                                  oq.getPlan().getId().equals(plan.getId()) &&
                                  !oq.getId().equals(orderedQuantity.getId()) &&
                                  (oq.getIsDeleted() == null || !oq.getIsDeleted()))
                    .mapToInt(OrderedQuantity::getOrderedQuota)
                    .sum();

            int newTotal = currentPlanTotal + newQuantity;
            int packageQuota = plan.getTourPackage().getQuota();

            logger.info("📊 Update validation - Current (excl this): {}, New quantity: {}, New total: {}, Quota: {}", 
                    currentPlanTotal, newQuantity, newTotal, packageQuota);

            if (newTotal > packageQuota) {
                logger.error("❌ Updated quantity would exceed package quota");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Quota Exceeded",
                            "message", "Total ordered quantity (" + newTotal + 
                                      ") exceeds package quota (" + packageQuota + ")",
                            "currentTotal", currentPlanTotal,
                            "requestedQuantity", newQuantity,
                            "packageQuota", packageQuota
                        ));
            }

            // ✅ Update OrderedQuantity untuk menyesuaikan dengan booking yang sudah dilakukan
            // Update ordered quota (jumlah yang dipesan)
            orderedQuantity.setOrderedQuota(newQuantity);
            // Update quota reference dari activity (untuk tracking)
            orderedQuantity.setQuota(activity.getCapacity());
            // Recalculate total price berdasarkan new quantity
            orderedQuantity.setPrice(activity.getPrice() * newQuantity);
            orderedQuantityRepository.save(orderedQuantity);

            logger.info("✅ Updated ordered quantity: {} → {}, Total price: {}", 
                       orderedQuantity.getOrderedQuota(), newQuantity, orderedQuantity.getPrice());

            // Update plan status
            if (newTotal == packageQuota) {
                plan.setStatus("Fulfilled");
            } else {
                plan.setStatus("Unfulfilled");
            }
            planRepository.save(plan);

            logger.info("✅ Ordered activity updated successfully");

            var response = new java.util.HashMap<String, Object>();
            response.put("id", orderedQuantity.getId());
            response.put("orderedQuota", orderedQuantity.getOrderedQuota());
            response.put("quota", activity.getCapacity());
            response.put("price", orderedQuantity.getPrice());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error updating ordered activity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                        "error", "Update Failed",
                        "message", e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{orderedActivityId}")
    public ResponseEntity<?> deleteOrderedActivity(@PathVariable("orderedActivityId") String orderedActivityId) {
        try {
            logger.info("🗑️ Deleting ordered activity: {}", orderedActivityId);

            OrderedQuantity orderedQuantity = orderedQuantityRepository.findById(UUID.fromString(orderedActivityId))
                    .orElseThrow(() -> new RuntimeException("Ordered activity not found"));

            Plan plan = orderedQuantity.getPlan();
            
            // Validasi: Package status harus "Pending"
            if (plan.getTourPackage() == null) {
                logger.error("❌ Package not found for this plan");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Package Not Found",
                            "message", "Package not found for this plan"
                        ));
            }
            
            if (!"Pending".equals(plan.getTourPackage().getStatus())) {
                logger.error("❌ Cannot delete ordered activity. Package status is not Pending: {}", 
                           plan.getTourPackage().getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Invalid Package Status",
                            "message", "Cannot delete ordered activity. Package status must be 'Pending', current status: " + 
                                      plan.getTourPackage().getStatus()
                        ));
            }
            
            // Validasi: OrderedQuantity belum di-soft delete
            if (Boolean.TRUE.equals(orderedQuantity.getIsDeleted())) {
                logger.error("❌ Ordered activity is already deleted");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of(
                            "error", "Already Deleted",
                            "message", "Ordered activity is already deleted"
                        ));
            }

            // Soft delete
            orderedQuantity.setIsDeleted(true);
            orderedQuantityRepository.save(orderedQuantity);

            // Update plan status
            int remainingTotal = orderedQuantityRepository.findAll().stream()
                    .filter(oq -> oq.getPlan() != null && 
                                  oq.getPlan().getId().equals(plan.getId()) &&
                                  (oq.getIsDeleted() == null || !oq.getIsDeleted()))
                    .mapToInt(OrderedQuantity::getOrderedQuota)
                    .sum();

            if (remainingTotal == plan.getTourPackage().getQuota()) {
                plan.setStatus("Fulfilled");
            } else {
                plan.setStatus("Unfulfilled");
            }
            planRepository.save(plan);

            logger.info("✅ Ordered activity deleted successfully");

            return ResponseEntity.ok(java.util.Map.of(
                "message", "Activity removed successfully",
                "deletedId", orderedActivityId
            ));

        } catch (Exception e) {
            logger.error("❌ Error deleting ordered activity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
