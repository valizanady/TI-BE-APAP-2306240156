
package apap.ti._5.tour_package_2306240156_be.restdto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PackageResponseDTO {
  private String id;
  private String userId;
  private String packageName;
  private int quota;
  private Long price;
  private String status;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
}
