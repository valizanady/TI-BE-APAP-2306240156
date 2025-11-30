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
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PlanRestService;
import org.springframework.validation.BindingResult;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/package")
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
  
  /**
   * Check if user has access to Package endpoints
   * Only Superadmin, Customer, and TourPackageVendor can access
   */
  private boolean hasPackageAccess(AuthenticatedUser user) {
    String role = user.getRole();
    return "Superadmin".equals(role) || 
           "Customer".equals(role) || 
           "TourPackageVendor".equals(role);
  }

  /**
   * GET /api/package
   * Get all packages with role-based filtering
   * 
   * Access Control:
   * - Customer: Can see packages created by Admin/Vendor + own packages
   * - Superadmin & TourPackageVendor: Can see all packages
   * 
   * @param user AuthenticatedUser from JWT token (auto-injected)
   */
  @GetMapping
  public ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> getAll(
      @AuthenticationPrincipal AuthenticatedUser user) {
    
    // ✅ Check role access
    if (!hasPackageAccess(user)) {
      logger.warn("❌ Access denied: Role {} cannot access package endpoints", user.getRole());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(BaseResponseDTO.<List<PackageResponseDTO>>builder()
              .status(HttpStatus.FORBIDDEN.value())
              .message("Access denied: You do not have permission to access packages")
              .build());
    }
    
    logger.info("🔍 GET /api/package - Fetching packages with role-based filtering");
    logger.info("👤 User: ID={}, Role={}, Email={}", user.getId(), user.getRole(), user.getEmail());
    
    List<PackageResponseDTO> packages;
    
    if (user.hasAdminPrivileges()) {
      // Superadmin & TourPackageVendor: See all packages
      logger.info("✅ Admin/Vendor access: Fetching all packages");
      packages = service.getAll();
    } else {
      // Customer: See packages from admin/vendor + own packages
      logger.info("👥 Customer access: Fetching filtered packages");
      packages = service.getPackagesForCustomer(user.getId());
    }
    
    logger.info("📦 Total packages returned: {}", packages.size());
    var body = new BaseResponseDTO<>(200, "Success", new Date(), packages);
    return ResponseEntity.ok(body);
  }

  /**
   * GET /package/{id}
   * Get package detail by ID with authorization check
   * 
   * Access Control:
   * - Customer: Can view own packages + packages created by Admin/Vendor (based on creatorRole)
   * - Superadmin & TourPackageVendor: Can view all packages
   */
  @GetMapping("/{id}")
  public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> getById(
      @PathVariable String id,
      @AuthenticationPrincipal AuthenticatedUser user) {
    
    // ✅ Check role access
    if (!hasPackageAccess(user)) {
      logger.warn("❌ Access denied: Role {} cannot access package endpoints", user.getRole());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(BaseResponseDTO.<PackageResponseDTO>builder()
              .status(HttpStatus.FORBIDDEN.value())
              .message("Access denied: You do not have permission to access packages")
              .build());
    }
    
    logger.info("🔍 GET /package/{} - Fetching package detail", id);
    logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
    
    // ✅ Use authorization-aware getById
    PackageResponseDTO packageData = service.getById(id, user.getId(), user.getRole());
    
    // Authorization check for Customer role
    if (!user.hasAdminPrivileges()) {
      boolean isOwnPackage = user.getId().equals(packageData.getUserId());
      
      // Check if package is created by Admin/Vendor (based on creatorRole)
      // If creatorRole is null (old data), allow access for backward compatibility
      boolean isAdminVendorPackage = packageData.getCreatorRole() == null 
          || "Superadmin".equals(packageData.getCreatorRole())
          || "TourPackageVendor".equals(packageData.getCreatorRole());
      
      if (!isOwnPackage && !isAdminVendorPackage) {
        // Package is created by another Customer
        logger.warn("❌ Access denied: Customer {} cannot view package {} created by another customer", 
                    user.getId(), id);
        var body = new BaseResponseDTO<PackageResponseDTO>(
          403, 
          "Access denied: You can only view your own packages or packages created by admin/vendor", 
          new Date(), 
          null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
      }
    }
    
    logger.info("✅ Access granted: Package detail retrieved successfully");
    var body = new BaseResponseDTO<>(200, "Success", new Date(), packageData);
    return ResponseEntity.ok(body);
  }

  /**
   * POST /api/package/create
   * Create new package with userId and userRole automatically set from JWT token
   * 
   * @param req Package details (without userId/role - auto-filled from token)
   * @param user AuthenticatedUser from JWT token (auto-injected)
   */
  @PostMapping("/create")
  public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> create(
      @Valid @RequestBody CreatePackageRequestDTO req,
      @AuthenticationPrincipal AuthenticatedUser user) {
    try {
      logger.info("📦 POST /api/package/create - Creating package");
      logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
      
      // Pass userId and userRole from JWT token to service
      var data = service.create(req, user.getId(), user.getRole());
      
      logger.info("✅ Package created successfully: {}", data.getId());
      var body = new BaseResponseDTO<>(201, "Created", new Date(), data);
      return ResponseEntity.status(HttpStatus.CREATED).body(body);
    } catch (IllegalArgumentException e) {
      logger.error("❌ Validation error: {}", e.getMessage());
      var body = new BaseResponseDTO<PackageResponseDTO>(400, e.getMessage(), new Date(), null);
      return ResponseEntity.badRequest().body(body);
    } catch (RuntimeException e) {
      logger.error("❌ Error creating package: {}", e.getMessage());
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
  /**
   * PUT /package/{id}/edit
   * Update package with authorization check
   * 
   * Access Control:
   * - Customer: Can only update own packages
   * - Superadmin & TourPackageVendor: Can update all packages
   */
  @PutMapping("/{id}/edit")
  public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> updatePackage(
      @PathVariable String id,
      @RequestBody @Valid UpdatePackageRequestDTO dto,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
      logger.info("🔍 PUT /package/{}/edit - Updating package", id);
      logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
      
      try {
          // Get existing package to check ownership
          PackageResponseDTO existingPackage = service.getById(id);
          
          // Authorization check
          if (!user.hasAdminPrivileges()) {
              // Customer: Can only update own packages
              if (!existingPackage.getUserId().equals(user.getId())) {
                  logger.warn("❌ Access denied: Customer {} cannot update package {} owned by {}", 
                              user.getId(), id, existingPackage.getUserId());
                  BaseResponseDTO<PackageResponseDTO> body = new BaseResponseDTO<>(
                      403,
                      "Access denied: You can only update your own packages",
                      new Date(),
                      null
                  );
                  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
              }
          }
          
          logger.info("✅ Access granted: Proceeding with package update");
          var updated = service.updatePackage(id, dto);
          var body = new BaseResponseDTO<>(200, "Package updated successfully", new Date(), updated);
          return ResponseEntity.ok(body);
      } catch (IllegalArgumentException e) {
          BaseResponseDTO<PackageResponseDTO> body = new BaseResponseDTO<>(
              400,
              e.getMessage(),
              new Date(),
              null
          );
          return ResponseEntity.badRequest().body(body);
      } catch (RuntimeException e) {
          BaseResponseDTO<PackageResponseDTO> body = new BaseResponseDTO<>(
              400,
              e.getMessage(),
              new Date(),
              null
          );
          return ResponseEntity.badRequest().body(body);
      }
  }

  /**
   * PUT /package/{id}/process
   * Process package (change status from Pending to Processed)
   * 
   * Access Control:
   * - ONLY Customer can process packages
   * - Customer can process ANY package (own, admin's, vendor's) IF all plans are fulfilled
   * - Superadmin & TourPackageVendor CANNOT process packages
   */
  @PutMapping("/{id}/process")
  @SuppressWarnings("null")
  public ResponseEntity<?> processPackage(
      @PathVariable String id,
      @AuthenticationPrincipal AuthenticatedUser user) {
      try {
          logger.info("🔄 Processing package: {}", id);
          logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
          
          // ✅ ONLY Customer can process packages
          if (!"Customer".equals(user.getRole())) {
              logger.warn("❌ Access denied: Role {} cannot process packages", user.getRole());
              return ResponseEntity.status(HttpStatus.FORBIDDEN)
                      .body(java.util.Map.of(
                          "error", "Access denied",
                          "message", "Only customers can process packages"
                      ));
          }
          
          Package tourPackage = packageRepository.findById(id)
                  .orElseThrow(() -> new RuntimeException("Package not found"));
          
          // ✅ Customer can process ANY package (removed ownership check)
          // Customer can process packages created by Admin, Vendor, or other Customers
          logger.info("📦 Package owner: {}, Processor: {}", tourPackage.getUserId(), user.getId());
          logger.info("✅ Authorization: Customer can process any fulfilled package");

          // ✨ Call service method (which includes Bill creation)
          PackageResponseDTO processedPackage = service.processPackage(id);

          logger.info("✅ Package {} processed successfully: Pending → Processed", id);

          return ResponseEntity.ok(java.util.Map.of(
              "message", "Package processed successfully",
              "packageId", id,
              "previousStatus", "Pending",
              "currentStatus", "Processed",
              "package", processedPackage
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

  /**
   * POST /api/package/payment/update
   * Update package payment status (Called by Bill Service via API Key)
   * 
   * Access Control:
   * - This endpoint is ONLY for Bill Service (microservice-to-microservice)
   * - Authenticated via x-api-key header (validated by ApiKeyFilter)
   * - Frontend NEVER calls this endpoint
   * - JWT authentication is BYPASSED for this endpoint
   * 
   * @param request UpdatePaymentStatusRequestDTO containing packageId and status
   */
  @PostMapping("/payment/update")
  public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> updatePaymentStatus(
      @Valid @RequestBody apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePaymentStatusRequestDTO request) {
    
    logger.info("🔔 Payment update request from Bill Service");
    logger.info("   Package ID: {}", request.getPackageId());
    logger.info("   Status: {}", request.getStatus());
    
    try {
      // Call service to update payment status
      PackageResponseDTO response = service.updatePaymentStatus(request.getPackageId(), request.getStatus());
      
      logger.info("✅ Payment status updated successfully for package: {}", request.getPackageId());
      return ResponseEntity.ok()
          .body(new BaseResponseDTO<>(200, "Payment status updated successfully", new Date(), response));
      
    } catch (RuntimeException e) {
      logger.error("❌ Error updating payment status: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(new BaseResponseDTO<>(400, e.getMessage(), new Date(), null));
    }
  }
}

