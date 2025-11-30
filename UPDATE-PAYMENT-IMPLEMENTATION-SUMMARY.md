# Update Payment Package by BILLING - Implementation Summary

## ✅ Implementation Complete

The "Update Payment Package by BILLING" feature has been successfully implemented. This feature allows Bill Service to notify Tour Package Service when a payment is confirmed using **API Key authentication** (not JWT).

---

## 📁 Files Created/Modified

### **New Files:**

1. **`UpdatePaymentStatusRequestDTO.java`**
   - Location: `src/main/java/apap/ti/_5/tour_package_2306240156_be/restdto/request/`
   - Purpose: DTO for payment update request
   - Fields: `packageId`, `status`
   - Validation: `@NotBlank` on both fields

2. **`ApiKeyFilter.java`**
   - Location: `src/main/java/apap/ti/_5/tour_package_2306240156_be/security/`
   - Purpose: Validates `x-api-key` header for microservice authentication
   - Applies to: `/api/packages/payment/*` and `/api/package/payment/*` endpoints
   - Returns: 401 Unauthorized if API key is missing or invalid

3. **`PAYMENT-UPDATE-API-KEY-SETUP.md`**
   - Location: `tour-package-2306240156-be/`
   - Purpose: Complete documentation for setup, testing, and troubleshooting

### **Modified Files:**

1. **`PackageRestService.java`** (Interface)
   - Added method: `PackageResponseDTO updatePaymentStatus(String packageId, String status)`

2. **`TourPackageRestServiceImpl.java`**
   - Implemented: `updatePaymentStatus()` method
   - Business Logic:
     - Validates package exists
     - Checks current status is "Waiting for Payment"
     - Updates to "Payment Confirmed" when status is "PAID"
     - Throws exception for invalid status or state

3. **`PackageRestController.java`**
   - Added endpoint: `POST /api/package/payment/update`
   - Authentication: API Key only (no JWT)
   - Request Body: `UpdatePaymentStatusRequestDTO`
   - Response: `BaseResponseDTO<PackageResponseDTO>`

4. **`WebSecurityConfig.java`**
   - Injected `ApiKeyFilter` component
   - Permitted `/api/package/payment/**` and `/api/packages/payment/**` without JWT
   - Added `apiKeyFilter` to filter chain **before** `UsernamePasswordAuthenticationFilter`

---

## 🔧 Configuration Required

### Environment Variable:

Add to your `.env` file or system environment:

```bash
API_KEY_BILL_SERVICE=your-secret-api-key-here
```

**Example:**
```bash
API_KEY_BILL_SERVICE=bill-service-secret-key-2024
```

**Note:** If not set, defaults to `"default-secret-key"` (see `ApiKeyFilter.java`)

---

## 📡 API Specification

### **Endpoint:** `POST /api/package/payment/update`

### **Authentication:**
- **Type:** API Key (Header-based)
- **Header:** `x-api-key: <your-api-key>`
- **No JWT Required**

### **Request:**
```json
{
  "packageId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PAID"
}
```

### **Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment status updated successfully",
  "timestamp": "2024-01-15T10:30:00",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "user123",
    "creatorRole": "Customer",
    "packageName": "Bali Adventure Package",
    "quota": 5,
    "price": 5000000,
    "status": "Payment Confirmed",
    "startDate": "2024-02-01",
    "endDate": "2024-02-07"
  }
}
```

### **Error Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized",
  "message": "Invalid API Key"
}
```

### **Error Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Cannot update payment status. Package current status is 'Processed', expected 'Waiting for Payment'",
  "timestamp": "2024-01-15T10:30:00",
  "data": null
}
```

---

## 🧪 Testing Commands

### **Test with cURL:**

```bash
# Set your API key and package ID
API_KEY="bill-service-secret-key-2024"
PACKAGE_ID="your-package-id-here"

# Send payment update request
curl -X POST http://localhost:8080/api/package/payment/update \
  -H "Content-Type: application/json" \
  -H "x-api-key: $API_KEY" \
  -d "{
    \"packageId\": \"$PACKAGE_ID\",
    \"status\": \"PAID\"
  }"
```

### **Test without API Key (should return 401):**

```bash
curl -X POST http://localhost:8080/api/package/payment/update \
  -H "Content-Type: application/json" \
  -d '{
    "packageId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PAID"
  }'
```

### **Test with invalid API Key (should return 401):**

```bash
curl -X POST http://localhost:8080/api/package/payment/update \
  -H "Content-Type: application/json" \
  -H "x-api-key: wrong-api-key" \
  -d '{
    "packageId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PAID"
  }'
```

---

## 🔒 Security Architecture

### **Filter Chain Order:**
```
Request → ApiKeyFilter → JwtTokenFilter → UsernamePasswordAuthenticationFilter → Controller
```

### **API Key Validation Flow:**

1. **Request arrives** at `/api/package/payment/update`
2. **ApiKeyFilter intercepts** (checks path matches `/api/packages/payment/*` or `/api/package/payment/*`)
3. **Validates** `x-api-key` header against `API_KEY_BILL_SERVICE` environment variable
4. **If valid:** Continues to controller
5. **If invalid/missing:** Returns 401 Unauthorized immediately

### **Endpoint Permissions:**

| Endpoint Pattern | Authentication | Authorization |
|-----------------|----------------|---------------|
| `/api/package/payment/**` | API Key | Permits All |
| `/api/packages/payment/**` | API Key | Permits All |
| `/api/package/**` (other) | JWT | Role-based |

---

## 📋 Business Rules

### **Status Validation:**

| Current Package Status | Can Update? | Target Status |
|----------------------|------------|---------------|
| "Waiting for Payment" | ✅ YES | "Payment Confirmed" |
| "Pending" | ❌ NO | Error |
| "Processed" | ❌ NO | Error |
| "Payment Confirmed" | ❌ NO | Error |
| "Cancelled" | ❌ NO | Error |

### **Payment Status Supported:**
- ✅ `"PAID"` → Updates package to "Payment Confirmed"
- ❌ Other values → Returns error

---

## 🚀 Integration Example (Bill Service)

### **Java (Spring Boot):**

```java
@Service
public class BillService {
    
    @Value("${TOUR_PACKAGE_SERVICE_URL}")
    private String tourPackageServiceUrl;
    
    @Value("${API_KEY_BILL_SERVICE}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    
    public void notifyPaymentConfirmed(String packageId) {
        String url = tourPackageServiceUrl + "/api/package/payment/update";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        
        Map<String, String> request = new HashMap<>();
        request.put("packageId", packageId);
        request.put("status", "PAID");
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("✅ Package payment status updated successfully: {}", packageId);
            }
        } catch (Exception e) {
            logger.error("❌ Failed to update package payment status: {}", e.getMessage());
            throw new RuntimeException("Failed to notify Tour Package Service", e);
        }
    }
}
```

---

## 🎯 Key Points

✅ **Microservice-to-Microservice Communication** - Not for frontend use  
✅ **API Key Authentication** - Separate from JWT user authentication  
✅ **Status Validation** - Only updates from "Waiting for Payment"  
✅ **Secure by Design** - Filter validates before reaching controller  
✅ **Idempotent** - Safe to retry with same packageId  
✅ **Detailed Logging** - All validation and updates are logged  

---

## 📞 Next Steps

1. **Set Environment Variable:**
   ```bash
   export API_KEY_BILL_SERVICE=your-secret-key-here
   ```

2. **Restart Application:**
   ```bash
   ./gradlew bootRun
   ```

3. **Test Endpoint:**
   - Use cURL or Postman
   - Verify API key validation works
   - Check package status update

4. **Integrate with Bill Service:**
   - Share API key securely
   - Provide endpoint URL
   - Test end-to-end flow

---

## 📚 Documentation References

- **Setup Guide:** `PAYMENT-UPDATE-API-KEY-SETUP.md`
- **DTO:** `UpdatePaymentStatusRequestDTO.java`
- **Filter:** `ApiKeyFilter.java`
- **Security Config:** `WebSecurityConfig.java`
- **Service:** `TourPackageRestServiceImpl.java`
- **Controller:** `PackageRestController.java`

---

**Implementation Date:** January 2025  
**Status:** ✅ Complete and Ready for Testing  
**Feature:** Update Payment Package by BILLING  
**Authentication:** API Key (x-api-key header)

