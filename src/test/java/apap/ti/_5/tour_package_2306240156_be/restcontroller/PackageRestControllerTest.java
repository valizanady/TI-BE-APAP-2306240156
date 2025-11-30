package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PlanResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PackageRestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.servlet.MockMvc;
import apap.ti._5.tour_package_2306240156_be.security.ApiKeyFilter;
import apap.ti._5.tour_package_2306240156_be.security.jwt.JwtTokenFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.hamcrest.Matchers.*;

import apap.ti._5.tour_package_2306240156_be.config.TestConfig;
import org.springframework.context.annotation.Import;
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@SpringBootTest
@AutoConfigureMockMvc
class PackageRestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PackageRestService packageRestService;

        @MockBean
        private JwtTokenFilter jwtTokenFilter;

        @MockBean
        private ApiKeyFilter apiKeyFilter;

        @MockBean
        private RestTemplate restTemplate;

        private String packageId;
        private Package tourPackage;
        private PackageResponseDTO packageResponseDTO;
        private CreatePackageRequestDTO createPackageRequestDTO;
        private UpdatePackageRequestDTO updatePackageRequestDTO;

        private AuthenticatedUser mockCustomerUser;
        private AuthenticatedUser mockVendorUser;
        private AuthenticatedUser mockAdminUser;

        @BeforeEach
        void setUp() {
                packageId = UUID.randomUUID().toString();

                // Setup Package entity
                tourPackage = new Package();
                tourPackage.setId(packageId);
                tourPackage.setUserId("user-123");
                tourPackage.setPackageName("Bali Adventure Package");
                tourPackage.setQuota(20);
                tourPackage.setPrice(5000000L);
                tourPackage.setStatus("PENDING");
                tourPackage.setStartDate(LocalDateTime.of(2024, 6, 1, 8, 0));
                tourPackage.setEndDate(LocalDateTime.of(2024, 6, 5, 18, 0));
                tourPackage.setPlans(new ArrayList<>());

                // Setup PackageResponseDTO
                packageResponseDTO = PackageResponseDTO.builder()
                                .id(packageId)
                                .userId("user-123")
                                .packageName("Bali Adventure Package")
                                .quota(20)
                                .price(5000000L)
                                .status("PENDING")
                                .startDate(LocalDateTime.of(2024, 6, 1, 8, 0))
                                .endDate(LocalDateTime.of(2024, 6, 5, 18, 0))
                                .plans(new ArrayList<>())
                                .build();

                // Setup CreatePackageRequestDTO
                createPackageRequestDTO = new CreatePackageRequestDTO();
                createPackageRequestDTO.setPackageName("Bali Adventure Package");

                // Setup Mock Users
                mockCustomerUser = new AuthenticatedUser("user-123", "customer_user", "customer@example.com",
                                "Customer Name", "Customer");
                mockVendorUser = new AuthenticatedUser("vendor-123", "vendor_user", "vendor@example.com", "Vendor Name",
                                "TourPackageVendor");
                mockAdminUser = new AuthenticatedUser("admin-123", "admin_user", "admin@example.com", "Admin Name",
                                "Superadmin");

                createPackageRequestDTO.setQuota(20);
                createPackageRequestDTO.setStartDate(LocalDateTime.of(2024, 6, 1, 8, 0));
                createPackageRequestDTO.setEndDate(LocalDateTime.of(2024, 6, 5, 18, 0));

                // Setup UpdatePackageRequestDTO
                updatePackageRequestDTO = UpdatePackageRequestDTO.builder()
                                .packageName("Updated Bali Package")
                                .quota(25)
                                .startDate(LocalDateTime.of(2024, 7, 1, 8, 0))
                                .endDate(LocalDateTime.of(2024, 7, 5, 18, 0))
                                .build();
        }

        // ==================== GET /package ====================

        @Test
        void testGetAll_Success() throws Exception {
                List<PackageResponseDTO> packages = new ArrayList<>();
                packages.add(packageResponseDTO);

                PackageResponseDTO package2 = PackageResponseDTO.builder()
                                .id(UUID.randomUUID().toString())
                                .userId("user-456")
                                .packageName("Jakarta City Tour")
                                .quota(15)
                                .price(2000000L)
                                .status("PROCESSED")
                                .startDate(LocalDateTime.of(2024, 7, 1, 8, 0))
                                .endDate(LocalDateTime.of(2024, 7, 3, 18, 0))
                                .plans(new ArrayList<>())
                                .build();
                packages.add(package2);

                when(packageRestService.getAll()).thenReturn(packages);

                mockMvc.perform(get("/api/package")
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Success"))
                                .andExpect(jsonPath("$.data", hasSize(2)))
                                .andExpect(jsonPath("$.data[0].id").value(packageId))
                                .andExpect(jsonPath("$.data[0].packageName").value("Bali Adventure Package"))
                                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                                .andExpect(jsonPath("$.data[1].packageName").value("Jakarta City Tour"))
                                .andExpect(jsonPath("$.data[1].status").value("PROCESSED"));

                verify(packageRestService, times(1)).getAll();
        }

        @Test
        void testGetAll_EmptyList() throws Exception {
                when(packageRestService.getAll()).thenReturn(new ArrayList<>());

                mockMvc.perform(get("/api/package")
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Success"))
                                .andExpect(jsonPath("$.data", hasSize(0)));

                verify(packageRestService, times(1)).getAll();
        }

        // ==================== GET /package/{id} ====================

        @Test
        void testGetById_Success() throws Exception {
                when(packageRestService.getById(packageId)).thenReturn(packageResponseDTO);

                mockMvc.perform(get("/api/package/{id}", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Success"))
                                .andExpect(jsonPath("$.data.id").value(packageId))
                                .andExpect(jsonPath("$.data.packageName").value("Bali Adventure Package"))
                                .andExpect(jsonPath("$.data.userId").value("user-123"))
                                .andExpect(jsonPath("$.data.quota").value(20))
                                .andExpect(jsonPath("$.data.price").value(5000000))
                                .andExpect(jsonPath("$.data.status").value("PENDING"));

                verify(packageRestService, times(1)).getById(packageId);
        }

        @Test
        void testGetById_PackageNotFound() throws Exception {
                when(packageRestService.getById(packageId))
                                .thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(get("/api/package/{id}", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").value("Package not found"))
                                .andExpect(jsonPath("$.data").doesNotExist());

                verify(packageRestService, times(1)).getById(packageId);
        }

        // ==================== POST /package/create ====================

        @Test
        void testCreate_Success() throws Exception {
                when(packageRestService.create(any(CreatePackageRequestDTO.class), anyString(), anyString()))
                                .thenReturn(packageResponseDTO);

                mockMvc.perform(post("/api/package/create")
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createPackageRequestDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value(201))
                                .andExpect(jsonPath("$.message").value("Created"))
                                .andExpect(jsonPath("$.data.id").value(packageId))
                                .andExpect(jsonPath("$.data.packageName").value("Bali Adventure Package"))
                                .andExpect(jsonPath("$.data.userId").value("user-123"))
                                .andExpect(jsonPath("$.data.quota").value(20))
                                .andExpect(jsonPath("$.data.status").value("PENDING"));

                verify(packageRestService, times(1)).create(any(CreatePackageRequestDTO.class), anyString(),
                                anyString());
        }

        @Test
        void testCreate_ValidationError_EmptyPackageName() throws Exception {
                CreatePackageRequestDTO invalidRequest = new CreatePackageRequestDTO();

                invalidRequest.setQuota(20);
                invalidRequest.setStartDate(LocalDateTime.of(2024, 6, 1, 8, 0));
                invalidRequest.setEndDate(LocalDateTime.of(2024, 6, 5, 18, 0));

                mockMvc.perform(post("/api/package/create")
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));

                verify(packageRestService, never()).create(any(), anyString(), anyString());
        }

        // Removed testCreate_ValidationError_EmptyUserId as userId is taken from token

        @Test
        void testCreate_ValidationError_InvalidQuota() throws Exception {
                CreatePackageRequestDTO invalidRequest = new CreatePackageRequestDTO();
                invalidRequest.setPackageName("Test Package");

                invalidRequest.setQuota(-5);
                invalidRequest.setStartDate(LocalDateTime.of(2024, 6, 1, 8, 0));
                invalidRequest.setEndDate(LocalDateTime.of(2024, 6, 5, 18, 0));

                mockMvc.perform(post("/api/package/create")
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));

                verify(packageRestService, never()).create(any(), anyString(), anyString());
        }

        @Test
        void testCreate_ValidationError_NullStartDate() throws Exception {
                CreatePackageRequestDTO invalidRequest = new CreatePackageRequestDTO();
                invalidRequest.setPackageName("Test Package");

                invalidRequest.setQuota(20);
                invalidRequest.setEndDate(LocalDateTime.of(2024, 6, 5, 18, 0));

                mockMvc.perform(post("/api/package/create")
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));

                verify(packageRestService, never()).create(any(), anyString(), anyString());
        }

        @Test
        void testCreate_ValidationError_NullEndDate() throws Exception {
                CreatePackageRequestDTO invalidRequest = new CreatePackageRequestDTO();
                invalidRequest.setPackageName("Test Package");

                invalidRequest.setQuota(20);
                invalidRequest.setStartDate(LocalDateTime.of(2024, 6, 1, 8, 0));

                mockMvc.perform(post("/api/package/create")
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));

                verify(packageRestService, never()).create(any(), anyString(), anyString());
        }

        @Test
        void testCreate_ServiceException() throws Exception {
                when(packageRestService.create(any(CreatePackageRequestDTO.class), anyString(), anyString()))
                                .thenThrow(new RuntimeException("Database error"));

                mockMvc.perform(post("/api/package/create")
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createPackageRequestDTO)))
                                .andExpect(status().isInternalServerError());

                verify(packageRestService, times(1)).create(any(CreatePackageRequestDTO.class), anyString(),
                                anyString());
        }

        // ==================== DELETE /package/{id}/delete ====================

        @Test
        void testDeletePackage_Success() throws Exception {
                when(packageRestService.deleteById(packageId)).thenReturn(packageResponseDTO);

                mockMvc.perform(delete("/api/package/{id}/delete", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Package deleted successfully"))
                                .andExpect(jsonPath("$.data.id").value(packageId))
                                .andExpect(jsonPath("$.data.packageName").value("Bali Adventure Package"));

                verify(packageRestService, times(1)).deleteById(packageId);
        }

        @Test
        void testDeletePackage_PackageNotFound() throws Exception {
                when(packageRestService.deleteById(packageId))
                                .thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(delete("/api/package/{id}/delete", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Package not found"))
                                .andExpect(jsonPath("$.data").doesNotExist());

                verify(packageRestService, times(1)).deleteById(packageId);
        }

        @Test
        void testDeletePackage_AlreadyProcessed() throws Exception {
                when(packageRestService.deleteById(packageId))
                                .thenThrow(new RuntimeException("Cannot delete processed package"));

                mockMvc.perform(delete("/api/package/{id}/delete", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Cannot delete processed package"));

                verify(packageRestService, times(1)).deleteById(packageId);
        }

        @Test
        void testDeletePackage_HasPlans() throws Exception {
                when(packageRestService.deleteById(packageId))
                                .thenThrow(new RuntimeException("Package has active plans"));

                mockMvc.perform(delete("/api/package/{id}/delete", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Package has active plans"));

                verify(packageRestService, times(1)).deleteById(packageId);
        }

        // ==================== PUT /package/{id}/edit ====================

        @Test
        void testUpdatePackage_Success() throws Exception {
                PackageResponseDTO updatedResponse = PackageResponseDTO.builder()
                                .id(packageId)
                                .userId("user-123")
                                .packageName("Updated Bali Package")
                                .quota(25)
                                .price(6000000L)
                                .status("PENDING")
                                .startDate(LocalDateTime.of(2024, 7, 1, 8, 0))
                                .endDate(LocalDateTime.of(2024, 7, 5, 18, 0))
                                .plans(new ArrayList<>())
                                .build();

                when(packageRestService.updatePackage(eq(packageId), any(UpdatePackageRequestDTO.class)))
                                .thenReturn(updatedResponse);

                mockMvc.perform(put("/api/package/{id}/edit", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePackageRequestDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Package updated successfully"))
                                .andExpect(jsonPath("$.data.id").value(packageId))
                                .andExpect(jsonPath("$.data.packageName").value("Updated Bali Package"))
                                .andExpect(jsonPath("$.data.quota").value(25))
                                .andExpect(jsonPath("$.data.price").value(6000000));

                verify(packageRestService, times(1)).updatePackage(eq(packageId), any(UpdatePackageRequestDTO.class));
        }

        @Test
        void testUpdatePackage_ValidationError_EmptyPackageName() throws Exception {
                UpdatePackageRequestDTO invalidRequest = UpdatePackageRequestDTO.builder()
                                .quota(25)
                                .startDate(LocalDateTime.of(2024, 7, 1, 8, 0))
                                .endDate(LocalDateTime.of(2024, 7, 5, 18, 0))
                                .build();

                mockMvc.perform(put("/api/package/{id}/edit", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));

                verify(packageRestService, never()).updatePackage(any(), any());
        }

        @Test
        void testUpdatePackage_ValidationError_InvalidQuota() throws Exception {
                UpdatePackageRequestDTO invalidRequest = UpdatePackageRequestDTO.builder()
                                .packageName("Updated Package")
                                .quota(-10)
                                .startDate(LocalDateTime.of(2024, 7, 1, 8, 0))
                                .endDate(LocalDateTime.of(2024, 7, 5, 18, 0))
                                .build();

                mockMvc.perform(put("/api/package/{id}/edit", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));

                verify(packageRestService, never()).updatePackage(any(), any());
        }

        @Test
        void testUpdatePackage_ValidationError_NullDates() throws Exception {
                UpdatePackageRequestDTO invalidRequest = UpdatePackageRequestDTO.builder()
                                .packageName("Updated Package")
                                .quota(25)
                                .build();

                mockMvc.perform(put("/api/package/{id}/edit", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));

                verify(packageRestService, never()).updatePackage(any(), any());
        }

        @Test
        void testUpdatePackage_PackageNotFound() throws Exception {
                when(packageRestService.updatePackage(eq(packageId), any(UpdatePackageRequestDTO.class)))
                                .thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(put("/api/package/{id}/edit", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePackageRequestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Package not found"))
                                .andExpect(jsonPath("$.data").doesNotExist());

                verify(packageRestService, times(1)).updatePackage(eq(packageId), any(UpdatePackageRequestDTO.class));
        }

        @Test
        void testUpdatePackage_AlreadyProcessed() throws Exception {
                when(packageRestService.updatePackage(eq(packageId), any(UpdatePackageRequestDTO.class)))
                                .thenThrow(new RuntimeException("Cannot update processed package"));

                mockMvc.perform(put("/api/package/{id}/edit", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePackageRequestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Cannot update processed package"));

                verify(packageRestService, times(1)).updatePackage(eq(packageId), any(UpdatePackageRequestDTO.class));
        }

        @Test
        void testUpdatePackage_PackageDeleted() throws Exception {
                when(packageRestService.updatePackage(eq(packageId), any(UpdatePackageRequestDTO.class)))
                                .thenThrow(new RuntimeException("Cannot update deleted package"));

                mockMvc.perform(put("/api/package/{id}/edit", packageId)
                                .with(authentication(createAuthentication(mockVendorUser)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePackageRequestDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Cannot update deleted package"));

                verify(packageRestService, times(1)).updatePackage(eq(packageId), any(UpdatePackageRequestDTO.class));
        }

        // ==================== PUT /package/{id}/process ====================

        @Test
        void testProcessPackage_Success() throws Exception {
                PackageResponseDTO processedResponse = PackageResponseDTO.builder()
                                .id(packageId)
                                .userId("user-123")
                                .packageName("Bali Adventure Package")
                                .quota(20)
                                .price(5000000L)
                                .status("PROCESSED")
                                .startDate(LocalDateTime.of(2024, 6, 1, 8, 0))
                                .endDate(LocalDateTime.of(2024, 6, 5, 18, 0))
                                .plans(new ArrayList<>())
                                .build();

                when(packageRestService.processPackage(eq(packageId), anyString())).thenReturn(processedResponse);

                mockMvc.perform(put("/api/package/{id}/process", packageId)
                                .with(authentication(createAuthentication(mockCustomerUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(200))
                                .andExpect(jsonPath("$.message").value("Package processed successfully"))
                                .andExpect(jsonPath("$.data.id").value(packageId))
                                .andExpect(jsonPath("$.data.status").value("PROCESSED"));

                verify(packageRestService, times(1)).processPackage(eq(packageId), anyString());
        }

        @Test
        void testProcessPackage_PackageNotFound() throws Exception {
                when(packageRestService.processPackage(eq(packageId), anyString()))
                                .thenThrow(new RuntimeException("Package not found"));

                mockMvc.perform(put("/api/package/{id}/process", packageId)
                                .with(authentication(createAuthentication(mockCustomerUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Package not found"))
                                .andExpect(jsonPath("$.data").doesNotExist());

                verify(packageRestService, times(1)).processPackage(eq(packageId), anyString());
        }

        @Test
        void testProcessPackage_AlreadyProcessed() throws Exception {
                when(packageRestService.processPackage(eq(packageId), anyString()))
                                .thenThrow(new RuntimeException("Package already processed"));

                mockMvc.perform(put("/api/package/{id}/process", packageId)
                                .with(authentication(createAuthentication(mockCustomerUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Package already processed"));

                verify(packageRestService, times(1)).processPackage(eq(packageId), anyString());
        }

        @Test
        void testProcessPackage_NoPlans() throws Exception {
                when(packageRestService.processPackage(eq(packageId), anyString()))
                                .thenThrow(new RuntimeException("Cannot process package without plans"));

                mockMvc.perform(put("/api/package/{id}/process", packageId)
                                .with(authentication(createAuthentication(mockCustomerUser)))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Package has no plans"));

                verify(packageRestService, times(1)).processPackage(eq(packageId), anyString());
        }

        @Test
        void testProcessPackage_InvalidStatus() throws Exception {
                when(packageRestService.processPackage(eq(packageId), anyString()))
                                .thenThrow(new RuntimeException("Package status must be PENDING"));

                mockMvc.perform(put("/api/package/{id}/process", packageId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Package status must be PENDING"));

                verify(packageRestService, times(1)).processPackage(eq(packageId), anyString());
        }

        @Test
        void testProcessPackage_PackageDeleted() throws Exception {
                when(packageRestService.processPackage(eq(packageId), anyString()))
                                .thenThrow(new RuntimeException("Cannot process deleted package"));

                mockMvc.perform(put("/api/package/{id}/process", packageId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Cannot process deleted package"));

                verify(packageRestService, times(1)).processPackage(eq(packageId), anyString());
        }

        private Authentication createAuthentication(AuthenticatedUser user) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());
                return new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
        }
}
