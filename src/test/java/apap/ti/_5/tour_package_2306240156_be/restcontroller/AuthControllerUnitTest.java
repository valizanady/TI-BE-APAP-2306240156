package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.request.TokenExchangeRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.TokenExchangeResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.ProfileServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private ProfileServiceClient profileServiceClient;

    @InjectMocks
    private AuthController authController;

    @Test
    void testExchangeToken_Success_ValidOtt_ReturnsJwt() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("valid-ott-token-123");
        
        String expectedJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.jwt.token";
        when(profileServiceClient.exchangeToken("valid-ott-token-123"))
                .thenReturn(expectedJwt);

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Token exchange successful", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(expectedJwt, response.getBody().getData().getJwt());
        
        verify(profileServiceClient, times(1)).exchangeToken("valid-ott-token-123");
    }

    @Test
    void testExchangeToken_NullOtt_ReturnsBadRequest() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt(null);

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("OTT is required", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(profileServiceClient, never()).exchangeToken(anyString());
    }

    @Test
    void testExchangeToken_EmptyOtt_ReturnsBadRequest() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("");

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("OTT is required", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(profileServiceClient, never()).exchangeToken(anyString());
    }

    @Test
    void testExchangeToken_BlankOtt_ReturnsBadRequest() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("   ");

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("OTT is required", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(profileServiceClient, never()).exchangeToken(anyString());
    }

    @Test
    void testExchangeToken_InvalidOtt_ServiceReturnsNull_ReturnsUnauthorized() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("invalid-ott-token");
        
        when(profileServiceClient.exchangeToken("invalid-ott-token"))
                .thenReturn(null);

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Token exchange failed. OTT may be invalid or expired.", 
                response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(profileServiceClient, times(1)).exchangeToken("invalid-ott-token");
    }

    @Test
    void testExchangeToken_InvalidOtt_ServiceReturnsEmptyString_ReturnsUnauthorized() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("expired-ott-token");
        
        when(profileServiceClient.exchangeToken("expired-ott-token"))
                .thenReturn("");

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Token exchange failed. OTT may be invalid or expired.", 
                response.getBody().getMessage());
        assertNull(response.getBody().getData());
        
        verify(profileServiceClient, times(1)).exchangeToken("expired-ott-token");
    }

    @Test
    void testExchangeToken_ServiceThrowsRuntimeException_ReturnsInternalServerError() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("ott-causing-error");
        
        when(profileServiceClient.exchangeToken("ott-causing-error"))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Internal server error during token exchange"));
        assertNull(response.getBody().getData());
        
        verify(profileServiceClient, times(1)).exchangeToken("ott-causing-error");
    }

    @Test
    void testExchangeToken_ServiceThrowsNullPointerException_ReturnsInternalServerError() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("ott-causing-npe");
        
        when(profileServiceClient.exchangeToken("ott-causing-npe"))
                .thenThrow(new NullPointerException("Null pointer in service"));

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Internal server error during token exchange"));
        assertNull(response.getBody().getData());
        
        verify(profileServiceClient, times(1)).exchangeToken("ott-causing-npe");
    }

    @Test
    void testExchangeToken_ValidOtt_VerifyTimestampExists() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("valid-ott-with-timestamp");
        
        String jwt = "jwt.token.here";
        when(profileServiceClient.exchangeToken("valid-ott-with-timestamp"))
                .thenReturn(jwt);

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        
        verify(profileServiceClient, times(1)).exchangeToken("valid-ott-with-timestamp");
    }

    @Test
    void testExchangeToken_ErrorResponse_VerifyTimestampExists() {
        // Arrange
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt(null);

        // Act
        ResponseEntity<BaseResponseDTO<TokenExchangeResponseDTO>> response = 
                authController.exchangeToken(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }
}
