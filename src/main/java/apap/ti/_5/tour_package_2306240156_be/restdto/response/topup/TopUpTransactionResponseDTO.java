package apap.ti._5.tour_package_2306240156_be.restdto.response.topup;

import apap.ti._5.tour_package_2306240156_be.model.TopUpTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpTransactionResponseDTO {
    
    private UUID id;
    private UUID customerId;
    private Long amount;
    private PaymentMethodResponseDTO paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static TopUpTransactionResponseDTO fromEntity(TopUpTransaction entity) {
        return TopUpTransactionResponseDTO.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .amount(entity.getAmount())
                .paymentMethod(PaymentMethodResponseDTO.fromEntity(entity.getPaymentMethod()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
