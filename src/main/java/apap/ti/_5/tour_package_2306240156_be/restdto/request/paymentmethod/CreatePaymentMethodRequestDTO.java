package apap.ti._5.tour_package_2306240156_be.restdto.request.paymentmethod;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentMethodRequestDTO {
    
    @NotBlank(message = "Method name is required")
    private String methodName;
    
    @NotBlank(message = "Provider is required")
    private String provider;
}
