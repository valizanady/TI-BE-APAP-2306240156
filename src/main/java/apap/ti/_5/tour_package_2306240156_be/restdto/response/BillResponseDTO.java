package apap.ti._5.tour_package_2306240156_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk response dari Bill Service setelah membuat Bill
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponseDTO {
    
    private String id;
    private String customerId;
    private String serviceName;
    private String serviceReferenceId;
    private String description;
    private Long amount;
    private Integer status; // 0 = Unpaid, 1 = Paid
    private String createdAt;
    private String updatedAt;
}
