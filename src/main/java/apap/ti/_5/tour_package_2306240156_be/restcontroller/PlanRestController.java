package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PlanDetailResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PlanRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/plans")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PlanRestController {

    private final PlanRestService planRestService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanDetail(@PathVariable UUID id) {
        System.out.println("🎯 Received GET request: /plans/" + id);

        try {
            Plan plan = planRestService.getPlanById(id);

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
            response.put("price", totalPrice);
            response.put("packageId", plan.getTourPackage().getId());
            response.put("packageName", plan.getTourPackage().getPackageName());
            response.put("packageStatus", plan.getTourPackage().getStatus());
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
            BindingResult bindingResult) {

        System.out.println("🎯 Received PUT request: /plans/" + id + "/edit");
        System.out.println("📦 Request body: " + request);

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            System.out.println("❌ Validation error: " + errorMessage);
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<PlanResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(errorMessage)
                            .build());
        }

        try {
            Plan plan = planRestService.updatePlan(id, request);
            PlanResponseDTO response = PlanResponseDTO.fromEntity(plan);

            System.out.println("✅ Plan updated successfully: " + plan.getId());
            return ResponseEntity.ok()
                    .body(BaseResponseDTO.<PlanResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Plan updated successfully")
                            .data(response)
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
    public ResponseEntity<BaseResponseDTO<Void>> deletePlan(@PathVariable UUID id) {
        System.out.println("🎯 Received DELETE request: /plans/" + id);
        
        try {
            planRestService.deletePlan(id);
            
            System.out.println("✅ Plan deleted successfully: " + id);
            return ResponseEntity.ok()
                    .body(BaseResponseDTO.<Void>builder()
                            .status(HttpStatus.OK.value())
                            .message("Plan deleted successfully")
                            .build());
                            
        } catch (RuntimeException e) {
            System.out.println("❌ Error deleting plan: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<Void>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .build());
        }
    }
}