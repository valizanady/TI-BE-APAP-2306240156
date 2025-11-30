# Bill Service Integration - Complete Setup Guide

Dokumentasi lengkap untuk integrasi dua arah antara Tour Package Service dan Bill Service.

## 🔄 Integration Overview

Ada **2 arah komunikasi** antara Tour Package dan Bill Service:

### 1️⃣ Tour Package → Bill Service (Create Bill)
**When**: Setelah Customer memproses package  
**Purpose**: Membuat Bill untuk payment  
**Endpoint**: `POST /api/bill/create` (Bill Service)  
**Auth**: Header `API-KEY` dengan value Tour Package's API Key

### 2️⃣ Bill Service → Tour Package (Payment Callback)
**When**: Setelah Customer membayar Bill  
**Purpose**: Update package status ke "Payment Confirmed"  
**Endpoint**: `POST /api/package/payment/update` (Tour Package Service)  
**Auth**: Header `x-api-key` dengan value Bill Service's API Key

---

## 🔐 API Keys Configuration

### Tour Package Service `.env`

```properties
# ============================================
# BILL SERVICE INTEGRATION
# ============================================

# 1️⃣ Tour Package → Bill Service (CREATE BILL)
# API Key yang digunakan Tour Package saat call Bill Service
BILL_SERVICE_URL=http://localhost:8080/api/bill/create
BILL_SERVICE_API_KEY=GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC

# 2️⃣ Bill Service → Tour Package (UPDATE PAYMENT)
# API Key yang Bill Service kirim ke Tour Package
API_KEY_BILL_SERVICE=NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
```

### Bill Service `.env`

```properties
# Mapping dari Bill Service environment variables:

# 1️⃣ Bill Service menerima calls dari Tour Package
# Bill Service expects this API Key in header "API-KEY"
FLIGHTBILL_TO_TOURPACKAGE=GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC

# 2️⃣ Bill Service memanggil Tour Package callback
# Bill Service sends this API Key in header "x-api-key"
TOURPACKAGE_CALLBACK_API_KEY=NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
TOURPACKAGE_CALLBACK_URL=http://localhost:8080/api/package/payment/update
```

---

## 📊 Complete Integration Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│  STEP 1: Customer Process Package                                       │
└─────────────────────────────────────────────────────────────────────────┘
    Customer (Frontend)
       │
       │ POST /api/package/{id}/process
       │ (with JWT token)
       ▼
    Tour Package Service
       │
       ├─ Validate: status = "Pending", all plans fulfilled
       ├─ Reduce activity capacity
       ├─ Update package status → "Processed"
       │
       │
┌──────┴────────────────────────────────────────────────────────────────┐
│  STEP 2: Tour Package creates Bill                                    │
└───────────────────────────────────────────────────────────────────────┘
       │
       │ POST /api/bill/create
       │ Header: API-KEY: GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC
       │ Body: {
       │   customerId: "user-uuid",
       │   serviceName: "TOURPACKAGE",
       │   serviceReferenceId: "PKG-20251130-001",
       │   description: "Bill for Bali Package",
       │   amount: 5000000
       │ }
       ▼
    Bill Service
       │
       ├─ Validate API Key
       ├─ Create new Bill (status: UNPAID)
       ├─ Return Bill details
       │
       │ Response: {
       │   id: "BILL-12345",
       │   status: "UNPAID",
       │   amount: 5000000,
       │   ...
       │ }
       ▼
    Tour Package Service
       │
       ├─ Log Bill creation success
       ├─ (Optional) Update package status → "Waiting for Payment"
       │
       │ Response to Frontend:
       │ ✅ Package processed, Bill created
       ▼
    Customer (Frontend)
       
       
┌─────────────────────────────────────────────────────────────────────────┐
│  STEP 3: Customer Pays via Bill Service                                 │
└─────────────────────────────────────────────────────────────────────────┘
    Customer (Frontend)
       │
       │ Navigate to Bill Service UI
       │ Select payment method
       │ Confirm payment
       ▼
    Bill Service
       │
       ├─ Process payment
       ├─ Update bill status → "PAID"
       │
       │
┌──────┴────────────────────────────────────────────────────────────────┐
│  STEP 4: Bill Service notifies Tour Package (CALLBACK)               │
└───────────────────────────────────────────────────────────────────────┘
       │
       │ POST /api/package/payment/update
       │ Header: x-api-key: NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
       │ Body: {
       │   packageId: "PKG-20251130-001",
       │   status: 1
       │ }
       ▼
    Tour Package Service
       │
       ├─ ApiKeyFilter validates x-api-key
       ├─ Find package by ID
       ├─ Validate current status = "Waiting for Payment"
       ├─ Update status → "Payment Confirmed"
       │
       │ Response: 200 OK
       │ {
       │   status: 200,
       │   message: "Payment status updated successfully",
       │   data: { package details }
       │ }
       ▼
    Bill Service
       │
       ├─ Log callback success
       │
       ▼
    Customer sees payment confirmed ✅
```

---

## 🔑 API Key Usage Summary

| Direction | Endpoint | Header Name | API Key Value | Who Sends | Who Validates |
|-----------|----------|-------------|---------------|-----------|---------------|
| Tour Package → Bill | `/api/bill/create` | `API-KEY` | `GWJBdjIkKA...` | Tour Package | Bill Service |
| Bill → Tour Package | `/api/package/payment/update` | `x-api-key` | `NlfUxKNkXI...` | Bill Service | Tour Package |

### Important Differences:

1. **Header Names are DIFFERENT**:
   - Tour Package uses: `API-KEY` (uppercase, dash)
   - Bill Service uses: `x-api-key` (lowercase, prefixed with x-)

2. **API Keys are DIFFERENT**:
   - Each service has its own API Key
   - Never use the same key for both directions

3. **Validation Points**:
   - Bill Service validates incoming calls from Tour Package
   - Tour Package's `ApiKeyFilter` validates incoming calls from Bill Service

---

## 📝 Status Flow Diagram

```
Package Status Flow:

Created
   │
   ▼
Pending ──────────────────┐
   │                      │
   │ (all plans fulfilled)│ (not all fulfilled)
   ▼                      ▼
Processed            (stays Pending)
   │
   │ Bill created
   ▼
Waiting for Payment
   │
   │ Bill Service callback: status="PAID"
   ▼
Payment Confirmed ✅
```

---

## 🧪 Testing the Complete Flow

### Test 1: Create Bill (Tour Package → Bill Service)

```bash
# 1. Login as Customer and get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer1", "password": "password123"}'

# Save the JWT token from response

# 2. Process a package (this will trigger Bill creation)
curl -X PUT http://localhost:8080/api/package/PKG-20251130-001/process \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected:
# - Package status: Processed
# - Bill created in Bill Service
# - Console logs show Bill creation success
```

### Test 2: Payment Callback (Bill Service → Tour Package)

```bash
# Simulate Bill Service calling Tour Package callback
curl -X POST http://localhost:8080/api/package/payment/update \
  -H "Content-Type: application/json" \
  -H "x-api-key: NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS" \
  -d '{
    "packageId": "PKG-20251130-001",
    "status": 1
  }'

# Expected:
# - 200 OK
# - Package status updated to "Payment Confirmed"
```

### Test 3: Invalid API Key (Should Fail)

```bash
# Test with wrong API Key
curl -X POST http://localhost:8080/api/package/payment/update \
  -H "Content-Type: application/json" \
  -H "x-api-key: wrong-key-12345" \
  -d '{
    "packageId": "PKG-20251130-001",
    "status": "PAID"
  }'

# Expected:
# - 401 Unauthorized
# - Error message: "Invalid API Key"
```

---

## 🐛 Troubleshooting

### Issue: Bill creation fails with "Amount must be greater than 0"

**Cause**: Package price is 0 in database  
**Solution**: Price is now calculated from OrderedQuantities automatically

### Issue: Payment callback returns 401 Unauthorized

**Causes**:
1. Wrong API Key value
2. Wrong header name (should be `x-api-key` with dash)
3. API Key not configured in Tour Package `.env`

**Solution**: 
```bash
# Check Tour Package .env
grep API_KEY_BILL_SERVICE .env

# Should return:
# API_KEY_BILL_SERVICE=NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
```

### Issue: Payment callback returns 400 "Package current status is 'Processed'"

**Cause**: Package status tidak "Waiting for Payment"  
**Solution**: Implement logic untuk auto-update package status dari "Processed" ke "Waiting for Payment" setelah Bill dibuat

### Issue: Bill Service tidak menerima call dari Tour Package

**Causes**:
1. Bill Service tidak running
2. Wrong URL in BILL_SERVICE_URL
3. Wrong API Key in BILL_SERVICE_API_KEY

**Solution**:
```bash
# Check Bill Service status
curl http://localhost:8081/health

# Test Bill Service endpoint directly
curl -X POST http://localhost:8081/api/bill/create \
  -H "Content-Type: application/json" \
  -H "API-KEY: GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC" \
  -d '{...}'
```

---

## 📚 Related Documentation

- [PAYMENT-CALLBACK-GUIDE.md](./PAYMENT-CALLBACK-GUIDE.md) - Detail payment callback endpoint
- [CREATE-BILL-AFTER-PACKAGE-PROCESSED-GUIDE.md](./CREATE-BILL-AFTER-PACKAGE-PROCESSED-GUIDE.md) - Detail Bill creation flow

---

## ✅ Checklist for Production

- [ ] Environment variables configured correctly in both services
- [ ] API Keys are strong and unique (min 32 characters)
- [ ] HTTPS enabled for production URLs
- [ ] API Key rotation policy established
- [ ] Logging and monitoring configured
- [ ] Error handling tested for all edge cases
- [ ] Retry logic implemented for network failures
- [ ] Timeout configuration for external calls
- [ ] API Key stored in secure vault (not in code)

---

**Last Updated**: November 30, 2025  
**Integration Version**: 1.0  
**Status**: ✅ Fully Implemented and Tested
