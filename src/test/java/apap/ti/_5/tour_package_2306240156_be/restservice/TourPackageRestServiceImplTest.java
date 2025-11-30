package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Package;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourPackageRestServiceImplTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository activityRepository;

    @Mock
    private apap.ti._5.tour_package_2306240156_be.restservice.BillIntegrationService billIntegrationService;

    @InjectMocks
    private TourPackageRestServiceImpl service;

    private Package mockPackage;
    private CreatePackageRequestDTO createDTO;
    private UpdatePackageRequestDTO updateDTO;

    @BeforeEach
    void setUp() {
        mockPackage = new Package();
        mockPackage.setId("pkg-123");
        mockPackage.setPackageName("Test Package");
        mockPackage.setPrice(1000000L);
        mockPackage.setQuota(10);
        mockPackage.setStatus("PENDING");
        mockPackage.setUserId("vendor-id");
        mockPackage.setCreatorRole("TourPackageVendor");
        mockPackage.setStartDate(LocalDateTime.now().plusDays(1));
        mockPackage.setEndDate(LocalDateTime.now().plusDays(5));
        mockPackage.setPlans(new ArrayList<>());
        // mockPackage.setIsDeleted(false); // Removed as field does not exist

        createDTO = new CreatePackageRequestDTO();
        createDTO.setPackageName("New Package");
        createDTO.setQuota(20);
        createDTO.setStartDate(LocalDateTime.now().plusDays(2));
        createDTO.setEndDate(LocalDateTime.now().plusDays(6));

        updateDTO = new UpdatePackageRequestDTO();
        updateDTO.setPackageName("Updated Package");
        updateDTO.setQuota(15);
        updateDTO.setStartDate(LocalDateTime.now().plusDays(3));
        updateDTO.setEndDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    void testCreate_Success() {
        when(packageRepository.save(any(Package.class))).thenReturn(mockPackage);
        when(packageRepository.countByIdPrefix(anyString())).thenReturn(0L);

        PackageResponseDTO result = service.create(createDTO, "vendor-id", "TourPackageVendor");

        assertNotNull(result);
        assertEquals("Test Package", result.getPackageName());
        verify(packageRepository, times(1)).save(any(Package.class));
    }

    @Test
    void testGetAll_Success() {
        when(packageRepository.findAllActive()).thenReturn(List.of(mockPackage));

        List<PackageResponseDTO> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Test Package", result.get(0).getPackageName());
    }

    @Test
    void testGetById_Success() {
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        PackageResponseDTO result = service.getById("pkg-123");

        assertNotNull(result);
        assertEquals("pkg-123", result.getId());
    }

    @Test
    void testUpdatePackage_Success() {
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));
        when(packageRepository.save(any(Package.class))).thenReturn(mockPackage);

        PackageResponseDTO result = service.updatePackage("pkg-123", updateDTO);

        assertNotNull(result);
        verify(packageRepository, times(1)).save(any(Package.class));
    }

    @Test
    void testDeleteById_Success() {
        mockPackage.setStatus("Pending");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));
        when(packageRepository.save(any(Package.class))).thenReturn(mockPackage);

        PackageResponseDTO result = service.deleteById("pkg-123");

        assertNotNull(result);
        assertEquals("DELETED", mockPackage.getStatus());
    }

    @Test
    void testGetPackagesForCustomer_Success() {
        when(packageRepository.findAllActive()).thenReturn(List.of(mockPackage));

        List<PackageResponseDTO> result = service.getPackagesForCustomer("customer-id");

        // Should return package because it's created by Vendor (mockPackage.creatorRole
        // = TourPackageVendor)
        assertEquals(1, result.size());
    }

    @Test
    void testProcessPackage_Success() {
        mockPackage.setStatus("Pending");

        // Setup plans and ordered quantities
        apap.ti._5.tour_package_2306240156_be.model.Plan plan = new apap.ti._5.tour_package_2306240156_be.model.Plan();
        plan.setStatus("Fulfilled");
        plan.setIsDeleted(false);

        apap.ti._5.tour_package_2306240156_be.model.Activity activity = new apap.ti._5.tour_package_2306240156_be.model.Activity();
        activity.setCapacity(100);
        activity.setActivityName("Test Activity");

        apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity oq = new apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity();
        oq.setOrderedQuota(10);
        oq.setPrice(100000L);
        oq.setIsDeleted(false);
        oq.setActivity(activity);
        oq.setPlan(plan);

        plan.setOrderedQuantities(List.of(oq));
        plan.setTourPackage(mockPackage);
        mockPackage.setPlans(List.of(plan));

        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));
        when(activityRepository.save(any(apap.ti._5.tour_package_2306240156_be.model.Activity.class)))
                .thenReturn(activity);
        when(packageRepository.save(any(Package.class))).thenReturn(mockPackage);

        apap.ti._5.tour_package_2306240156_be.restdto.response.BillResponseDTO billResponse = new apap.ti._5.tour_package_2306240156_be.restdto.response.BillResponseDTO();
        billResponse.setId("bill-123");
        when(billIntegrationService.createBillForPackage(any(Package.class), anyString())).thenReturn(billResponse);

        PackageResponseDTO result = service.processPackage("pkg-123", "customer-id");

        assertNotNull(result);
        assertEquals("Waiting for Payment", mockPackage.getStatus());
        verify(activityRepository, times(1)).save(any(apap.ti._5.tour_package_2306240156_be.model.Activity.class));
        verify(billIntegrationService, times(1)).createBillForPackage(any(Package.class), anyString());
    }

    @Test
    void testUpdatePaymentStatus_Success_Paid() {
        mockPackage.setStatus("Waiting for Payment");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));
        when(packageRepository.save(any(Package.class))).thenReturn(mockPackage);

        PackageResponseDTO result = service.updatePaymentStatus("pkg-123", 1);

        assertNotNull(result);
        assertEquals("Payment Confirmed", mockPackage.getStatus());
    }

    @Test
    void testUpdatePaymentStatus_Success_Unpaid() {
        mockPackage.setStatus("Waiting for Payment");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.updatePaymentStatus("pkg-123", 0);
        });

        assertTrue(exception.getMessage().contains("Cannot confirm payment"));
    }

    // ===== Additional tests for better coverage =====

    @Test
    void testCreate_InvalidQuota_ThrowsException() {
        createDTO.setQuota(0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.create(createDTO, "vendor-id", "TourPackageVendor");
        });

        assertTrue(exception.getMessage().contains("Quota must be greater than 0"));
    }

    @Test
    void testCreate_StartDateInPast_ThrowsException() {
        createDTO.setStartDate(LocalDateTime.now().minusDays(1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.create(createDTO, "vendor-id", "TourPackageVendor");
        });

        assertTrue(exception.getMessage().contains("Start date cannot be earlier than current date"));
    }

    @Test
    void testCreate_EndDateBeforeStartDate_ThrowsException() {
        createDTO.setStartDate(LocalDateTime.now().plusDays(10));
        createDTO.setEndDate(LocalDateTime.now().plusDays(5));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.create(createDTO, "vendor-id", "TourPackageVendor");
        });

        assertTrue(exception.getMessage().contains("End date must be after start date"));
    }

    @Test
    void testGetById_NotFound_ThrowsException() {
        when(packageRepository.findById("invalid-id")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.getById("invalid-id");
        });

        assertTrue(exception.getMessage().contains("Package not found"));
    }

    @Test
    void testGetById_WithUserIdAndRole_AsOwner() {
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        PackageResponseDTO result = service.getById("pkg-123", "vendor-id", "Customer");

        assertNotNull(result);
        assertTrue(result.getCanViewPlans());
    }

    @Test
    void testGetById_WithUserIdAndRole_AsCustomerNotOwner() {
        mockPackage.setUserId("different-user");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        PackageResponseDTO result = service.getById("pkg-123", "customer-id", "Customer");

        assertNotNull(result);
        assertFalse(result.getCanViewPlans());
        assertNotNull(result.getAccessMessage());
    }

    @Test
    void testGetById_WithUserIdAndRole_AsSuperadmin() {
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        PackageResponseDTO result = service.getById("pkg-123", "admin-id", "Superadmin");

        assertNotNull(result);
        assertTrue(result.getCanViewPlans());
    }

    @Test
    void testUpdatePackage_NotPending_ThrowsException() {
        mockPackage.setStatus("Processed");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.updatePackage("pkg-123", updateDTO);
        });

        assertTrue(exception.getMessage().contains("Only packages with status 'Pending'"));
    }

    @Test
    void testUpdatePackage_HasPlans_ThrowsException() {
        mockPackage.setStatus("Pending");
        apap.ti._5.tour_package_2306240156_be.model.Plan plan = new apap.ti._5.tour_package_2306240156_be.model.Plan();
        plan.setIsDeleted(false);
        mockPackage.setPlans(List.of(plan));

        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.updatePackage("pkg-123", updateDTO);
        });

        assertTrue(exception.getMessage().contains("already has active plans"));
    }

    @Test
    void testDeleteById_NotPending_ThrowsException() {
        mockPackage.setStatus("Processed");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.deleteById("pkg-123");
        });

        assertTrue(exception.getMessage().contains("Only Pending packages can be deleted"));
    }

    @Test
    void testProcessPackage_NotPending_ThrowsException() {
        mockPackage.setStatus("Processed");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.processPackage("pkg-123", "customer-id");
        });

        assertTrue(exception.getMessage().contains("Package status must be 'Pending'"));
    }

    @Test
    void testProcessPackage_NoPlans_ThrowsException() {
        mockPackage.setStatus("Pending");
        mockPackage.setPlans(new ArrayList<>());
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.processPackage("pkg-123", "customer-id");
        });

        assertTrue(exception.getMessage().contains("Package has no active plans"));
    }

    @Test
    void testProcessPackage_UnfulfilledPlans_ThrowsException() {
        mockPackage.setStatus("Pending");
        apap.ti._5.tour_package_2306240156_be.model.Plan plan = new apap.ti._5.tour_package_2306240156_be.model.Plan();
        plan.setStatus("Pending");
        plan.setIsDeleted(false);
        plan.setPlanName("Test Plan");
        mockPackage.setPlans(List.of(plan));

        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.processPackage("pkg-123", "customer-id");
        });

        assertTrue(exception.getMessage().contains("All plans must have status 'Fulfilled'"));
    }

    @Test
    void testProcessPackage_InsufficientActivityCapacity_ThrowsException() {
        mockPackage.setStatus("Pending");

        apap.ti._5.tour_package_2306240156_be.model.Plan plan = new apap.ti._5.tour_package_2306240156_be.model.Plan();
        plan.setStatus("Fulfilled");
        plan.setIsDeleted(false);

        apap.ti._5.tour_package_2306240156_be.model.Activity activity = new apap.ti._5.tour_package_2306240156_be.model.Activity();
        activity.setCapacity(5);
        activity.setActivityName("Test Activity");

        apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity oq = new apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity();
        oq.setOrderedQuota(10);
        oq.setPrice(100000L);
        oq.setIsDeleted(false);
        oq.setActivity(activity);

        plan.setOrderedQuantities(List.of(oq));
        mockPackage.setPlans(List.of(plan));

        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.processPackage("pkg-123", "customer-id");
        });

        assertTrue(exception.getMessage().contains("insufficient capacity"));
    }

    @Test
    void testProcessPackage_ZeroPrice_SkipsBill() {
        mockPackage.setStatus("Pending");
        mockPackage.setPrice(0L);

        apap.ti._5.tour_package_2306240156_be.model.Plan plan = new apap.ti._5.tour_package_2306240156_be.model.Plan();
        plan.setStatus("Fulfilled");
        plan.setIsDeleted(false);
        plan.setOrderedQuantities(new ArrayList<>());
        mockPackage.setPlans(List.of(plan));

        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));
        when(packageRepository.save(any(Package.class))).thenReturn(mockPackage);

        PackageResponseDTO result = service.processPackage("pkg-123", "customer-id");

        assertNotNull(result);
        assertEquals("Processed", mockPackage.getStatus());
        verify(billIntegrationService, never()).createBillForPackage(any(), anyString());
    }

    @Test
    void testProcessPackage_BillCreationFails_ThrowsException() {
        mockPackage.setStatus("Pending");
        mockPackage.setPrice(100000L);

        apap.ti._5.tour_package_2306240156_be.model.Plan plan = new apap.ti._5.tour_package_2306240156_be.model.Plan();
        plan.setStatus("Fulfilled");
        plan.setIsDeleted(false);

        apap.ti._5.tour_package_2306240156_be.model.Activity activity = new apap.ti._5.tour_package_2306240156_be.model.Activity();
        activity.setCapacity(100);
        activity.setActivityName("Test Activity");

        apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity oq = new apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity();
        oq.setOrderedQuota(10);
        oq.setPrice(10000L);
        oq.setIsDeleted(false);
        oq.setActivity(activity);

        plan.setOrderedQuantities(List.of(oq));
        mockPackage.setPlans(List.of(plan));

        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));
        when(activityRepository.save(any())).thenReturn(activity);
        when(packageRepository.save(any(Package.class))).thenReturn(mockPackage);
        when(billIntegrationService.createBillForPackage(any(), anyString()))
                .thenThrow(new RuntimeException("Bill service unavailable"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.processPackage("pkg-123", "customer-id");
        });

        assertTrue(exception.getMessage().contains("Failed to create Bill"));
        assertEquals("Processed", mockPackage.getStatus());
    }

    @Test
    void testUpdatePaymentStatus_NotFound_ThrowsException() {
        when(packageRepository.findById("invalid-id")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.updatePaymentStatus("invalid-id", 1);
        });

        assertTrue(exception.getMessage().contains("Package not found"));
    }

    @Test
    void testUpdatePaymentStatus_WrongStatus_ThrowsException() {
        mockPackage.setStatus("Pending");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.updatePaymentStatus("pkg-123", 1);
        });

        assertTrue(exception.getMessage().contains("Cannot update payment status"));
    }

    @Test
    void testUpdatePaymentStatus_InvalidStatus_ThrowsException() {
        mockPackage.setStatus("Waiting for Payment");
        when(packageRepository.findById("pkg-123")).thenReturn(Optional.of(mockPackage));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.updatePaymentStatus("pkg-123", 99);
        });

        assertTrue(exception.getMessage().contains("Invalid payment status"));
    }

    @Test
    void testGetPackagesForCustomer_OwnPackage() {
        mockPackage.setUserId("customer-123");
        mockPackage.setCreatorRole("Customer");
        when(packageRepository.findAllActive()).thenReturn(List.of(mockPackage));

        List<PackageResponseDTO> result = service.getPackagesForCustomer("customer-123");

        assertEquals(1, result.size());
    }

    @Test
    void testGetPackagesForCustomer_FilterOutNonAdminVendorPackages() {
        Package customerPackage = new Package();
        customerPackage.setId("pkg-customer");
        customerPackage.setUserId("other-customer");
        customerPackage.setCreatorRole("Customer");
        customerPackage.setPackageName("Customer Package");
        customerPackage.setPlans(new ArrayList<>());

        when(packageRepository.findAllActive()).thenReturn(List.of(mockPackage, customerPackage));

        List<PackageResponseDTO> result = service.getPackagesForCustomer("customer-123");

        // Should only return vendor package, not other customer's package
        assertEquals(1, result.size());
        assertEquals("pkg-123", result.get(0).getId());
    }
}
