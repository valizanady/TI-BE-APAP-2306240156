package apap.ti._5.tour_package_2306240156_be.restcontroller.topup;

import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.restdto.request.paymentmethod.CreatePaymentMethodRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.paymentmethod.PaymentMethodRestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentMethodRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentMethodRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentMethodRestService paymentMethodRestService;

    private PaymentMethod mockPaymentMethod;
    private final UUID PAYMENT_METHOD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockPaymentMethod = new PaymentMethod();
        mockPaymentMethod.setId(PAYMENT_METHOD_ID);
        mockPaymentMethod.setMethodName("Bank Transfer");
        mockPaymentMethod.setStatus("Active");
    }

    @Test
    @WithMockUser
    void getAllPaymentMethods_success() throws Exception {
        when(paymentMethodRestService.getAllPaymentMethods()).thenReturn(List.of(mockPaymentMethod));

        mockMvc.perform(get("/api/payment-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(PAYMENT_METHOD_ID.toString()))
                .andExpect(jsonPath("$.data[0].methodName").value("Bank Transfer"));
    }

    @Test
    @WithMockUser
    void getAllPaymentMethods_withStatus_success() throws Exception {
        when(paymentMethodRestService.getPaymentMethodsByStatus("Active")).thenReturn(List.of(mockPaymentMethod));

        mockMvc.perform(get("/api/payment-methods").param("status", "Active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(PAYMENT_METHOD_ID.toString()));
    }

    @Test
    @WithMockUser
    void getPaymentMethodById_success() throws Exception {
        when(paymentMethodRestService.getPaymentMethodById(PAYMENT_METHOD_ID)).thenReturn(mockPaymentMethod);

        mockMvc.perform(get("/api/payment-methods/{id}", PAYMENT_METHOD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(PAYMENT_METHOD_ID.toString()));
    }

    @Test
    @WithMockUser(roles = "Superadmin")
    void createPaymentMethod_success() throws Exception {
        CreatePaymentMethodRequestDTO request = new CreatePaymentMethodRequestDTO();
        request.setMethodName("New Method");
        request.setProvider("New Provider");

        PaymentMethod createdMethod = new PaymentMethod();
        createdMethod.setId(UUID.randomUUID());
        createdMethod.setMethodName("New Method");
        createdMethod.setStatus("Active");

        when(paymentMethodRestService.createPaymentMethod(any(CreatePaymentMethodRequestDTO.class)))
                .thenReturn(createdMethod);

        mockMvc.perform(post("/api/payment-methods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.methodName").value("New Method"));
    }

    @Test
    @WithMockUser(roles = "Superadmin")
    void updatePaymentMethodStatus_success() throws Exception {
        Map<String, String> request = Map.of("status", "Inactive");
        mockPaymentMethod.setStatus("Inactive");

        when(paymentMethodRestService.updatePaymentMethodStatus(eq(PAYMENT_METHOD_ID), eq("Inactive")))
                .thenReturn(mockPaymentMethod);

        mockMvc.perform(put("/api/payment-methods/{id}/status", PAYMENT_METHOD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("Inactive"));
    }

    @Test
    @WithMockUser(roles = "Superadmin")
    void deletePaymentMethod_success() throws Exception {
        mockMvc.perform(delete("/api/payment-methods/{id}", PAYMENT_METHOD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment method deleted successfully"));
    }
}
