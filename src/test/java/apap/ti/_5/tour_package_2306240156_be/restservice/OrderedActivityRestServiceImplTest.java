package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateOrderedActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.ActivityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderedActivityRestServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private OrderedQuantityRepository orderedQuantityRepository;

    @Mock
    private PackageRepository packageRepository;

    @InjectMocks
    private OrderedActivityRestServiceImpl orderedActivityRestService;

    private Plan testPlan;
    private Package testPackage;
    private Activity testActivity;
    private Activity testActivity2;
    private OrderedQuantity testOrderedQuantity;
    private UUID testPlanId;
    private UUID testOrderedQuantityId;

    @BeforeEach
    void setUp() {
        testPlanId = UUID.randomUUID();
        testOrderedQuantityId = UUID.randomUUID();

        testPackage = Package.builder()
                .id("PKG001")
                .packageName("Test Package")
                .status("Pending")
                .quota(50)
                .price(0L)
                .startDate(LocalDateTime.of(2025, 11, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 11, 7, 0, 0))
                .build();

        testPlan = Plan.builder()
                .id(testPlanId)
                .planName("Test Plan")
                .activityType("Flight")
                .startDate(LocalDateTime.of(2025, 11, 1, 8, 0))
                .endDate(LocalDateTime.of(2025, 11, 1, 10, 30))
                .startLocation("Jakarta")
                .endLocation("Bali")
                .status("Unfulfilled")
                .price(0L)
                .isDeleted(false)
                .tourPackage(testPackage)
                .orderedQuantities(new ArrayList<>())
                .build();

        testActivity = Activity.builder()
                .id("ACT001")
                .activityName("Jakarta to Bali Flight")
                .activityType("Flight")
                .capacity(50)
                .price(1500000L)
                .startDate(LocalDateTime.of(2025, 11, 1, 8, 0))
                .endDate(LocalDateTime.of(2025, 11, 1, 10, 30))
                .startLocation("Jakarta")
                .endLocation("Bali")
                .build();

        testActivity2 = Activity.builder()
                .id("ACT002")
                .activityName("Another Flight")
                .activityType("Flight")
                .capacity(40)
                .price(2000000L)
                .startDate(LocalDateTime.of(2025, 11, 1, 8, 0))
                .endDate(LocalDateTime.of(2025, 11, 1, 10, 30))
                .startLocation("Jakarta")
                .endLocation("Bali")
                .build();

        testOrderedQuantity = OrderedQuantity.builder()
                .id(testOrderedQuantityId)
                .plan(testPlan)
                .activity(testActivity)
                .orderedQuota(25)
                .quota(50)
                .price(1500000L)
                .startDate(testActivity.getStartDate())
                .endDate(testActivity.getEndDate())
                .isDeleted(false)
                .build();
    }

    // ==================== GET ELIGIBLE ACTIVITIES TESTS ====================

    @Test
    void testDeleteOrderedActivity_RecalculatesPriceAndStatus() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setIsDeleted(false);
        testOrderedQuantity.setOrderedQuota(25);
        testOrderedQuantity.setPrice(1500000L);
        
        OrderedQuantity other = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .orderedQuota(30)
                .price(2000000L)
                .isDeleted(false)
                .build();
        
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        testPlan.getOrderedQuantities().add(other);
        testPlan.setPrice(97500000L); // 25*1500000 + 30*2000000
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);

        assertEquals(60000000L, testPlan.getPrice()); // Only 30*2000000 remains
        assertEquals("Unfulfilled", testPlan.getStatus()); // 30 < 50
    }

    @Test
    void testDeleteOrderedActivity_StatusFulfilledWhenRemainingMeetsQuota() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setIsDeleted(false);
        testOrderedQuantity.setOrderedQuota(10);
        
        OrderedQuantity other = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .orderedQuota(50)
                .price(2000000L)
                .isDeleted(false)
                .build();
        
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        testPlan.getOrderedQuantities().add(other);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);

        assertEquals("Fulfilled", testPlan.getStatus()); // 50 >= 50
    }

    @Test
    void testDeleteOrderedActivity_ExcludesAlreadyDeletedFromCalculation() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setIsDeleted(false);
        testOrderedQuantity.setOrderedQuota(25);
        testOrderedQuantity.setPrice(1500000L);
        
        OrderedQuantity alreadyDeleted = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .orderedQuota(100)
                .price(5000000L)
                .isDeleted(true)
                .build();
        
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        testPlan.getOrderedQuantities().add(alreadyDeleted);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);

        assertEquals(0L, testPlan.getPrice()); // All deleted
        assertEquals("Unfulfilled", testPlan.getStatus()); // 0 < 50
    }

    @Test
    void testDeleteOrderedActivity_LastActiveItem() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setIsDeleted(false);
        testOrderedQuantity.setOrderedQuota(25);
        testOrderedQuantity.setPrice(1500000L);
        
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);

        assertEquals(0L, testPlan.getPrice());
        assertEquals("Unfulfilled", testPlan.getStatus());
        assertTrue(testOrderedQuantity.getIsDeleted());
    }

    @Test
    void testDeleteOrderedActivity_IsDeletedNull() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setIsDeleted(null);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);

        assertTrue(testOrderedQuantity.getIsDeleted());
    }

    // ==================== EDGE CASES AND INTEGRATION TESTS ====================

    @Test
    void testAddActivityToPlan_ActivityDatesEqualPlanDates() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        testActivity.setStartDate(testPlan.getStartDate());
        testActivity.setEndDate(testPlan.getEndDate());

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        OrderedQuantity result = orderedActivityRestService.addActivityToPlan(testPlanId, request);

        assertNotNull(result);
    }

    @Test
    void testAddActivityToPlan_ExactlyPackageQuota() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(50);

        testPackage.setQuota(50);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        OrderedQuantity result = orderedActivityRestService.addActivityToPlan(testPlanId, request);

        assertNotNull(result);
        assertEquals("Fulfilled", testPlan.getStatus());
    }

    @Test
    void testUpdateOrderedActivity_ExactlyPackageQuota() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setOrderedQuota(25);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);

        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        OrderedQuantity result = orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 50);

        assertNotNull(result);
        assertEquals("Fulfilled", testPlan.getStatus());
    }

    @Test
    void testUpdateOrderedActivity_ExactlyActivityCapacity() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setQuota(50);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);

        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        OrderedQuantity result = orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 50);

        assertNotNull(result);
        assertEquals(50, result.getOrderedQuota());
    }

    @Test
    void testGetEligibleActivities_MultipleActivities() {
        List<Activity> activities = List.of(testActivity, testActivity2);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findEligibleActivitiesForPlan(any(), any(), any(), any(), any()))
                .thenReturn(activities);

        List<ActivityDTO> result = orderedActivityRestService.getEligibleActivities(testPlanId);

        assertEquals(2, result.size());
    }

    @Test
    void testAddActivityToPlan_UpdatesPlanPrice() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(10);

        OrderedQuantity existing = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .orderedQuota(5)
                .price(2000000L)
                .isDeleted(false)
                .build();
        testPlan.getOrderedQuantities().add(existing);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.addActivityToPlan(testPlanId, request);

        // Should calculate: (5 * 2000000) + (10 * 1500000) = 25000000
        verify(planRepository).save(argThat(plan -> 
            plan.getPrice() != null && plan.getPrice() > 0
        ));
    }

    @Test
    void testUpdateOrderedActivity_UpdatesPlanPrice() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setOrderedQuota(25);
        testOrderedQuantity.setPrice(1500000L);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);

        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 30);

        verify(planRepository).save(argThat(plan -> 
            plan.getPrice() != null
        ));
    }


    @Test
    void testGetEligibleActivities_PlanNotFound() {
        when(planRepository.findById(testPlanId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.getEligibleActivities(testPlanId);
        });
        
        assertTrue(exception.getMessage().contains("Plan not found with id:"));
        verify(planRepository).findById(testPlanId);
        verify(activityRepository, never()).findEligibleActivitiesForPlan(any(), any(), any(), any(), any());
    }

    @Test
    void testGetEligibleActivities_EmptyList() {
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findEligibleActivitiesForPlan(any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        List<ActivityDTO> result = orderedActivityRestService.getEligibleActivities(testPlanId);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetEligibleActivities_FiltersActiveOrderedActivities() {
        OrderedQuantity activeOrderedQuantity = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .activity(testActivity)
                .isDeleted(false)
                .build();
        
        testPlan.getOrderedQuantities().add(activeOrderedQuantity);
        
        List<Activity> allActivities = List.of(testActivity, testActivity2);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findEligibleActivitiesForPlan(any(), any(), any(), any(), any()))
                .thenReturn(allActivities);

        List<ActivityDTO> result = orderedActivityRestService.getEligibleActivities(testPlanId);

        assertEquals(1, result.size());
        assertEquals("ACT002", result.get(0).getId());
    }

    @Test
    void testGetEligibleActivities_IncludesDeletedOrderedActivities() {
        OrderedQuantity deletedOrderedQuantity = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .activity(testActivity)
                .isDeleted(true)
                .build();
        
        testPlan.getOrderedQuantities().add(deletedOrderedQuantity);
        
        List<Activity> allActivities = List.of(testActivity);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findEligibleActivitiesForPlan(any(), any(), any(), any(), any()))
                .thenReturn(allActivities);

        List<ActivityDTO> result = orderedActivityRestService.getEligibleActivities(testPlanId);

        assertEquals(1, result.size());
        assertEquals("ACT001", result.get(0).getId());
    }

    @Test
    void testGetEligibleActivities_MultipleActivitiesWithMixedOrderedStatus() {
        OrderedQuantity active1 = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .activity(testActivity)
                .isDeleted(false)
                .build();
        
        OrderedQuantity deleted1 = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .activity(testActivity2)
                .isDeleted(true)
                .build();
        
        testPlan.getOrderedQuantities().add(active1);
        testPlan.getOrderedQuantities().add(deleted1);
        
        List<Activity> allActivities = List.of(testActivity, testActivity2);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findEligibleActivitiesForPlan(any(), any(), any(), any(), any()))
                .thenReturn(allActivities);

        List<ActivityDTO> result = orderedActivityRestService.getEligibleActivities(testPlanId);

        assertEquals(1, result.size());
        assertEquals("ACT002", result.get(0).getId());
    }

    // ==================== ADD ACTIVITY TO PLAN TESTS ====================

    @Test
    void testAddActivityToPlan_Success() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenAnswer(invocation -> {
            OrderedQuantity savedOQ = invocation.getArgument(0);
            savedOQ.setId(UUID.randomUUID());
            return savedOQ;
        });
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        OrderedQuantity result = orderedActivityRestService.addActivityToPlan(testPlanId, request);

        assertNotNull(result);
        assertEquals(25, result.getOrderedQuota());
        verify(planRepository).findById(testPlanId);
        verify(activityRepository).findById("ACT001");
        verify(orderedQuantityRepository).save(any(OrderedQuantity.class));
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testAddActivityToPlan_PlanNotFound() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertTrue(exception.getMessage().contains("Plan not found with id:"));
        verify(activityRepository, never()).findById(any());
    }

    @Test
    void testAddActivityToPlan_PackageNotFound() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        testPlan.setTourPackage(null);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertEquals("Package not found for this plan", exception.getMessage());
    }

    @Test
    void testAddActivityToPlan_InvalidPackageStatus() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        testPackage.setStatus("Processed");
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertEquals("Cannot add activity. Package status must be 'Pending'", exception.getMessage());
    }

    @Test
    void testAddActivityToPlan_ActivityNotFound() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT999");
        request.setOrderedQuantity(25);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertTrue(exception.getMessage().contains("Activity not found with id:"));
    }

    @Test
    void testAddActivityToPlan_ActivityTypeMismatch() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        testActivity.setActivityType("Accommodation");
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertEquals("Activity type must match plan activity type", exception.getMessage());
    }

    @Test
    void testAddActivityToPlan_ActivityStartDateBeforePlanStart() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        testActivity.setStartDate(LocalDateTime.of(2025, 11, 1, 7, 59));
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertTrue(exception.getMessage().contains("Activity start date/time"));
        assertTrue(exception.getMessage().contains("must be on or after plan start date/time"));
    }

    @Test
    void testAddActivityToPlan_ActivityEndDateAfterPlanEnd() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        testActivity.setEndDate(LocalDateTime.of(2025, 11, 1, 10, 31));
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertTrue(exception.getMessage().contains("Activity end date/time"));
        assertTrue(exception.getMessage().contains("must be on or before plan end date/time"));
    }

    @Test
    void testAddActivityToPlan_LocationMismatch() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        testActivity.setStartLocation("Surabaya");
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertEquals("Activity locations must match plan locations", exception.getMessage());
    }

    @Test
    void testAddActivityToPlan_ExceedsPackageQuota() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(30);

        OrderedQuantity existing = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .orderedQuota(25)
                .isDeleted(false)
                .build();
        testPlan.getOrderedQuantities().add(existing);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertTrue(exception.getMessage().contains("Total ordered quantity in this plan"));
        assertTrue(exception.getMessage().contains("would exceed package quota"));
    }

    @Test
    void testAddActivityToPlan_ExcludesDeletedFromQuotaCalculation() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(30);

        OrderedQuantity deleted = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .orderedQuota(100)
                .isDeleted(true)
                .build();
        testPlan.getOrderedQuantities().add(deleted);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        OrderedQuantity result = orderedActivityRestService.addActivityToPlan(testPlanId, request);

        assertNotNull(result);
    }

    @Test
    void testAddActivityToPlan_ExceedsActivityCapacity() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(100);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.addActivityToPlan(testPlanId, request);
        });
        
        assertTrue(exception.getMessage().contains("Ordered quantity"));
        assertTrue(exception.getMessage().contains("cannot exceed activity capacity"));
    }

    @Test
    void testAddActivityToPlan_StatusFulfilledWhenReachesQuota() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(50);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.addActivityToPlan(testPlanId, request);

        assertEquals("Fulfilled", testPlan.getStatus());
    }

    @Test
    void testAddActivityToPlan_StatusUnfulfilledWhenBelowQuota() {
        CreateOrderedActivityRequestDTO request = new CreateOrderedActivityRequestDTO();
        request.setActivityId("ACT001");
        request.setOrderedQuantity(25);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(activityRepository.findById("ACT001")).thenReturn(Optional.of(testActivity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.addActivityToPlan(testPlanId, request);

        assertEquals("Unfulfilled", testPlan.getStatus());
    }

    // ==================== UPDATE ORDERED ACTIVITY TESTS ====================

    @Test
    void testUpdateOrderedActivity_Success() {
        testOrderedQuantity.setPlan(testPlan);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        OrderedQuantity result = orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 30);

        assertNotNull(result);
        verify(orderedQuantityRepository).findById(testOrderedQuantityId);
        verify(orderedQuantityRepository).save(any(OrderedQuantity.class));
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testUpdateOrderedActivity_NotFound() {
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 30);
        });
        
        assertEquals("Ordered activity not found", exception.getMessage());
    }

    @Test
    void testUpdateOrderedActivity_InvalidPackageStatus() {
        testOrderedQuantity.setPlan(testPlan);
        testPackage.setStatus("Processed");
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 30);
        });
        
        assertEquals("Cannot update. Package status must be 'Pending'", exception.getMessage());
    }

    @Test
    void testUpdateOrderedActivity_ExceedsPackageQuota() {
        testOrderedQuantity.setPlan(testPlan);
        
        OrderedQuantity other = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .orderedQuota(40)
                .isDeleted(false)
                .build();
        testPlan.getOrderedQuantities().add(other);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 15);
        });
        
        assertTrue(exception.getMessage().contains("Total ordered in this plan"));
        assertTrue(exception.getMessage().contains("would exceed package quota"));
    }

    @Test
    void testUpdateOrderedActivity_ExceedsActivityCapacity() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setQuota(50);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 100);
        });
        
        assertTrue(exception.getMessage().contains("Ordered quantity"));
        assertTrue(exception.getMessage().contains("cannot exceed activity capacity"));
    }

    @Test
    void testUpdateOrderedActivity_ExcludesDeletedFromCalculation() {
        testOrderedQuantity.setPlan(testPlan);
        
        OrderedQuantity deleted = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .orderedQuota(100)
                .isDeleted(true)
                .build();
        testPlan.getOrderedQuantities().add(deleted);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        OrderedQuantity result = orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 40);

        assertNotNull(result);
    }

    @Test
    void testUpdateOrderedActivity_StatusFulfilledWhenReachesQuota() {
        testOrderedQuantity.setPlan(testPlan);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 50);

        assertEquals("Fulfilled", testPlan.getStatus());
    }

    @Test
    void testUpdateOrderedActivity_StatusUnfulfilledWhenBelowQuota() {
        testOrderedQuantity.setPlan(testPlan);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.updateOrderedActivity(testOrderedQuantityId, 30);

        assertEquals("Unfulfilled", testPlan.getStatus());
    }

    // ==================== DELETE ORDERED ACTIVITY TESTS ====================

    @Test
    void testDeleteOrderedActivity_Success() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setIsDeleted(false);
        testPlan.getOrderedQuantities().add(testOrderedQuantity);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class)))
                .thenReturn(testOrderedQuantity);
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);

        verify(orderedQuantityRepository).findById(testOrderedQuantityId);
        assertTrue(testOrderedQuantity.getIsDeleted());
        verify(orderedQuantityRepository).save(testOrderedQuantity);
        verify(planRepository).save(testPlan);
    }

    @Test
    void testDeleteOrderedActivity_NotFound() {
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);
        });
        
        assertEquals("Ordered activity not found", exception.getMessage());
    }

    @Test
    void testDeleteOrderedActivity_AlreadyDeleted() {
        testOrderedQuantity.setIsDeleted(true);
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);
        });
        
        assertEquals("Ordered activity is already deleted", exception.getMessage());
        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testDeleteOrderedActivity_InvalidPackageStatus() {
        testOrderedQuantity.setPlan(testPlan);
        testOrderedQuantity.setIsDeleted(false);
        testPackage.setStatus("Processed");
        
        when(orderedQuantityRepository.findById(testOrderedQuantityId))
                .thenReturn(Optional.of(testOrderedQuantity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderedActivityRestService.deleteOrderedActivity(testOrderedQuantityId);
        });
        
        assertEquals("Cannot delete. Package status must be 'Pending'", exception.getMessage());
    }
}
