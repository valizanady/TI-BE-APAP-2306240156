package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PlanRestService;
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
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlanRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanRestService planRestService;

    private String packageId;
    private UUID planId;
    private Plan plan;
    private Package tourPackage;
    private CreatePlanRequestDTO createPlanRequestDTO;
    private UpdatePlanRequestDTO updatePlanRequestDTO;

    @BeforeEach
    void setUp() {
        packageId = UUID.randomUUID().toString();
        planId = UUID.randomUUID();

        // Setup Package
        tourPackage = new Package();
        tourPackage.setId(packageId);
        tourPackage.setPackageName("Bali Adventure Package");
        tourPackage.setQuota(20);
        tourPackage.setPrice(5000000L);
        tourPackage.setStatus("PENDING");
        tourPackage.setStartDate(LocalDateTime.of(2024, 6, 1, 8, 0));
        tourPackage.setEndDate(LocalDateTime.of(2024, 6, 5, 18, 0));

        // Setup Plan
        plan = new Plan();
        plan.setId(planId);
        plan.setPlanName("Bali Day 1 - Water Activities");
        plan.setActivityType("Water Sports");
        plan.setStatus("PENDING");
        plan.setStartDate(LocalDateTime.of(2024, 6, 1, 9, 0));
        plan.setEndDate(LocalDateTime.of(2024, 6, 1, 17, 0));
        plan.setStartLocation("Beach Point A");
        plan.setEndLocation("Beach Point B");
        plan.setTourPackage(tourPackage);
        plan.setOrderedQuantities(new ArrayList<>());
        plan.setIsDeleted(false);

        // Setup CreatePlanRequestDTO
        createPlanRequestDTO = new CreatePlanRequestDTO();
        createPlanRequestDTO.setPlanName("Bali Day 1 - Water Activities");
        createPlanRequestDTO.setActivityType("Water Sports");
        createPlanRequestDTO.setStartDate(LocalDateTime.of(2024, 6, 1, 9, 0));
        createPlanRequestDTO.setEndDate(LocalDateTime.of(2024, 6, 1, 17, 0));
        createPlanRequestDTO.setStartLocation("Beach Point A");
        createPlanRequestDTO.setEndLocation("Beach Point B");

        // Setup UpdatePlanRequestDTO
        updatePlanRequestDTO = new UpdatePlanRequestDTO();
        updatePlanRequestDTO.setPlanName("Updated Bali Day 1");
        updatePlanRequestDTO.setStartDate(LocalDateTime.of(2024, 6, 2, 9, 0));
        updatePlanRequestDTO.setEndDate(LocalDateTime.of(2024, 6, 2, 17, 0));
        updatePlanRequestDTO.setStartLocation("Temple A");
        updatePlanRequestDTO.setEndLocation("Temple B");
    }

    // ==================== POST /package/{id}/plans/create ====================

    @Test
    void testCreatePlan_Success() throws Exception {
        when(planRestService.createPlan(eq(packageId), any(CreatePlanRequestDTO.class)))
                .thenReturn(plan);

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPlanRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Plan created successfully"))
                .andExpect(jsonPath("$.data.id").value(planId.toString()))
                .andExpect(jsonPath("$.data.planName").value("Bali Day 1 - Water Activities"))
                .andExpect(jsonPath("$.data.activityType").value("Water Sports"))
                .andExpect(jsonPath("$.data.startLocation").value("Beach Point A"))
                .andExpect(jsonPath("$.data.endLocation").value("Beach Point B"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.packageId").value(packageId));

        verify(planRestService, times(1))
                .createPlan(eq(packageId), any(CreatePlanRequestDTO.class));
    }

    @Test
    void testCreatePlan_WithOrderedQuantities() throws Exception {
        // Add ordered quantities to plan
        Activity activity = new Activity();
        activity.setId(UUID.randomUUID().toString());
        activity.setActivityName("Snorkeling");
        activity.setPrice(150000L);

        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setActivity(activity);
        oq.setOrderedQuota(2);
        oq.setPrice(150000L);
        oq.setIsDeleted(false);
        oq.setPlan(plan);

        plan.getOrderedQuantities().add(oq);

        when(planRestService.createPlan(eq(packageId), any(CreatePlanRequestDTO.class)))
                .thenReturn(plan);

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPlanRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.price").value(300000L)); // 2 * 150000

        verify(planRestService, times(1))
                .createPlan(eq(packageId), any(CreatePlanRequestDTO.class));
    }

    @Test
    void testCreatePlan_WithDeletedOrderedQuantities() throws Exception {
        // Add deleted ordered quantity (should be excluded from price)
        Activity activity = new Activity();
        activity.setId(UUID.randomUUID().toString());
        activity.setActivityName("Snorkeling");
        activity.setPrice(150000L);

        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setActivity(activity);
        oq.setOrderedQuota(2);
        oq.setPrice(150000L);
        oq.setIsDeleted(true); // Soft deleted
        oq.setPlan(plan);

        plan.getOrderedQuantities().add(oq);

        when(planRestService.createPlan(eq(packageId), any(CreatePlanRequestDTO.class)))
                .thenReturn(plan);

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPlanRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.price").value(0L)); // Excluded

        verify(planRestService, times(1))
                .createPlan(eq(packageId), any(CreatePlanRequestDTO.class));
    }

    @Test
    void testCreatePlan_ValidationError_EmptyPlanName() throws Exception {
        CreatePlanRequestDTO invalidRequest = new CreatePlanRequestDTO();
        invalidRequest.setActivityType("Water Sports");
        invalidRequest.setStartDate(LocalDateTime.of(2024, 6, 1, 9, 0));
        invalidRequest.setEndDate(LocalDateTime.of(2024, 6, 1, 17, 0));
        invalidRequest.setStartLocation("Beach A");
        invalidRequest.setEndLocation("Beach B");

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());

        verify(planRestService, never()).createPlan(any(), any());
    }

    @Test
    void testCreatePlan_ValidationError_EmptyActivityType() throws Exception {
        CreatePlanRequestDTO invalidRequest = new CreatePlanRequestDTO();
        invalidRequest.setPlanName("Test Plan");
        invalidRequest.setStartDate(LocalDateTime.of(2024, 6, 1, 9, 0));
        invalidRequest.setEndDate(LocalDateTime.of(2024, 6, 1, 17, 0));
        invalidRequest.setStartLocation("Beach A");
        invalidRequest.setEndLocation("Beach B");

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(planRestService, never()).createPlan(any(), any());
    }

    @Test
    void testCreatePlan_ValidationError_EmptyDates() throws Exception {
        CreatePlanRequestDTO invalidRequest = new CreatePlanRequestDTO();
        invalidRequest.setPlanName("Test Plan");
        invalidRequest.setActivityType("Water Sports");
        invalidRequest.setStartLocation("Beach A");
        invalidRequest.setEndLocation("Beach B");

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(planRestService, never()).createPlan(any(), any());
    }

    @Test
    void testCreatePlan_ValidationError_EmptyLocations() throws Exception {
        CreatePlanRequestDTO invalidRequest = new CreatePlanRequestDTO();
        invalidRequest.setPlanName("Test Plan");
        invalidRequest.setActivityType("Water Sports");
        invalidRequest.setStartDate(LocalDateTime.of(2024, 6, 1, 9, 0));
        invalidRequest.setEndDate(LocalDateTime.of(2024, 6, 1, 17, 0));

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(planRestService, never()).createPlan(any(), any());
    }

    @Test
    void testCreatePlan_PackageNotFound() throws Exception {
        when(planRestService.createPlan(eq(packageId), any(CreatePlanRequestDTO.class)))
                .thenThrow(new RuntimeException("Package not found"));

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPlanRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Package not found"));

        verify(planRestService, times(1))
                .createPlan(eq(packageId), any(CreatePlanRequestDTO.class));
    }

    @Test
    void testCreatePlan_PackageAlreadyProcessed() throws Exception {
        when(planRestService.createPlan(eq(packageId), any(CreatePlanRequestDTO.class)))
                .thenThrow(new RuntimeException("Cannot add plan to processed package"));

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPlanRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot add plan to processed package"));

        verify(planRestService, times(1))
                .createPlan(eq(packageId), any(CreatePlanRequestDTO.class));
    }

    @Test
    void testCreatePlan_DuplicatePlanName() throws Exception {
        when(planRestService.createPlan(eq(packageId), any(CreatePlanRequestDTO.class)))
                .thenThrow(new RuntimeException("Plan with this name already exists"));

        mockMvc.perform(post("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPlanRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Plan with this name already exists"));

        verify(planRestService, times(1))
                .createPlan(eq(packageId), any(CreatePlanRequestDTO.class));
    }

    // ==================== GET /plans/{id} ====================

    @Test
    void testGetPlanDetail_Success() throws Exception {
        when(planRestService.getPlanById(planId)).thenReturn(plan);

        mockMvc.perform(get("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plan detail retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(planId.toString()))
                .andExpect(jsonPath("$.data.planName").value("Bali Day 1 - Water Activities"))
                .andExpect(jsonPath("$.data.activityType").value("Water Sports"))
                .andExpect(jsonPath("$.data.packageName").value("Bali Adventure Package"))
                .andExpect(jsonPath("$.data.packageStatus").value("PENDING"));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetPlanDetail_WithOrderedQuantities() throws Exception {
        Activity activity = new Activity();
        activity.setId(UUID.randomUUID().toString());
        activity.setActivityName("Snorkeling");
        activity.setPrice(150000L);
        activity.setCapacity(10);

        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setActivity(activity);
        oq.setOrderedQuota(2);
        oq.setPrice(150000L);
        oq.setIsDeleted(false);
        oq.setPlan(plan);
        oq.setStartDate(LocalDateTime.of(2024, 6, 1, 9, 0));
        oq.setEndDate(LocalDateTime.of(2024, 6, 1, 12, 0));

        plan.getOrderedQuantities().add(oq);

        when(planRestService.getPlanById(planId)).thenReturn(plan);

        mockMvc.perform(get("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderedQuantities", hasSize(1)))
                .andExpect(jsonPath("$.data.orderedQuantities[0].activityName").value("Snorkeling"))
                .andExpect(jsonPath("$.data.orderedQuantities[0].orderedQuota").value(2))
                .andExpect(jsonPath("$.data.orderedQuantities[0].quota").value(10))
                .andExpect(jsonPath("$.data.orderedQuantities[0].total").value(300000L));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetPlanDetail_WithDeletedOrderedQuantities() throws Exception {
        Activity activity = new Activity();
        activity.setId(UUID.randomUUID().toString());
        activity.setActivityName("Snorkeling");
        activity.setPrice(150000L);

        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setActivity(activity);
        oq.setOrderedQuota(2);
        oq.setPrice(150000L);
        oq.setIsDeleted(true); // Soft deleted
        oq.setPlan(plan);

        plan.getOrderedQuantities().add(oq);

        when(planRestService.getPlanById(planId)).thenReturn(plan);

        mockMvc.perform(get("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderedQuantities", hasSize(0))) // Filtered out
                .andExpect(jsonPath("$.data.price").value(0L));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetPlanDetail_WithNullActivity() throws Exception {
        OrderedQuantity oq = new OrderedQuantity();
        oq.setId(UUID.randomUUID());
        oq.setActivity(null); // Null activity
        oq.setOrderedQuota(2);
        oq.setPrice(150000L);
        oq.setIsDeleted(false);
        oq.setPlan(plan);

        plan.getOrderedQuantities().add(oq);

        when(planRestService.getPlanById(planId)).thenReturn(plan);

        mockMvc.perform(get("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderedQuantities[0].activityName").value("-"))
                .andExpect(jsonPath("$.data.orderedQuantities[0].activityId").value("-"))
                .andExpect(jsonPath("$.data.orderedQuantities[0].quota").value(0));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetPlanDetail_WithNullPackage() throws Exception {
        plan.setTourPackage(null);

        when(planRestService.getPlanById(planId)).thenReturn(plan);

        mockMvc.perform(get("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.packageId").isEmpty())
                .andExpect(jsonPath("$.data.packageName").isEmpty())
                .andExpect(jsonPath("$.data.packageStatus").isEmpty());

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetPlanDetail_PlanNotFound() throws Exception {
        when(planRestService.getPlanById(planId))
                .thenThrow(new RuntimeException("Plan not found"));

        mockMvc.perform(get("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plan not found"));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetPlanDetail_PlanDeleted() throws Exception {
        when(planRestService.getPlanById(planId))
                .thenThrow(new RuntimeException("Plan has been deleted"));

        mockMvc.perform(get("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plan has been deleted"));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    // ==================== GET /plans/{id}/edit ====================

    @Test
    void testGetEditPlanForm_Success() throws Exception {
        when(planRestService.getPlanById(planId)).thenReturn(plan);

        mockMvc.perform(get("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plan data retrieved for editing"))
                .andExpect(jsonPath("$.data.id").value(planId.toString()))
                .andExpect(jsonPath("$.data.planName").value("Bali Day 1 - Water Activities"))
                .andExpect(jsonPath("$.data.packageName").value("Bali Adventure Package"))
                .andExpect(jsonPath("$.data.packageStatus").value("PENDING"));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetEditPlanForm_WithOrderedQuantities() throws Exception {
        Activity activity1 = new Activity();
        activity1.setId(UUID.randomUUID().toString());
        activity1.setActivityName("Snorkeling");
        activity1.setPrice(150000L);

        Activity activity2 = new Activity();
        activity2.setId(UUID.randomUUID().toString());
        activity2.setActivityName("Diving");
        activity2.setPrice(300000L);

        OrderedQuantity oq1 = new OrderedQuantity();
        oq1.setId(UUID.randomUUID());
        oq1.setActivity(activity1);
        oq1.setOrderedQuota(2);
        oq1.setPrice(150000L);
        oq1.setIsDeleted(false);
        oq1.setPlan(plan);

        OrderedQuantity oq2 = new OrderedQuantity();
        oq2.setId(UUID.randomUUID());
        oq2.setActivity(activity2);
        oq2.setOrderedQuota(1);
        oq2.setPrice(300000L);
        oq2.setIsDeleted(false);
        oq2.setPlan(plan);

        plan.getOrderedQuantities().add(oq1);
        plan.getOrderedQuantities().add(oq2);

        when(planRestService.getPlanById(planId)).thenReturn(plan);

        mockMvc.perform(get("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderedQuantities", hasSize(2)))
                .andExpect(jsonPath("$.data.orderedQuantities[0].activityName").value("Snorkeling"))
                .andExpect(jsonPath("$.data.orderedQuantities[1].activityName").value("Diving"));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetEditPlanForm_PlanNotFound() throws Exception {
        when(planRestService.getPlanById(planId))
                .thenThrow(new RuntimeException("Plan not found"));

        mockMvc.perform(get("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plan not found"));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    @Test
    void testGetEditPlanForm_EmptyOrderedQuantities() throws Exception {
        when(planRestService.getPlanById(planId)).thenReturn(plan);

        mockMvc.perform(get("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderedQuantities", hasSize(0)));

        verify(planRestService, times(1)).getPlanById(planId);
    }

    // ==================== PUT /plans/{id}/edit ====================

    @Test
    void testUpdatePlan_Success() throws Exception {
        Plan updatedPlan = new Plan();
        updatedPlan.setId(planId);
        updatedPlan.setPlanName("Updated Bali Day 1");
        updatedPlan.setActivityType("Cultural");
        updatedPlan.setStartDate(LocalDateTime.of(2024, 6, 2, 9, 0));
        updatedPlan.setEndDate(LocalDateTime.of(2024, 6, 2, 17, 0));
        updatedPlan.setStartLocation("Temple A");
        updatedPlan.setEndLocation("Temple B");
        updatedPlan.setTourPackage(tourPackage);
        updatedPlan.setOrderedQuantities(new ArrayList<>());

        when(planRestService.updatePlan(eq(planId), any(UpdatePlanRequestDTO.class)))
                .thenReturn(updatedPlan);

        mockMvc.perform(put("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePlanRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plan updated successfully"))
                .andExpect(jsonPath("$.data.id").value(planId.toString()))
                .andExpect(jsonPath("$.data.planName").value("Updated Bali Day 1"))
                .andExpect(jsonPath("$.data.activityType").value("Cultural"))
                .andExpect(jsonPath("$.data.startLocation").value("Temple A"))
                .andExpect(jsonPath("$.data.endLocation").value("Temple B"));

        verify(planRestService, times(1))
                .updatePlan(eq(planId), any(UpdatePlanRequestDTO.class));
    }

    @Test
    void testUpdatePlan_ValidationError() throws Exception {
        UpdatePlanRequestDTO invalidRequest = new UpdatePlanRequestDTO();

        mockMvc.perform(put("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(planRestService, never()).updatePlan(any(), any());
    }

    @Test
    void testUpdatePlan_PlanNotFound() throws Exception {
        when(planRestService.updatePlan(eq(planId), any(UpdatePlanRequestDTO.class)))
                .thenThrow(new RuntimeException("Plan not found"));

        mockMvc.perform(put("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePlanRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Plan not found"));

        verify(planRestService, times(1))
                .updatePlan(eq(planId), any(UpdatePlanRequestDTO.class));
    }

    @Test
    void testUpdatePlan_PackageAlreadyProcessed() throws Exception {
        when(planRestService.updatePlan(eq(planId), any(UpdatePlanRequestDTO.class)))
                .thenThrow(new RuntimeException("Cannot update plan in processed package"));

        mockMvc.perform(put("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePlanRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot update plan in processed package"));

        verify(planRestService, times(1))
                .updatePlan(eq(planId), any(UpdatePlanRequestDTO.class));
    }

    @Test
    void testUpdatePlan_PlanDeleted() throws Exception {
        when(planRestService.updatePlan(eq(planId), any(UpdatePlanRequestDTO.class)))
                .thenThrow(new RuntimeException("Cannot update deleted plan"));

        mockMvc.perform(put("/plans/{id}/edit", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePlanRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot update deleted plan"));

        verify(planRestService, times(1))
                .updatePlan(eq(planId), any(UpdatePlanRequestDTO.class));
    }

    // ==================== GET /package/{id}/plans/create ====================

    @Test
    void testShowCreateForm_Success() throws Exception {
        mockMvc.perform(get("/package/{id}/plans/create", packageId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Create plan form endpoint"))
                .andExpect(jsonPath("$.data").value("Ready to create plan for package: " + packageId));

        verifyNoInteractions(planRestService);
    }

    @Test
    void testShowCreateForm_DifferentPackageId() throws Exception {
        String differentPackageId = UUID.randomUUID().toString();

        mockMvc.perform(get("/package/{id}/plans/create", differentPackageId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Ready to create plan for package: " + differentPackageId));

        verifyNoInteractions(planRestService);
    }

    // ==================== DELETE /plans/{id} ====================

    @Test
    void testDeletePlan_Success() throws Exception {
        doNothing().when(planRestService).deletePlan(planId);

        mockMvc.perform(delete("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plan deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(planRestService, times(1)).deletePlan(planId);
    }

    @Test
    void testDeletePlan_PlanNotFound() throws Exception {
        doThrow(new RuntimeException("Plan not found"))
                .when(planRestService).deletePlan(planId);

        mockMvc.perform(delete("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Plan not found"));

        verify(planRestService, times(1)).deletePlan(planId);
    }

    @Test
    void testDeletePlan_PackageAlreadyProcessed() throws Exception {
        doThrow(new RuntimeException("Cannot delete plan from processed package"))
                .when(planRestService).deletePlan(planId);

        mockMvc.perform(delete("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot delete plan from processed package"));

        verify(planRestService, times(1)).deletePlan(planId);
    }

    @Test
    void testDeletePlan_PlanAlreadyDeleted() throws Exception {
        doThrow(new RuntimeException("Plan already deleted"))
                .when(planRestService).deletePlan(planId);

        mockMvc.perform(delete("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Plan already deleted"));

        verify(planRestService, times(1)).deletePlan(planId);
    }

    @Test
    void testDeletePlan_PlanHasOrderedQuantities() throws Exception {
        doThrow(new RuntimeException("Cannot delete plan with existing activities"))
                .when(planRestService).deletePlan(planId);

        mockMvc.perform(delete("/plans/{id}", planId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot delete plan with existing activities"));

        verify(planRestService, times(1)).deletePlan(planId);
    }
}