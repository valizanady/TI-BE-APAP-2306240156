package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.StatisticsResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.StatisticsRestService;
import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsRestController {

    private final StatisticsRestService statisticsRestService;
    private boolean hasStatisticsAccess(String role) {
        return "Superadmin".equals(role) || "TourPackageVendor".equals(role);
    }

    /**
     * GET /api/statistics/revenue?year={year}&month={month}
     * Calculate revenue by activity type
     */
    @GetMapping("/revenue")
    public ResponseEntity<BaseResponseDTO<StatisticsResponseDTO>> getPotentialRevenue(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {
        if (!hasStatisticsAccess(user.getRole())) {
            System.out.println("❌ Access denied for role: " + user.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message("Access denied. Only Superadmin and TourPackageVendor can access statistics.")
                            .build());
        }
        
        if (year == null || year < 2000 || year > 2100) {
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Year must be between 2000 and 2100")
                            .build());
        }
        
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

    /**
     * GET /api/statistics/revenue/yearly/{year}
     * Mengembalikan statistik revenue per bulan dalam satu tahun
     * Data diambil dari OrderedActivities yang sudah fulfilled (Package status = "Processed")
     */
    @GetMapping("/revenue/yearly/{year}")
    public ResponseEntity<BaseResponseDTO<StatisticsResponseDTO>> getYearlyRevenue(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer year) {
        
        System.out.println("🎯 GET /api/statistics/revenue/yearly/" + year);
        System.out.println("👤 User role: " + user.getRole());
        
        if (!hasStatisticsAccess(user.getRole())) {
            System.out.println("❌ Access denied for role: " + user.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message("Access denied. Only Superadmin and TourPackageVendor can access statistics.")
                            .build());
        }
        
        if (year == null || year < 2000 || year > 2100) {
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Year must be between 2000 and 2100")
                            .build());
        }
        
        try {
            StatisticsResponseDTO statistics = statisticsRestService.calculatePotentialRevenue(year, null);
            
            System.out.println("✅ Yearly statistics calculated successfully");
            
            return ResponseEntity.ok()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Yearly revenue statistics retrieved successfully")
                            .data(statistics)
                            .build());
                            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to calculate yearly statistics: " + e.getMessage())
                            .build());
        }
    }

    /**
     * GET /api/statistics/revenue/monthly/{year}/{month}
     * Mengembalikan detail statistik revenue untuk satu bulan tertentu
     * Response mencakup totalRevenue dan breakdown per activityType
     * Data diambil dari OrderedActivities yang sudah fulfilled (Package status = "Processed")
     */
    @GetMapping("/revenue/monthly/{year}/{month}")
    public ResponseEntity<BaseResponseDTO<StatisticsResponseDTO>> getMonthlyRevenue(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Integer year,
            @PathVariable Integer month) {
        
        System.out.println("🎯 GET /api/statistics/revenue/monthly/" + year + "/" + month);
        System.out.println("👤 User role: " + user.getRole());
        
        // Authorization check
        if (!hasStatisticsAccess(user.getRole())) {
            System.out.println("❌ Access denied for role: " + user.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message("Access denied. Only Superadmin and TourPackageVendor can access statistics.")
                            .build());
        }
        
        // Validate year
        if (year == null || year < 2000 || year > 2100) {
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Year must be between 2000 and 2100")
                            .build());
        }
        
        // Validate month
        if (month == null || month < 1 || month > 12) {
            return ResponseEntity.badRequest()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("Month must be between 1 and 12")
                            .build());
        }
        
        try {
            // Call service dengan specific month untuk monthly statistics
            StatisticsResponseDTO statistics = statisticsRestService.calculatePotentialRevenue(year, month);
            
            System.out.println("✅ Monthly statistics calculated successfully");
            
            return ResponseEntity.ok()
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Monthly revenue statistics retrieved successfully")
                            .data(statistics)
                            .build());
                            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponseDTO.<StatisticsResponseDTO>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to calculate monthly statistics: " + e.getMessage())
                            .build());
        }
    }
}