package apap.ti._5.tour_package_2306240156_be.restdto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenExchangeRequestDTO {
    private String ott; // One-Time Token from auth service
}
