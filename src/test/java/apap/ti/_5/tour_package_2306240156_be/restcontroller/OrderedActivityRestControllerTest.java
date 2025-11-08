package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.*;
import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306240156_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateOrderedActivityRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderedActivityRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityRepository activityRepository;

    @MockBean
    private PlanRepository planRepository;

    @MockBean
    private OrderedQuantityRepository orderedQuantityRepository;

    private UUID planId;
    private UUID orderedActivityId;
    private String activityId;
    private Plan plan;
    private Package tourPackage;
    private Activity activity;
    private OrderedQuantity orderedQuantity;
    private CreateOrderedActivityRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();
        orderedActivityId = UUID.randomUUID();
        activityId = "ACT001";

        // Setup Package
        tourPackage = new Package();
        tourPackage.setId(UUID.randomUUID().toString());
        tourPackage.setPackageName("Bali Adventure Package");
        tourPackage.setQuota(50);
        tourPackage.setPrice(5000000L);
        tourPackage.setStatus("PENDING");
        tourPackage.setStartDate(LocalDateTime.of(2024, 6, 1, 8, 0));
        tourPackage.setEndDate(LocalDateTime.of(2024, 6, 5, 18, 0));

        // Setup Plan
        plan = new Plan();
        plan.setId(planId);
        plan.setPlanName("Bali Day 1 - Water Activities");
        plan.setActivityType("Water Sports");
        plan.setStatus("Unfulfilled");
        plan.setStartDate(LocalDateTime.of(2024, 6, 1, 9, 0));
        plan.setEndDate(LocalDateTime.of(2024, 6, 1, 17, 0));
        plan.setStartLocation("Beach Point A");
        plan.setEndLocation("Beach Point B");
        plan.setTourPackage(tourPackage);
        plan.setOrderedQuantities(new ArrayList<>());
        plan.setIsDeleted(false);

        // Setup Activity
        activity = new Activity();
        activity.setId(activityId);
        activity.setActivityName("Snorkeling Adventure");
        activity.setActivityType("Water Sports");
        activity.setPrice(150000L);
        activity.setCapacity(30);
        activity.setStartDate(LocalDateTime.of(2024, 6, 1, 9, 0));
        activity.setEndDate(LocalDateTime.of(2024, 6, 1, 12, 0));
        activity.setStartLocation("Beach Point A");
        activity.setEndLocation("Beach Point B");

        // Setup OrderedQuantity
        orderedQuantity = new OrderedQuantity();
        orderedQuantity.setId(orderedActivityId);
        orderedQuantity.setActivity(activity);
        orderedQuantity.setPlan(plan);
        orderedQuantity.setOrderedQuota(10);
        orderedQuantity.setPrice(1500000L);
        orderedQuantity.setQuota(30);
        orderedQuantity.setStartDate(activity.getStartDate());
        orderedQuantity.setEndDate(activity.getEndDate());
        orderedQuantity.setIsDeleted(false);

        // Setup CreateOrderedActivityRequestDTO
        createRequest = new CreateOrderedActivityRequestDTO();
        createRequest.setActivityId(activityId);
        createRequest.setOrderedQuantity(10);
    }

    // ==================== GET /ordered-activities/eligible ====================

    @Test
    void testGetEligibleActivities_Success() throws Exception {
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findEligibleActivitiesForPlan(
                plan.getActivityType(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getStartLocation(),
                plan.getEndLocation()
        )).thenReturn(Arrays.asList(activity));

        mockMvc.perform(get("/ordered-activities/eligible")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activities", hasSize(1)))
                .andExpect(jsonPath("$.activities[0].id").value("ACT001"))
                .andExpect(jsonPath("$.activities[0].activityName").value("Snorkeling Adventure"));

        verify(planRepository, times(1)).findById(planId);
        verify(activityRepository, times(1)).findEligibleActivitiesForPlan(any(), any(), any(), any(), any());
    }

    @Test
    void testGetEligibleActivities_MultipleActivities() throws Exception {
        Activity activity2 = new Activity();
        activity2.setId("ACT002");
        activity2.setActivityName("Jet Ski");
        activity2.setActivityType("Water Sports");
        activity2.setPrice(200000L);
        activity2.setCapacity(20);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findEligibleActivitiesForPlan(any(), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(activity, activity2));

        mockMvc.perform(get("/ordered-activities/eligible")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activities", hasSize(2)));

        verify(planRepository, times(1)).findById(planId);
    }

    @Test
    void testGetEligibleActivities_EmptyList() throws Exception {
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findEligibleActivitiesForPlan(any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/ordered-activities/eligible")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activities", hasSize(0)));

        verify(planRepository, times(1)).findById(planId);
    }

    @Test
    void testGetEligibleActivities_PlanNotFound() throws Exception {
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/ordered-activities/eligible")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(containsString("Plan not found")));

        verify(planRepository, times(1)).findById(planId);
    }

    @Test
    void testGetEligibleActivities_InvalidPlanIdFormat() throws Exception {
        mockMvc.perform(get("/ordered-activities/eligible")
                        .param("planId", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(containsString("Invalid plan ID format")));

        verify(planRepository, never()).findById(any());
    }

    @Test
    void testGetEligibleActivities_PlanWithNullFields() throws Exception {
        plan.setActivityType(null);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        mockMvc.perform(get("/ordered-activities/eligible")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(containsString("incomplete data")));

        verify(planRepository, times(1)).findById(planId);
    }

    // ==================== POST /ordered-activities/create ====================

    @Test
    void testCreateOrderedActivity_Success() throws Exception {
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findAll()).thenReturn(Collections.emptyList());
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(post("/ordered-activities/create")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderedActivityId.toString()))
                .andExpect(jsonPath("$.orderedQuota").value(10))
                .andExpect(jsonPath("$.activityName").value("Snorkeling Adventure"))
                .andExpect(jsonPath("$.price").value(1500000));

        verify(planRepository, times(1)).findById(planId);
        verify(activityRepository, times(1)).findById(activityId);
        verify(orderedQuantityRepository, times(1)).save(any(OrderedQuantity.class));
        verify(planRepository, times(1)).save(any(Plan.class));
    }

    @Test
    void testCreateOrderedActivity_ExceedsActivityCapacity() throws Exception {
        createRequest.setOrderedQuantity(50);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));

        mockMvc.perform(post("/ordered-activities/create")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(containsString("exceeds available capacity")));

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testCreateOrderedActivity_ExceedsPackageQuota() throws Exception {
        // Existing OQ with 45 quantity
        OrderedQuantity existingOq = new OrderedQuantity();
        existingOq.setId(UUID.randomUUID());
        existingOq.setPlan(plan);
        existingOq.setOrderedQuota(45);
        existingOq.setIsDeleted(false);

        // Trying to add 10 more (total = 55 > 50)
        createRequest.setOrderedQuantity(10);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(existingOq));

        mockMvc.perform(post("/ordered-activities/create")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quota Exceeded"))
                .andExpect(jsonPath("$.currentTotal").value(45))
                .andExpect(jsonPath("$.packageQuota").value(50));

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testCreateOrderedActivity_MakesPlanFulfilled() throws Exception {
        // Current total = 40, adding 10 = 50 (equals package quota)
        OrderedQuantity existingOq = new OrderedQuantity();
        existingOq.setId(UUID.randomUUID());
        existingOq.setPlan(plan);
        existingOq.setOrderedQuota(40);
        existingOq.setIsDeleted(false);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(existingOq));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(post("/ordered-activities/create")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        verify(planRepository, times(1)).save(argThat(p -> 
            p.getId().equals(planId) && "Fulfilled".equals(p.getStatus())
        ));
    }

    @Test
    void testCreateOrderedActivity_PlanNotFound() throws Exception {
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/ordered-activities/create")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(containsString("Plan not found")));

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testCreateOrderedActivity_ActivityNotFound() throws Exception {
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/ordered-activities/create")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(containsString("Activity not found")));

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testCreateOrderedActivity_InvalidPlanIdFormat() throws Exception {
        mockMvc.perform(post("/ordered-activities/create")
                        .param("planId", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(containsString("Invalid ID format")));

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testCreateOrderedActivity_IgnoresDeletedOQ() throws Exception {
        OrderedQuantity deletedOq = new OrderedQuantity();
        deletedOq.setId(UUID.randomUUID());
        deletedOq.setPlan(plan);
        deletedOq.setOrderedQuota(20);
        deletedOq.setIsDeleted(true);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(deletedOq));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(post("/ordered-activities/create")
                        .param("planId", planId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        verify(orderedQuantityRepository, times(1)).save(any());
    }

    // ==================== PUT /ordered-activities/{id} ====================

    @Test
    void testUpdateOrderedActivity_Success() throws Exception {
        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.of(orderedQuantity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(orderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(put("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .param("quantity", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderedActivityId.toString()))
                .andExpect(jsonPath("$.orderedQuota").exists());

        verify(orderedQuantityRepository, times(1)).findById(orderedActivityId);
        verify(orderedQuantityRepository, times(1)).save(any(OrderedQuantity.class));
    }

    @Test
    void testUpdateOrderedActivity_ExceedsCapacity() throws Exception {
        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.of(orderedQuantity));

        mockMvc.perform(put("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .param("quantity", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Capacity Exceeded"));

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testUpdateOrderedActivity_ExceedsPackageQuota() throws Exception {
        OrderedQuantity otherOq = new OrderedQuantity();
        otherOq.setId(UUID.randomUUID());
        otherOq.setPlan(plan);
        otherOq.setOrderedQuota(40);
        otherOq.setIsDeleted(false);

        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.of(orderedQuantity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(orderedQuantity, otherOq));

        mockMvc.perform(put("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .param("quantity", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quota Exceeded"));

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testUpdateOrderedActivity_NotFound() throws Exception {
        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .param("quantity", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testUpdateOrderedActivity_MakesPlanFulfilled() throws Exception {
        // Update to make total = 50
        OrderedQuantity otherOq = new OrderedQuantity();
        otherOq.setId(UUID.randomUUID());
        otherOq.setPlan(plan);
        otherOq.setOrderedQuota(35);
        otherOq.setIsDeleted(false);

        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.of(orderedQuantity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(orderedQuantity, otherOq));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(put("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .param("quantity", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(planRepository, times(1)).save(argThat(p -> "Fulfilled".equals(p.getStatus())));
    }

    // ==================== DELETE /ordered-activities/{id} ====================

    @Test
    void testDeleteOrderedActivity_Success() throws Exception {
        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.of(orderedQuantity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(orderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(delete("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Activity removed successfully"))
                .andExpect(jsonPath("$.deletedId").value(orderedActivityId.toString()));

        verify(orderedQuantityRepository, times(1)).findById(orderedActivityId);
        verify(orderedQuantityRepository, times(1)).save(argThat(oq -> 
            oq.getId().equals(orderedActivityId) && oq.getIsDeleted()
        ));
    }

    @Test
    void testDeleteOrderedActivity_NotFound() throws Exception {
        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(orderedQuantityRepository, never()).save(any());
    }

    @Test
    void testDeleteOrderedActivity_UpdatesPlanStatus() throws Exception {
        OrderedQuantity otherOq = new OrderedQuantity();
        otherOq.setId(UUID.randomUUID());
        otherOq.setPlan(plan);
        otherOq.setOrderedQuota(50);
        otherOq.setIsDeleted(false);

        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.of(orderedQuantity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(orderedQuantity, otherOq));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(delete("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(planRepository, times(1)).save(any(Plan.class));
    }

    @Test
    void testDeleteOrderedActivity_MakesPlanFulfilled() throws Exception {
        // After delete, remaining = 50 (equals quota)
        OrderedQuantity otherOq = new OrderedQuantity();
        otherOq.setId(UUID.randomUUID());
        otherOq.setPlan(plan);
        otherOq.setOrderedQuota(50);
        otherOq.setIsDeleted(false);

        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.of(orderedQuantity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(orderedQuantity, otherOq));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(delete("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(planRepository, times(1)).save(argThat(p -> "Fulfilled".equals(p.getStatus())));
    }

    @Test
    void testDeleteOrderedActivity_MakesPlanUnfulfilled() throws Exception {
        // After delete, remaining < quota
        when(orderedQuantityRepository.findById(orderedActivityId)).thenReturn(Optional.of(orderedQuantity));
        when(orderedQuantityRepository.findAll()).thenReturn(Arrays.asList(orderedQuantity));
        when(orderedQuantityRepository.save(any(OrderedQuantity.class))).thenReturn(orderedQuantity);

        mockMvc.perform(delete("/ordered-activities/{orderedActivityId}", orderedActivityId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(planRepository, times(1)).save(argThat(p -> "Unfulfilled".equals(p.getStatus())));
    }
}