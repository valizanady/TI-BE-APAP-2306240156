// restcontroller/PackageRestController.java
package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PackageRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PlanRestService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/package")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")

@RequiredArgsConstructor
public class PackageRestController {
  private static final Logger logger = LoggerFactory.getLogger(PackageRestController.class);
  
  private final PackageRestService service;
  
  @Autowired
  private PackageRepository packageRepository;
  
  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private PlanRestService planRestService;

  @GetMapping
  public ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> getAll() {
    var body = new BaseResponseDTO<>(200, "Success", new Date(), service.getAll());
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> getById(@PathVariable String id) {
    var body = new BaseResponseDTO<>(200, "Success", new Date(), service.getById(id));
    return ResponseEntity.ok(body);
  }

  @PostMapping("/create")
  public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> create(@Valid @RequestBody CreatePackageRequestDTO req) {
    try {
      var data = service.create(req);
      var body = new BaseResponseDTO<>(201, "Created", new Date(), data);
      return ResponseEntity.status(HttpStatus.CREATED).body(body);
    } catch (IllegalArgumentException e) {
      var body = new BaseResponseDTO<PackageResponseDTO>(400, e.getMessage(), new Date(), null);
      return ResponseEntity.badRequest().body(body);
    } catch (RuntimeException e) {
      var body = new BaseResponseDTO<PackageResponseDTO>(500, "An error occurred: " + e.getMessage(), new Date(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
  }

  @DeleteMapping("/{id}/delete")
  public ResponseEntity<?> deletePackage(@PathVariable String id) {
      try {
          var deleted = service.deleteById(id);
          var body = new BaseResponseDTO<>(200, "Package deleted successfully", new Date(), deleted);
          return ResponseEntity.ok(body);
      } catch (RuntimeException e) {
          var body = new BaseResponseDTO<>(400, e.getMessage(), new Date(), null);
          return ResponseEntity.badRequest().body(body);
      }
  }
  @PutMapping("/{id}/edit")
  public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> updatePackage(
      @PathVariable String id,
      @RequestBody @Valid UpdatePackageRequestDTO dto
  ) {
      try {
          var updated = service.updatePackage(id, dto);
          var body = new BaseResponseDTO<>(200, "Package updated successfully", new Date(), updated);
          return ResponseEntity.ok(body);
      } catch (RuntimeException e) {
          // ✅ Fixed: Pastikan tipe generic konsisten
          BaseResponseDTO<PackageResponseDTO> body = new BaseResponseDTO<>(
              400,
              e.getMessage(),
              new Date(),
              null
          );
          return ResponseEntity.badRequest().body(body);
      }
  }

  @PutMapping("/{id}/process")
  public ResponseEntity<?> processPackage(@PathVariable String id) {
      try {
          logger.info("🔄 Processing package: {}", id);
          
          Package tourPackage = packageRepository.findById(id)
                  .orElseThrow(() -> new RuntimeException("Package not found"));

          if ("Processed".equals(tourPackage.getStatus())) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                      .body(java.util.Map.of(
                          "error", "Package already processed",
                          "message", "This package has already been processed"
                      ));
          }

          if (!"Pending".equals(tourPackage.getStatus())) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                      .body(java.util.Map.of(
                          "error", "Invalid status",
                          "message", "Only packages with 'Pending' status can be processed"
                      ));
          }

          // Get all plans for this package
          List<Plan> plans = tourPackage.getPlans();
          
          for (Plan plan : plans) {
              if (plan.getIsDeleted() != null && plan.getIsDeleted()) continue;
              
              // Get all ordered quantities for this plan
              List<OrderedQuantity> orderedQuantities = plan.getOrderedQuantities();
              
              for (OrderedQuantity oq : orderedQuantities) {
                  if (oq.getIsDeleted() != null && oq.getIsDeleted()) continue;
                  
                  Activity activity = oq.getActivity();
                  
                  // Kurangi capacity dengan ordered quota
                  int oldCapacity = activity.getCapacity();
                  int newCapacity = oldCapacity - oq.getOrderedQuota();
                  
                  if (newCapacity < 0) {
                      logger.error("❌ Activity {} would have negative capacity", activity.getId());
                      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                              .body(java.util.Map.of(
                                  "error", "Insufficient capacity",
                                  "message", "Activity '" + activity.getActivityName() + 
                                           "' has insufficient capacity (" + oldCapacity + 
                                           ") for ordered quantity (" + oq.getOrderedQuota() + ")"
                              ));
                  }
                  
                  activity.setCapacity(newCapacity);
                  activityRepository.save(activity);
                  
                  logger.info("✅ Updated activity {} capacity: {} -> {}", 
                          activity.getActivityName(), oldCapacity, newCapacity);
              }
          }

          // Update package status from Pending to Processed
          tourPackage.setStatus("Processed");
          packageRepository.save(tourPackage);

          logger.info("✅ Package {} processed successfully: Pending → Processed", id);

          return ResponseEntity.ok(java.util.Map.of(
              "message", "Package processed successfully",
              "packageId", id,
              "previousStatus", "Pending",
              "currentStatus", "Processed"
          ));

      } catch (Exception e) {
          logger.error("❌ Error processing package", e);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(java.util.Map.of(
                      "error", "Processing failed",
                      "message", e.getMessage()
                  ));
      }
  }

  @PostMapping("/{id}/plans/create")
  public ResponseEntity<BaseResponseDTO<PlanResponseDTO>> createPlan(
          @PathVariable String id,
          @Valid @RequestBody CreatePlanRequestDTO request,
          BindingResult bindingResult) {

      logger.info("🎯 Creating plan for package: {}", id);

      if (bindingResult.hasErrors()) {
          String errorMessage = bindingResult.getAllErrors().stream()
                  .map(error -> error.getDefaultMessage())
                  .collect(Collectors.joining(", "));
          logger.error("❌ Validation error: {}", errorMessage);
          return ResponseEntity.badRequest()
                  .body(new BaseResponseDTO<>(400, errorMessage, new Date(), null));
      }

      try {
          Plan plan = planRestService.createPlan(id, request);
          PlanResponseDTO response = PlanResponseDTO.fromEntity(plan);

          logger.info("✅ Plan created successfully: {}", plan.getId());
          return ResponseEntity.status(HttpStatus.CREATED)
                  .body(new BaseResponseDTO<>(201, "Plan created successfully", new Date(), response));

      } catch (RuntimeException e) {
          logger.error("❌ Error creating plan: {}", e.getMessage());
          return ResponseEntity.badRequest()
                  .body(new BaseResponseDTO<>(400, e.getMessage(), new Date(), null));
      }
  }

  @GetMapping("/{id}/plans/create")
  public ResponseEntity<BaseResponseDTO<String>> showCreatePlanForm(@PathVariable String id) {
      logger.info("🎯 Show create plan form for package: {}", id);
      return ResponseEntity.ok()
              .body(new BaseResponseDTO<>(200, "Create plan form endpoint", new Date(), 
                      "Ready to create plan for package: " + id));
  }
}
