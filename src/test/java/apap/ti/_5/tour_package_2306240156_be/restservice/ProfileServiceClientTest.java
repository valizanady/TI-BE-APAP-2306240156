package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.restdto.external.ProfileResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.external.UpdateProfileRequestDTO;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProfileServiceClient profileServiceClient;

    private final String OTT = "valid-ott";
    private final String JWT = "jwt-token";
    private ProfileResponseDTO mockProfile;

    @BeforeEach
    void setUp() {
        mockProfile = new ProfileResponseDTO();
        mockProfile.setId("user-123");
        mockProfile.setUsername("testuser");
        mockProfile.setName("Test User");
        mockProfile.setEmail("test@example.com");
        mockProfile.setSaldo(100000L);
        mockProfile.setGender(true); // true = Male
    }

    // ========== exchangeToken Tests ==========

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

    // ========== Additional Comprehensive Tests ==========

    @Test
    void testExchangeToken_MalformedJson() {
        String malformedResponse = "{\"status\":200,\"malformed\":true}";
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(ResponseEntity.ok(malformedResponse));

        String result = profileServiceClient.exchangeToken(OTT);

        assertNull(result);
    }

    @Test
    void testExchangeToken_HttpClientError() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        String result = profileServiceClient.exchangeToken(OTT);

        assertNull(result);
    }

    @Test
    void testExchangeToken_GenericException() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenThrow(new RuntimeException("Connection timeout"));

        String result = profileServiceClient.exchangeToken(OTT);

        assertNull(result);
    }

    @Test
    void testGetUserProfile_NullBody() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(null));

        ProfileResponseDTO result = profileServiceClient.getUserProfile(JWT);

        assertNull(result);
    }

    @Test
    void testGetUserProfile_Exception() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new RuntimeException("Service unavailable"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            profileServiceClient.getUserProfile(JWT);
        });

        assertTrue(exception.getMessage().contains("Gagal terhubung ke layanan Profile"));
    }

    @Test
    void testUpdateUserBalance_Success() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Object.class))).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> {
            profileServiceClient.updateUserBalance(JWT, "user-123", 200000L, mockProfile);
        });

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Object.class));
    }

    @Test
    void testUpdateUserBalance_Exception() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Object.class))).thenThrow(new RuntimeException("Update failed"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            profileServiceClient.updateUserBalance(JWT, "user-123", 200000L, mockProfile);
        });

        assertTrue(exception.getMessage().contains("Gagal update saldo user"));
    }

    @Test
    void testAddBalance_Success() {
        BaseResponseDTO<ProfileResponseDTO> getResponse = new BaseResponseDTO<>(200, "Success", null, mockProfile);
        when(restTemplate.exchange(
                contains("/auth/me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(getResponse));

        when(restTemplate.exchange(
                contains("/profile/update"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Object.class))).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> {
            profileServiceClient.addBalance(JWT, 50000L);
        });

        verify(restTemplate, times(1)).exchange(contains("/auth/me"), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class));
        verify(restTemplate, times(1)).exchange(contains("/profile/update"), eq(HttpMethod.PUT), any(), eq(Object.class));
    }

    @Test
    void testDeductBalance_Success() {
        BaseResponseDTO<ProfileResponseDTO> getResponse = new BaseResponseDTO<>(200, "Success", null, mockProfile);
        when(restTemplate.exchange(
                contains("/auth/me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(getResponse));

        when(restTemplate.exchange(
                contains("/profile/update"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Object.class))).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> {
            profileServiceClient.deductBalance(JWT, 30000L);
        });

        verify(restTemplate, times(1)).exchange(contains("/profile/update"), eq(HttpMethod.PUT), any(), eq(Object.class));
    }

    @Test
    void testDeductBalance_InsufficientBalance() {
        BaseResponseDTO<ProfileResponseDTO> getResponse = new BaseResponseDTO<>(200, "Success", null, mockProfile);
        when(restTemplate.exchange(
                contains("/auth/me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(getResponse));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            profileServiceClient.deductBalance(JWT, 200000L);
        });

        assertTrue(exception.getMessage().contains("Saldo tidak mencukupi"));
        verify(restTemplate, never()).exchange(contains("/profile/update"), eq(HttpMethod.PUT), any(), eq(Object.class));
    }

    @Test
    void testGetCustomerProfileByUsername_Success() {
        BaseResponseDTO<ProfileResponseDTO> responseBody = new BaseResponseDTO<>(200, "Success", null, mockProfile);
        ResponseEntity<String> rawResponse = ResponseEntity.ok("{\"data\":{}}");
        ResponseEntity<BaseResponseDTO<ProfileResponseDTO>> typedResponse = ResponseEntity.ok(responseBody);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(rawResponse);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(typedResponse);

        ProfileResponseDTO result = profileServiceClient.getCustomerProfileByUsername("testuser", JWT);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetCustomerProfileById_Success() {
        List<ProfileResponseDTO> customers = Arrays.asList(mockProfile);
        BaseResponseDTO<List<ProfileResponseDTO>> responseBody = new BaseResponseDTO<>(200, "Success", null, customers);
        ResponseEntity<BaseResponseDTO<List<ProfileResponseDTO>>> response = ResponseEntity.ok(responseBody);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(response);

        ProfileResponseDTO result = profileServiceClient.getCustomerProfileById("user-123", JWT);

        assertNotNull(result);
        assertEquals("user-123", result.getId());
    }

    @Test
    void testGetCustomerProfileById_NotFound() {
        ProfileResponseDTO otherProfile = new ProfileResponseDTO();
        otherProfile.setId("user-456");
        
        List<ProfileResponseDTO> customers = Arrays.asList(otherProfile);
        BaseResponseDTO<List<ProfileResponseDTO>> responseBody = new BaseResponseDTO<>(200, "Success", null, customers);
        ResponseEntity<BaseResponseDTO<List<ProfileResponseDTO>>> response = ResponseEntity.ok(responseBody);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(response);

        ProfileResponseDTO result = profileServiceClient.getCustomerProfileById("user-123", JWT);

        assertNull(result);
    }

    @Test
    void testAddBalanceToProfile_Success() {
        // Mock getCustomerProfileByUsername
        BaseResponseDTO<ProfileResponseDTO> getResponse = new BaseResponseDTO<>(200, "Success", null, mockProfile);
        ResponseEntity<String> rawResponse = ResponseEntity.ok("{\"data\":{}}");
        ResponseEntity<BaseResponseDTO<ProfileResponseDTO>> typedResponse = ResponseEntity.ok(getResponse);

        when(restTemplate.exchange(
                contains("/profile/testuser"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(rawResponse);

        when(restTemplate.exchange(
                contains("/profile/testuser"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(typedResponse);

        // Mock update balance
        BaseResponseDTO<Object> updateResponse = new BaseResponseDTO<>(200, "Success", null, null);
        ResponseEntity<BaseResponseDTO<Object>> updateResponseEntity = ResponseEntity.ok(updateResponse);

        when(restTemplate.exchange(
                contains("/profile/update/saldo"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(updateResponseEntity);

        boolean result = profileServiceClient.addBalanceToProfile("testuser", 50000L, JWT);

        assertTrue(result);
        verify(restTemplate, times(1)).exchange(contains("/profile/update/saldo"), eq(HttpMethod.PUT), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void testAddBalanceToProfile_CustomerNotFound() {
        when(restTemplate.exchange(
                contains("/profile/testuser"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        boolean result = profileServiceClient.addBalanceToProfile("testuser", 50000L, JWT);

        assertFalse(result);
        verify(restTemplate, never()).exchange(contains("/profile/update/saldo"), any(), any(), any(ParameterizedTypeReference.class));
    }
}

