package apap.ti._5.tour_package_2306240156_be.restdto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenExchangeResponseDTO {
    private String jwt; // JWT access token
}
