package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.*;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdateActivityRequestDTO;
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityRestControllerUnitTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityRestController activityRestController;

    private AuthenticatedUser createAdmin() {
        return AuthenticatedUser.builder()
                .id("admin-1")
                .role("Superadmin")
                .build();
    }

    private AuthenticatedUser createVendor() {
        return AuthenticatedUser.builder()
                .id("vendor-1")
                .role("TourPackageVendor")
                .build();
    }

    private AuthenticatedUser createCustomer() {
        return AuthenticatedUser.builder()
                .id("customer-1")
                .role("Customer")
                .build();
    }

    @Test
    void testGetAllActivities_Success() {
        Activity activity = Activity.builder()
                .id("ACT-1")
                .activityName("Test Activity")
                .isDeleted(false)
                .startDate(LocalDateTime.now().plusDays(1))
                .build();
        
        when(activityRepository.findAll()).thenReturn(List.of(activity));

        ResponseEntity<?> response = activityRestController.getAllActivities(
            null, null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityRepository, times(1)).findAll();
    }

    @Test
    void testGetActivityById_Success() {
        Activity activity = Activity.builder()
                .id("ACT-1")
                .isDeleted(false)
                .build();
        
        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(activity));

        ResponseEntity<?> response = activityRestController.getActivityById("ACT-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityRepository, times(1)).findById("ACT-1");
    }

    @Test
    void testGetActivityById_NotFound() {
        when(activityRepository.findById("ACT-999")).thenReturn(Optional.empty());

        ResponseEntity<?> response = activityRestController.getActivityById("ACT-999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetActivityById_Deleted_NotFound() {
        Activity deletedActivity = Activity.builder()
                .id("ACT-1")
                .isDeleted(true)
                .build();
        
        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(deletedActivity));

        ResponseEntity<?> response = activityRestController.getActivityById("ACT-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteActivity_Success() {
        Activity activity = Activity.builder()
                .id("ACT-1")
                .vendorId("vendor-1")
                .isDeleted(false)
                .orderedQuantities(new ArrayList<>())
                .build();
        
        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        ResponseEntity<?> response = activityRestController.deleteActivity(createVendor(), "ACT-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void testDeleteActivity_AsCustomer_Forbidden() {
        ResponseEntity<?> response = activityRestController.deleteActivity(createCustomer(), "ACT-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testDeleteActivity_AlreadyDeleted() {
        Activity activity = Activity.builder()
                .id("ACT-1")
                .vendorId("vendor-1")
                .isDeleted(true)
                .build();
        
        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(activity));

        ResponseEntity<?> response = activityRestController.deleteActivity(createVendor(), "ACT-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testDeleteActivity_WithUnfulfilledOrders() {
        Plan plan = Plan.builder()
                .id(UUID.randomUUID())
                .build();
        
        OrderedQuantity oq = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .plan(plan)
                .isDeleted(false)
                .build();
        
        Activity activity = Activity.builder()
                .id("ACT-1")
                .vendorId("vendor-1")
                .isDeleted(false)
                .orderedQuantities(List.of(oq))
                .build();
        
        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(activity));

        ResponseEntity<?> response = activityRestController.deleteActivity(createVendor(), "ACT-1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    // ==================== CREATE ACTIVITY TESTS ====================

    @Test
    void testCreateActivity_AsVendor_Success() {
        CreateActivityRequestDTO request = CreateActivityRequestDTO.builder()
                .activityName("Diving")
                .activityItem("Equipment")
                .activityType("Water Sports")
                .capacity(10)
                .price(500000L)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .startLocation("Beach")
                .endLocation("Beach")
                .build();

        Activity savedActivity = Activity.builder()
                .id("ACT-20241201-001")
                .activityName("Diving")
                .build();

        when(activityRepository.countByIdPrefix(anyString())).thenReturn(0L);
        when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);

        ResponseEntity<?> response = activityRestController.createActivity(createVendor(), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void testCreateActivity_AsCustomer_Forbidden() {
        CreateActivityRequestDTO request = CreateActivityRequestDTO.builder()
                .activityName("Diving")
                .activityItem("Equipment")
                .activityType("Water Sports")
                .capacity(10)
                .price(500000L)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .startLocation("Beach")
                .endLocation("Beach")
                .build();

        ResponseEntity<?> response = activityRestController.createActivity(createCustomer(), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testCreateActivity_StartDateAfterEndDate_BadRequest() {
        CreateActivityRequestDTO request = CreateActivityRequestDTO.builder()
                .activityName("Diving")
                .activityItem("Equipment")
                .activityType("Water Sports")
                .capacity(10)
                .price(500000L)
                .startDate(LocalDateTime.now().plusDays(2))
                .endDate(LocalDateTime.now().plusDays(1)) // End before start
                .startLocation("Beach")
                .endLocation("Beach")
                .build();

        ResponseEntity<?> response = activityRestController.createActivity(createVendor(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testCreateActivity_StartDateInPast_BadRequest() {
        CreateActivityRequestDTO request = CreateActivityRequestDTO.builder()
                .activityName("Diving")
                .activityItem("Equipment")
                .activityType("Water Sports")
                .capacity(10)
                .price(500000L)
                .startDate(LocalDateTime.now().minusDays(1)) // Past date
                .endDate(LocalDateTime.now().plusDays(1))
                .startLocation("Beach")
                .endLocation("Beach")
                .build();

        ResponseEntity<?> response = activityRestController.createActivity(createVendor(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    // ==================== UPDATE ACTIVITY TESTS ====================

    @Test
    void testUpdateActivity_AsVendor_Success() {
        Activity activity = Activity.builder()
                .id("ACT-1")
                .vendorId("vendor-1")
                .activityName("Old Name")
                .isDeleted(false)
                .orderedQuantities(new ArrayList<>())
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        UpdateActivityRequestDTO request = UpdateActivityRequestDTO.builder()
                .activityName("New Name")
                .price(600000L)
                .build();

        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        ResponseEntity<?> response = activityRestController.updateActivity(createVendor(), "ACT-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void testUpdateActivity_AsCustomer_Forbidden() {
        UpdateActivityRequestDTO request = UpdateActivityRequestDTO.builder()
                .activityName("New Name")
                .build();

        ResponseEntity<?> response = activityRestController.updateActivity(createCustomer(), "ACT-1", request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testUpdateActivity_ActivityNotFound() {
        UpdateActivityRequestDTO request = UpdateActivityRequestDTO.builder()
                .activityName("New Name")
                .build();

        when(activityRepository.findById("ACT-999")).thenReturn(Optional.empty());

        ResponseEntity<?> response = activityRestController.updateActivity(createVendor(), "ACT-999", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testUpdateActivity_ActivityDeleted_BadRequest() {
        Activity deletedActivity = Activity.builder()
                .id("ACT-1")
                .vendorId("vendor-1")
                .isDeleted(true)
                .build();

        UpdateActivityRequestDTO request = UpdateActivityRequestDTO.builder()
                .activityName("New Name")
                .build();

        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(deletedActivity));

        ResponseEntity<?> response = activityRestController.updateActivity(createVendor(), "ACT-1", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testUpdateActivity_NonOwnerVendor_Forbidden() {
        Activity activity = Activity.builder()
                .id("ACT-1")
                .vendorId("other-vendor")
                .isDeleted(false)
                .build();

        UpdateActivityRequestDTO request = UpdateActivityRequestDTO.builder()
                .activityName("New Name")
                .build();

        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(activity));

        ResponseEntity<?> response = activityRestController.updateActivity(createVendor(), "ACT-1", request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void testUpdateActivity_StartDateInPast_BadRequest() {
        Activity activity = Activity.builder()
                .id("ACT-1")
                .vendorId("vendor-1")
                .isDeleted(false)
                .orderedQuantities(new ArrayList<>())
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        UpdateActivityRequestDTO request = UpdateActivityRequestDTO.builder()
                .startDate(LocalDateTime.now().minusDays(1)) // Past date
                .build();

        when(activityRepository.findById("ACT-1")).thenReturn(Optional.of(activity));

        ResponseEntity<?> response = activityRestController.updateActivity(createVendor(), "ACT-1", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(activityRepository, never()).save(any(Activity.class));
    }
}
