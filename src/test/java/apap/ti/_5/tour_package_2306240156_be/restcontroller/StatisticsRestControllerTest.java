package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.response.StatisticsResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.StatisticsRestService;
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StatisticsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatisticsRestService statisticsRestService;

    private StatisticsResponseDTO monthlyStats;
    private StatisticsResponseDTO yearlyStats;

    @BeforeEach
    void setUp() {
        // Setup Monthly Statistics
        monthlyStats = new StatisticsResponseDTO();
        monthlyStats.setPeriod("2024-06");
        monthlyStats.setTotalRevenue(5000000L);
        
        Map<String, Object> monthlyBreakdown = new LinkedHashMap<>();
        monthlyBreakdown.put("Water Sports", 2000000L);
        monthlyBreakdown.put("Cultural", 1500000L);
        monthlyBreakdown.put("Adventure", 1500000L);
        monthlyStats.setBreakdown(monthlyBreakdown);

        // Setup Yearly Statistics
        yearlyStats = new StatisticsResponseDTO();
        yearlyStats.setPeriod("2024");
        yearlyStats.setTotalRevenue(60000000L);
        
        Map<String, Object> yearlyBreakdown = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("totalRevenue", 5000000L);
            monthData.put("Water Sports", 2000000L);
            monthData.put("Cultural", 1500000L);
            monthData.put("Adventure", 1500000L);
            yearlyBreakdown.put(String.valueOf(i), monthData);
        }
        yearlyStats.setBreakdown(yearlyBreakdown);
    }

    // Helper methods for creating authenticated users
    private AuthenticatedUser createSuperadmin() {
        return AuthenticatedUser.builder()
                .id("admin-id")
                .username("admin")
                .email("admin@example.com")
                .name("Admin User")
                .role("Superadmin")
                .build();
    }

    private AuthenticatedUser createVendor() {
        return AuthenticatedUser.builder()
                .id("vendor-id")
                .username("vendor")
                .email("vendor@example.com")
                .name("Vendor User")
                .role("TourPackageVendor")
                .build();
    }

    private AuthenticatedUser createCustomer() {
        return AuthenticatedUser.builder()
                .id("customer-id")
                .username("customer")
                .email("customer@example.com")
                .name("Customer User")
                .role("Customer")
                .build();
    }

    // ==================== GET /api/statistics/revenue ====================

    @Test
    void testGetPotentialRevenue_Success_Superadmin_WithMonth() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(monthlyStats);

        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.period").value("2024-06"))
                .andExpect(jsonPath("$.data.totalRevenue").value(5000000))
                .andExpect(jsonPath("$.data.breakdown['Water Sports']").value(2000000))
                .andExpect(jsonPath("$.data.breakdown.Cultural").value(1500000));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_Success_Vendor_WithMonth() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(monthlyStats);

        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createVendor()))
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_Success_WithoutMonth() throws Exception {
        StatisticsResponseDTO yearStats = new StatisticsResponseDTO();
        yearStats.setPeriod("2024");
        yearStats.setTotalRevenue(60000000L);
        yearStats.setBreakdown(new HashMap<>());

        when(statisticsRestService.calculatePotentialRevenue(eq(2024), isNull()))
                .thenReturn(yearStats);

        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("2024"))
                .andExpect(jsonPath("$.data.totalRevenue").value(60000000));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(eq(2024), isNull());
    }

    @Test
    void testGetPotentialRevenue_AccessDenied_Customer() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createCustomer()))
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString("Access denied")));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), any());
    }

    @Test
    void testGetPotentialRevenue_ValidationError_YearTooLow() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "1999")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), any());
    }

    @Test
    void testGetPotentialRevenue_ValidationError_YearTooHigh() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2101")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), any());
    }

    @Test
    void testGetPotentialRevenue_ValidationError_MonthTooLow() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2024")
                        .param("month", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), any());
    }

    @Test
    void testGetPotentialRevenue_ValidationError_MonthTooHigh() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2024")
                        .param("month", "13")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), any());
    }

    @Test
    void testGetPotentialRevenue_ServiceException() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(containsString("Failed to calculate statistics")));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_EmptyResult() throws Exception {
        StatisticsResponseDTO emptyStats = new StatisticsResponseDTO();
        emptyStats.setPeriod("2024-06");
        emptyStats.setTotalRevenue(0L);
        emptyStats.setBreakdown(new HashMap<>());

        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(emptyStats);

        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue").value(0))
                .andExpect(jsonPath("$.data.breakdown").isEmpty());

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    // ==================== GET /api/statistics/revenue/yearly/{year} ====================

    @Test
    void testGetYearlyRevenue_Success_Superadmin() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(eq(2024), isNull()))
                .thenReturn(yearlyStats);

        mockMvc.perform(get("/api/statistics/revenue/yearly/{year}", 2024)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Yearly revenue statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.period").value("2024"))
                .andExpect(jsonPath("$.data.totalRevenue").value(60000000))
                .andExpect(jsonPath("$.data.breakdown", aMapWithSize(12)));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(eq(2024), isNull());
    }

    @Test
    void testGetYearlyRevenue_Success_Vendor() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(eq(2024), isNull()))
                .thenReturn(yearlyStats);

        mockMvc.perform(get("/api/statistics/revenue/yearly/{year}", 2024)
                        .with(user(createVendor()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(eq(2024), isNull());
    }

    @Test
    void testGetYearlyRevenue_AccessDenied_Customer() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/yearly/{year}", 2024)
                        .with(user(createCustomer()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString("Access denied")));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), any());
    }

    @Test
    void testGetYearlyRevenue_ValidationError_YearTooLow() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/yearly/{year}", 1999)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), any());
    }

    @Test
    void testGetYearlyRevenue_ValidationError_YearTooHigh() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/yearly/{year}", 2101)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), any());
    }

    @Test
    void testGetYearlyRevenue_ServiceException() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(eq(2024), isNull()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/statistics/revenue/yearly/{year}", 2024)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(containsString("Failed to calculate yearly statistics")));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(eq(2024), isNull());
    }

    @Test
    void testGetYearlyRevenue_EmptyResult() throws Exception {
        StatisticsResponseDTO emptyStats = new StatisticsResponseDTO();
        emptyStats.setPeriod("2024");
        emptyStats.setTotalRevenue(0L);
        emptyStats.setBreakdown(new HashMap<>());

        when(statisticsRestService.calculatePotentialRevenue(eq(2024), isNull()))
                .thenReturn(emptyStats);

        mockMvc.perform(get("/api/statistics/revenue/yearly/{year}", 2024)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue").value(0))
                .andExpect(jsonPath("$.data.breakdown").isEmpty());

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(eq(2024), isNull());
    }

    // ==================== GET /api/statistics/revenue/monthly/{year}/{month} ====================

    @Test
    void testGetMonthlyRevenue_Success_Superadmin() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(monthlyStats);

        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 6)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Monthly revenue statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.period").value("2024-06"))
                .andExpect(jsonPath("$.data.totalRevenue").value(5000000))
                .andExpect(jsonPath("$.data.breakdown['Water Sports']").value(2000000));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetMonthlyRevenue_Success_Vendor() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(monthlyStats);

        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 6)
                        .with(user(createVendor()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetMonthlyRevenue_AccessDenied_Customer() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 6)
                        .with(user(createCustomer()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(containsString("Access denied")));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), anyInt());
    }

    @Test
    void testGetMonthlyRevenue_ValidationError_YearTooLow() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 1999, 6)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), anyInt());
    }

    @Test
    void testGetMonthlyRevenue_ValidationError_YearTooHigh() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2101, 6)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), anyInt());
    }

    @Test
    void testGetMonthlyRevenue_ValidationError_MonthTooLow() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 0)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), anyInt());
    }

    @Test
    void testGetMonthlyRevenue_ValidationError_MonthTooHigh() throws Exception {
        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 13)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));

        verify(statisticsRestService, never()).calculatePotentialRevenue(anyInt(), anyInt());
    }

    @Test
    void testGetMonthlyRevenue_ServiceException() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 6)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(containsString("Failed to calculate monthly statistics")));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetMonthlyRevenue_EmptyResult() throws Exception {
        StatisticsResponseDTO emptyStats = new StatisticsResponseDTO();
        emptyStats.setPeriod("2024-06");
        emptyStats.setTotalRevenue(0L);
        emptyStats.setBreakdown(new HashMap<>());

        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(emptyStats);

        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 6)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue").value(0))
                .andExpect(jsonPath("$.data.breakdown").isEmpty());

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    // ==================== Edge Cases ====================

    @Test
    void testGetPotentialRevenue_AllMonths() throws Exception {
        for (int month = 1; month <= 12; month++) {
            when(statisticsRestService.calculatePotentialRevenue(2024, month))
                    .thenReturn(monthlyStats);

            mockMvc.perform(get("/api/statistics/revenue")
                            .with(user(createSuperadmin()))
                            .param("year", "2024")
                            .param("month", String.valueOf(month))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        verify(statisticsRestService, times(12)).calculatePotentialRevenue(eq(2024), anyInt());
    }

    @Test
    void testGetYearlyRevenue_MultipleYears() throws Exception {
        for (int year = 2020; year <= 2024; year++) {
            StatisticsResponseDTO stats = new StatisticsResponseDTO();
            stats.setPeriod(String.valueOf(year));
            stats.setTotalRevenue(60000000L);
            stats.setBreakdown(new HashMap<>());

            when(statisticsRestService.calculatePotentialRevenue(eq(year), isNull()))
                    .thenReturn(stats);

            mockMvc.perform(get("/api/statistics/revenue/yearly/{year}", year)
                            .with(user(createSuperadmin()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.period").value(String.valueOf(year)));
        }

        verify(statisticsRestService, times(5)).calculatePotentialRevenue(anyInt(), isNull());
    }

    @Test
    void testGetPotentialRevenue_BoundaryYear2000() throws Exception {
        StatisticsResponseDTO stats = new StatisticsResponseDTO();
        stats.setPeriod("2000-06");
        stats.setTotalRevenue(1000000L);
        stats.setBreakdown(new HashMap<>());

        when(statisticsRestService.calculatePotentialRevenue(2000, 6))
                .thenReturn(stats);

        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2000")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("2000-06"));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2000, 6);
    }

    @Test
    void testGetPotentialRevenue_BoundaryYear2100() throws Exception {
        StatisticsResponseDTO stats = new StatisticsResponseDTO();
        stats.setPeriod("2100-06");
        stats.setTotalRevenue(1000000L);
        stats.setBreakdown(new HashMap<>());

        when(statisticsRestService.calculatePotentialRevenue(2100, 6))
                .thenReturn(stats);

        mockMvc.perform(get("/api/statistics/revenue")
                        .with(user(createSuperadmin()))
                        .param("year", "2100")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("2100-06"));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2100, 6);
    }

    @Test
    void testGetMonthlyRevenue_BoundaryMonth1() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 1))
                .thenReturn(monthlyStats);

        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 1)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 1);
    }

    @Test
    void testGetMonthlyRevenue_BoundaryMonth12() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 12))
                .thenReturn(monthlyStats);

        mockMvc.perform(get("/api/statistics/revenue/monthly/{year}/{month}", 2024, 12)
                        .with(user(createSuperadmin()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 12);
    }
}
