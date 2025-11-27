package apap.ti._5.tour_package_2306240156_be.restdto.response.topup;

import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
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
public class PaymentMethodResponseDTO {
    
    private UUID id;
    private String methodName;
    private String provider;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static PaymentMethodResponseDTO fromEntity(PaymentMethod entity) {
        return PaymentMethodResponseDTO.builder()
                .id(entity.getId())
                .methodName(entity.getMethodName())
                .provider(entity.getProvider())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}