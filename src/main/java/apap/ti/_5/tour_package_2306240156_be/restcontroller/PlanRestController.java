package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PlanRestService;
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanRestController {

    private static final Logger logger = LoggerFactory.getLogger(PlanRestController.class);
    private final PlanRestService planRestService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        
        logger.info("🎯 GET /plans/{} - Fetching plan detail", id);
        
        // ✅ DEBUG: Check if user is null
        if (user == null) {
            logger.error("❌ AuthenticatedUser is NULL - SecurityContext not set properly");
            var errorResponse = new java.util.HashMap<String, Object>();
            errorResponse.put("status", 401);
            errorResponse.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
        logger.info("👤 User object: {}", user);

        try {
            Plan plan = planRestService.getPlanById(id);
            
            // ✅ Authorization check for Customer
            if (!user.hasAdminPrivileges()) {
                String packageUserId = plan.getTourPackage().getUserId();
                
                logger.info("🔍 Authorization check: packageUserId={}, currentUserId={}", 
                           packageUserId, user.getId());
                
                if (packageUserId == null || !packageUserId.equals(user.getId())) {
                    logger.warn("❌ Access denied: Customer {} cannot view plan {} from package owned by {}", 
                                user.getId(), id, packageUserId);
                    var errorResponse = new java.util.HashMap<String, Object>();
                    errorResponse.put("status", 403);
                    errorResponse.put("message", "Access denied: You can only view plans from your own packages");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
                }
            }
            
            logger.info("✅ Access granted: Plan detail retrieved successfully");

            // Build ordered quantities response dengan quota dari activity.capacity
            var orderedQuantitiesResponse = plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .map(oq -> {
                    var oqMap = new java.util.HashMap<String, Object>();
                    oqMap.put("id", oq.getId());
                    oqMap.put("activityName", oq.getActivity().getActivityName());
                    oqMap.put("activityId", oq.getActivity().getId());
                    oqMap.put("startDate", oq.getStartDate());
                    oqMap.put("endDate", oq.getEndDate());
                    oqMap.put("price", oq.getPrice());
                    oqMap.put("quota", oq.getActivity().getCapacity()); // ✅ FIX: Use activity.capacity
                    oqMap.put("orderedQuota", oq.getOrderedQuota());
                    oqMap.put("remaining", oq.getActivity().getCapacity());
                    oqMap.put("total", oq.getPrice() * oq.getOrderedQuota());
                    return oqMap;
                })
                .toList();
            
            // Calculate total price
            Long totalPrice = orderedQuantitiesResponse.stream()
                    .mapToLong(oq -> ((Number) oq.get("total")).longValue())
                    .sum();
            
            // Prepare response
            var response = new java.util.HashMap<String, Object>();
            response.put("id", plan.getId());
            response.put("planName", plan.getPlanName());
            response.put("activityType", plan.getActivityType());
            response.put("status", plan.getStatus());
            response.put("startDate", plan.getStartDate());
            response.put("endDate", plan.getEndDate());
            response.put("startLocation", plan.getStartLocation());
            response.put("endLocation", plan.getEndLocation());
            response.put("price", totalPrice); // Total calculated price for display
            response.put("planPrice", plan.getPrice()); // ✅ Original plan price for edit
            response.put("packageId", plan.getTourPackage().getId());
            response.put("packageName", plan.getTourPackage().getPackageName());
            response.put("packageStatus", plan.getTourPackage().getStatus());
            response.put("packageUserId", plan.getTourPackage().getUserId()); // ✅ Add for RBAC
            response.put("orderedQuantities", orderedQuantitiesResponse);

            System.out.println("📦 Package Status: " + plan.getTourPackage().getStatus());
            System.out.println("✅ Plan detail retrieved successfully: " + plan.getId());
            
            // ✅ FIX: Use HashMap instead of Map.of() to allow null values
            var responseWrapper = new java.util.HashMap<String, Object>();
            responseWrapper.put("status", 200);
            responseWrapper.put("message", "Plan detail retrieved successfully");
            responseWrapper.put("timestamp", null);
            responseWrapper.put("data", response);
            
            return ResponseEntity.ok().body(responseWrapper);

        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            
            var errorResponse = new java.util.HashMap<String, Object>();
            errorResponse.put("status", 404);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/{id}/edit") // ✅ FIX: Remove duplicate "/plans"
    public ResponseEntity<BaseResponseDTO<PlanDetailResponseDTO>> getEditPlanForm(
            @PathVariable UUID id) {

        System.out.println("🎯 Received GET request: /plans/" + id + "/edit");

        try {
            Plan plan = planRestService.getPlanById(id);
            PlanDetailResponseDTO response = PlanDetailResponseDTO.fromEntity(plan);

            // Debug: Print package status
            System.out.println("📦 Package Status: " + response.getPackageStatus());
            System.out.println("📦 Package Name: " + response.getPackageName());
            System.out.println("📋 Ordered Quantities Count: " + 
                (response.getOrderedQuantities() != null ? response.getOrderedQuantities().size() : 0));
            System.out.println("✅ Plan edit form data retrieved: " + plan.getId());
            
            return ResponseEntity.ok()
                    .body(BaseResponseDTO.<PlanDetailResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Plan data retrieved for editing")
                            .data(response)
                            .build());

        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponseDTO.<PlanDetailResponseDTO>builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/{id}/edit") // ✅ FIX: Remove duplicate "/plans"
    public ResponseEntity<BaseResponseDTO<PlanResponseDTO>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUser user,
            BindingResult bindingResult) {

        logger.info("🎯 PUT /plans/{}/edit - Updating plan", id);
        logger.info("� User: ID={}, Role={}", user.getId(), user.getRole());

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            logger.error("❌ Validation error: {}", errorMessage);
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<PlanResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(errorMessage)
                            .build());
        }

        try {
            // ✅ Authorization check BEFORE update
            Plan existingPlan = planRestService.getPlanById(id);
            
            if (!user.hasAdminPrivileges()) {
                String packageUserId = existingPlan.getTourPackage().getUserId();
                
                if (packageUserId == null || !packageUserId.equals(user.getId())) {
                    logger.warn("❌ Access denied: Customer {} cannot update plan {} from package owned by {}", 
                                user.getId(), id, packageUserId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(BaseResponseDTO.<PlanResponseDTO>builder()
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .message("Access denied: You can only update plans from your own packages")
                                    .build());
                }
            }
            
            Plan plan = planRestService.updatePlan(id, request);
            PlanResponseDTO response = PlanResponseDTO.fromEntity(plan);

            logger.info("✅ Plan updated successfully: {}", plan.getId());
            return ResponseEntity.ok()
                    .body(BaseResponseDTO.<PlanResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Plan updated successfully")
                            .data(response)
                            .build());

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Validation error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<PlanResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            System.out.println("❌ Business logic error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<PlanResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/{id}") // ✅ Already correct
    public ResponseEntity<BaseResponseDTO<Void>> deletePlan(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        
        logger.info("🎯 DELETE /plans/{} - Deleting plan", id);
        logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
        
        try {
            // ✅ Authorization check BEFORE delete
            Plan existingPlan = planRestService.getPlanById(id);
            
            if (!user.hasAdminPrivileges()) {
                String packageUserId = existingPlan.getTourPackage().getUserId();
                
                if (packageUserId == null || !packageUserId.equals(user.getId())) {
                    logger.warn("❌ Access denied: Customer {} cannot delete plan {} from package owned by {}", 
                                user.getId(), id, packageUserId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(BaseResponseDTO.<Void>builder()
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .message("Access denied: You can only delete plans from your own packages")
                                    .build());
                }
            }
            
            planRestService.deletePlan(id);
            
            logger.info("✅ Plan deleted successfully: {}", id);
            return ResponseEntity.ok()
                    .body(BaseResponseDTO.<Void>builder()
                            .status(HttpStatus.OK.value())
                            .message("Plan deleted successfully")
                            .build());
                            
        } catch (RuntimeException e) {
            logger.error("❌ Error deleting plan: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<Void>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        }
    }
}