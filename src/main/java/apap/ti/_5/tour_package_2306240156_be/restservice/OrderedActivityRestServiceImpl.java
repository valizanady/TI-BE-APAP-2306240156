package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.*;
import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.repository.*;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateOrderedActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.ActivityDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderedActivityRestServiceImpl implements OrderedActivityRestService {

    private final PlanRepository planRepository;
    private final ActivityRepository activityRepository;
    private final OrderedQuantityRepository orderedQuantityRepository;
    private final PackageRepository packageRepository;

    @Override
    public List<ActivityDTO> getEligibleActivities(UUID planId) {
        System.out.println("🔍 Getting eligible activities for plan: " + planId);
        
        // 1. Find Plan
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + planId));
        
        System.out.println("📋 Plan details:");
        System.out.println("   Activity Type: " + plan.getActivityType());
        System.out.println("   Start Date: " + plan.getStartDate());
        System.out.println("   End Date: " + plan.getEndDate());
        System.out.println("   Start Location: " + plan.getStartLocation());
        System.out.println("   End Location: " + plan.getEndLocation());
        
        // 2. Find eligible activities with correct criteria
        List<Activity> eligibleActivities = activityRepository.findEligibleActivitiesForPlan(
                plan.getActivityType(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getStartLocation(),
                plan.getEndLocation()
        );
        
        System.out.println("📦 Total matching activities: " + eligibleActivities.size());
        
        // 3. Get all ACTIVE (non-deleted) ordered quantities for this plan
        List<OrderedQuantity> activeOrderedQuantities = plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .toList();
        
        System.out.println("🚫 Active ordered activities in plan: " + activeOrderedQuantities.size());
        
        // 4. Filter out activities that are already ACTIVELY added to this plan
        // Activities with isDeleted=true are eligible again
        List<String> activeActivityIds = activeOrderedQuantities.stream()
                .map(oq -> oq.getActivity().getId())
                .toList();
        
        List<Activity> filteredActivities = eligibleActivities.stream()
                .filter(activity -> !activeActivityIds.contains(activity.getId()))
                .toList();
        
        System.out.println("✅ Eligible activities (excluding active ones): " + filteredActivities.size());
        
        // 5. Convert to DTO to avoid circular reference
        return filteredActivities.stream()
                .map(ActivityDTO::fromEntity)
                .toList();
    }

    @Override
    public OrderedQuantity addActivityToPlan(UUID planId, CreateOrderedActivityRequestDTO request) {
        System.out.println("➕ Adding activity to plan: " + planId);
        System.out.println("   Activity ID: " + request.getActivityId());
        System.out.println("   Ordered Quantity: " + request.getOrderedQuantity());
        
        // 1. Find Plan
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + planId));
        
        // 2. Validate Package Status must be "Pending"
        Package tourPackage = plan.getTourPackage();
        if (tourPackage == null) {
            throw new RuntimeException("Package not found for this plan");
        }
        
        if (!"Pending".equals(tourPackage.getStatus())) {
            throw new RuntimeException("Cannot add activity. Package status must be 'Pending'");
        }
        
        // 3. Find Activity
        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + request.getActivityId()));
        
        // 4. Validate Activity meets Plan criteria
        if (!activity.getActivityType().equals(plan.getActivityType())) {
            throw new RuntimeException("Activity type must match plan activity type");
        }
        
        // StartDate Activity >= StartDate Plan (termasuk waktu)
        if (activity.getStartDate().isBefore(plan.getStartDate())) {
            throw new RuntimeException(
                String.format("Activity start date/time (%s) must be on or after plan start date/time (%s)",
                    activity.getStartDate(), plan.getStartDate())
            );
        }
        
        // EndDate Activity <= EndDate Plan (termasuk waktu)
        if (activity.getEndDate().isAfter(plan.getEndDate())) {
            throw new RuntimeException(
                String.format("Activity end date/time (%s) must be on or before plan end date/time (%s)",
                    activity.getEndDate(), plan.getEndDate())
            );
        }
        
        if (!activity.getStartLocation().equals(plan.getStartLocation()) || 
            !activity.getEndLocation().equals(plan.getEndLocation())) {
            throw new RuntimeException("Activity locations must match plan locations");
        }
        
        // 5. Calculate current total ordered quantity in THIS PLAN only (exclude soft-deleted)
        int currentTotalOrderedInPlan = plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();
        
        System.out.println("📊 Current total ordered in THIS plan: " + currentTotalOrderedInPlan);
        System.out.println("📊 Package quota: " + tourPackage.getQuota());
        
        // 6. Validate: Total ordered quantity in THIS PLAN must not exceed Package quota
        if (currentTotalOrderedInPlan + request.getOrderedQuantity() > tourPackage.getQuota()) {
            throw new RuntimeException(
                String.format("Cannot add activity. Total ordered quantity in this plan (%d + %d = %d) would exceed package quota (%d)",
                    currentTotalOrderedInPlan, 
                    request.getOrderedQuantity(),
                    currentTotalOrderedInPlan + request.getOrderedQuantity(),
                    tourPackage.getQuota()
                )
            );
        }
        
        // 7. Validate: Ordered quantity must not exceed Activity capacity
        if (request.getOrderedQuantity() > activity.getCapacity()) {
            throw new RuntimeException(
                String.format("Ordered quantity (%d) cannot exceed activity capacity (%d)",
                    request.getOrderedQuantity(),
                    activity.getCapacity()
                )
            );
        }
        
        // 8. Create OrderedQuantity
        OrderedQuantity orderedQuantity = OrderedQuantity.builder()
                .plan(plan)
                .activity(activity)
                .quota(activity.getCapacity())
                .orderedQuota(request.getOrderedQuantity())
                .price(activity.getPrice())
                .startDate(activity.getStartDate())
                .endDate(activity.getEndDate())
                .build();
        
        OrderedQuantity saved = orderedQuantityRepository.save(orderedQuantity);
        
        // 9. Update Plan total price (exclude soft-deleted)
        Long totalPrice = plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .mapToLong(oq -> (long) oq.getOrderedQuota() * oq.getPrice())
                .sum();
        plan.setPrice(totalPrice);
        
        // 10. Update Plan status based on THIS PLAN's ordered quantity vs Package quota
        int newTotalOrderedInPlan = currentTotalOrderedInPlan + request.getOrderedQuantity();
        if (newTotalOrderedInPlan >= tourPackage.getQuota()) {
            plan.setStatus("Fulfilled");
            System.out.println("✅ Plan status updated to Fulfilled (ordered: " + newTotalOrderedInPlan + " >= quota: " + tourPackage.getQuota() + ")");
        } else {
            plan.setStatus("Unfulfilled");
            System.out.println("📋 Plan status remains Unfulfilled (ordered: " + newTotalOrderedInPlan + " < quota: " + tourPackage.getQuota() + ")");
        }
        
        planRepository.save(plan);
        
        System.out.println("✅ Activity added successfully to plan");
        return saved;
    }

    @Override
    public OrderedQuantity updateOrderedActivity(UUID orderedActivityId, int newQuantity) {
        System.out.println("🔄 Updating ordered activity: " + orderedActivityId);
        System.out.println("   New Quantity: " + newQuantity);
        
        // 1. Find OrderedQuantity
        OrderedQuantity orderedQuantity = orderedQuantityRepository.findById(orderedActivityId)
                .orElseThrow(() -> new RuntimeException("Ordered activity not found"));
        
        Plan plan = orderedQuantity.getPlan();
        Package tourPackage = plan.getTourPackage();
        
        // 2. Validate Package Status
        if (!"Pending".equals(tourPackage.getStatus())) {
            throw new RuntimeException("Cannot update. Package status must be 'Pending'");
        }
        
        // 3. Calculate current total in THIS PLAN (excluding this ordered quantity and soft-deleted)
        int currentTotalExcludingThis = plan.getOrderedQuantities().stream()
                .filter(oq -> !oq.getId().equals(orderedActivityId))
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();
        
        System.out.println("📊 Current total in plan (excluding this): " + currentTotalExcludingThis);
        System.out.println("📊 New quantity: " + newQuantity);
        System.out.println("📊 Package quota: " + tourPackage.getQuota());
        
        // 4. Validate: Total ordered in THIS PLAN must not exceed Package quota
        if (currentTotalExcludingThis + newQuantity > tourPackage.getQuota()) {
            throw new RuntimeException(
                String.format("Cannot update. Total ordered in this plan (%d + %d = %d) would exceed package quota (%d)",
                    currentTotalExcludingThis,
                    newQuantity,
                    currentTotalExcludingThis + newQuantity,
                    tourPackage.getQuota()
                )
            );
        }
        
        // 5. Validate: New quantity must not exceed Activity capacity
        if (newQuantity > orderedQuantity.getQuota()) {
            throw new RuntimeException(
                String.format("Ordered quantity (%d) cannot exceed activity capacity (%d)",
                    newQuantity,
                    orderedQuantity.getQuota()
                )
            );
        }
        
        // 6. Update quantity
        orderedQuantity.setOrderedQuota(newQuantity);
        OrderedQuantity updated = orderedQuantityRepository.save(orderedQuantity);
        
        // 7. Update Plan price (exclude soft-deleted)
        Long totalPrice = plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .mapToLong(oq -> (long) oq.getOrderedQuota() * oq.getPrice())
                .sum();
        plan.setPrice(totalPrice);
        
        // 8. Update Plan status
        int newTotalOrdered = currentTotalExcludingThis + newQuantity;
        if (newTotalOrdered >= tourPackage.getQuota()) {
            plan.setStatus("Fulfilled");
        } else {
            plan.setStatus("Unfulfilled");
        }
        
        planRepository.save(plan);
        
        System.out.println("✅ Ordered activity updated successfully");
        return updated;
    }

    @Override
    public void deleteOrderedActivity(UUID orderedActivityId) {
        System.out.println("🗑️ Soft deleting ordered activity: " + orderedActivityId);
        
        // 1. Find OrderedQuantity
        OrderedQuantity orderedQuantity = orderedQuantityRepository.findById(orderedActivityId)
                .orElseThrow(() -> new RuntimeException("Ordered activity not found"));
        
        // Check if already soft-deleted
        if (Boolean.TRUE.equals(orderedQuantity.getIsDeleted())) {
            throw new RuntimeException("Ordered activity is already deleted");
        }
        
        Plan plan = orderedQuantity.getPlan();
        Package tourPackage = plan.getTourPackage();
        
        // 2. Validate Package Status
        if (!"Pending".equals(tourPackage.getStatus())) {
            throw new RuntimeException("Cannot delete. Package status must be 'Pending'");
        }
        
        // 3. Soft delete: Mark as deleted (keeps record in DB)
        orderedQuantity.setIsDeleted(true);
        orderedQuantityRepository.save(orderedQuantity);
        
        // 4. Recalculate Plan price (excluding soft-deleted items)
        Long totalPrice = plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .mapToLong(oq -> (long) oq.getOrderedQuota() * oq.getPrice())
                .sum();
        
        // 5. Update Plan status based on active (non-deleted) ordered quantities
        int activeTotalOrdered = plan.getOrderedQuantities().stream()
                .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();
        
        plan.setPrice(totalPrice);
        
        if (activeTotalOrdered >= tourPackage.getQuota()) {
            plan.setStatus("Fulfilled");
        } else {
            plan.setStatus("Unfulfilled");
        }
        
        planRepository.save(plan);
        
        System.out.println("✅ Ordered activity soft-deleted successfully (kept in DB with isDeleted=true)");
    }
}