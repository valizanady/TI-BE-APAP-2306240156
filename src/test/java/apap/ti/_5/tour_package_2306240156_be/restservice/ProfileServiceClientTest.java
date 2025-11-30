package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.restdto.external.ProfileResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProfileServiceClient profileServiceClient;

    private final String OTT = "valid-ott";
    private final String JWT = "jwt-token";

    @Test
    void exchangeToken_success() {
        String mockResponse = "{\"status\":200,\"message\":\"Success\",\"data\":{\"token\":\"" + JWT + "\"}}";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(ResponseEntity.ok(mockResponse));

        String result = profileServiceClient.exchangeToken(OTT);

        assertEquals(JWT, result);
    }

    @Test
    void exchangeToken_failure() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(ResponseEntity.badRequest().build());

        String result = profileServiceClient.exchangeToken(OTT);

        assertNull(result);
    }

    @Test
    void getUserProfile_success() {
        ProfileResponseDTO mockProfile = new ProfileResponseDTO();
        mockProfile.setUsername("user1");

        BaseResponseDTO<ProfileResponseDTO> responseDTO = new BaseResponseDTO<>();
        responseDTO.setData(mockProfile);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(responseDTO));

        ProfileResponseDTO result = profileServiceClient.getUserProfile(JWT);

        assertNotNull(result);
        assertEquals("user1", result.getUsername());
    }
}
