package apap.ti._5.tour_package_2306240156_be.restdto.request.topup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentMethodStatusRequestDTO {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "Active|Inactive", message = "Status must be Active or Inactive")
    private String status;
}