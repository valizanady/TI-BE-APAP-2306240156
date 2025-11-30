package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.request.TokenExchangeRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.ProfileServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for this test
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileServiceClient profileServiceClient;

    @Test
    void exchangeToken_success() throws Exception {
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("valid-ott");

        when(profileServiceClient.exchangeToken("valid-ott")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Token exchange successful"))
                .andExpect(jsonPath("$.data.jwt").value("jwt-token"));
    }

    @Test
    void exchangeToken_missingOtt_shouldReturnBadRequest() throws Exception {
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        // OTT is null

        mockMvc.perform(post("/api/auth/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("OTT is required"));
    }

    @Test
    void exchangeToken_invalidOtt_shouldReturnUnauthorized() throws Exception {
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("invalid-ott");

        when(profileServiceClient.exchangeToken("invalid-ott")).thenReturn(null);

        mockMvc.perform(post("/api/auth/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Token exchange failed. OTT may be invalid or expired."));
    }

    @Test
    void exchangeToken_serviceException_shouldReturnInternalServerError() throws Exception {
        TokenExchangeRequestDTO request = new TokenExchangeRequestDTO();
        request.setOtt("valid-ott");

        when(profileServiceClient.exchangeToken("valid-ott")).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/auth/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error during token exchange: Service error"));
    }
}
