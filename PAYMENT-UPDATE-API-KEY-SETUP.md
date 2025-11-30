# Payment Update API Key Setup

## Overview
The **Update Payment Package by BILLING** feature allows Bill Service to notify Tour Package Service when a payment is confirmed. This communication uses **API Key authentication** (not JWT) for microservice-to-microservice calls.

---

## 🔧 Configuration

### Environment Variable Setup

Add the following environment variable to your `.env` file or system environment:

```bash
API_KEY_BILL_SERVICE=your-secret-api-key-here
```

**Example `.env` file:**
```properties
# Database Configuration
DATABASE_URL_DEV=jdbc:postgresql://localhost:5432/tour_package_db
DEV_USERNAME=postgres
DEV_PASSWORD=your-password

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# JWT Configuration
JWT_SECRET=your-jwt-secret-key

# API Key for Bill Service
API_KEY_BILL_SERVICE=bill-service-secret-key-2024
```

---

## 📡 API Endpoint

### **POST** `/api/package/payment/update`

Updates package payment status from "Waiting for Payment" to "Payment Confirmed" when Bill Service confirms payment.

#### **Authentication:**
- **Type:** API Key (Header-based)
- **Header:** `x-api-key: <API_KEY_BILL_SERVICE>`
- **Note:** JWT token is NOT required for this endpoint

#### **Request Body:**
```json
{
  "packageId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PAID"
}
```

#### **Success Response (200 OK):**
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

#### **Error Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Cannot update payment status. Package current status is 'Processed', expected 'Waiting for Payment'",
  "timestamp": "2024-01-15T10:30:00",
  "data": null
}
```

#### **Error Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized",
  "message": "Invalid API Key"
}
```

---

## 🧪 Testing the Endpoint

### Using cURL:

```bash
curl -X POST http://localhost:8080/api/package/payment/update \
  -H "Content-Type: application/json" \
  -H "x-api-key: bill-service-secret-key-2024" \
  -d '{
    "packageId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PAID"
  }'
```

### Using Postman:

1. **Method:** POST
2. **URL:** `http://localhost:8080/api/package/payment/update`
3. **Headers:**
   - `Content-Type: application/json`
   - `x-api-key: bill-service-secret-key-2024`
4. **Body (JSON):**
   ```json
   {
     "packageId": "your-package-id",
     "status": "PAID"
   }
   ```

---

## 🔐 Security Details

### API Key Validation Flow:
1. **ApiKeyFilter** intercepts requests to `/api/package/payment/*`
2. Validates `x-api-key` header against `API_KEY_BILL_SERVICE` environment variable
3. If valid → proceeds to controller
4. If invalid/missing → returns 401 Unauthorized

### Filter Chain Order:
```
ApiKeyFilter → JwtTokenFilter → UsernamePasswordAuthenticationFilter
```

### Endpoint Permissions:
- `/api/package/payment/**` - Permits all (API Key validated by filter)
- Other endpoints - Requires JWT authentication

---

## 🚀 Integration with Bill Service

Bill Service should call this endpoint after confirming payment:

```java
// Example Bill Service code
RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
headers.set("x-api-key", System.getenv("API_KEY_BILL_SERVICE"));

Map<String, String> request = new HashMap<>();
request.put("packageId", "550e8400-e29b-41d4-a716-446655440000");
request.put("status", "PAID");

HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

ResponseEntity<String> response = restTemplate.exchange(
    "http://tour-package-service:8080/api/package/payment/update",
    HttpMethod.POST,
    entity,
    String.class
);
```

---

## 📋 Business Logic

### Status Validation:
- **Current Status:** Must be "Waiting for Payment"
- **Target Status:** "Payment Confirmed"
- **Supported Payment Status:** "PAID" only

### Error Cases:
- Package not found → 400 Bad Request
- Current status is not "Waiting for Payment" → 400 Bad Request
- Invalid payment status (not "PAID") → 400 Bad Request
- Missing/invalid API Key → 401 Unauthorized

---

## 🎯 Important Notes

1. **Frontend Never Calls This Endpoint** - This is strictly for microservice communication
2. **No JWT Required** - API Key authentication only
3. **Package Status Must Be "Waiting for Payment"** - Cannot update if package is already processed or cancelled
4. **Idempotency** - Calling multiple times with same packageId returns same result if status hasn't changed
5. **API Key Security** - Keep `API_KEY_BILL_SERVICE` secret and rotate periodically

---

## 📁 Related Files

- **DTO:** `UpdatePaymentStatusRequestDTO.java`
- **Filter:** `ApiKeyFilter.java`
- **Security Config:** `WebSecurityConfig.java`
- **Service:** `TourPackageRestServiceImpl.updatePaymentStatus()`
- **Controller:** `PackageRestController.updatePaymentStatus()`

---

## 🐛 Troubleshooting

### 401 Unauthorized Error:
- Check if `x-api-key` header is included
- Verify API key matches `API_KEY_BILL_SERVICE` environment variable
- Check server logs for validation details

### 400 Bad Request - Status Error:
- Verify package exists in database
- Check current package status (must be "Waiting for Payment")
- Ensure `status` field in request is "PAID"

### Environment Variable Not Loading:
- Restart Spring Boot application after changing `.env`
- Check if `.env` file is in correct location
- Verify Spring Boot is configured to load `.env` file (check `application-dev.yaml`)

---

## 📞 Support

For issues or questions about this feature, contact the Tour Package Service development team.
