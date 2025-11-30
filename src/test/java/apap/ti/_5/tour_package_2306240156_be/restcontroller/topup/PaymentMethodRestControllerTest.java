package apap.ti._5.tour_package_2306240156_be.restcontroller.topup;

import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.restdto.request.paymentmethod.CreatePaymentMethodRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.paymentmethod.PaymentMethodRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodRestControllerTest {

    @Mock
    private PaymentMethodRestService paymentMethodRestService;

    @InjectMocks
    private PaymentMethodRestController controller;

    private PaymentMethod mockPaymentMethod;
    private UUID paymentMethodId;

    @BeforeEach
    void setUp() {
        paymentMethodId = UUID.randomUUID();
        
        mockPaymentMethod = new PaymentMethod();
        mockPaymentMethod.setId(paymentMethodId);
        mockPaymentMethod.setMethodName("Bank Transfer");
        mockPaymentMethod.setProvider("BCA");
        mockPaymentMethod.setStatus("Active");
    }

    @Test
    void getAllPaymentMethods_noStatus_success() {
        when(paymentMethodRestService.getAllPaymentMethods()).thenReturn(List.of(mockPaymentMethod));

        ResponseEntity<BaseResponseDTO<List<PaymentMethod>>> response = controller.getAllPaymentMethods(null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Bank Transfer", response.getBody().getData().get(0).getMethodName());
    }

    @Test
    void getAllPaymentMethods_withStatus_success() {
        when(paymentMethodRestService.getPaymentMethodsByStatus("Active")).thenReturn(List.of(mockPaymentMethod));

        ResponseEntity<BaseResponseDTO<List<PaymentMethod>>> response = controller.getAllPaymentMethods("Active");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getAllPaymentMethods_emptyList() {
        when(paymentMethodRestService.getAllPaymentMethods()).thenReturn(new ArrayList<>());

        ResponseEntity<BaseResponseDTO<List<PaymentMethod>>> response = controller.getAllPaymentMethods(null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void getPaymentMethodById_success() {
        when(paymentMethodRestService.getPaymentMethodById(paymentMethodId)).thenReturn(mockPaymentMethod);

        ResponseEntity<BaseResponseDTO<PaymentMethod>> response = controller.getPaymentMethodById(paymentMethodId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(paymentMethodId, response.getBody().getData().getId());
    }

    @Test
    void getPaymentMethodById_notFound() {
        UUID invalidId = UUID.randomUUID();
        when(paymentMethodRestService.getPaymentMethodById(invalidId))
                .thenThrow(new RuntimeException("Payment method not found"));

        assertThrows(RuntimeException.class, () -> controller.getPaymentMethodById(invalidId));
    }

    @Test
    void createPaymentMethod_success() {
        CreatePaymentMethodRequestDTO request = new CreatePaymentMethodRequestDTO();
        request.setMethodName("New Method");
        request.setProvider("New Provider");

        PaymentMethod createdMethod = new PaymentMethod();
        createdMethod.setId(UUID.randomUUID());
        createdMethod.setMethodName("New Method");
        createdMethod.setStatus("Active");

        when(paymentMethodRestService.createPaymentMethod(any(CreatePaymentMethodRequestDTO.class)))
                .thenReturn(createdMethod);

        ResponseEntity<BaseResponseDTO<PaymentMethod>> response = controller.createPaymentMethod(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("New Method", response.getBody().getData().getMethodName());
    }

    @Test
    void updatePaymentMethodStatus_success() {
        when(paymentMethodRestService.updatePaymentMethodStatus(paymentMethodId, "Inactive"))
                .thenReturn(mockPaymentMethod);

        ResponseEntity<BaseResponseDTO<PaymentMethod>> response = 
                controller.updatePaymentMethodStatus(paymentMethodId, Map.of("status", "Inactive"));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deletePaymentMethod_success() {
        doNothing().when(paymentMethodRestService).deletePaymentMethod(paymentMethodId);

        ResponseEntity<BaseResponseDTO<Void>> response = controller.deletePaymentMethod(paymentMethodId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentMethodRestService, times(1)).deletePaymentMethod(paymentMethodId);
    }
}
