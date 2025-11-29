// restservice/TourPackageRestServiceImpl.java
package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePackageRequestDTO;
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
  public PackageResponseDTO processPackage(String id) {
      System.out.println("🔄 Processing package: " + id);
      
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
      
      // 6. Update Package status to "Processed"
      pkg.setStatus("Processed");
      repo.save(pkg);
      
      System.out.println("✅ Package processed successfully!");
      System.out.println("   Total activities capacity reduced: " + totalActivitiesProcessed);
      System.out.println("   Package status: Pending → Processed");
      
      return getById(id);
  }

}
  
