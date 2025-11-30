package apap.ti._5.tour_package_2306240156_be.restcontroller.topup;

import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.model.TopUpTransaction;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.CreateTopUpTransactionRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.UpdateTopUpStatusRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.topup.TopUpTransactionResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.topup.TopUpTransactionRestService;
import apap.ti._5.tour_package_2306240156_be.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopUpTransactionRestControllerTest {

    @Mock
    private TopUpTransactionRestService topUpTransactionRestService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TopUpTransactionRestController controller;

    private TopUpTransaction mockTransaction;
    private UUID transactionId;
    private UUID customerId;
    private UUID paymentMethodId;
    private String customerUsername = "testuser";

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();

        PaymentMethod mockPaymentMethod = new PaymentMethod();
        mockPaymentMethod.setId(paymentMethodId);
        mockPaymentMethod.setMethodName("Bank Transfer");

        mockTransaction = new TopUpTransaction();
        mockTransaction.setId(transactionId);
        mockTransaction.setCustomerId(customerId);
        mockTransaction.setCustomerUsername(customerUsername);
        mockTransaction.setAmount(100000L);
        mockTransaction.setPaymentMethod(mockPaymentMethod);
        mockTransaction.setStatus("Pending");
        mockTransaction.setCreatedAt(LocalDateTime.now());

        SecurityContextHolder.setContext(securityContext);
    }

    private void setupSecurityContext(String role, UUID userId, String username) {
        Map<String, Object> details = new HashMap<>();
        details.put("role", role);
        details.put("id", userId.toString());
        details.put("username", username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(details);
    }

    @Test
    void getAllTransactions_asCustomer_success() {
        setupSecurityContext("Customer", customerId, customerUsername);
        when(topUpTransactionRestService.getAllTransactions("Customer", customerId))
                .thenReturn(List.of(mockTransaction));

        ResponseEntity<BaseResponseDTO<List<TopUpTransactionResponseDTO>>> response = 
                controller.getAllTransactions(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getAllTransactions_asSuperadmin_success() {
        UUID adminId = UUID.randomUUID();
        setupSecurityContext("Superadmin", adminId, "admin");
        when(topUpTransactionRestService.getAllTransactions("Superadmin", adminId))
                .thenReturn(List.of(mockTransaction));

        ResponseEntity<BaseResponseDTO<List<TopUpTransactionResponseDTO>>> response = 
                controller.getAllTransactions(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllTransactions_emptyList() {
        setupSecurityContext("Customer", customerId, customerUsername);
        when(topUpTransactionRestService.getAllTransactions("Customer", customerId))
                .thenReturn(new ArrayList<>());

        ResponseEntity<BaseResponseDTO<List<TopUpTransactionResponseDTO>>> response = 
                controller.getAllTransactions(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void getAllTransactions_serviceThrowsException() {
        setupSecurityContext("Customer", customerId, customerUsername);
        when(topUpTransactionRestService.getAllTransactions("Customer", customerId))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<BaseResponseDTO<List<TopUpTransactionResponseDTO>>> response = 
                controller.getAllTransactions(request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getTransactionById_asSuperadmin_success() {
        UUID adminId = UUID.randomUUID();
        setupSecurityContext("Superadmin", adminId, "admin");
        when(topUpTransactionRestService.getTransactionById(transactionId)).thenReturn(mockTransaction);

        ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> response = 
                controller.getTransactionById(transactionId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getTransactionById_asCustomer_forbidden() {
        setupSecurityContext("Customer", customerId, customerUsername);

        ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> response = 
                controller.getTransactionById(transactionId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createTransaction_asCustomer_success() {
        CreateTopUpTransactionRequestDTO requestDTO = new CreateTopUpTransactionRequestDTO();
        requestDTO.setCustomerId(customerId);
        requestDTO.setPaymentMethodId(paymentMethodId);
        requestDTO.setAmount(100000L);

        setupSecurityContext("Customer", customerId, customerUsername);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(topUpTransactionRestService.createTransaction(eq(requestDTO), eq(customerUsername)))
                .thenReturn(mockTransaction);

        ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> response = 
                controller.createTransaction(requestDTO, bindingResult, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createTransaction_validationError() {
        CreateTopUpTransactionRequestDTO requestDTO = new CreateTopUpTransactionRequestDTO();
        requestDTO.setAmount(-100L);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(
                List.of(new org.springframework.validation.FieldError("requestDTO", "amount", "Amount must be positive"))
        );

        ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> response = 
                controller.createTransaction(requestDTO, bindingResult, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createTransaction_wrongCustomerId_forbidden() {
        UUID otherCustomerId = UUID.randomUUID();
        CreateTopUpTransactionRequestDTO requestDTO = new CreateTopUpTransactionRequestDTO();
        requestDTO.setCustomerId(otherCustomerId);
        requestDTO.setAmount(100000L);

        setupSecurityContext("Customer", customerId, customerUsername);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> response = 
                controller.createTransaction(requestDTO, bindingResult, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateTransactionStatus_asSuperadmin_success() {
        UUID adminId = UUID.randomUUID();
        UpdateTopUpStatusRequestDTO requestDTO = new UpdateTopUpStatusRequestDTO();
        requestDTO.setStatus("Approved");

        TopUpTransaction updatedTransaction = new TopUpTransaction();
        updatedTransaction.setId(transactionId);
        updatedTransaction.setStatus("Approved");

        setupSecurityContext("Superadmin", adminId, "admin");
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(topUpTransactionRestService.updateTransactionStatus(eq(transactionId), eq(requestDTO), eq("test-token")))
                .thenReturn(updatedTransaction);

        ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> response = 
                controller.updateTransactionStatus(transactionId, requestDTO, bindingResult, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateTransactionStatus_asCustomer_forbidden() {
        UpdateTopUpStatusRequestDTO requestDTO = new UpdateTopUpStatusRequestDTO();
        requestDTO.setStatus("Approved");

        setupSecurityContext("Customer", customerId, customerUsername);

        ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> response = 
                controller.updateTransactionStatus(transactionId, requestDTO, bindingResult, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteTransaction_asSuperadmin_success() {
        UUID adminId = UUID.randomUUID();
        setupSecurityContext("Superadmin", adminId, "admin");
        doNothing().when(topUpTransactionRestService).deleteTransaction(transactionId);

        ResponseEntity<BaseResponseDTO<String>> response = 
                controller.deleteTransaction(transactionId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteTransaction_asCustomer_forbidden() {
        setupSecurityContext("Customer", customerId, customerUsername);

        ResponseEntity<BaseResponseDTO<String>> response = 
                controller.deleteTransaction(transactionId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
