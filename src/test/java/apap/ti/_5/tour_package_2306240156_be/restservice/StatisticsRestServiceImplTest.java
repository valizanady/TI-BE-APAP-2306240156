package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.response.StatisticsResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsRestServiceImplTest {

    @Mock
    private OrderedQuantityRepository orderedQuantityRepository;

    @InjectMocks
    private StatisticsRestServiceImpl statisticsRestService;

    private List<OrderedQuantity> testOrderedQuantities;
    private Activity flightActivity;
    private Activity accommodationActivity;
    private Plan testPlan;
    private apap.ti._5.tour_package_2306240156_be.model.Package testPackage;

    @BeforeEach
    void setUp() {
        // Setup test package with "Processed" status
        testPackage = apap.ti._5.tour_package_2306240156_be.model.Package.builder()
                .id("PKG001")
                .packageName("Test Package")
                .status("Processed") // ✅ Valid status
                .userId("user001")
                .quota(25)
                .price(50000000L)
                .startDate(LocalDateTime.of(2025, 11, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 11, 7, 0, 0))
                .build();

        // Setup test plan
        testPlan = Plan.builder()
                .id(UUID.randomUUID())
                .planName("Test Plan")
                .activityType("Flight")
                .status("Fulfilled")
                .isDeleted(false)
                .tourPackage(testPackage)
                .orderedQuantities(new ArrayList<>())
                .build();

        // Setup activities
        flightActivity = Activity.builder()
                .id("ACT001")
                .activityName("Jakarta to Bali Flight")
                .activityType("Flight")
                .capacity(50)
                .price(1500000L)
                .startDate(LocalDateTime.of(2025, 11, 1, 8, 0))
                .endDate(LocalDateTime.of(2025, 11, 1, 10, 30))
                .build();

        accommodationActivity = Activity.builder()
                .id("ACT002")
                .activityName("Bali Hotel")
                .activityType("Accommodation")
                .capacity(30)
                .price(2000000L)
                .startDate(LocalDateTime.of(2025, 11, 1, 14, 0))
                .endDate(LocalDateTime.of(2025, 11, 5, 12, 0))
                .build();

        // Setup ordered quantities
        OrderedQuantity oq1 = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .plan(testPlan)
                .activity(flightActivity)
                .orderedQuota(25)
                .price(1500000L)
                .startDate(flightActivity.getStartDate())
                .endDate(flightActivity.getEndDate())
                .isDeleted(false)
                .build();

        OrderedQuantity oq2 = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .plan(testPlan)
                .activity(accommodationActivity)
                .orderedQuota(15)
                .price(2000000L)
                .startDate(accommodationActivity.getStartDate())
                .endDate(accommodationActivity.getEndDate())
                .isDeleted(false)
                .build();

        testOrderedQuantities = List.of(oq1, oq2);
    }

    @Test
    void testCalculatePotentialRevenue_WithYearOnly() {
        // Arrange
        when(orderedQuantityRepository.findAll()).thenReturn(testOrderedQuantities);

        // Act
        StatisticsResponseDTO result = statisticsRestService.calculatePotentialRevenue(2025, null);

        // Assert
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertNull(result.getMonth());
        assertEquals(67500000L, result.getTotalRevenue()); // (25 * 1500000) + (15 * 2000000)
        assertEquals(2, result.getRevenueByActivityType().size());
        assertEquals(37500000L, result.getRevenueByActivityType().get("Flight"));
        assertEquals(30000000L, result.getRevenueByActivityType().get("Accommodation"));
        verify(orderedQuantityRepository).findAll();
    }

    @Test
    void testCalculatePotentialRevenue_WithYearAndMonth() {
        // Arrange
        when(orderedQuantityRepository.findAll()).thenReturn(testOrderedQuantities);

        // Act
        StatisticsResponseDTO result = statisticsRestService.calculatePotentialRevenue(2025, 11);

        // Assert
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(11, result.getMonth());
        assertEquals(67500000L, result.getTotalRevenue());
        verify(orderedQuantityRepository).findAll();
    }

    @Test
    void testCalculatePotentialRevenue_FiltersSoftDeletedOrderedQuantities() {
        // Arrange
        OrderedQuantity deletedOq = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .plan(testPlan)
                .activity(flightActivity)
                .orderedQuota(100)
                .price(1500000L)
                .startDate(flightActivity.getStartDate())
                .isDeleted(true) // Soft deleted
                .build();

        List<OrderedQuantity> allOqs = new ArrayList<>(testOrderedQuantities);
        allOqs.add(deletedOq);
        
        when(orderedQuantityRepository.findAll()).thenReturn(allOqs);

        // Act
        StatisticsResponseDTO result = statisticsRestService.calculatePotentialRevenue(2025, null);

        // Assert
        assertEquals(67500000L, result.getTotalRevenue()); // Should not include deleted OQ
    }

    @Test
    void testCalculatePotentialRevenue_FiltersSoftDeletedPlans() {
        // Arrange
        Plan deletedPlan = Plan.builder()
                .id(UUID.randomUUID())
                .isDeleted(true)
                .tourPackage(testPackage)
                .build();

        OrderedQuantity oqWithDeletedPlan = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .plan(deletedPlan)
                .activity(flightActivity)
                .orderedQuota(100)
                .price(1500000L)
                .startDate(flightActivity.getStartDate())
                .isDeleted(false)
                .build();

        List<OrderedQuantity> allOqs = new ArrayList<>(testOrderedQuantities);
        allOqs.add(oqWithDeletedPlan);
        
        when(orderedQuantityRepository.findAll()).thenReturn(allOqs);

        // Act
        StatisticsResponseDTO result = statisticsRestService.calculatePotentialRevenue(2025, null);

        // Assert
        assertEquals(67500000L, result.getTotalRevenue()); // Should not include OQ with deleted plan
    }

    @Test
    void testCalculatePotentialRevenue_OnlyProcessedPackages() {
        // Arrange
        apap.ti._5.tour_package_2306240156_be.model.Package pendingPackage = 
            apap.ti._5.tour_package_2306240156_be.model.Package.builder()
                .id("PKG002")
                .status("Pending") // ✅ Only Pending or Processed are valid
                .build();

        Plan planWithPendingPackage = Plan.builder()
                .id(UUID.randomUUID())
                .isDeleted(false)
                .tourPackage(pendingPackage)
                .build();

        OrderedQuantity oqWithPendingPackage = OrderedQuantity.builder()
                .id(UUID.randomUUID())
                .plan(planWithPendingPackage)
                .activity(flightActivity)
                .orderedQuota(100)
                .price(1500000L)
                .startDate(flightActivity.getStartDate())
                .isDeleted(false)
                .build();

        List<OrderedQuantity> allOqs = new ArrayList<>(testOrderedQuantities);
        allOqs.add(oqWithPendingPackage);
        
        when(orderedQuantityRepository.findAll()).thenReturn(allOqs);

        // Act
        StatisticsResponseDTO result = statisticsRestService.calculatePotentialRevenue(2025, null);

        // Assert
        assertEquals(67500000L, result.getTotalRevenue()); // Should not include pending package
    }

    @Test
    void testCalculatePotentialRevenue_NoMatchingData() {
        // Arrange
        when(orderedQuantityRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        StatisticsResponseDTO result = statisticsRestService.calculatePotentialRevenue(2026, null);

        // Assert
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(0L, result.getTotalRevenue());
        assertTrue(result.getRevenueByActivityType().isEmpty());
    }
}
