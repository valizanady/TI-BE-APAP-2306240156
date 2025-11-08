package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.*;
import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePlanRequestDTO;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanRestServiceImpl implements PlanRestService {

    private final PlanRepository planRepository;
    private final PackageRepository packageRepository;

    @Override
    public Plan createPlan(String packageId, CreatePlanRequestDTO request) {
        // Validasi package existence
        Package tourPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        // Validasi status package harus "Pending"
        if (!"Pending".equals(tourPackage.getStatus())) {
            throw new RuntimeException("Cannot create plan. Package status must be 'Pending'");
        }

        // Validasi endDate tidak lebih dahulu dari startDate (termasuk waktu)
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException(
                String.format("End date/time (%s) cannot be before start date/time (%s)",
                    request.getEndDate(), request.getStartDate())
            );
        }

        // Validasi startDate >= startDate Package (termasuk waktu)
        // Boleh sama tanggal tapi jamnya harus sama atau setelah
        if (request.getStartDate().isBefore(tourPackage.getStartDate())) {
            throw new RuntimeException(
                String.format("Plan start date/time (%s) must be on or after package start date/time (%s)",
                    request.getStartDate(), tourPackage.getStartDate())
            );
        }

        // Validasi endDate <= endDate Package (termasuk waktu)
        // Boleh sama tanggal tapi jamnya harus sama atau sebelum
        if (request.getEndDate().isAfter(tourPackage.getEndDate())) {
            throw new RuntimeException(
                String.format("Plan end date/time (%s) must be on or before package end date/time (%s)",
                    request.getEndDate(), tourPackage.getEndDate())
            );
        }

        // Validasi startLocation dan endLocation untuk Accommodation
        if ("Accommodation".equals(request.getActivityType())) {
            if (!request.getStartLocation().equals(request.getEndLocation())) {
                throw new RuntimeException("For Accommodation, start and end location must be the same");
            }
        }

        // Buat Plan baru
        Plan plan = Plan.builder()
                .planName(request.getPlanName())
                .activityType(request.getActivityType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startLocation(request.getStartLocation())
                .endLocation(request.getEndLocation())
                .status("Unfulfilled")
                .price(0L)
                .tourPackage(tourPackage)
                .build();

        return planRepository.save(plan);
    }

    @Override
    public Plan getPlanById(UUID id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + id));
        
        // Check if soft-deleted
        if (Boolean.TRUE.equals(plan.getIsDeleted())) {
            throw new RuntimeException("Plan not found with id: " + id);
        }
        
        return plan;
    }

    @Override
    public Plan updatePlan(UUID id, UpdatePlanRequestDTO request) {
        System.out.println("🔄 Starting updatePlan for ID: " + id);
        
        // 1. Cari Plan berdasarkan ID
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + id));
        
        System.out.println("✅ Plan found: " + plan.getPlanName());

        // 2. Ambil Package terkait
        Package tourPackage = plan.getTourPackage();
        if (tourPackage == null) {
            throw new RuntimeException("Package not found for this plan");
        }
        
        System.out.println("✅ Package found: " + tourPackage.getPackageName() + " (Status: " + tourPackage.getStatus() + ")");

        // 3. Validasi: Package harus berstatus "Pending"
        if (!"Pending".equals(tourPackage.getStatus())) {
            String errorMsg = "Cannot update plan. Package status must be 'Pending', current status: " + tourPackage.getStatus();
            System.out.println("❌ " + errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // 4. Validasi: Plan tidak boleh memiliki OrderedQuantity yang aktif (exclude soft-deleted)
        long activeOrderedQuantities = plan.getOrderedQuantities() == null ? 0 : 
            plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .count();
        
        if (activeOrderedQuantities > 0) {
            String errorMsg = "Cannot update plan. Plan has " + activeOrderedQuantities + " active ordered activities.";
            System.out.println("❌ " + errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        System.out.println("✅ Plan has no ordered quantities, can proceed");

        // 5. Validasi endDate tidak lebih dahulu dari startDate (termasuk waktu)
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException(
                String.format("End date/time (%s) cannot be before start date/time (%s)",
                    request.getEndDate(), request.getStartDate())
            );
        }

        // 6. Validasi startDate >= startDate Package (termasuk waktu)
        if (request.getStartDate().isBefore(tourPackage.getStartDate())) {
            throw new RuntimeException(
                String.format("Plan start date/time (%s) must be on or after package start date/time (%s)",
                    request.getStartDate(), tourPackage.getStartDate())
            );
        }

        // 7. Validasi endDate <= endDate Package (termasuk waktu)
        if (request.getEndDate().isAfter(tourPackage.getEndDate())) {
            throw new RuntimeException(
                String.format("Plan end date/time (%s) must be on or before package end date/time (%s)",
                    request.getEndDate(), tourPackage.getEndDate())
            );
        }

        // 8. Validasi startLocation dan endLocation untuk Accommodation
        if ("Accommodation".equals(plan.getActivityType())) {
            if (!request.getStartLocation().equals(request.getEndLocation())) {
                throw new RuntimeException("For Accommodation activity type, start and end location must be the same");
            }
        }
        
        System.out.println("✅ All validations passed");

        // 9. Update field-field yang diizinkan
        String oldPlanName = plan.getPlanName();
        plan.setPlanName(request.getPlanName());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setStartLocation(request.getStartLocation());
        plan.setEndLocation(request.getEndLocation());
        
        System.out.println("📝 Updating plan:");
        System.out.println("   Plan Name: " + oldPlanName + " → " + request.getPlanName());
        System.out.println("   Start Date/Time: " + request.getStartDate());
        System.out.println("   End Date/Time: " + request.getEndDate());
        System.out.println("   Start Location: " + request.getStartLocation());
        System.out.println("   End Location: " + request.getEndLocation());

        // 10. Save dan return
        Plan updatedPlan = planRepository.save(plan);
        System.out.println("✅ Plan updated successfully with ID: " + updatedPlan.getId());
        
        return updatedPlan;
    }

    @Override
    public void deletePlan(UUID id) {
        System.out.println("🗑️ Soft deleting plan: " + id);
        
        // 1. Find Plan
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + id));
        
        // Check if already soft-deleted
        if (Boolean.TRUE.equals(plan.getIsDeleted())) {
            throw new RuntimeException("Plan is already deleted");
        }
        
        // 2. Validate Package Status must be "Pending"
        Package tourPackage = plan.getTourPackage();
        if (tourPackage == null) {
            throw new RuntimeException("Package not found for this plan");
        }
        
        if (!"Pending".equals(tourPackage.getStatus())) {
            throw new RuntimeException("Cannot delete plan. Package status must be 'Pending', current status: " + tourPackage.getStatus());
        }
        
        System.out.println("✅ Validation passed, proceeding with soft delete");
        
        // 3. Soft delete: Mark as deleted (keeps record in DB)
        plan.setIsDeleted(true);
        planRepository.save(plan);
        
        System.out.println("✅ Plan soft-deleted successfully (kept in DB with isDeleted=true)");
    }

    // Di method getPlanDetail atau similar, pastikan mapping quota:
    // orderedQuantityDTO.setQuota(orderedQuantity.getActivity().getCapacity()); // Ini akan ambil capacity yang sudah dikurangi
}