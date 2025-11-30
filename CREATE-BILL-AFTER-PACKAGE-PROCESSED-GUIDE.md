# Create Bill After Package Processed - Implementation Guide

## 📋 Overview

Implementasi fitur **"Create Bill setelah Package diproses"** untuk Tour Package Service. Setelah package berhasil diproses (status → "Processed"), sistem otomatis membuat Bill di Bill Service menggunakan API Key authentication.

---

## 🎯 Flow Bisnis

```
1. User klik "Process Package" di Frontend
         ↓
2. Tour Package BE: Validasi Package
   - Minimal 1 Plan
   - Semua Plan status = "Fulfilled"
         ↓
3. Tour Package BE: Update Package status → "Processed"
   - Reduce Activity capacity
   - Save ke database
         ↓
4. Tour Package BE: Call Bill Service API
   - POST /api/bill/create
   - Header: x-api-key: <API_KEY>
   - Body: packageId, userId, amount, description
         ↓
5. Bill Service: Validate API Key
   - Check x-api-key header
   - Accept if key matches API_KEY_TOUR_PACKAGE_TO_BILL
         ↓
6. Bill Service: Create Bill
   - Status: 0 (Unpaid)
   - Return Bill ID and details
         ↓
7. Tour Package BE: Return success to Frontend
   ✅ Package Processed + Bill Created
```

---

## 📁 Files Created/Modified

### **Tour Package Service:**

#### **New Files:**

1. **`CreateBillRequestDTO.java`**
   - Location: `src/main/java/.../restdto/request/`
   - Purpose: DTO untuk request ke Bill Service
   - Fields:
     - `customerId` (String) - User ID yang memproses package
     - `serviceName` (String) - Fixed: **"TOURPACKAGE"** (no underscore, matches Bill Service enum)
     - `serviceReferenceId` (String) - Package ID
     - `description` (String) - Deskripsi bill
     - `amount` (Long) - Total harga package

2. **`BillResponseDTO.java`**
   - Location: `src/main/java/.../restdto/response/`
   - Purpose: DTO untuk response dari Bill Service
   - Fields: id, customerId, serviceName, serviceReferenceId, description, amount, status, createdAt, updatedAt

3. **`BillIntegrationService.java`**
   - Location: `src/main/java/.../restservice/`
   - Purpose: Service untuk integrasi dengan Bill Service
   - Method: `createBillForPackage(Package pkg)`
   - Features:
     - HTTP POST dengan RestTemplate
     - API Key authentication via header
     - Error handling (4xx, 5xx, timeout)
     - Detailed logging

#### **Modified Files:**

1. **`TourPackageRestServiceImpl.java`**
   - Injected: `BillIntegrationService`
   - Modified: `processPackage()` method
   - Added: Call to `billIntegrationService.createBillForPackage()` after status update
   - Error handling: Throw exception if Bill creation fails

### **Bill Service:**

**TIDAK ADA PERUBAHAN!** ✅

Bill Service sudah memiliki API Key validation yang berfungsi. Tour Package Service akan menggunakan:
- Header: `API-KEY` (bukan `x-api-key`)
- Protected endpoint: `/api/bill/create` (sudah di-protect oleh `ApiKeyFilter`)
- API Key: Menggunakan `${flight.api.key}` yang sudah ada

---

## 🔧 Configuration Setup

### **Tour Package Service**

Create or update `.env` file in project root:

```bash
# Bill Service Integration
BILL_SERVICE_URL=http://localhost:8081/api/bill/create
BILL_SERVICE_API_KEY=<sama-dengan-flight.api.key-di-bill-service>
```

**Environment Variables:**

| Variable | Description | Default Value | Required |
|----------|-------------|---------------|----------|
| `BILL_SERVICE_URL` | Endpoint untuk create bill di Bill Service | `http://localhost:8081/api/bill/create` | No |
| `BILL_SERVICE_API_KEY` | API Key untuk autentikasi ke Bill Service (harus sama dengan `flight.api.key`) | `tour-package-secret-key-2024` | No |

### **Bill Service**

**TIDAK PERLU PERUBAHAN!** ✅

Bill Service sudah memiliki konfigurasi API Key di `.env`:

```bash
# API Key yang sudah ada (existing)
flight.api.key=your-api-key-here
```

**Important:** 
- Tour Package Service akan menggunakan API Key yang **SAMA** dengan `flight.api.key` di Bill Service
- Pastikan nilai `BILL_SERVICE_API_KEY` di Tour Package **SAMA** dengan `flight.api.key` di Bill Service
- Header yang digunakan: `API-KEY` (sesuai dengan Bill Service yang sudah ada)

---

## 🚀 How to Run

### 1. **Start Bill Service (Port 8081)**

```bash
cd /Users/valizanadya/Documents/SMT\ 5/APAP/tugas\ individu/ti-2306165931

# TIDAK PERLU perubahan .env, gunakan yang sudah ada
# Pastikan flight.api.key sudah di-set

# Run service
./gradlew bootRun
```

**Verify Bill Service is running:**
```bash
curl http://localhost:8081/api/bill
```

**Check API Key configuration:**
Bill Service sudah memiliki `ApiKeyFilter` yang protect `/api/bill/create` dengan header `API-KEY`.

### 2. **Start Tour Package Service (Port 8080)**

```bash
cd /Users/valizanadya/Documents/SMT\ 5/APAP/tugas\ individu/tour-package-2306240156-be

# Create .env file (if not exists)
cat > .env << EOF
BILL_SERVICE_URL=http://localhost:8081/api/bill/create
BILL_SERVICE_API_KEY=<COPY-VALUE-dari-flight.api.key-di-bill-service>
EOF

# Run service
./gradlew bootRun
```

### 3. **Test Integration**

**Prerequisites:**
- Bill Service running on port 8081 ✅
- Tour Package Service running on port 8080 ✅
- Database populated with test data ✅

**Test Steps:**

1. **Create a Package with Plans:**
   ```bash
   # Login first to get JWT token
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "customer@example.com",
       "password": "password123"
     }'
   ```

2. **Process the Package:**
   ```bash
   # Replace <JWT_TOKEN> and <PACKAGE_ID>
   curl -X PUT http://localhost:8080/api/package/<PACKAGE_ID>/process \
     -H "Authorization: Bearer <JWT_TOKEN>"
   ```

3. **Check Logs:**
   
   **Tour Package Service logs:**
   ```
   🔄 Processing package: <PACKAGE_ID>
   ✅ All X plans are fulfilled
   📉 Activity 'Activity Name': capacity X → Y (-Z)
   ✅ Package processed successfully!
   📄 Creating Bill for processed package...
   🔔 Creating Bill for processed package: <PACKAGE_ID>
   📡 Sending POST request to Bill Service...
   ✅ Bill created successfully!
      Bill ID: <BILL_ID>
      Status: 201
   ✅ Bill created successfully in Bill Service!
   ```

   **Bill Service logs:**
   ```
   ✅ API Key validated for path: /api/bill/create
   ```

4. **Verify Bill Created:**
   ```bash
   # Check all bills
   curl http://localhost:8081/api/bill
   ```

---

## 🧪 Testing Scenarios

### ✅ **Scenario 1: Successful Bill Creation**

**Setup:**
- Package dengan status "Pending"
- Minimal 1 Plan dengan status "Fulfilled"
- Bill Service running dan API Key valid

**Expected Result:**
- Package status → "Processed"
- Bill created di Bill Service
- Response 200 OK dengan package details

**Test Command:**
```bash
curl -X PUT http://localhost:8080/api/package/<PACKAGE_ID>/process \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

### ❌ **Scenario 2: Bill Service Down**

**Setup:**
- Package valid dan siap diproses
- Bill Service TIDAK running (port 8081 closed)

**Expected Result:**
- Package status → "Processed" (sudah di-save)
- Error message: "Package processed successfully, but failed to create Bill: ..."
- HTTP 500 Internal Server Error

**Test:**
1. Stop Bill Service
2. Process package
3. Check error response

---

### ❌ **Scenario 3: Invalid API Key**

**Setup:**
- Package valid
- `BILL_SERVICE_API_KEY` di Tour Package ≠ `flight.api.key` di Bill Service

**Expected Result:**
- Package status → "Processed"
- Error: "Client error when creating Bill: 401 - Unauthorized: Invalid API Key"
- HTTP 500 Internal Server Error

**Test:**
1. Ubah `BILL_SERVICE_API_KEY` di Tour Package Service (gunakan nilai yang salah)
2. Restart Tour Package Service
3. Process package
4. Check error response

---

### ❌ **Scenario 4: Package Already Processed**

**Setup:**
- Package dengan status "Processed" (bukan "Pending")

**Expected Result:**
- Error: "Cannot process package. Package status must be 'Pending', current status: Processed"
- HTTP 400 Bad Request
- No Bill created

---

## 📊 API Specification

### **Tour Package → Bill Service Call**

**Endpoint:** `POST ${BILL_SERVICE_URL}`  
**Default:** `http://localhost:8081/api/bill/create`

**Headers:**
```
Content-Type: application/json
API-KEY: <BILL_SERVICE_API_KEY>
```

**Note:** Header menggunakan `API-KEY` (bukan `x-api-key`) sesuai dengan Bill Service yang sudah ada.

**Request Body:**
```json
{
  "customerId": "user123",
  "serviceName": "TOURPACKAGE",
  "serviceReferenceId": "package-uuid-here",
  "description": "Bill for processed tour package: Bali Adventure",
  "amount": 5000000
}
```

**⚠️ IMPORTANT:** `serviceName` harus **"TOURPACKAGE"** (tanpa underscore) untuk match dengan enum di Bill Service. Jika menggunakan "TOUR_PACKAGE" akan dapat error 400 Bad Request.

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Bill created successfully",
  "timestamp": "2024-01-15T10:30:00",
  "data": {
    "id": "bill-uuid-here",
    "customerId": "user123",
    "serviceName": "TOUR_PACKAGE",
    "serviceReferenceId": "package-uuid-here",
    "description": "Bill for processed tour package: Bali Adventure",
    "amount": 5000000,
    "status": 0,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": 401,
  "message": "Unauthorized: Invalid or missing API Key"
}
```

---

## 🔒 Security Details

### **API Key Authentication Flow:**

```
Tour Package Service                    Bill Service
        |                                     |
        | POST /api/bill/create               |
        | Header: API-KEY: <KEY>              |
        |------------------------------------>|
        |                                     |
        |                         ApiKeyFilter intercepts (EXISTING)
        |                         Validates API-KEY header
        |                         Compares with flight.api.key
        |                                     |
        |                         If valid → proceed to controller
        |                         If invalid → return 401
        |                                     |
        |<------------------------------------|
        | Response (201 or 401)               |
```

### **Security Best Practices:**

1. **Never commit API Keys to Git**
   - Use `.env` files (add to `.gitignore`)
   - Use environment variables in production

2. **Use different API Keys per environment**
   - Development: `tour-package-dev-key-2024`
   - Production: `tour-package-prod-key-<random-string>`

3. **Rotate API Keys periodically**
   - Change keys every 3-6 months
   - Update both services simultaneously

4. **Use HTTPS in production**
   - API Keys transmitted in plain text over HTTP
   - Always use HTTPS to encrypt headers

---

## 🐛 Troubleshooting

### **Problem: "Connection refused" error**

**Symptoms:**
```
Failed to create Bill: Connection refused
```

**Solutions:**
1. Check Bill Service is running: `curl http://localhost:8081/api/bill`
2. Verify port 8081 is not blocked by firewall
3. Check `BILL_SERVICE_URL` in Tour Package `.env`

---

### **Problem: "401 Unauthorized" error**

**Symptoms:**
```
Client error when creating Bill: 401 - Unauthorized: Invalid or missing API Key
```

**Solutions:**
1. Verify `BILL_SERVICE_API_KEY` di Tour Package = `flight.api.key` di Bill Service
2. Check environment variables are loaded: `echo $BILL_SERVICE_API_KEY`
3. Restart both services after changing `.env`
4. Check Bill Service logs for API key validation
5. Test dengan direct cURL ke Bill Service untuk verify API key bekerja

---

### **Problem: Bill created but Package processing fails**

**Symptoms:**
- Bill exists in Bill Service database
- Package status stuck at "Pending" in Tour Package database

**Root Cause:**
- This should NOT happen - Package is updated BEFORE calling Bill Service
- If Bill call fails, Package is already "Processed"

**Solutions:**
1. Check database - Package should be "Processed"
2. If stuck at "Pending", check for transaction rollback issues
3. Review logs for exceptions during status update

---

### **Problem: Package "Processed" but Bill not created**

**Symptoms:**
- Package status = "Processed"
- No Bill in Bill Service
- Frontend shows success (no error)

**Root Cause:**
- Exception during Bill creation was caught and logged but not thrown

**Solutions:**
1. Check Tour Package logs for "Failed to create Bill" messages
2. Verify exception is thrown after logging (current implementation does this)
3. Frontend should show error if Bill creation fails

---

## 📝 Code Examples

### **Manual Test: Create Bill via cURL**

```bash
# Direct call to Bill Service (bypass Tour Package)
curl -X POST http://localhost:8081/api/bill/create \
  -H "Content-Type: application/json" \
  -H "API-KEY: your-flight-api-key-here" \
  -d '{
    "customerId": "test-user-123",
    "serviceName": "TOURPACKAGE",
    "serviceReferenceId": "test-package-uuid",
    "description": "Test bill for manual testing",
    "amount": 1000000
  }'
```

**⚠️ Note:** Gunakan `"TOURPACKAGE"` (bukan `"TOUR_PACKAGE"`) untuk match Bill Service enum.

**Note:** Gunakan nilai `flight.api.key` yang sama dari Bill Service `.env`

**Expected Response:**
```json
{
  "status": 201,
  "message": "Bill created successfully",
  "timestamp": "...",
  "data": {
    "id": "...",
    "customerId": "test-user-123",
    ...
  }
}
```

---

## 📚 Related Documentation

- **Tour Package Service:**
  - `PackageRestService.java` - Interface
  - `TourPackageRestServiceImpl.java` - Implementation with Bill integration
  - `BillIntegrationService.java` - Bill Service client

- **Bill Service:**
  - `BillController.java` - Create bill endpoint
  - `ApiKeyFilter.java` - API Key validation

---

## ✅ Checklist

**Before Testing:**
- [ ] Bill Service running on port 8081
- [ ] Tour Package Service running on port 8080
- [ ] `.env` files created with API Keys
- [ ] API Keys match in both services
- [ ] Database has test data (Package with Plans)

**During Testing:**
- [ ] Package status changes to "Processed"
- [ ] Bill created in Bill Service
- [ ] Logs show successful integration
- [ ] Frontend receives success response

**Edge Cases:**
- [ ] Test with Bill Service down
- [ ] Test with wrong API Key
- [ ] Test with Package already processed
- [ ] Test with Package without Plans

---

## 📞 Support

**Implementation Date:** November 2024  
**Feature:** Create Bill After Package Processed  
**Integration Type:** Microservice-to-Microservice (API Key Authentication)  
**Status:** ✅ Complete and Ready for Testing

For issues or questions, check logs in both services and review error messages carefully.
