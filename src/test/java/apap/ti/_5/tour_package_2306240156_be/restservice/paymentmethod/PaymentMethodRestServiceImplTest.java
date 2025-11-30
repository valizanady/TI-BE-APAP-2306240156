package apap.ti._5.tour_package_2306240156_be.restservice.paymentmethod;

import apap.ti._5.tour_package_2306240156_be.exception.BadRequestException;
import apap.ti._5.tour_package_2306240156_be.exception.NotFoundException;
import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.repository.PaymentMethodRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.paymentmethod.CreatePaymentMethodRequestDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodRestServiceImplTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodRestServiceImpl paymentMethodRestService;

    private PaymentMethod mockPaymentMethod;
    private final UUID PAYMENT_METHOD_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockPaymentMethod = new PaymentMethod();
        mockPaymentMethod.setId(PAYMENT_METHOD_ID);
        mockPaymentMethod.setMethodName("Bank Transfer");
        mockPaymentMethod.setProvider("BCA");
        mockPaymentMethod.setStatus("Active");
    }

    @Test
    void getAllPaymentMethods_success() {
        when(paymentMethodRepository.findByDeletedAtIsNullOrderByCreatedAtDesc())
                .thenReturn(List.of(mockPaymentMethod));

        List<PaymentMethod> result = paymentMethodRestService.getAllPaymentMethods();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getPaymentMethodsByStatus_success() {
        when(paymentMethodRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc("Active"))
                .thenReturn(List.of(mockPaymentMethod));

        List<PaymentMethod> result = paymentMethodRestService.getPaymentMethodsByStatus("Active");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getPaymentMethodById_success() {
        when(paymentMethodRepository.findByIdAndDeletedAtIsNull(PAYMENT_METHOD_ID))
                .thenReturn(Optional.of(mockPaymentMethod));

        PaymentMethod result = paymentMethodRestService.getPaymentMethodById(PAYMENT_METHOD_ID);

        assertNotNull(result);
        assertEquals(PAYMENT_METHOD_ID, result.getId());
    }

    @Test
    void getPaymentMethodById_notFound_throwsException() {
        when(paymentMethodRepository.findByIdAndDeletedAtIsNull(PAYMENT_METHOD_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentMethodRestService.getPaymentMethodById(PAYMENT_METHOD_ID));
    }

    @Test
    void createPaymentMethod_success() {
        CreatePaymentMethodRequestDTO request = new CreatePaymentMethodRequestDTO();
        request.setMethodName("New Method");
        request.setProvider("New Provider");

        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(invocation -> {
            PaymentMethod pm = invocation.getArgument(0);
            pm.setId(UUID.randomUUID());
            return pm;
        });

        PaymentMethod result = paymentMethodRestService.createPaymentMethod(request);

        assertNotNull(result);
        assertEquals("New Method", result.getMethodName());
        assertEquals("Active", result.getStatus());
    }

    @Test
    void createPaymentMethod_invalidInput_throwsException() {
        CreatePaymentMethodRequestDTO request = new CreatePaymentMethodRequestDTO();
        // Missing name and provider

        assertThrows(BadRequestException.class, () -> paymentMethodRestService.createPaymentMethod(request));
    }

    @Test
    void updatePaymentMethodStatus_success() {
        when(paymentMethodRepository.findByIdAndDeletedAtIsNull(PAYMENT_METHOD_ID))
                .thenReturn(Optional.of(mockPaymentMethod));
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(mockPaymentMethod);

        PaymentMethod result = paymentMethodRestService.updatePaymentMethodStatus(PAYMENT_METHOD_ID, "Inactive");

        assertNotNull(result);
        assertEquals("Inactive", result.getStatus());
    }

    @Test
    void updatePaymentMethodStatus_invalidStatus_throwsException() {
        when(paymentMethodRepository.findByIdAndDeletedAtIsNull(PAYMENT_METHOD_ID))
                .thenReturn(Optional.of(mockPaymentMethod));

        assertThrows(BadRequestException.class,
                () -> paymentMethodRestService.updatePaymentMethodStatus(PAYMENT_METHOD_ID, "InvalidStatus"));
    }

    @Test
    void deletePaymentMethod_success() {
        when(paymentMethodRepository.findByIdAndDeletedAtIsNull(PAYMENT_METHOD_ID))
                .thenReturn(Optional.of(mockPaymentMethod));

        paymentMethodRestService.deletePaymentMethod(PAYMENT_METHOD_ID);

        verify(paymentMethodRepository).save(mockPaymentMethod);
        assertNotNull(mockPaymentMethod.getDeletedAt());
    }
}
