package apap.ti._5.tour_package_2306240156_be.restdto.request.topup;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTopUpTransactionRequestDTO {
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be positive")
    private Long amount;
    
    @NotNull(message = "Payment method ID is required")
    private UUID paymentMethodId;
}