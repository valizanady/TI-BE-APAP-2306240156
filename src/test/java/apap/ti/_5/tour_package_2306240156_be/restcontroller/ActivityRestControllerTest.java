package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.model.Package; 
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdateActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; // Diperlukan untuk tipe return createAuthentication
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors; 
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityRestController.class)
@ActiveProfiles("test")
class ActivityRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityRepository activityRepository;

    private Activity mockActivity;
    private AuthenticatedUser mockVendorUser;
    private AuthenticatedUser mockSuperadminUser;
    private AuthenticatedUser mockCustomerUser;

    private final String ACTIVITY_ID = "ACT-20240101-001";
    private final String VENDOR_USER_ID = UUID.randomUUID().toString();
    private final String SUPERADMIN_USER_ID = UUID.randomUUID().toString();
    private final String VENDOR_ID_FROM_JWT = VENDOR_USER_ID; 

    @BeforeEach
    void setUp() {
        // Mock Activity (Menggunakan Long untuk price)
        mockActivity = Activity.builder()
                .id(ACTIVITY_ID)
                .vendorId(VENDOR_ID_FROM_JWT)
                .activityName("Eiffel Tower Visit")
                .activityItem("Ticket")
                .activityType("Accommodation") 
                .capacity(100)
                .price(1000L) 
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(6))
                .startLocation("Paris")
                .endLocation("Paris")
                .isDeleted(false)
                .orderedQuantities(Collections.emptyList())
                .build();

        // Mock AuthenticatedUser - Vendor (Override getRole() dan isVendor())
        mockVendorUser = new AuthenticatedUser(
                VENDOR_USER_ID, "vendor@example.com", "TourPackageVendor", null, VENDOR_ID_FROM_JWT 
        ) {
            @Override public boolean isVendor() { return true; } 
            @Override public boolean isSuperadmin() { return false; }
            @Override public String getRole() { return "TourPackageVendor"; } 
        };
        
        // Mock AuthenticatedUser - Superadmin (Override getRole() dan isSuperadmin())
        mockSuperadminUser = new AuthenticatedUser(
                SUPERADMIN_USER_ID, "superadmin@example.com", "Superadmin", null, SUPERADMIN_USER_ID 
        ) {
            @Override public boolean isVendor() { return false; }
            @Override public boolean isSuperadmin() { return true; }
            @Override public String getRole() { return "Superadmin"; }
        };
        
        // Mock AuthenticatedUser - Customer
        mockCustomerUser = new AuthenticatedUser(
                UUID.randomUUID().toString(), "customer@example.com", "Customer", null, null
        ) {
            @Override public boolean isVendor() { return false; }
            @Override public boolean isSuperadmin() { return false; }
            @Override public String getRole() { return "Customer"; } 
        };

        reset(activityRepository);
    }
    
    /**
     * Helper method yang membungkus AuthenticatedUser ke dalam objek Authentication.
     * Ini mengatasi error 'authentication(AuthenticatedUser) is undefined'.
     */
    private Authentication createAuthentication(AuthenticatedUser user) {
        // Buat SimpleGrantedAuthority dari Role (dikonversi ke format ROLE_UPPERCASE)
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());
        
        // Gunakan UsernamePasswordAuthenticationToken (implementasi Authentication)
        return new UsernamePasswordAuthenticationToken(
            user,           // Principal (objek AuthenticatedUser)
            null,           // Credentials
            List.of(authority) // Authorities/Roles
        );
    }

    // --- GET /api/activities ---
    
    @Test
    @WithMockUser
    void getAllActivities_noFilters_shouldReturnAllActiveSortedByDate() throws Exception {
        // Arrange
        Activity a1 = mockActivity.toBuilder().id("ACT-A").startDate(LocalDateTime.now().plusDays(1)).isDeleted(false).activityType("Flight").build();
        Activity a2 = mockActivity.toBuilder().id("ACT-B").startDate(LocalDateTime.now().plusDays(3)).isDeleted(false).activityType("Accommodation").build();
        Activity a3_deleted = mockActivity.toBuilder().id("ACT-C").startDate(LocalDateTime.now().plusDays(2)).isDeleted(true).activityType("Flight").build();
        when(activityRepository.findAll()).thenReturn(List.of(a2, a3_deleted, a1)); 
        
        mockMvc.perform(get("/api/activities")) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.data[0].id").value("ACT-A"));
    }
    
    @Test
    @WithMockUser
    void getAllActivities_filterAllNullsAndEmptyStrings_shouldReturnAll() throws Exception {
        // Arrange
        // Aktivitas dengan nilai null pada activityType, startLocation, activityItem
        Activity a1 = mockActivity.toBuilder().id("ACT-A").activityType(null).startLocation(null).endLocation("").activityItem(null).build();
        when(activityRepository.findAll()).thenReturn(List.of(a1)); 

        mockMvc.perform(get("/api/activities")
                        .param("activityType", "")
                        .param("startLocation", "")
                        .param("endLocation", "")
                        .param("search", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data[0].id").value("ACT-A"));
    }
    
    @Test
    @WithMockUser
    void getAllActivities_repositoryThrowsException_shouldReturn500() throws Exception {
        when(activityRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve activities: DB Error"));
    }
    
    // --- GET /api/activities/{id} ---

    @Test
    @WithMockUser
    void getActivityById_foundAndActive_shouldReturnActivity() throws Exception {
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(mockActivity));

        mockMvc.perform(get("/api/activities/{id}", ACTIVITY_ID))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser
    void getActivityById_foundButDeleted_shouldReturn404() throws Exception {
        Activity deletedActivity = mockActivity.toBuilder().isDeleted(true).build();
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(deletedActivity));

        mockMvc.perform(get("/api/activities/{id}", ACTIVITY_ID))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser
    void getActivityById_repositoryThrowsException_shouldReturn500() throws Exception {
        when(activityRepository.findById(ACTIVITY_ID)).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(get("/api/activities/{id}", ACTIVITY_ID))
                .andExpect(status().isInternalServerError());
    }

    // --- POST /api/activities ---
    
    @Test
    void createActivity_asVendor_success() throws Exception {
        // Arrange
        CreateActivityRequestDTO request = new CreateActivityRequestDTO(
                "New Activity", "New Item", "Flight", 50, 
                200L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 
                "Loc A", "Loc B"
        );
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String expectedIdPrefix = "ACT-" + dateStr + "-";
        
        when(activityRepository.countByIdPrefix(expectedIdPrefix)).thenReturn(0L); 
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity savedActivity = invocation.getArgument(0);
            savedActivity.setId(expectedIdPrefix + "001"); 
            return savedActivity;
        });

        // Act & Assert - Menggunakan helper createAuthentication()
        mockMvc.perform(post("/api/activities")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.vendorId").value(VENDOR_ID_FROM_JWT));
    }
    
    @Test
    void createActivity_asCustomer_accessDenied() throws Exception {
        // Arrange
        CreateActivityRequestDTO request = new CreateActivityRequestDTO(
                "New Activity", "New Item", "Flight", 50, 200L, 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), "Loc A", "Loc B"
        );

        // Act & Assert
        mockMvc.perform(post("/api/activities")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockCustomerUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void createActivity_startDateInThePast_badRequest() throws Exception {
        CreateActivityRequestDTO request = new CreateActivityRequestDTO(
                "New Activity", "New Item", "Flight", 50, 200L, 
                LocalDateTime.now().minusDays(1), // Past (Invalid)
                LocalDateTime.now().plusDays(1), 
                "Loc A", "Loc B"
        );

        mockMvc.perform(post("/api/activities")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date must be in the present or future"));
    }
    
    @Test
    void createActivity_repositoryThrowsException_shouldReturn500() throws Exception {
        CreateActivityRequestDTO request = new CreateActivityRequestDTO(
                "New Activity", "New Item", "Flight", 50, 200L, 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), "Loc A", "Loc B"
        );
        
        when(activityRepository.countByIdPrefix(anyString())).thenReturn(0L);
        when(activityRepository.save(any(Activity.class))).thenThrow(new RuntimeException("DB Save Error"));

        mockMvc.perform(post("/api/activities")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
    
    // --- PUT /api/activities/{id} ---

    @Test
    void updateActivity_asVendorOwner_success() throws Exception {
        // Arrange
        UpdateActivityRequestDTO request = new UpdateActivityRequestDTO(
                "Updated Name", null, 150, 2000L, 
                LocalDateTime.now().plusDays(10), null, "New Location A", null
        );
        
        Activity oldActivity = mockActivity.toBuilder()
                .vendorId(VENDOR_ID_FROM_JWT) 
                .startDate(LocalDateTime.now().plusDays(5)) 
                .endDate(LocalDateTime.now().plusDays(6))
                .orderedQuantities(Collections.emptyList())
                .build();
        
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(oldActivity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        mockMvc.perform(put("/api/activities/{id}", ACTIVITY_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityName").value("Updated Name"));
    }
    
    @Test
    void updateActivity_vendorNotOwner_shouldReturn403() throws Exception {
        UpdateActivityRequestDTO request = new UpdateActivityRequestDTO("Updated Name", null, null, null, null, null, null, null);
        Activity otherVendorActivity = mockActivity.toBuilder().vendorId("OTHER_VENDOR").build();
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(otherVendorActivity));

        mockMvc.perform(put("/api/activities/{id}", ACTIVITY_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void updateActivity_hasFulfilledOrderedActivities_shouldReturn400() throws Exception {
        // Arrange
        UpdateActivityRequestDTO request = new UpdateActivityRequestDTO("Updated Name", null, null, null, null, null, null, null);
        
        // Status "Fulfilled" atau "Processed" dianggap fulfilled
        Package fulfilledPackage = Package.builder().status("Fulfilled").build();
        Plan plan = Plan.builder().Package(fulfilledPackage).build();
        OrderedQuantity fulfilledOrder = OrderedQuantity.builder().plan(plan).isDeleted(false).build();
        
        Activity fulfilledActivity = mockActivity.toBuilder()
                .vendorId(VENDOR_ID_FROM_JWT)
                .orderedQuantities(List.of(fulfilledOrder))
                .build();
        
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(fulfilledActivity));

        mockMvc.perform(put("/api/activities/{id}", ACTIVITY_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot update activity that has fulfilled orderedActivities"));
    }
    
    // --- DELETE /api/activities/{id} ---

    @Test
    void deleteActivity_hasFulfilledOrderedActivities_success() throws Exception {
        // Arrange
        // Order dianggap 'fulfilled' jika Package status = "Processed"
        Package fulfilledPackage = Package.builder().status("Processed").build();
        Plan plan = Plan.builder().Package(fulfilledPackage).build();
        OrderedQuantity fulfilledOrder = OrderedQuantity.builder().plan(plan).isDeleted(false).build();
        
        Activity fulfilledActivity = mockActivity.toBuilder()
                .vendorId(VENDOR_ID_FROM_JWT)
                .isDeleted(false)
                .orderedQuantities(List.of(fulfilledOrder))
                .build();
        
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(fulfilledActivity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(delete("/api/activities/{id}", ACTIVITY_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDeleted").value(true));
    }
    
    @Test
    void deleteActivity_hasUnfulfilledOrderedActivities_shouldReturn400() throws Exception {
        // Arrange
        // Status selain "Processed" (misal "Pending") dianggap unfulfilled
        Package pendingPackage = Package.builder().status("Pending").build();
        Plan plan = Plan.builder().Package(pendingPackage).build();
        OrderedQuantity unfulfilledOrder = OrderedQuantity.builder().plan(plan).isDeleted(false).build();
        
        Activity unfulfilledActivity = mockActivity.toBuilder()
                .vendorId(VENDOR_ID_FROM_JWT)
                .isDeleted(false)
                .orderedQuantities(List.of(unfulfilledOrder))
                .build();
        
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(unfulfilledActivity));

        mockMvc.perform(delete("/api/activities/{id}", ACTIVITY_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete activity that has unfulfilled orderedActivities. Only activities with no orders or only fulfilled orders can be deleted."));
    }
    
    @Test
    void deleteActivity_alreadyDeleted_shouldReturn400() throws Exception {
        Activity deletedActivity = mockActivity.toBuilder().vendorId(VENDOR_ID_FROM_JWT).isDeleted(true).build();
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(deletedActivity));

        mockMvc.perform(delete("/api/activities/{id}", ACTIVITY_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Activity is already deleted"));
    }
    
    @Test
    void deleteActivity_repositoryThrowsException_shouldReturn500() throws Exception {
        Activity activeActivity = mockActivity.toBuilder().vendorId(VENDOR_ID_FROM_JWT).isDeleted(false).orderedQuantities(Collections.emptyList()).build();
        
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activeActivity));
        when(activityRepository.save(any(Activity.class))).thenThrow(new RuntimeException("DB Delete Error"));

        mockMvc.perform(delete("/api/activities/{id}", ACTIVITY_ID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(mockVendorUser))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to delete activity: DB Delete Error"));
    }
}