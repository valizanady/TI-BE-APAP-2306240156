package apap.ti._5.tour_package_2306240156_be.restcontroller.topup;

import apap.ti._5.tour_package_2306240156_be.model.TopUpTransaction;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.CreateTopUpTransactionRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.UpdateTopUpStatusRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.topup.TopUpTransactionRestService;
import apap.ti._5.tour_package_2306240156_be.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopUpTransactionRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class TopUpTransactionRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TopUpTransactionRestService topUpTransactionRestService;

    @MockBean
    private JwtUtils jwtUtils;

    private TopUpTransaction mockTransaction;
    private final UUID TRANSACTION_ID = UUID.randomUUID();
    private final UUID CUSTOMER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockTransaction = new TopUpTransaction();
        mockTransaction.setId(TRANSACTION_ID);
        mockTransaction.setAmount(100000L);
        mockTransaction.setStatus("Pending");
    }

    private void setupSecurityContext(String role, UUID userId, String username) {
        Map<String, Object> details = new HashMap<>();
        details.put("role", role);
        details.put("id", userId.toString());
        details.put("username", username);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, null, Collections.emptyList());
        authentication.setDetails(details);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getAllTransactions_asSuperadmin_success() throws Exception {
        setupSecurityContext("Superadmin", UUID.randomUUID(), "admin");
        when(topUpTransactionRestService.getAllTransactions("Superadmin", null)).thenReturn(List.of(mockTransaction));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(TRANSACTION_ID.toString()));
    }

    @Test
    void getAllTransactions_asCustomer_success() throws Exception {
        setupSecurityContext("Customer", CUSTOMER_ID, "customer");
        when(topUpTransactionRestService.getAllTransactions(eq("Customer"), eq(CUSTOMER_ID)))
                .thenReturn(List.of(mockTransaction));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(TRANSACTION_ID.toString()));
    }

    @Test
    void getTransactionById_asSuperadmin_success() throws Exception {
        setupSecurityContext("Superadmin", UUID.randomUUID(), "admin");
        when(topUpTransactionRestService.getTransactionById(TRANSACTION_ID)).thenReturn(mockTransaction);

        mockMvc.perform(get("/api/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(TRANSACTION_ID.toString()));
    }

    @Test
    void getTransactionById_asCustomer_forbidden() throws Exception {
        setupSecurityContext("Customer", CUSTOMER_ID, "customer");

        mockMvc.perform(get("/api/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTransaction_success() throws Exception {
        setupSecurityContext("Customer", CUSTOMER_ID, "customer");
        CreateTopUpTransactionRequestDTO request = new CreateTopUpTransactionRequestDTO();
        request.setAmount(100000L);
        request.setPaymentMethodId(UUID.randomUUID());
        request.setCustomerId(CUSTOMER_ID);

        when(topUpTransactionRestService.createTransaction(any(CreateTopUpTransactionRequestDTO.class), eq("customer")))
                .thenReturn(mockTransaction);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(TRANSACTION_ID.toString()));
    }

    @Test
    void createTransaction_forOtherUser_forbidden() throws Exception {
        setupSecurityContext("Customer", CUSTOMER_ID, "customer");
        CreateTopUpTransactionRequestDTO request = new CreateTopUpTransactionRequestDTO();
        request.setAmount(100000L);
        request.setPaymentMethodId(UUID.randomUUID());
        request.setCustomerId(UUID.randomUUID()); // Different ID

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTransactionStatus_asSuperadmin_success() throws Exception {
        setupSecurityContext("Superadmin", UUID.randomUUID(), "admin");
        UpdateTopUpStatusRequestDTO request = new UpdateTopUpStatusRequestDTO();
        request.setStatus("Success");

        mockTransaction.setStatus("Success");
        when(topUpTransactionRestService.updateTransactionStatus(eq(TRANSACTION_ID),
                any(UpdateTopUpStatusRequestDTO.class), any()))
                .thenReturn(mockTransaction);

        mockMvc.perform(put("/api/transactions/{id}/status", TRANSACTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("Success"));
    }

    @Test
    void deleteTransaction_asSuperadmin_success() throws Exception {
        setupSecurityContext("Superadmin", UUID.randomUUID(), "admin");

        mockMvc.perform(delete("/api/transactions/{id}", TRANSACTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
