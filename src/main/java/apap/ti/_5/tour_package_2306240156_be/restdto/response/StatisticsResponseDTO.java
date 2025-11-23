package apap.ti._5.tour_package_2306240156_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponseDTO {
    
    /**
     * Period dalam format:
     * - "YYYY" jika month = null (revenue per bulan dalam tahun)
     * - "YYYY-MM" jika month disediakan (revenue untuk bulan tertentu)
     */
    private String period;
    
    /**
     * Total revenue dari semua activity types
     */
    private Long totalRevenue;
    
    /**
     * Breakdown revenue per activity type (Flight, Accommodation, Vehicle Rental)
     * Format:
     * - Jika month disediakan: {"Flight": 10000, "Accommodation": 20000, ...}
     * - Jika month = null: {"January": {"Flight": 1000, "totalRevenue": 5000, ...}, "February": {...}, ...}
     */
    private Map<String, Object> breakdown;
}