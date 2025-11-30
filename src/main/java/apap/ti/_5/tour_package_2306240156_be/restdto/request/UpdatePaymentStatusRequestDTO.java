package apap.ti._5.tour_package_2306240156_be.restdto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequestDTO {
    
    @NotBlank(message = "Package ID is required")
    private String packageId;
    
    @NotNull(message = "Status is required")
    private Integer status;  // Bill Service sends: 0 = UNPAID, 1 = PAID
}
