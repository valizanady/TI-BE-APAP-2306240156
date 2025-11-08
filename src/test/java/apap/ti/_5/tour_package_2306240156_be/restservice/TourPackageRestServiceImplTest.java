package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
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
class TourPackageRestServiceImplTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private TourPackageRestServiceImpl tourPackageRestService;

    private apap.ti._5.tour_package_2306240156_be.model.Package testPackage;
    private Plan testPlan;
    private Activity testActivity;
    private OrderedQuantity testOrderedQuantity;

    @BeforeEach
    void setUp() {
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

        testOrderedQuantity = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .activity(testActivity)
                .orderedQuota(25)
                .quota(50)
                .price(1500000L)
                .startDate(testActivity.getStartDate())
                .endDate(testActivity.getEndDate())
                .isDeleted(false)
                .build();

        testPlan = Plan.builder()
                .id(UUID.randomUUID())
                .planName("Jakarta-Bali Flight Plan")
                .activityType("Flight")
                .status("Fulfilled")
                .isDeleted(false)
                .orderedQuantities(new ArrayList<>(List.of(testOrderedQuantity)))
                .build();

        testOrderedQuantity.setPlan(testPlan);

        testPackage = apap.ti._5.tour_package_2306240156_be.model.Package.builder()
                .id("PKG001")
                .packageName("Jakarta - Bali Adventure Package")
                .status("Pending")
                .userId("user001")
                .quota(25)
                .price(0L)
                .startDate(LocalDateTime.of(2025, 11, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 11, 7, 0, 0))
                .plans(new ArrayList<>(List.of(testPlan)))
                .build();

        testPlan.setTourPackage(testPackage);
    }

    @Test
    void testProcessPackage_Success() {
        // Arrange
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(activityRepository.save(any(Activity.class))).thenReturn(testActivity);
        when(packageRepository.save(any(apap.ti._5.tour_package_2306240156_be.model.Package.class)))
                .thenReturn(testPackage);

        // Act
        PackageResponseDTO result = tourPackageRestService.processPackage("PKG001");

        // Assert
        assertNotNull(result);
        assertEquals("Processed", testPackage.getStatus());
        assertEquals(25, testActivity.getCapacity()); // 50 - 25 = 25
        verify(packageRepository, times(2)).findById("PKG001"); // Once in processPackage, once in getById
        verify(activityRepository, times(1)).save(any(Activity.class));
        verify(packageRepository, times(1)).save(any(apap.ti._5.tour_package_2306240156_be.model.Package.class));
    }

    @Test
    void testProcessPackage_PackageNotFound() {
        // Arrange
        when(packageRepository.findById("PKG999")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tourPackageRestService.processPackage("PKG999");
        });
        assertTrue(exception.getMessage().contains("Package not found"));
        verify(packageRepository).findById("PKG999");
        verify(activityRepository, never()).save(any());
        verify(packageRepository, never()).save(any());
    }

    @Test
    void testProcessPackage_InvalidStatus() {
        // Arrange
        testPackage.setStatus("Processed");
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tourPackageRestService.processPackage("PKG001");
        });
        assertTrue(exception.getMessage().contains("status must be 'Pending'"));
        verify(packageRepository).findById("PKG001");
        verify(activityRepository, never()).save(any());
    }

    @Test
    void testProcessPackage_NoActivePlans() {
        // Arrange
        testPlan.setIsDeleted(true);
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tourPackageRestService.processPackage("PKG001");
        });
        assertTrue(exception.getMessage().contains("no active plans"));
        verify(packageRepository).findById("PKG001");
        verify(activityRepository, never()).save(any());
    }

    @Test
    void testProcessPackage_UnfulfilledPlans() {
        // Arrange
        testPlan.setStatus("Unfulfilled");
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tourPackageRestService.processPackage("PKG001");
        });
        assertTrue(exception.getMessage().contains("All plans must have status 'Fulfilled'"));
        assertTrue(exception.getMessage().contains("Jakarta-Bali Flight Plan"));
        verify(packageRepository).findById("PKG001");
        verify(activityRepository, never()).save(any());
    }

    @Test
    void testProcessPackage_InsufficientCapacity() {
        // Arrange
        testActivity.setCapacity(10); // Less than orderedQuota (25)
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tourPackageRestService.processPackage("PKG001");
        });
        assertTrue(exception.getMessage().contains("insufficient capacity"));
        verify(packageRepository).findById("PKG001");
        verify(activityRepository, never()).save(any());
    }

    @Test
    void testProcessPackage_SkipsSoftDeletedOrderedQuantities() {
        // Arrange
        OrderedQuantity deletedOQ = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .activity(testActivity)
                .orderedQuota(100)
                .isDeleted(true) // Soft deleted
                .build();
        
        testPlan.getOrderedQuantities().add(deletedOQ);
        
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(activityRepository.save(any(Activity.class))).thenReturn(testActivity);
        when(packageRepository.save(any(apap.ti._5.tour_package_2306240156_be.model.Package.class)))
                .thenReturn(testPackage);

        // Act
        tourPackageRestService.processPackage("PKG001");

        // Assert
        assertEquals(25, testActivity.getCapacity()); // Should only subtract 25, not 125
        verify(activityRepository, times(1)).save(testActivity); // Only once for non-deleted OQ
    }

    @Test
    void testProcessPackage_MultipleActivities() {
        // Arrange
        Activity secondActivity = Activity.builder()
                .id("ACT002")
                .activityName("Bali Hotel")
                .activityType("Accommodation")
                .capacity(30)
                .price(2000000L)
                .startDate(LocalDateTime.of(2025, 11, 1, 14, 0))
                .endDate(LocalDateTime.of(2025, 11, 1, 15, 0))
                .startLocation("Bali")
                .endLocation("Bali")
                .build();

        OrderedQuantity secondOQ = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .activity(secondActivity)
                .orderedQuota(15)
                .quota(30)
                .price(2000000L)
                .startDate(secondActivity.getStartDate())
                .endDate(secondActivity.getEndDate())
                .isDeleted(false)
                .plan(testPlan)
                .build();

        testPlan.getOrderedQuantities().add(secondOQ);

        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(packageRepository.save(any(apap.ti._5.tour_package_2306240156_be.model.Package.class)))
                .thenReturn(testPackage);

        // Act
        tourPackageRestService.processPackage("PKG001");

        // Assert
        assertEquals(25, testActivity.getCapacity()); // 50 - 25
        assertEquals(15, secondActivity.getCapacity()); // 30 - 15
        verify(packageRepository, times(2)).findById("PKG001"); // processPackage + getById
        verify(activityRepository, times(2)).save(any(Activity.class));
    }

    @Test
    void testGetAll() {
        // Arrange
        List<apap.ti._5.tour_package_2306240156_be.model.Package> packages = List.of(testPackage);
        when(packageRepository.findAllActive()).thenReturn(packages);

        // Act
        List<PackageResponseDTO> result = tourPackageRestService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PKG001", result.get(0).getId());
        verify(packageRepository).findAllActive();
    }

    @Test
    void testGetById_Success() {
        // Arrange
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        // Act
        PackageResponseDTO result = tourPackageRestService.getById("PKG001");

        // Assert
        assertNotNull(result);
        assertEquals("PKG001", result.getId());
        assertEquals("Jakarta - Bali Adventure Package", result.getPackageName());
        verify(packageRepository).findById("PKG001");
    }

    @Test
    void testCreate_Success() {
        // Arrange
        CreatePackageRequestDTO request = new CreatePackageRequestDTO();
        request.setUserId("user001");
        request.setPackageName("New Package");
        request.setQuota(30);
        request.setStartDate(LocalDateTime.of(2025, 12, 1, 0, 0));
        request.setEndDate(LocalDateTime.of(2025, 12, 7, 0, 0));

        when(packageRepository.countByUserId("user001")).thenReturn(0L);
        when(packageRepository.save(any(apap.ti._5.tour_package_2306240156_be.model.Package.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PackageResponseDTO result = tourPackageRestService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("New Package", result.getPackageName());
        verify(packageRepository).countByUserId("user001");
        verify(packageRepository).save(any(apap.ti._5.tour_package_2306240156_be.model.Package.class));
    }

    @Test
    void testDeleteById_Success() {
        // Arrange
        testPackage.setStatus("Pending"); // ✅ Changed from "DRAFT"
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(packageRepository.save(any(apap.ti._5.tour_package_2306240156_be.model.Package.class)))
                .thenReturn(testPackage);

        // Act
        PackageResponseDTO result = tourPackageRestService.deleteById("PKG001");

        // Assert
        assertNotNull(result);
        assertEquals("DELETED", testPackage.getStatus());
        verify(packageRepository).findById("PKG001");
        verify(packageRepository).save(testPackage);
    }

    @Test
    void testDeleteById_InvalidStatus() {
        // Arrange
        testPackage.setStatus("Processed");
        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tourPackageRestService.deleteById("PKG001");
        });
        assertTrue(exception.getMessage().contains("Cannot delete package with status"));
    }

    @Test
    void testUpdatePackage_Success() {
        // Arrange
        testPackage.setStatus("Pending"); // ✅ Changed from "DRAFT"
        testPackage.setPlans(new ArrayList<>());
        
        UpdatePackageRequestDTO request = new UpdatePackageRequestDTO();
        request.setPackageName("Updated Package");
        request.setQuota(40);
        request.setStartDate(LocalDateTime.of(2025, 12, 1, 0, 0));
        request.setEndDate(LocalDateTime.of(2025, 12, 7, 0, 0));

        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));
        when(packageRepository.save(any(apap.ti._5.tour_package_2306240156_be.model.Package.class)))
                .thenReturn(testPackage);

        // Act
        PackageResponseDTO result = tourPackageRestService.updatePackage("PKG001", request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Package", testPackage.getPackageName());
        verify(packageRepository, times(2)).findById("PKG001"); // update + getById
        verify(packageRepository).save(testPackage);
    }

    @Test
    void testUpdatePackage_InvalidStatus() {
        // Arrange
        testPackage.setStatus("Processed");
        
        UpdatePackageRequestDTO request = new UpdatePackageRequestDTO();
        request.setPackageName("Updated Package");
        request.setQuota(40);
        request.setStartDate(LocalDateTime.of(2025, 12, 1, 0, 0));
        request.setEndDate(LocalDateTime.of(2025, 12, 7, 0, 0));

        when(packageRepository.findById("PKG001")).thenReturn(Optional.of(testPackage));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tourPackageRestService.updatePackage("PKG001", request);
        });
        assertTrue(exception.getMessage().contains("cannot be updated"));
    }
}
