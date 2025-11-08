package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePlanRequestDTO;
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
class PlanRestServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PackageRepository packageRepository;

    @InjectMocks
    private PlanRestServiceImpl planRestService;

    private apap.ti._5.tour_package_2306240156_be.model.Package testPackage;
    private Plan testPlan;
    private UUID testPlanId;
    private CreatePlanRequestDTO createRequest;
    private UpdatePlanRequestDTO updateRequest;

    @BeforeEach
    void setUp() {
        testPlanId = UUID.randomUUID();

        testPackage = apap.ti._5.tour_package_2306240156_be.model.Package.builder()
                .id("PKG001")
                .packageName("Test Package")
                .status("Pending")
                .userId("user001")
                .quota(25)
                .startDate(LocalDateTime.of(2025, 11, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 11, 7, 0, 0))
                .plans(new ArrayList<>())
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
                .isDeleted(false)
                .tourPackage(testPackage)
                .orderedQuantities(new ArrayList<>())
                .build();

        createRequest = new CreatePlanRequestDTO();
        createRequest.setPlanName("New Plan");
        createRequest.setActivityType("Flight");
        createRequest.setStartDate(LocalDateTime.of(2025, 11, 1, 8, 0));
        createRequest.setEndDate(LocalDateTime.of(2025, 11, 1, 10, 30));
        createRequest.setStartLocation("Jakarta");
        createRequest.setEndLocation("Bali");

        updateRequest = new UpdatePlanRequestDTO();
        updateRequest.setPlanName("Updated Plan");
        updateRequest.setStartDate(LocalDateTime.of(2025, 11, 2, 8, 0));
        updateRequest.setEndDate(LocalDateTime.of(2025, 11, 2, 10, 30));
        updateRequest.setStartLocation("Bandung");
        updateRequest.setEndLocation("Yogyakarta");
    }

    // ==================== CREATE PLAN TESTS ====================

    @Test
    void testCreatePlan_Success() {
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.createPlan("PKG001", createRequest);

        assertNotNull(result);
        verify(packageRepository).findById("PKG001");
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testCreatePlan_PackageNotFound() {
        when(packageRepository.findById("PKG999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.createPlan("PKG999", createRequest);
        });
        
        assertEquals("Package not found", exception.getMessage());
        verify(packageRepository).findById("PKG999");
        verify(planRepository, never()).save(any());
    }

    @Test
    void testCreatePlan_InvalidPackageStatus() {
        testPackage.setStatus("Processed");
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.createPlan("PKG001", createRequest);
        });
        
        assertEquals("Cannot create plan. Package status must be 'Pending'", exception.getMessage());
        verify(packageRepository).findById("PKG001");
        verify(planRepository, never()).save(any());
    }

    @Test
    void testCreatePlan_EndDateBeforeStartDate() {
        createRequest.setStartDate(LocalDateTime.of(2025, 11, 5, 10, 0));
        createRequest.setEndDate(LocalDateTime.of(2025, 11, 5, 8, 0));
        
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.createPlan("PKG001", createRequest);
        });
        
        assertTrue(exception.getMessage().contains("End date/time"));
        assertTrue(exception.getMessage().contains("cannot be before start date/time"));
        verify(planRepository, never()).save(any());
    }

    @Test
    void testCreatePlan_StartDateBeforePackageStartDate() {
        createRequest.setStartDate(LocalDateTime.of(2025, 10, 31, 23, 59));
        
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.createPlan("PKG001", createRequest);
        });
        
        assertTrue(exception.getMessage().contains("Plan start date/time"));
        assertTrue(exception.getMessage().contains("must be on or after package start date/time"));
        verify(planRepository, never()).save(any());
    }

    @Test
    void testCreatePlan_EndDateAfterPackageEndDate() {
        createRequest.setEndDate(LocalDateTime.of(2025, 11, 7, 0, 1));
        
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.createPlan("PKG001", createRequest);
        });
        
        assertTrue(exception.getMessage().contains("Plan end date/time"));
        assertTrue(exception.getMessage().contains("must be on or before package end date/time"));
        verify(planRepository, never()).save(any());
    }

    @Test
    void testCreatePlan_AccommodationWithDifferentLocations() {
        createRequest.setActivityType("Accommodation");
        createRequest.setStartLocation("Jakarta");
        createRequest.setEndLocation("Bali");
        
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.createPlan("PKG001", createRequest);
        });
        
        assertEquals("For Accommodation, start and end location must be the same", exception.getMessage());
        verify(planRepository, never()).save(any());
    }

    @Test
    void testCreatePlan_AccommodationWithSameLocations_Success() {
        createRequest.setActivityType("Accommodation");
        createRequest.setStartLocation("Jakarta");
        createRequest.setEndLocation("Jakarta");
        
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.createPlan("PKG001", createRequest);

        assertNotNull(result);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testCreatePlan_StartDateEqualsPackageStartDate() {
        createRequest.setStartDate(LocalDateTime.of(2025, 11, 1, 0, 0));
        
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.createPlan("PKG001", createRequest);

        assertNotNull(result);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testCreatePlan_EndDateEqualsPackageEndDate() {
        createRequest.setEndDate(LocalDateTime.of(2025, 11, 7, 0, 0));
        
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.createPlan("PKG001", createRequest);

        assertNotNull(result);
        verify(planRepository).save(any(Plan.class));
    }

    // ==================== GET PLAN BY ID TESTS ====================

    @Test
    void testGetPlanById_Success() {
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        Plan result = planRestService.getPlanById(testPlanId);

        assertNotNull(result);
        assertEquals(testPlanId, result.getId());
        assertEquals("Test Plan", result.getPlanName());
        verify(planRepository).findById(testPlanId);
    }

    @Test
    void testGetPlanById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(planRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.getPlanById(nonExistentId);
        });
        
        assertTrue(exception.getMessage().contains("Plan not found with id:"));
        verify(planRepository).findById(nonExistentId);
    }

    @Test
    void testGetPlanById_SoftDeleted() {
        testPlan.setIsDeleted(true);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.getPlanById(testPlanId);
        });
        
        assertTrue(exception.getMessage().contains("Plan not found with id:"));
    }

    @Test
    void testGetPlanById_IsDeletedFalse() {
        testPlan.setIsDeleted(false);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        Plan result = planRestService.getPlanById(testPlanId);

        assertNotNull(result);
        assertFalse(result.getIsDeleted());
    }

    @Test
    void testGetPlanById_IsDeletedNull() {
        testPlan.setIsDeleted(null);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        Plan result = planRestService.getPlanById(testPlanId);

        assertNotNull(result);
    }

    // ==================== UPDATE PLAN TESTS ====================

    @Test
    void testUpdatePlan_Success() {
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.updatePlan(testPlanId, updateRequest);

        assertNotNull(result);
        verify(planRepository).findById(testPlanId);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testUpdatePlan_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(planRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(nonExistentId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("Plan not found with id:"));
    }

    @Test
    void testUpdatePlan_PackageNotFound() {
        testPlan.setTourPackage(null);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertEquals("Package not found for this plan", exception.getMessage());
    }

    @Test
    void testUpdatePlan_InvalidPackageStatus() {
        testPackage.setStatus("Processed");
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("Cannot update plan. Package status must be 'Pending'"));
    }

    @Test
    void testUpdatePlan_HasActiveOrderedQuantities() {
        OrderedQuantity oq = new OrderedQuantity();
        oq.setIsDeleted(false);
        testPlan.setOrderedQuantities(List.of(oq));
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("Cannot update plan. Plan has"));
        assertTrue(exception.getMessage().contains("active ordered activities"));
    }

    @Test
    void testUpdatePlan_HasOnlySoftDeletedOrderedQuantities() {
        OrderedQuantity oq = new OrderedQuantity();
        oq.setIsDeleted(true);
        testPlan.setOrderedQuantities(List.of(oq));
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.updatePlan(testPlanId, updateRequest);

        assertNotNull(result);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testUpdatePlan_OrderedQuantitiesNull() {
        testPlan.setOrderedQuantities(null);
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.updatePlan(testPlanId, updateRequest);

        assertNotNull(result);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testUpdatePlan_EndDateBeforeStartDate() {
        updateRequest.setStartDate(LocalDateTime.of(2025, 11, 5, 10, 0));
        updateRequest.setEndDate(LocalDateTime.of(2025, 11, 5, 8, 0));
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("End date/time"));
        assertTrue(exception.getMessage().contains("cannot be before start date/time"));
    }

    @Test
    void testUpdatePlan_StartDateBeforePackageStartDate() {
        updateRequest.setStartDate(LocalDateTime.of(2025, 10, 31, 23, 59));
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("Plan start date/time"));
        assertTrue(exception.getMessage().contains("must be on or after package start date/time"));
    }

    @Test
    void testUpdatePlan_EndDateAfterPackageEndDate() {
        updateRequest.setEndDate(LocalDateTime.of(2025, 11, 7, 0, 1));
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("Plan end date/time"));
        assertTrue(exception.getMessage().contains("must be on or before package end date/time"));
    }

    @Test
    void testUpdatePlan_AccommodationWithDifferentLocations() {
        testPlan.setActivityType("Accommodation");
        updateRequest.setStartLocation("Jakarta");
        updateRequest.setEndLocation("Bali");
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertEquals("For Accommodation activity type, start and end location must be the same", exception.getMessage());
    }

    @Test
    void testUpdatePlan_AccommodationWithSameLocations() {
        testPlan.setActivityType("Accommodation");
        updateRequest.setStartLocation("Jakarta");
        updateRequest.setEndLocation("Jakarta");
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.updatePlan(testPlanId, updateRequest);

        assertNotNull(result);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testUpdatePlan_NonAccommodationType() {
        testPlan.setActivityType("Flight");
        updateRequest.setStartLocation("Jakarta");
        updateRequest.setEndLocation("Bali");
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        Plan result = planRestService.updatePlan(testPlanId, updateRequest);

        assertNotNull(result);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void testUpdatePlan_MultipleActiveOrderedQuantities() {
        OrderedQuantity oq1 = new OrderedQuantity();
        oq1.setIsDeleted(false);
        OrderedQuantity oq2 = new OrderedQuantity();
        oq2.setIsDeleted(false);
        testPlan.setOrderedQuantities(List.of(oq1, oq2));
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("2 active ordered activities"));
    }

    @Test
    void testUpdatePlan_MixedOrderedQuantities() {
        OrderedQuantity oq1 = new OrderedQuantity();
        oq1.setIsDeleted(false);
        OrderedQuantity oq2 = new OrderedQuantity();
        oq2.setIsDeleted(true);
        testPlan.setOrderedQuantities(List.of(oq1, oq2));
        
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.updatePlan(testPlanId, updateRequest);
        });
        
        assertTrue(exception.getMessage().contains("1 active ordered activities"));
    }

    // ==================== DELETE PLAN TESTS ====================

    @Test
    void testDeletePlan_Success() {
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        planRestService.deletePlan(testPlanId);

        assertTrue(testPlan.getIsDeleted());
        verify(planRepository).findById(testPlanId);
        verify(planRepository).save(testPlan);
    }

    @Test
    void testDeletePlan_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(planRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.deletePlan(nonExistentId);
        });
        
        assertTrue(exception.getMessage().contains("Plan not found with id:"));
    }

    @Test
    void testDeletePlan_AlreadySoftDeleted() {
        testPlan.setIsDeleted(true);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.deletePlan(testPlanId);
        });
        
        assertEquals("Plan is already deleted", exception.getMessage());
        verify(planRepository, never()).save(any());
    }

    @Test
    void testDeletePlan_PackageNotFound() {
        testPlan.setTourPackage(null);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.deletePlan(testPlanId);
        });
        
        assertEquals("Package not found for this plan", exception.getMessage());
    }

    @Test
    void testDeletePlan_InvalidPackageStatus() {
        testPackage.setStatus("Processed");
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.deletePlan(testPlanId);
        });
        
        assertTrue(exception.getMessage().contains("Cannot delete plan. Package status must be 'Pending'"));
    }

    @Test
    void testDeletePlan_PackageStatusCancelled() {
        testPackage.setStatus("Cancelled");
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            planRestService.deletePlan(testPlanId);
        });
        
        assertTrue(exception.getMessage().contains("Cannot delete plan. Package status must be 'Pending'"));
    }

    @Test
    void testDeletePlan_IsDeletedNull() {
        testPlan.setIsDeleted(null);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        planRestService.deletePlan(testPlanId);

        assertTrue(testPlan.getIsDeleted());
        verify(planRepository).save(testPlan);
    }

    @Test
    void testDeletePlan_IsDeletedFalse() {
        testPlan.setIsDeleted(false);
        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(planRepository.save(any(Plan.class))).thenReturn(testPlan);

        planRestService.deletePlan(testPlanId);

        assertTrue(testPlan.getIsDeleted());
        verify(planRepository).save(testPlan);
    }
}