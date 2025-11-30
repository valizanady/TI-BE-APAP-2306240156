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
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    // ========== Additional Comprehensive Tests ==========

    @Test
    void testCreateBillForPackage_Success_201Created() {
        BillResponseDTO mockResponse = new BillResponseDTO();
        mockResponse.setId(UUID.randomUUID().toString());
        mockResponse.setServiceName("TOURPACKAGE");
        mockResponse.setServiceReferenceId("PKG-001");

        ResponseEntity<BillResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class))).thenReturn(response);

        BillResponseDTO result = billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);

        assertNotNull(result);
        assertEquals("TOURPACKAGE", result.getServiceName());
        assertEquals("PKG-001", result.getServiceReferenceId());
    }

    @Test
    void testCreateBillForPackage_VerifyRequestBody() {
        BillResponseDTO mockResponse = new BillResponseDTO();
        mockResponse.setId(UUID.randomUUID().toString());
        ResponseEntity<BillResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class))).thenReturn(response);

        billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);

        verify(restTemplate).exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    @SuppressWarnings("unchecked")
                    HttpEntity<CreateBillRequestDTO> httpEntity = (HttpEntity<CreateBillRequestDTO>) entity;
                    CreateBillRequestDTO body = httpEntity.getBody();
                    
                    return body != null &&
                           CUSTOMER_ID.equals(body.getCustomerId()) &&
                           "TOURPACKAGE".equals(body.getServiceName()) &&
                           "PKG-001".equals(body.getServiceReferenceId()) &&
                           500000L == body.getAmount();
                }),
                eq(BillResponseDTO.class)
        );
    }

    @Test
    void testCreateBillForPackage_VerifyHeaders() {
        BillResponseDTO mockResponse = new BillResponseDTO();
        mockResponse.setId(UUID.randomUUID().toString());
        ResponseEntity<BillResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class))).thenReturn(response);

        billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);

        verify(restTemplate).exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    @SuppressWarnings("unchecked")
                    HttpEntity<?> httpEntity = (HttpEntity<?>) entity;
                    HttpHeaders headers = httpEntity.getHeaders();
                    
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                           API_KEY.equals(headers.getFirst("API-KEY"));
                }),
                eq(BillResponseDTO.class)
        );
    }

    @Test
    void testCreateBillForPackage_Unauthorized_401() {
        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid API key"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);
        });

        assertTrue(exception.getMessage().contains("Client error when creating Bill"));
    }

    @Test
    void testCreateBillForPackage_Forbidden_403() {
        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access denied"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);
        });

        assertTrue(exception.getMessage().contains("Client error when creating Bill"));
    }

    @Test
    void testCreateBillForPackage_ServerError_500() {
        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);
        });

        assertTrue(exception.getMessage().contains("Server error from Bill Service"));
    }

    @Test
    void testCreateBillForPackage_ServiceUnavailable_503() {
        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);
        });

        assertTrue(exception.getMessage().contains("Server error from Bill Service"));
    }

    @Test
    void testCreateBillForPackage_GenericException() {
        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);
        });

        assertTrue(exception.getMessage().contains("Failed to create Bill"));
    }

    @Test
    void testCreateBillForPackage_UnexpectedStatusCode() {
        BillResponseDTO mockResponse = new BillResponseDTO();
        ResponseEntity<BillResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.ACCEPTED);

        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class))).thenReturn(response);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            billIntegrationService.createBillForPackage(mockPackage, CUSTOMER_ID);
        });

        assertTrue(exception.getMessage().contains("Unexpected response from Bill Service"));
    }

    @Test
    void testCreateBillForPackage_DifferentCustomerThanOwner() {
        mockPackage.setUserId("owner-123");
        String processorId = "processor-456";

        BillResponseDTO mockResponse = new BillResponseDTO();
        mockResponse.setId(UUID.randomUUID().toString());
        ResponseEntity<BillResponseDTO> response = new ResponseEntity<>(mockResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(BillResponseDTO.class))).thenReturn(response);

        billIntegrationService.createBillForPackage(mockPackage, processorId);

        verify(restTemplate).exchange(
                eq(BILL_SERVICE_URL),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    @SuppressWarnings("unchecked")
                    HttpEntity<CreateBillRequestDTO> httpEntity = (HttpEntity<CreateBillRequestDTO>) entity;
                    CreateBillRequestDTO body = httpEntity.getBody();
                    
                    return body != null && processorId.equals(body.getCustomerId());
                }),
                eq(BillResponseDTO.class)
        );
    }
}

