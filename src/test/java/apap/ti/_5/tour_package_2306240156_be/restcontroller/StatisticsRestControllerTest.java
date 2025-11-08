package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.response.StatisticsResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.StatisticsRestService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

    private StatisticsResponseDTO statisticsResponseDTO;
    private Map<String, Long> revenueByActivityType;

    @BeforeEach
    void setUp() {
        // Setup revenue by activity type
        revenueByActivityType = new LinkedHashMap<>();
        revenueByActivityType.put("Flight", 15000000L);
        revenueByActivityType.put("Accommodation", 10000000L);
        revenueByActivityType.put("Vehicle Rental", 5000000L);
        revenueByActivityType.put("Water Sports", 3000000L);

        // Setup StatisticsResponseDTO
        statisticsResponseDTO = StatisticsResponseDTO.builder()
                .year(2024)
                .month(6)
                .revenueByActivityType(revenueByActivityType)
                .totalRevenue(33000000L)
                .build();
    }

    // ==================== GET /statistics?year={year}&month={month} ====================

    @Test
    void testGetPotentialRevenue_Success_WithMonth() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(statisticsResponseDTO);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.year").value(2024))
                .andExpect(jsonPath("$.data.month").value(6))
                .andExpect(jsonPath("$.data.totalRevenue").value(33000000))
                .andExpect(jsonPath("$.data.revenueByActivityType.Flight").value(15000000))
                .andExpect(jsonPath("$.data.revenueByActivityType.Accommodation").value(10000000))
                .andExpect(jsonPath("$.data.revenueByActivityType['Vehicle Rental']").value(5000000))
                .andExpect(jsonPath("$.data.revenueByActivityType['Water Sports']").value(3000000));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_Success_WithoutMonth() throws Exception {
        StatisticsResponseDTO yearlyStats = StatisticsResponseDTO.builder()
                .year(2024)
                .month(null)
                .revenueByActivityType(revenueByActivityType)
                .totalRevenue(33000000L)
                .build();

        when(statisticsRestService.calculatePotentialRevenue(2024, null))
                .thenReturn(yearlyStats);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.year").value(2024))
                .andExpect(jsonPath("$.data.month").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.data.totalRevenue").value(33000000));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, null);
    }

    @Test
    void testGetPotentialRevenue_Success_EmptyRevenue() throws Exception {
        StatisticsResponseDTO emptyStats = StatisticsResponseDTO.builder()
                .year(2024)
                .month(12)
                .revenueByActivityType(new HashMap<>())
                .totalRevenue(0L)
                .build();

        when(statisticsRestService.calculatePotentialRevenue(2024, 12))
                .thenReturn(emptyStats);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue").value(0))
                .andExpect(jsonPath("$.data.revenueByActivityType").isEmpty());

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 12);
    }

    @Test
    void testGetPotentialRevenue_Success_SingleActivityType() throws Exception {
        Map<String, Long> singleActivity = new HashMap<>();
        singleActivity.put("Flight", 20000000L);

        StatisticsResponseDTO singleStats = StatisticsResponseDTO.builder()
                .year(2024)
                .month(3)
                .revenueByActivityType(singleActivity)
                .totalRevenue(20000000L)
                .build();

        when(statisticsRestService.calculatePotentialRevenue(2024, 3))
                .thenReturn(singleStats);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revenueByActivityType", aMapWithSize(1)))
                .andExpect(jsonPath("$.data.revenueByActivityType.Flight").value(20000000))
                .andExpect(jsonPath("$.data.totalRevenue").value(20000000));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 3);
    }

    @Test
    void testGetPotentialRevenue_Success_MultipleActivityTypes() throws Exception {
        Map<String, Long> multipleActivities = new LinkedHashMap<>();
        multipleActivities.put("Flight", 15000000L);
        multipleActivities.put("Accommodation", 10000000L);
        multipleActivities.put("Vehicle Rental", 5000000L);
        multipleActivities.put("Water Sports", 3000000L);
        multipleActivities.put("Cultural Tour", 2000000L);
        multipleActivities.put("Food & Beverage", 1500000L);

        StatisticsResponseDTO multiStats = StatisticsResponseDTO.builder()
                .year(2024)
                .month(7)
                .revenueByActivityType(multipleActivities)
                .totalRevenue(36500000L)
                .build();

        when(statisticsRestService.calculatePotentialRevenue(2024, 7))
                .thenReturn(multiStats);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revenueByActivityType", aMapWithSize(6)))
                .andExpect(jsonPath("$.data.totalRevenue").value(36500000));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 7);
    }

    @Test
    void testGetPotentialRevenue_Success_January() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 1))
                .thenReturn(statisticsResponseDTO);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 1);
    }

    @Test
    void testGetPotentialRevenue_Success_December() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 12))
                .thenReturn(statisticsResponseDTO);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 12);
    }

    @Test
    void testGetPotentialRevenue_Success_Year2000() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2000, 6))
                .thenReturn(statisticsResponseDTO);

        mockMvc.perform(get("/statistics")
                        .param("year", "2000")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2000, 6);
    }

    @Test
    void testGetPotentialRevenue_Success_Year2100() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2100, 6))
                .thenReturn(statisticsResponseDTO);

        mockMvc.perform(get("/statistics")
                        .param("year", "2100")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2100, 6);
    }

    // ==================== Validation Error Tests ====================

    @Test
    void testGetPotentialRevenue_ValidationError_MissingYear() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_YearTooLow() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "1999")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_YearTooHigh() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "2101")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_MonthTooLow() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_MonthTooHigh() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "13")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_NegativeMonth() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Month must be between 1 and 12"));

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_NegativeYear() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "-2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_YearZero() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "0")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Year must be between 2000 and 2100"));

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_InvalidYearFormat() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "abc")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(statisticsRestService);
    }

    @Test
    void testGetPotentialRevenue_ValidationError_InvalidMonthFormat() throws Exception {
        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "xyz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(statisticsRestService);
    }

    // ==================== Service Exception Tests ====================

    @Test
    void testGetPotentialRevenue_ServiceException() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to calculate statistics: Database connection failed"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_ServiceException_NoData() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenThrow(new RuntimeException("No packages found for given period"));

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to calculate statistics: No packages found for given period"));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_ServiceException_NullPointer() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, null))
                .thenThrow(new NullPointerException("Unexpected null value"));

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to calculate statistics: Unexpected null value"));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, null);
    }

    @Test
    void testGetPotentialRevenue_ServiceException_IllegalArgument() throws Exception {
        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenThrow(new IllegalArgumentException("Invalid calculation parameters"));

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to calculate statistics: Invalid calculation parameters"));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    // ==================== Edge Cases ====================

    @Test
    void testGetPotentialRevenue_EdgeCase_VeryLargeRevenue() throws Exception {
        Map<String, Long> largeRevenue = new HashMap<>();
        largeRevenue.put("Flight", 999999999999L);
        largeRevenue.put("Accommodation", 888888888888L);

        StatisticsResponseDTO largeStats = StatisticsResponseDTO.builder()
                .year(2024)
                .month(6)
                .revenueByActivityType(largeRevenue)
                .totalRevenue(1888888888887L)
                .build();

        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(largeStats);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue").value(1888888888887L));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_EdgeCase_ActivityTypeWithSpecialCharacters() throws Exception {
        Map<String, Long> specialChars = new HashMap<>();
        specialChars.put("Food & Beverage", 1000000L);
        specialChars.put("Spa & Wellness", 2000000L);
        specialChars.put("City Tour (Day)", 1500000L);

        StatisticsResponseDTO specialStats = StatisticsResponseDTO.builder()
                .year(2024)
                .month(6)
                .revenueByActivityType(specialChars)
                .totalRevenue(4500000L)
                .build();

        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(specialStats);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revenueByActivityType['Food & Beverage']").value(1000000))
                .andExpect(jsonPath("$.data.revenueByActivityType['Spa & Wellness']").value(2000000))
                .andExpect(jsonPath("$.data.revenueByActivityType['City Tour (Day)']").value(1500000));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_EdgeCase_ZeroRevenue() throws Exception {
        Map<String, Long> zeroRevenue = new HashMap<>();
        zeroRevenue.put("Flight", 0L);
        zeroRevenue.put("Accommodation", 0L);

        StatisticsResponseDTO zeroStats = StatisticsResponseDTO.builder()
                .year(2024)
                .month(6)
                .revenueByActivityType(zeroRevenue)
                .totalRevenue(0L)
                .build();

        when(statisticsRestService.calculatePotentialRevenue(2024, 6))
                .thenReturn(zeroStats);

        mockMvc.perform(get("/statistics")
                        .param("year", "2024")
                        .param("month", "6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue").value(0));

        verify(statisticsRestService, times(1)).calculatePotentialRevenue(2024, 6);
    }

    @Test
    void testGetPotentialRevenue_Success_AllMonthsOfYear() throws Exception {
        for (int month = 1; month <= 12; month++) {
            StatisticsResponseDTO monthStats = StatisticsResponseDTO.builder()
                    .year(2024)
                    .month(month)
                    .revenueByActivityType(revenueByActivityType)
                    .totalRevenue(33000000L)
                    .build();

            when(statisticsRestService.calculatePotentialRevenue(2024, month))
                    .thenReturn(monthStats);

            mockMvc.perform(get("/statistics")
                            .param("year", "2024")
                            .param("month", String.valueOf(month))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }

        verify(statisticsRestService, times(12)).calculatePotentialRevenue(eq(2024), any());
    }
}