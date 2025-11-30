package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreateBillRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BillResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service untuk integrasi dengan Bill Service
 * Menangani pembuatan Bill setelah Package diproses
 */
@Service
@RequiredArgsConstructor
public class BillIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(BillIntegrationService.class);

    private final RestTemplate restTemplate;

    @Value("${BILL_SERVICE_URL:http://2306165931-be.hafizmuh.site/api/bill/create}")
    private String billServiceUrl;

    @Value("${BILL_SERVICE_API_KEY:NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS}")
    private String apiKey;

    /**
     * Membuat Bill di Bill Service setelah Package berhasil diproses
     * 
     * @param processedPackage Package yang sudah diproses (status = "Processed")
     * @param authenticatedCustomerId ID customer yang melakukan process package (dari JWT token)
     * @return BillResponseDTO dari Bill Service
     * @throws RuntimeException jika gagal membuat Bill
     */
    public BillResponseDTO createBillForPackage(Package processedPackage, String authenticatedCustomerId) {
        logger.info("🔔 Creating Bill for processed package: {}", processedPackage.getId());
        
        try {
            // 1. Build request DTO
            // ⚠️ IMPORTANT: customerId diambil dari authenticated user (yang process package),
            //               BUKAN dari package.userId (owner package)
            CreateBillRequestDTO billRequest = CreateBillRequestDTO.builder()
                    .customerId(authenticatedCustomerId) // ✅ Customer yang process package
                    .serviceName("TOURPACKAGE") // ✅ Must match enum in Bill Service
                    .serviceReferenceId(processedPackage.getId())
                    .description("Bill for processed tour package: " + processedPackage.getPackageName())
                    .amount(processedPackage.getPrice())
                    .build();

            logger.info("   Request Body:");
            logger.info("   - Customer ID (Processor): {}", billRequest.getCustomerId());
            logger.info("   - Package Owner ID: {}", processedPackage.getUserId());
            logger.info("   - Service Name: {}", billRequest.getServiceName());
            logger.info("   - Service Reference ID: {}", billRequest.getServiceReferenceId());
            logger.info("   - Amount: Rp {}", billRequest.getAmount());
            logger.info("   - Description: {}", billRequest.getDescription());

            // 2. Set headers with API Key (menggunakan header "API-KEY" sesuai Bill Service)
            // ⚠️ DO NOT send Authorization header - Bill Service will use customerId from request body
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("API-KEY", apiKey);

            logger.info("   API Key: {}***", apiKey.substring(0, Math.min(10, apiKey.length())));
            logger.info("   Bill Service URL: {}", billServiceUrl);

            // 3. Create HTTP entity
            HttpEntity<CreateBillRequestDTO> requestEntity = new HttpEntity<>(billRequest, headers);

            // 4. Call Bill Service
            logger.info("📡 Sending POST request to Bill Service...");
            ResponseEntity<BillResponseDTO> response = restTemplate.exchange(
                    billServiceUrl,
                    HttpMethod.POST,
                    requestEntity,
                    BillResponseDTO.class
            );

            // 5. Check response
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                BillResponseDTO billResponse = response.getBody();
                logger.info("✅ Bill created successfully!");
                logger.info("   Bill ID: {}", billResponse != null ? billResponse.getId() : "N/A");
                logger.info("   Status: {}", response.getStatusCode());
                return billResponse;
            } else {
                String errorMsg = String.format("Unexpected response from Bill Service: %s", response.getStatusCode());
                logger.error("❌ {}", errorMsg);
                throw new RuntimeException(errorMsg);
            }

        } catch (HttpClientErrorException e) {
            // 4xx errors (Bad Request, Unauthorized, etc.)
            String errorMsg = String.format("Client error when creating Bill: %s - %s", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            logger.error("❌ {}", errorMsg);
            throw new RuntimeException(errorMsg, e);

        } catch (HttpServerErrorException e) {
            // 5xx errors (Internal Server Error, etc.)
            String errorMsg = String.format("Server error from Bill Service: %s - %s", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            logger.error("❌ {}", errorMsg);
            throw new RuntimeException(errorMsg, e);

        } catch (Exception e) {
            // Other errors (timeout, connection refused, etc.)
            String errorMsg = String.format("Failed to create Bill: %s", e.getMessage());
            logger.error("❌ {}", errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }
}
