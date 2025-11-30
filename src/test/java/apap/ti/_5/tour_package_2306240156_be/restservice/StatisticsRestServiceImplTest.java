package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306240156_be.model.Package;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsRestServiceImplTest {

        @Mock
        private OrderedQuantityRepository orderedQuantityRepository;

        @InjectMocks
        private StatisticsRestServiceImpl service;

        private OrderedQuantity mockOq;

        @BeforeEach
        void setUp() {
                Package mockPackage = new Package();
                mockPackage.setStatus("Processed");

                Plan mockPlan = new Plan();
                mockPlan.setTourPackage(mockPackage);
                mockPlan.setIsDeleted(false);

                Activity mockActivity = new Activity();
                mockActivity.setActivityType("Adventure");

                mockOq = new OrderedQuantity();
                mockOq.setPlan(mockPlan);
                mockOq.setActivity(mockActivity);
                mockOq.setPrice(1000000L);
                mockOq.setStartDate(LocalDateTime.of(2023, 1, 15, 10, 0));
                mockOq.setIsDeleted(false);
        }

        @Test
        void testCalculatePotentialRevenue_Success() {
                when(orderedQuantityRepository.findAll()).thenReturn(List.of(mockOq));

                StatisticsResponseDTO result = service.calculatePotentialRevenue(2023, 1);

                assertNotNull(result);
                assertEquals(1000000L, result.getTotalRevenue());
        }

        @Test
        void testCalculatePotentialRevenue_YearOnly_Success() {
                when(orderedQuantityRepository.findAll()).thenReturn(List.of(mockOq));

                StatisticsResponseDTO result = service.calculatePotentialRevenue(2023, null);

                assertNotNull(result);
                assertEquals(1000000L, result.getTotalRevenue()); // Should sum up for the year
        }
}
