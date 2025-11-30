// restservice/TourPackageRestServiceImpl.java
package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BillResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PlanResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourPackageRestServiceImpl implements PackageRestService {
  private final PackageRepository repo;
  private final ActivityRepository activityRepository;
  private final BillIntegrationService billIntegrationService;

  private PackageResponseDTO map(Package p) {
    // Calculate total package price from active plans
    Long totalPackagePrice = 0L;
    if (p.getPlans() != null && !p.getPlans().isEmpty()) {
      totalPackagePrice = p.getPlans().stream()
          .filter(plan -> !Boolean.TRUE.equals(plan.getIsDeleted())) // Only active plans
          .mapToLong(plan -> {
            // Calculate plan price from active ordered quantities
            if (plan.getOrderedQuantities() == null || plan.getOrderedQuantities().isEmpty()) {
              return 0L;
            }
            return plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted())) // Only active ordered quantities
                .mapToLong(oq -> (long) oq.getOrderedQuota() * oq.getPrice())
                .sum();
          })
          .sum();
    }
    
    return PackageResponseDTO.builder()
        .id(p.getId())
        .userId(p.getUserId())
        .creatorRole(p.getCreatorRole())  // ✅ Include creator role for authorization
        .packageName(p.getPackageName())
        .quota(p.getQuota())
        .price(totalPackagePrice) // ✅ Calculate from plans
        .status(p.getStatus())
        .startDate(p.getStartDate())
        .endDate(p.getEndDate())
        .build();
  }

  public List<PackageResponseDTO> getAll() {
      return repo.findAllActive().stream().map(this::map).toList();
  }

  @Override
  public List<PackageResponseDTO> getPackagesForCustomer(String userId) {
      // Customer melihat:
      // 1. Package milik sendiri (semua status)
      // 2. Package yang dibuat oleh Superadmin atau TourPackageVendor (berdasarkan creatorRole)
      return repo.findAllActive().stream()
          .filter(pkg -> {
              String pkgUserId = pkg.getUserId();
              String creatorRole = pkg.getCreatorRole();
              
              // Show own package
              boolean isOwnPackage = pkgUserId != null && pkgUserId.equals(userId);
              
              // Show packages created by Admin/Vendor (berdasarkan creatorRole)
              // If creatorRole is null (old data), assume it's admin/vendor package (backward compatibility)
              boolean isAdminVendorPackage = creatorRole == null 
                  || "Superadmin".equals(creatorRole) 
                  || "TourPackageVendor".equals(creatorRole);
              
              return isOwnPackage || isAdminVendorPackage;
          })
          .map(this::map)
          .toList();
  }

  @Override
  public PackageResponseDTO getById(String id) {
    var pkg = repo.findById(id).orElseThrow(() -> new RuntimeException("Package not found"));
    
    // Map plans and calculate their prices from OrderedQuantity
    var plans = pkg.getPlans() == null ? List.<PlanResponseDTO>of()
        : pkg.getPlans().stream()
            .filter(plan -> !Boolean.TRUE.equals(plan.getIsDeleted()))  // Filter soft-deleted plans
            .map(PlanResponseDTO::fromEntity) // ✅ Use fromEntity to calculate plan price
            .toList();

    // Calculate total package price from all active plans
    Long totalPackagePrice = plans.stream()
        .mapToLong(plan -> plan.getPrice() != null ? plan.getPrice() : 0L)
        .sum();

    return PackageResponseDTO.builder()
        .id(pkg.getId())
        .userId(pkg.getUserId())
        .packageName(pkg.getPackageName())
        .quota(pkg.getQuota())
        .price(totalPackagePrice) // ✅ Total dari semua plan
        .status(pkg.getStatus())
        .startDate(pkg.getStartDate())
        .endDate(pkg.getEndDate())
        .plans(plans)
        .build();
  }

  @Override
  public PackageResponseDTO getById(String id, String userId, String userRole) {
    var pkg = repo.findById(id).orElseThrow(() -> new RuntimeException("Package not found"));
    
    // ✅ Authorization check: Customer can only see Plans from their own packages
    boolean isOwner = userId != null && userId.equals(pkg.getUserId());
    boolean isAdminOrVendor = "Superadmin".equals(userRole) || "TourPackageVendor".equals(userRole);
    boolean canSeePlans = isOwner || isAdminOrVendor;
    
    System.out.println("🔍 Authorization check for package " + id);
    System.out.println("   Package owner: " + pkg.getUserId());
    System.out.println("   Current user: " + userId + " (Role: " + userRole + ")");
    System.out.println("   Can see plans: " + canSeePlans);
    
    // Map plans only if authorized
    var plans = !canSeePlans ? List.<PlanResponseDTO>of() // ✅ Hide plans if not authorized
        : (pkg.getPlans() == null ? List.<PlanResponseDTO>of()
            : pkg.getPlans().stream()
                .filter(plan -> !Boolean.TRUE.equals(plan.getIsDeleted()))
                .map(PlanResponseDTO::fromEntity)
                .toList());

    // Calculate total package price from all active plans (even if user can't see details)
    Long totalPackagePrice = pkg.getPlans() == null ? 0L
        : pkg.getPlans().stream()
            .filter(plan -> !Boolean.TRUE.equals(plan.getIsDeleted()))
            .mapToLong(plan -> {
                if (plan.getOrderedQuantities() == null) return 0L;
                return plan.getOrderedQuantities().stream()
                    .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                    .mapToLong(oq -> (long) oq.getOrderedQuota() * oq.getPrice())
                    .sum();
            })
            .sum();

    // ✅ Check if package can be processed (all plans fulfilled)
    boolean allPlansFulfilled = pkg.getPlans() != null && !pkg.getPlans().isEmpty() &&
        pkg.getPlans().stream()
            .filter(plan -> !Boolean.TRUE.equals(plan.getIsDeleted()))
            .allMatch(plan -> "Fulfilled".equals(plan.getStatus()));
    
    boolean canProcess = "Pending".equals(pkg.getStatus()) && allPlansFulfilled;
    
    // Generate access message
    String accessMessage = null;
    if (!canSeePlans && "Customer".equals(userRole)) {
        String creatorName = pkg.getCreatorRole() != null ? pkg.getCreatorRole() : "Admin/Vendor";
        accessMessage = "This package was created by " + creatorName + ". Plan details are not visible to you, but you can process it if all plans are fulfilled.";
    }

    return PackageResponseDTO.builder()
        .id(pkg.getId())
        .userId(pkg.getUserId())
        .creatorRole(pkg.getCreatorRole())
        .packageName(pkg.getPackageName())
        .quota(pkg.getQuota())
        .price(totalPackagePrice)
        .status(pkg.getStatus())
        .startDate(pkg.getStartDate())
        .endDate(pkg.getEndDate())
        .plans(plans) // ✅ Empty list if not authorized
        .canViewPlans(canSeePlans)
        .canProcess(canProcess)
        .accessMessage(accessMessage)
        .build();
  }

  @Override
  @SuppressWarnings("null")
  public PackageResponseDTO create(CreatePackageRequestDTO req, String userId, String userRole) {
    LocalDateTime now = LocalDateTime.now();
    
    // Validasi: quota harus > 0
    if (req.getQuota() <= 0) {
      throw new IllegalArgumentException("Quota must be greater than 0");
    }
    
    // Validasi: startDate harus >= now
    if (req.getStartDate().isBefore(now)) {
      throw new IllegalArgumentException("Start date cannot be earlier than current date and time");
    }
    
    // Validasi: endDate harus > startDate
    if (req.getEndDate().isBefore(req.getStartDate()) || req.getEndDate().isEqual(req.getStartDate())) {
      throw new IllegalArgumentException("End date must be after start date");
    }

    // Generate ID dengan format PKG-{YYYYMMDD}-{XXX}
    String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String prefix = "PKG-" + dateStr + "-";
    
    // Count packages dengan prefix yang sama hari ini
    long seq = repo.countByIdPrefix(prefix) + 1;
    String id = prefix + String.format("%03d", seq);

    var entity = Package.builder()
        .id(id)
        .userId(userId)  // ✅ Use userId from JWT token (passed from controller)
        .creatorRole(userRole)  // ✅ Store creator's role for authorization
        .packageName(req.getPackageName())
        .quota(req.getQuota())
        .price(0L)
        .status("Pending")   // ✅ Status awal langsung Pending
        .startDate(req.getStartDate())
        .endDate(req.getEndDate())
        .build();

    var saved = repo.save(entity);
    return map(saved);
  }

  @Transactional
  @Override
  public PackageResponseDTO deleteById(String id) {
      var pkg = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Package not found"));

      // ✅ Only allow delete if status is Pending
      if (!"Pending".equalsIgnoreCase(pkg.getStatus())) {
          throw new RuntimeException("Cannot delete package with status: " + pkg.getStatus() + ". Only Pending packages can be deleted.");
      }

      pkg.setStatus("DELETED");
      repo.save(pkg);

      return map(pkg);
  }

  @Override
  public PackageResponseDTO updatePackage(String id, UpdatePackageRequestDTO dto) {
      var pkg = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Package not found"));

      LocalDateTime now = LocalDateTime.now();

      // ✅ Validasi: quota harus > 0
      if (dto.getQuota() <= 0) {
          throw new IllegalArgumentException("Quota must be greater than 0");
      }

      // ✅ Validasi: startDate harus >= now
      if (dto.getStartDate().isBefore(now)) {
          throw new IllegalArgumentException("Start date cannot be earlier than current date and time");
      }

      // ✅ Validasi: endDate harus > startDate
      if (dto.getEndDate().isBefore(dto.getStartDate()) || dto.getEndDate().isEqual(dto.getStartDate())) {
          throw new IllegalArgumentException("End date must be after start date");
      }

      // ✅ Only allow update if status is Pending AND no active plans
      if (!"Pending".equalsIgnoreCase(pkg.getStatus())) {
          throw new RuntimeException("Package cannot be updated. Only packages with status 'Pending' can be edited.");
      }
      
      boolean hasPlans = pkg.getPlans() != null && 
                         pkg.getPlans().stream()
                            .anyMatch(plan -> !Boolean.TRUE.equals(plan.getIsDeleted()));
      
      if (hasPlans) {
          throw new RuntimeException("Package cannot be updated because it already has active plans.");
      }

      pkg.setPackageName(dto.getPackageName());
      pkg.setQuota(dto.getQuota());
      pkg.setStartDate(dto.getStartDate());
      pkg.setEndDate(dto.getEndDate());

      repo.save(pkg);
      return getById(id); // reuse mapper
  }

  @Transactional
  @Override
  public PackageResponseDTO processPackage(String id, String authenticatedCustomerId) {
      System.out.println("🔄 Processing package: " + id);
      System.out.println("👤 Authenticated Customer ID: " + authenticatedCustomerId);
      
      // 1. Find Package
      var pkg = repo.findById(id)
          .orElseThrow(() -> new RuntimeException("Package not found"));
      
      // 2. Validate: Package status must be "Pending"
      if (!"Pending".equalsIgnoreCase(pkg.getStatus())) {
          throw new RuntimeException("Cannot process package. Package status must be 'Pending', current status: " + pkg.getStatus());
      }
      
      // 3. Get active (non-deleted) plans
      var activePlans = pkg.getPlans().stream()
          .filter(plan -> !Boolean.TRUE.equals(plan.getIsDeleted()))
          .toList();
      
      if (activePlans.isEmpty()) {
          throw new RuntimeException("Cannot process package. Package has no active plans.");
      }
      
      // 4. Validate: ALL active plans must have status "Fulfilled"
      var unfulfilledPlans = activePlans.stream()
          .filter(plan -> !"Fulfilled".equals(plan.getStatus()))
          .toList();
      
      if (!unfulfilledPlans.isEmpty()) {
          String planNames = unfulfilledPlans.stream()
              .map(p -> p.getPlanName() + " (status: " + p.getStatus() + ")")
              .reduce((a, b) -> a + ", " + b)
              .orElse("");
          throw new RuntimeException("Cannot process package. All plans must have status 'Fulfilled'. Unfulfilled plans: " + planNames);
      }
      
      System.out.println("✅ All " + activePlans.size() + " plans are fulfilled");
      
      // 5. Reduce Activity capacity for each OrderedQuantity
      int totalActivitiesProcessed = 0;
      for (var plan : activePlans) {
          var activeOrderedQuantities = plan.getOrderedQuantities().stream()
              .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
              .toList();
          
          for (var orderedQuantity : activeOrderedQuantities) {
              var activity = orderedQuantity.getActivity();
              if (activity != null) {
                  int oldCapacity = activity.getCapacity();
                  int orderedQuota = orderedQuantity.getOrderedQuota();
                  int newCapacity = oldCapacity - orderedQuota;
                  
                  if (newCapacity < 0) {
                      throw new RuntimeException(
                          String.format("Cannot process. Activity '%s' has insufficient capacity. Current: %d, Ordered: %d",
                              activity.getActivityName(), oldCapacity, orderedQuota)
                      );
                  }
                  
                  activity.setCapacity(newCapacity);
                  activityRepository.save(activity); // ✅ SAVE ACTIVITY TO DATABASE
                  System.out.println(String.format("   📉 Activity '%s': capacity %d → %d (-%d)", 
                      activity.getActivityName(), oldCapacity, newCapacity, orderedQuota));
                  totalActivitiesProcessed++;
              }
          }
      }
      
      System.out.println("✅ Activities processed successfully!");
      System.out.println("   Total activities capacity reduced: " + totalActivitiesProcessed);
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      
      // 6. ✨ Create Bill in Bill Service after successful processing
      // Calculate total price from OrderedQuantities (same formula as DTO mapper)
      long totalPriceFromPlans = pkg.getPlans().stream()
          .filter(plan -> !Boolean.TRUE.equals(plan.getIsDeleted()))
          .mapToLong(plan -> {
              return plan.getOrderedQuantities().stream()
                  .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                  .mapToLong(oq -> (long) oq.getOrderedQuota() * oq.getPrice())
                  .sum();
          })
          .sum();
      
      // Use calculated price if package price field is 0 (which is common since price is computed)
      Long billAmount = (pkg.getPrice() == null || pkg.getPrice() <= 0) ? totalPriceFromPlans : pkg.getPrice();
      
      System.out.println("📊 Bill Amount Calculation:");
      System.out.println("   Package.price (DB field): " + pkg.getPrice());
      System.out.println("   Calculated from OrderedQuantities: " + totalPriceFromPlans);
      System.out.println("   Final Bill Amount: " + billAmount);
      System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      
      // Only skip if amount is still 0 or negative after calculation
      if (billAmount == null || billAmount <= 0) {
          System.out.println("⚠️ BILL CREATION SKIPPED");
          System.out.println("   Reason: Final amount is " + billAmount);
          System.out.println("   Package has no price and OrderedQuantities have no total");
          System.out.println("   Bill Service requires amount > 0");
          System.out.println("   📝 Setting status to 'Processed' (no bill created)");
          System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
          
          // Set status to "Processed" when bill creation is skipped
          pkg.setStatus("Processed");
          repo.save(pkg);
      } else {
          try {
              System.out.println("📄 STARTING BILL CREATION PROCESS...");
              System.out.println("   Package ID: " + pkg.getId());
              System.out.println("   Package Name: " + pkg.getPackageName());
              System.out.println("   Package Owner ID: " + pkg.getUserId());
              System.out.println("   Customer ID (Processor): " + authenticatedCustomerId);
              System.out.println("   Amount: Rp " + billAmount);
              System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
              
              // Set price for Bill creation (using calculated amount)
              Long originalPrice = pkg.getPrice();
              if (originalPrice == null || originalPrice <= 0) {
                  pkg.setPrice(billAmount);
                  System.out.println("   ℹ️  Using calculated price from OrderedQuantities: Rp " + billAmount);
              }
              
              // ✅ Pass authenticatedCustomerId untuk Bill customerId
              BillResponseDTO billResponse = billIntegrationService.createBillForPackage(pkg, authenticatedCustomerId);
              
              System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
              System.out.println("🎉 BILL CREATED SUCCESSFULLY!");
              System.out.println("   Bill ID: " + (billResponse != null ? billResponse.getId() : "N/A"));
              System.out.println("   Service Name: " + (billResponse != null ? billResponse.getServiceName() : "N/A"));
              System.out.println("   Reference ID: " + (billResponse != null ? billResponse.getServiceReferenceId() : "N/A"));
              
              // 7. Update Package status to "Waiting for Payment" after bill created
              pkg.setStatus("Waiting for Payment");
              repo.save(pkg);
              System.out.println("   📝 Package status updated: Pending → Waiting for Payment");
              System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
              
          } catch (Exception e) {
              System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
              System.err.println("❌ BILL CREATION FAILED!");
              System.err.println("   Error: " + e.getMessage());
              System.err.println("   Package ID: " + pkg.getId());
              System.err.println("   Note: Activities capacity already reduced, but Bill was not created");
              System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
              
              // Rollback: Set status to Processed (activities already reduced)
              pkg.setStatus("Processed");
              repo.save(pkg);
              
              // Throw exception agar FE tahu ada masalah dengan Bill Service
              throw new RuntimeException("Failed to create Bill after processing package: " + e.getMessage(), e);
          }
      } // End of if (price > 0) block
      
      return getById(id);
  }

  @Override
  @Transactional
  public PackageResponseDTO updatePaymentStatus(String packageId, Integer status) {
      System.out.println("🔔 Received payment update from Bill Service");
      System.out.println("   Package ID: " + packageId);
      System.out.println("   Status: " + status + " (" + (status == 1 ? "PAID" : status == 0 ? "UNPAID" : "UNKNOWN") + ")");
      
      // 1. Find package
      var pkg = repo.findById(packageId)
          .orElseThrow(() -> new RuntimeException("Package not found with id: " + packageId));
      
      // 2. Validate current status
      if (!"Waiting for Payment".equals(pkg.getStatus())) {
          throw new RuntimeException(
              String.format("Cannot update payment status. Package current status is '%s', expected 'Waiting for Payment'", 
                  pkg.getStatus())
          );
      }
      
      // 3. Update status based on payment status (Bill Service sends: 0=UNPAID, 1=PAID)
      if (status == 1) {  // PAID
          pkg.setStatus("Payment Confirmed");
          System.out.println("   ✅ Package status updated: Waiting for Payment → Payment Confirmed");
      } else if (status == 0) {  // UNPAID
          // Optional: handle unpaid status if needed
          throw new RuntimeException("Cannot confirm payment. Bill status is still UNPAID (0).");
      } else {
          throw new RuntimeException("Invalid payment status: " + status + ". Expected: 0 (UNPAID) or 1 (PAID).");
      }
      
      // 4. Save package
      repo.save(pkg);
      
      System.out.println("✅ Payment status updated successfully!");
      
      return getById(packageId);
  }

}
  

