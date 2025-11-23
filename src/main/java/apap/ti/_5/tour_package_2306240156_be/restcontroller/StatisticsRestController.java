package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.StatisticsResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.StatisticsRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsRestController {

    private final StatisticsRestService statisticsRestService;

    /**
     * GET /api/statistics/revenue?year={year}&month={month}
     * Calculate revenue by activity type
     * 
     * @param year Required - Year to filter
     * @param month Optional - Month to filter (1-12), null = all months
     * @return Statistics with revenue breakdown
     */
    @GetMapping("/revenue")
    public ResponseEntity<BaseResponseDTO<StatisticsResponseDTO>> getPotentialRevenue(
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        
        System.out.println("🎯 GET /api/statistics/revenue?year=" + year + "&month=" + month);
        
        // Validate year
        if (year == null || year < 2000 || year > 2100) {
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Year must be between 2000 and 2100")
                            .build());
        }
        
        // Validate month
        if (month != null && (month < 1 || month > 12)) {
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Month must be between 1 and 12")
                            .build());
        }
        
        try {
            StatisticsResponseDTO statistics = statisticsRestService.calculatePotentialRevenue(year, month);
            
            System.out.println("✅ Statistics calculated successfully");
            
            return ResponseEntity.ok()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Statistics retrieved successfully")
                            .data(statistics)
                            .build());
                            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to calculate statistics: " + e.getMessage())
                            .build());
        }
    }
}