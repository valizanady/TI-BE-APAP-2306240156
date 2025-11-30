package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateBillRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BillResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillIntegrationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BillIntegrationService billIntegrationService;

    private Package mockPackage;
    private final String CUSTOMER_ID = UUID.randomUUID().toString();
    private final String BILL_SERVICE_URL = "http://test-url/api/bill/create";
    private final String API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        mockPackage = new Package();
        mockPackage.setId("PKG-001");
        mockPackage.setPackageName("Test Package");
        mockPackage.setPrice(500000L);
        mockPackage.setUserId("owner-id");

        ReflectionTestUtils.setField(billIntegrationService, "billServiceUrl", BILL_SERVICE_URL);
        ReflectionTestUtils.setField(billIntegrationService, "apiKey", API_KEY);
    }

    @Test
    void createBillForPackage_success() {
        BillResponseDTO mockResponse = new BillResponseDTO();
        mockResponse.setId(UUID.randomUUID().toString());

        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class))).thenReturn(ResponseEntity.ok(mockResponse));

        BillResponseDTO result = billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);

        assertNotNull(result);
        assertEquals(mockResponse.getId(), result.getId());
    }

    @Test
    void createBillForPackage_clientError_throwsRuntimeException() {
        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        assertThrows(RuntimeException.class,
                () -> billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID));
    }

    @Test
    void createBillForPackage_serverError_throwsRuntimeException() {
        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        assertThrows(RuntimeException.class,
                () -> billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID));
    }
}
