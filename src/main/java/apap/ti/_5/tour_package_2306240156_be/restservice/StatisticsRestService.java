package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.restdto.response.StatisticsResponseDTO;

public interface StatisticsRestService {
    
    StatisticsResponseDTO calculatePotentialRevenue(Integer year, Integer month);
}