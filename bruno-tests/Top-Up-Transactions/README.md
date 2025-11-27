# Top Up Transaction API Testing Guide

## 📋 Table of Contents
1. [Setup](#setup)
2. [Environment Variables](#environment-variables)
3. [Test Scenarios](#test-scenarios)
4. [Expected Results](#expected-results)

---

## 🛠️ Setup

### Prerequisites
- Bruno API Client installed
- Backend server running on `http://localhost:8080`
- PostgreSQL database with seed data

### Import Bruno Collection
1. Open Bruno
2. Click **Open Collection**
3. Navigate to `bruno-tests/Top-Up-Transactions/`
4. Bruno will load all `.bru` files

---

## 🔧 Environment Variables

### Create Bruno Environment
1. Click on **Environment** in Bruno
2. Create new environment: `Local`
3. Add these variables:

```json
{
  "BASE_URL": "http://localhost:8080",
  "CUSTOMER_TOKEN": "your-customer-jwt-token",
  "SUPERADMIN_TOKEN": "your-superadmin-jwt-token",
  "CUSTOMER_ID": "customer-uuid-from-jwt",
  "PAYMENT_METHOD_ID": "payment-method-uuid"
}
```

### Get Tokens from Profile Service

#### Option 1: Using cURL
```bash
# Login as Customer
curl -X POST https://acc-be.beel.my.id/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer-username",
    "password": "customer-password"
  }'

# Response will contain JWT token
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": { ... }
  }
}
```

#### Option 2: Using Bruno (Profile Service Collection)
1. Create separate collection for Profile Service
2. POST `/api/auth/login` with credentials
3. Copy `accessToken` from response
4. Set as `CUSTOMER_TOKEN` in environment

### Get Customer ID from JWT Token

#### Option 1: Decode JWT manually
```bash
# Install jq if not exists: brew install jq
echo "your-jwt-token" | cut -d. -f2 | base64 -d | jq
```

#### Option 2: Call Profile Service `/auth/me`
```bash
curl -X GET https://acc-be.beel.my.id/api/auth/me \
  -H "Authorization: Bearer your-jwt-token"
```

### Get Payment Method ID
```bash
# Run GET All Payment Methods first
curl -X GET http://localhost:8080/payment-methods \
  -H "Authorization: Bearer your-customer-token"

# Copy an Active payment method ID
```

---

## 🧪 Test Scenarios

### Scenario 1: Create Valid Top Up Transaction ✅

**File**: `POST-Create-Top-Up-Transaction.bru`

**Request:**
```json
POST /transactions
Authorization: Bearer {{CUSTOMER_TOKEN}}

{
  "customerId": "{{CUSTOMER_ID}}",
  "amount": 100000,
  "paymentMethodId": "{{PAYMENT_METHOD_ID}}"
}
```

**Expected Response (201 Created):**
```json
{
  "status": 201,
  "message": "Top-up transaction created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "customerId": "{{CUSTOMER_ID}}",
    "amount": 100000,
    "paymentMethod": {
      "id": "{{PAYMENT_METHOD_ID}}",
      "methodName": "Bank Transfer",
      "provider": "BCA",
      "status": "Active"
    },
    "status": "Pending",
    "createdAt": "2025-11-24T10:30:00",
    "updatedAt": "2025-11-24T10:30:00"
  }
}
```

**Validations:**
- ✅ Status code is 201
- ✅ Transaction status is "Pending"
- ✅ Amount is correct (100000)
- ✅ Customer ID matches
- ✅ Payment method details are included
- ✅ Transaction has UUID id
- ✅ createdAt and updatedAt timestamps exist

---

### Scenario 2: Create Transaction with Negative Amount ❌

**File**: `POST-Create-Invalid-Amount.bru`

**Request:**
```json
POST /transactions

{
  "customerId": "{{CUSTOMER_ID}}",
  "amount": -100000,
  "paymentMethodId": "{{PAYMENT_METHOD_ID}}"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Amount must be positive",
  "data": null
}
```

**Validations:**
- ✅ Status code is 400
- ✅ Error message mentions amount validation

---

### Scenario 3: Create Transaction with Zero Amount ❌

**File**: `POST-Create-Zero-Amount.bru`

**Request:**
```json
POST /transactions

{
  "customerId": "{{CUSTOMER_ID}}",
  "amount": 0,
  "paymentMethodId": "{{PAYMENT_METHOD_ID}}"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Amount must be positive",
  "data": null
}
```

---

### Scenario 4: Create Transaction with Invalid Payment Method ❌

**Request:**
```json
POST /transactions

{
  "customerId": "{{CUSTOMER_ID}}",
  "amount": 100000,
  "paymentMethodId": "00000000-0000-0000-0000-000000000000"
}
```

**Expected Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Payment method not found",
  "data": null
}
```

---

### Scenario 5: Create Transaction with Inactive Payment Method ❌

**Prerequisite:**
- Payment method exists but status is "Inactive"

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Payment method is not active",
  "data": null
}
```

---

## 📊 Expected Results Summary

| Test Case | Expected Status | Expected Message |
|-----------|----------------|------------------|
| Valid transaction | 201 | "Top-up transaction created successfully" |
| Negative amount | 400 | "Amount must be positive" |
| Zero amount | 400 | "Amount must be positive" |
| Null amount | 400 | "Amount is required" |
| Invalid payment method | 404 | "Payment method not found" |
| Inactive payment method | 400 | "Payment method is not active" |
| Null customerId | 400 | "Customer ID is required" |
| Null paymentMethodId | 400 | "Payment method ID is required" |

---

## 🔍 Verification in Database

After creating a transaction, verify in PostgreSQL:

```sql
-- Check the created transaction
SELECT 
  id,
  customer_id,
  amount,
  payment_method_id,
  status,
  created_at,
  updated_at,
  deleted_at
FROM topup_transaction
WHERE customer_id = 'your-customer-uuid'
ORDER BY created_at DESC
LIMIT 5;

-- Verify payment method details
SELECT 
  t.id as transaction_id,
  t.amount,
  t.status as transaction_status,
  pm.method_name,
  pm.provider,
  pm.status as payment_method_status
FROM topup_transaction t
JOIN payment_method pm ON t.payment_method_id = pm.id
WHERE t.customer_id = 'your-customer-uuid'
ORDER BY t.created_at DESC;
```

---

## 🎯 Testing Workflow

### Complete Testing Flow
```
1. Setup Environment
   ├── Get Customer JWT token from Profile Service
   ├── Decode JWT to get Customer ID
   └── Get Active Payment Method ID

2. Run Positive Test
   ├── POST Create Valid Transaction
   ├── Verify status 201
   ├── Verify transaction status = "Pending"
   └── Save transaction ID for later tests

3. Run Negative Tests
   ├── POST with negative amount (expect 400)
   ├── POST with zero amount (expect 400)
   ├── POST with invalid payment method (expect 404)
   └── POST with null fields (expect 400)

4. Verify in Database
   ├── Check transaction exists
   ├── Verify status is "Pending"
   └── Verify timestamps are set

5. Integration Test
   ├── Create transaction (status: Pending)
   ├── Update status to "Success" (as Superadmin)
   └── Verify balance updated in Profile Service
```

---

## 🐛 Troubleshooting

### Issue: "Payment method not found"
**Solution:**
```sql
-- Check active payment methods
SELECT * FROM payment_method WHERE deleted_at IS NULL AND status = 'Active';

-- If no active payment methods, insert one:
INSERT INTO payment_method (id, method_name, provider, status, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'Bank Transfer',
  'BCA',
  'Active',
  NOW(),
  NOW()
);
```

### Issue: "Unauthorized" or 401 Error
**Solution:**
- Check if JWT token is valid (not expired)
- Verify token format: `Bearer your-token-here`
- Get new token from Profile Service if expired

### Issue: "Amount must be positive" for valid amount
**Solution:**
- Check DTO validation in `CreateTopUpTransactionRequestDTO.java`
- Ensure `@Min(value = 1)` is present
- Verify Jackson is parsing JSON correctly

---

## 📝 Notes

### Important Points
1. **Transaction Status Flow:**
   - Created → "Pending"
   - Approved by Superadmin → "Success" (balance added)
   - Rejected by Superadmin → "Failed"

2. **Customer ID Validation:**
   - Currently accepts any UUID
   - In production, should verify customer exists in Profile Service

3. **Payment Method Validation:**
   - Must exist in database
   - Must have `status = "Active"`
   - Must not be soft-deleted (`deleted_at IS NULL`)

4. **Amount Validation:**
   - Must be positive integer
   - No maximum limit (configure if needed)
   - Stored as `Long` (up to 9,223,372,036,854,775,807)

---

## 🚀 Next Steps

After testing POST Create Transaction:
1. Test **PUT Update Transaction Status** (Superadmin only)
2. Test **GET All Transactions** (with role-based filtering)
3. Test **GET Transaction by ID** (Superadmin only)
4. Test **DELETE Transaction** (Superadmin only - soft delete)

---

## 📞 Support

If you encounter issues:
1. Check server logs: `tail -f logs/application.log`
2. Check database connection
3. Verify Bruno environment variables
4. Test JWT token with Profile Service first

Happy Testing! 🎉
