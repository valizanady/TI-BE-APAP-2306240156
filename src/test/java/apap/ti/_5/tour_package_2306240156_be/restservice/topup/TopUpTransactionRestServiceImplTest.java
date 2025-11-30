package apap.ti._5.tour_package_2306240156_be.restservice.topup;

import apap.ti._5.tour_package_2306240156_be.exception.BadRequestException;
import apap.ti._5.tour_package_2306240156_be.exception.NotFoundException;
import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.model.TopUpTransaction;
import apap.ti._5.tour_package_2306240156_be.repository.PaymentMethodRepository;
import apap.ti._5.tour_package_2306240156_be.repository.TopUpTransactionRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.CreateTopUpTransactionRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.UpdateTopUpStatusRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.ProfileServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopUpTransactionRestServiceImplTest {

    @Mock
    private TopUpTransactionRepository topUpTransactionRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private ProfileServiceClient profileServiceClient;

    @InjectMocks
    private TopUpTransactionRestServiceImpl topUpTransactionRestService;

    private TopUpTransaction mockTransaction;
    private PaymentMethod mockPaymentMethod;
    private final UUID TRANSACTION_ID = UUID.randomUUID();
    private final UUID PAYMENT_METHOD_ID = UUID.randomUUID();
    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final String CUSTOMER_USERNAME = "customer";

    @BeforeEach
    void setUp() {
        mockPaymentMethod = new PaymentMethod();
        mockPaymentMethod.setId(PAYMENT_METHOD_ID);
        mockPaymentMethod.setStatus("Active");

        mockTransaction = new TopUpTransaction();
        mockTransaction.setId(TRANSACTION_ID);
        mockTransaction.setCustomerId(CUSTOMER_ID);
        mockTransaction.setCustomerUsername(CUSTOMER_USERNAME);
        mockTransaction.setAmount(100000L);
        mockTransaction.setStatus("Pending");
        mockTransaction.setPaymentMethod(mockPaymentMethod);
    }

    @Test
    void getAllTransactions_superadmin_success() {
        when(topUpTransactionRepository.findByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(List.of(mockTransaction));

        List<TopUpTransaction> result = topUpTransactionRestService.getAllTransactions("Superadmin", null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllTransactions_customer_success() {
        when(topUpTransactionRepository.findByCustomerIdAndDeletedAtIsNullOrderByCreatedAtDesc(CUSTOMER_ID))
                .thenReturn(List.of(mockTransaction));

        List<TopUpTransaction> result = topUpTransactionRestService.getAllTransactions("Customer", CUSTOMER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getTransactionById_success() {
        when(topUpTransactionRepository.findByIdAndDeletedAtIsNull(TRANSACTION_ID))
                .thenReturn(Optional.of(mockTransaction));

        TopUpTransaction result = topUpTransactionRestService.getTransactionById(TRANSACTION_ID);

        assertNotNull(result);
        assertEquals(TRANSACTION_ID, result.getId());
    }

    @Test
    void createTransaction_success() {
        CreateTopUpTransactionRequestDTO request = new CreateTopUpTransactionRequestDTO();
        request.setAmount(100000L);
        request.setPaymentMethodId(PAYMENT_METHOD_ID);
        request.setCustomerId(CUSTOMER_ID);

        when(paymentMethodRepository.findByIdAndDeletedAtIsNull(PAYMENT_METHOD_ID))
                .thenReturn(Optional.of(mockPaymentMethod));
        when(topUpTransactionRepository.save(any(TopUpTransaction.class))).thenReturn(mockTransaction);

        TopUpTransaction result = topUpTransactionRestService.createTransaction(request, CUSTOMER_USERNAME);

        assertNotNull(result);
        assertEquals(100000L, result.getAmount());
    }

    @Test
    void createTransaction_invalidAmount_throwsException() {
        CreateTopUpTransactionRequestDTO request = new CreateTopUpTransactionRequestDTO();
        request.setAmount(-100L);

        assertThrows(BadRequestException.class,
                () -> topUpTransactionRestService.createTransaction(request, CUSTOMER_USERNAME));
    }

    @Test
    void updateTransactionStatus_approve_success() {
        UpdateTopUpStatusRequestDTO request = new UpdateTopUpStatusRequestDTO();
        request.setStatus("Success");

        when(topUpTransactionRepository.findByIdAndDeletedAtIsNull(TRANSACTION_ID))
                .thenReturn(Optional.of(mockTransaction));
        when(profileServiceClient.addBalanceToProfile(eq(CUSTOMER_USERNAME), eq(100000L), anyString()))
                .thenReturn(true);
        when(topUpTransactionRepository.save(any(TopUpTransaction.class))).thenReturn(mockTransaction);

        TopUpTransaction result = topUpTransactionRestService.updateTransactionStatus(TRANSACTION_ID, request, "token");

        assertNotNull(result);
        assertEquals("Success", result.getStatus());
        verify(profileServiceClient).addBalanceToProfile(eq(CUSTOMER_USERNAME), eq(100000L), anyString());
    }

    @Test
    void updateTransactionStatus_alreadyApproved_skipsBalanceUpdate() {
        mockTransaction.setStatus("Success");
        UpdateTopUpStatusRequestDTO request = new UpdateTopUpStatusRequestDTO();
        request.setStatus("Success");

        when(topUpTransactionRepository.findByIdAndDeletedAtIsNull(TRANSACTION_ID))
                .thenReturn(Optional.of(mockTransaction));

        TopUpTransaction result = topUpTransactionRestService.updateTransactionStatus(TRANSACTION_ID, request, "token");

        assertNotNull(result);
        verify(profileServiceClient, never()).addBalanceToProfile(anyString(), anyLong(), anyString());
    }

    @Test
    void updateTransactionStatus_reject_success() {
        UpdateTopUpStatusRequestDTO request = new UpdateTopUpStatusRequestDTO();
        request.setStatus("Failed");

        when(topUpTransactionRepository.findByIdAndDeletedAtIsNull(TRANSACTION_ID))
                .thenReturn(Optional.of(mockTransaction));
        when(topUpTransactionRepository.save(any(TopUpTransaction.class))).thenReturn(mockTransaction);

        TopUpTransaction result = topUpTransactionRestService.updateTransactionStatus(TRANSACTION_ID, request, "token");

        assertNotNull(result);
        assertEquals("Failed", result.getStatus());
        verify(profileServiceClient, never()).addBalanceToProfile(anyString(), anyLong(), anyString());
    }

    @Test
    void deleteTransaction_success() {
        when(topUpTransactionRepository.findByIdAndDeletedAtIsNull(TRANSACTION_ID))
                .thenReturn(Optional.of(mockTransaction));

        topUpTransactionRestService.deleteTransaction(TRANSACTION_ID);

        verify(topUpTransactionRepository).save(mockTransaction);
        assertNotNull(mockTransaction.getDeletedAt());
    }
}
