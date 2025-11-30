package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PackageRestService;
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Direct unit tests for PackageRestController - bypasses Spring
 * Security/MockMvc complexity
 * Tests controller methods directly with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class PackageRestControllerUnitTest {

    @Mock
    private PackageRestService packageRestService;

    @Mock
    private apap.ti._5.tour_package_2306240156_be.repository.PackageRepository packageRepository;

    @Mock
    private apap.ti._5.tour_package_2306240156_be.restservice.PlanRestService planRestService;

    @InjectMocks
    private PackageRestController controller;

    private AuthenticatedUser vendorUser;
    private AuthenticatedUser customerUser;
    private AuthenticatedUser adminUser;
    private PackageResponseDTO mockPackageDTO;
    private apap.ti._5.tour_package_2306240156_be.model.Package mockPackage;

    @BeforeEach
    void setUp() {
        vendorUser = new AuthenticatedUser("vendor-id", "vendor", "vendor@test.com", "Vendor", "TourPackageVendor");
        customerUser = new AuthenticatedUser("customer-id", "customer", "customer@test.com", "Customer", "Customer");
        adminUser = new AuthenticatedUser("admin-id", "admin", "admin@test.com", "Admin", "Superadmin");

        mockPackageDTO = PackageResponseDTO.builder()
                .id("pkg-123")
                .userId("vendor-id")
                .packageName("Test Package")
                .quota(10)
                .price(1000000L)
                .status("PENDING")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .creatorRole("TourPackageVendor")
                .plans(new ArrayList<>())
                .build();

        mockPackage = new apap.ti._5.tour_package_2306240156_be.model.Package();
        mockPackage.setId("pkg-123");
        mockPackage.setUserId("vendor-id");
    }

    // ==================== GET ALL Tests ====================

    @Test
    void testGetAll_AsVendor_Success() {
        List<PackageResponseDTO> packages = List.of(mockPackageDTO);
        when(packageRestService.getAll()).thenReturn(packages);

        ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> response = controller.getAll(vendorUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        verify(packageRestService, times(1)).getAll();
    }

    @Test
    void testGetAll_AsCustomer_Success() {
        List<PackageResponseDTO> packages = List.of(mockPackageDTO);
        when(packageRestService.getPackagesForCustomer(anyString())).thenReturn(packages);

        ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> response = controller.getAll(customerUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        verify(packageRestService, times(1)).getPackagesForCustomer("customer-id");
    }

    @Test
    void testGetAll_AsAdmin_Success() {
        List<PackageResponseDTO> packages = List.of(mockPackageDTO);
        when(packageRestService.getAll()).thenReturn(packages);

        ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> response = controller.getAll(adminUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(packageRestService, times(1)).getAll();
    }

    // ==================== GET BY ID Tests ====================

    @Test
    void testGetById_AsVendor_Success() {
        when(packageRestService.getById(eq("pkg-123"), eq("vendor-id"), eq("TourPackageVendor")))
                .thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.getById("pkg-123", vendorUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Package", response.getBody().getData().getPackageName());
        verify(packageRestService, times(1)).getById("pkg-123", "vendor-id", "TourPackageVendor");
    }

    @Test
    void testGetById_AsCustomer_OwnPackage_Success() {
        mockPackageDTO.setUserId("customer-id");
        mockPackageDTO.setCreatorRole("Customer");
        when(packageRestService.getById(eq("pkg-123"), eq("customer-id"), eq("Customer")))
                .thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.getById("pkg-123", customerUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetById_AsCustomer_AdminPackage_Success() {
        mockPackageDTO.setCreatorRole("Superadmin");
        when(packageRestService.getById(eq("pkg-123"), eq("customer-id"), eq("Customer")))
                .thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.getById("pkg-123", customerUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetById_AsCustomer_OtherCustomerPackage_Forbidden() {
        mockPackageDTO.setUserId("other-customer");
        mockPackageDTO.setCreatorRole("Customer");
        when(packageRestService.getById(eq("pkg-123"), eq("customer-id"), eq("Customer")))
                .thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.getById("pkg-123", customerUser);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ==================== CREATE Tests ====================

    @Test
    void testCreate_AsVendor_Success() {
        CreatePackageRequestDTO requestDTO = new CreatePackageRequestDTO();
        requestDTO.setPackageName("New Package");
        requestDTO.setQuota(15);
        requestDTO.setStartDate(LocalDateTime.now().plusDays(2));
        requestDTO.setEndDate(LocalDateTime.now().plusDays(6));

        when(packageRestService.create(any(), eq("vendor-id"), eq("TourPackageVendor")))
                .thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.create(requestDTO, vendorUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatus());
        verify(packageRestService, times(1)).create(any(), eq("vendor-id"), eq("TourPackageVendor"));
    }

    // ==================== UPDATE Tests ====================

    @Test
    void testUpdate_AsVendor_Success() {
        UpdatePackageRequestDTO requestDTO = new UpdatePackageRequestDTO();
        requestDTO.setPackageName("Updated");
        requestDTO.setQuota(20);
        requestDTO.setStartDate(LocalDateTime.now().plusDays(3));
        requestDTO.setEndDate(LocalDateTime.now().plusDays(7));

        when(packageRestService.updatePackage(eq("pkg-123"), any()))
                .thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.updatePackage("pkg-123", requestDTO,
                vendorUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(packageRestService, times(1)).updatePackage(eq("pkg-123"), any());
    }

    // ==================== DELETE Tests ====================

    @Test
    void testDelete_AsVendor_Success() {
        when(packageRestService.deleteById(eq("pkg-123")))
                .thenReturn(mockPackageDTO);

        ResponseEntity<?> response = controller.deletePackage("pkg-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(packageRestService, times(1)).deleteById("pkg-123");
    }

    // ==================== PROCESS Tests ====================

    @Test
    void testProcess_AsCustomer_Success() {
        when(packageRepository.findById("pkg-123")).thenReturn(java.util.Optional.of(mockPackage));
        when(packageRestService.processPackage(eq("pkg-123"), eq("customer-id")))
                .thenReturn(mockPackageDTO);

        ResponseEntity<?> response = controller.processPackage("pkg-123", customerUser);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response body: " + response.getBody());
        verify(packageRestService, times(1)).processPackage("pkg-123", "customer-id");
    }

    // ==================== Edge Cases ====================

    @Test
    void testGetAll_EmptyList() {
        when(packageRestService.getAll()).thenReturn(new ArrayList<>());

        ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> response = controller.getAll(vendorUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getData().size());
    }

    @Test
    void testCreate_AsCustomer_Success() {
        CreatePackageRequestDTO requestDTO = new CreatePackageRequestDTO();
        requestDTO.setPackageName("Customer Package");
        requestDTO.setQuota(5);
        requestDTO.setStartDate(LocalDateTime.now().plusDays(1));
        requestDTO.setEndDate(LocalDateTime.now().plusDays(3));

        when(packageRestService.create(any(), eq("customer-id"), eq("Customer")))
                .thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.create(requestDTO,
                customerUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testGetById_NullCreatorRole() {
        mockPackageDTO.setCreatorRole(null);
        when(packageRestService.getById(anyString(), anyString(), anyString()))
                .thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.getById("pkg-123", customerUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ==================== PAYMENT STATUS Tests ====================

    @Test
    void testUpdatePaymentStatus_Success() {
        apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePaymentStatusRequestDTO request = new apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePaymentStatusRequestDTO();
        request.setPackageId("pkg-123");
        request.setStatus(1);

        when(packageRestService.getById("pkg-123")).thenReturn(mockPackageDTO);
        when(packageRestService.updatePaymentStatus("pkg-123", 1)).thenReturn(mockPackageDTO);

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.updatePaymentStatus(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(packageRestService, times(1)).updatePaymentStatus("pkg-123", 1);
    }

    @Test
    void testUpdatePaymentStatus_Failure() {
        apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePaymentStatusRequestDTO request = new apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePaymentStatusRequestDTO();
        request.setPackageId("pkg-123");
        request.setStatus(0);

        when(packageRestService.getById("pkg-123")).thenReturn(mockPackageDTO);
        when(packageRestService.updatePaymentStatus("pkg-123", 0)).thenThrow(new RuntimeException("Update failed"));

        ResponseEntity<BaseResponseDTO<PackageResponseDTO>> response = controller.updatePaymentStatus(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== PLAN Tests ====================

    @Test
    void testCreatePlan_Success() {
        apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO request = new apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO();

        apap.ti._5.tour_package_2306240156_be.model.Plan mockPlan = new apap.ti._5.tour_package_2306240156_be.model.Plan();
        mockPlan.setId(java.util.UUID.randomUUID());

        org.springframework.validation.BindingResult bindingResult = mock(
                org.springframework.validation.BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        when(planRestService.createPlan(eq("pkg-123"), any())).thenReturn(mockPlan);

        ResponseEntity<apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO<apap.ti._5.tour_package_2306240156_be.restdto.response.PlanResponseDTO>> response = controller
                .createPlan("pkg-123", request, bindingResult);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testCreatePlan_ValidationFailure() {
        apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO request = new apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO();

        org.springframework.validation.BindingResult bindingResult = mock(
                org.springframework.validation.BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        // Mock error list
        org.springframework.validation.ObjectError error = new org.springframework.validation.ObjectError("field",
                "error message");
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        ResponseEntity<apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO<apap.ti._5.tour_package_2306240156_be.restdto.response.PlanResponseDTO>> response = controller
                .createPlan("pkg-123", request, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testShowCreatePlanForm() {
        ResponseEntity<BaseResponseDTO<String>> response = controller.showCreatePlanForm("pkg-123");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
