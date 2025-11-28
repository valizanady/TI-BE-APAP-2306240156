package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.request.TokenExchangeRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.TokenExchangeResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.ProfileServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ProfileServiceClient profileServiceClient;

    /**
     * Exchange OTT (One-Time Token) for JWT access token
     * 
     * Frontend flow:
     * 1. User redirected from auth service with OTT: http://localhost:5173/login-success/auth?ott=2U9ZTI
     * 2. Frontend calls this endpoint: POST /api/auth/exchange with body: {ott: "2U9ZTI"}
     * 3. Backend calls Nabeel's service: POST https://acc-be.beel.my.id/api/auth/exchange
     * 4. Backend returns JWT token to frontend
     * 5. Frontend stores JWT and uses it for subsequent API calls
     * 
     * @param request TokenExchangeRequestDTO containing OTT
     * @return JWT access token wrapped in BaseResponseDTO
     */
    @PostMapping("/exchange")
    public ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> exchangeToken(
            @RequestBody TokenExchangeRequestDTO request
    ) {
        try {
            System.out.println("🔐 Token exchange request received | OTT: " + request.getOtt());
            
            if (request.getOtt() == null || request.getOtt().isEmpty()) {
                BaseResponseDTO<TokenExchangeResponseDTO> response = new BaseResponseDTO<>(
                        400,
                        "OTT is required",
                        new Date(),
                        null
                );
                return ResponseEntity.badRequest().body(response);
            }
            
            // Call Profile Service to exchange OTT for JWT
            String jwt = profileServiceClient.exchangeToken(request.getOtt());
            
            if (jwt == null || jwt.isEmpty()) {
                BaseResponseDTO<TokenExchangeResponseDTO> response = new BaseResponseDTO<>(
                        401,
                        "Token exchange failed. OTT may be invalid or expired.",
                        new Date(),
                        null
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Success - return JWT token
            TokenExchangeResponseDTO data = TokenExchangeResponseDTO.builder()
                    .jwt(jwt)
                    .build();
            
            BaseResponseDTO<TokenExchangeResponseDTO> response = new BaseResponseDTO<>(
                    200,
                    "Token exchange successful",
                    new Date(),
                    data
            );
            
            System.out.println("✅ Token exchange successful");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Token exchange error: " + e.getMessage());
            e.printStackTrace();
            
            BaseResponseDTO<TokenExchangeResponseDTO> response = new BaseResponseDTO<>(
                    500,
                    "Internal server error during token exchange: " + e.getMessage(),
                    new Date(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
