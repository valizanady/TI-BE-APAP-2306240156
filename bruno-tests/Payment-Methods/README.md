# Payment Methods API - Testing Guide

## 📋 Overview

Complete guide untuk testing **Payment Methods API** menggunakan Bruno.

---

## 🎯 Endpoints Summary

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| **POST** | `/api/payment-methods` | Superadmin | Create payment method |
| **GET** | `/api/payment-methods` | All | List all payment methods |
| **GET** | `/api/payment-methods?status=Active` | All | Filter by status |
| **GET** | `/api/payment-methods/{id}` | All | Get payment method detail |
| **PUT** | `/api/payment-methods/{id}/status` | Superadmin | Update status |
| **DELETE** | `/api/payment-methods/{id}` | Superadmin | Delete (soft delete) |

---

## 🚀 Quick Start

### 1. Setup Environment Variables

Edit `bruno-tests/environments/Local.bru`:

```
vars {
  baseUrl: http://localhost:8080
  superadminToken: <your-superadmin-jwt-token>
  customerToken: <your-customer-jwt-token>
}
```

### 2. Get JWT Tokens

```bash
# Login as Superadmin
curl -X POST https://acc-be.beel.my.id/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "superadmin@example.com",
    "password": "your-password"
  }'

# Login as Customer
curl -X POST https://acc-be.beel.my.id/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "password": "your-password"
  }'
```

Copy `accessToken` dari response dan paste ke environment variables.

---

## 📝 POST Create Payment Method

### ✅ Valid Request

**Endpoint:** `POST /api/payment-methods`

**Headers:**
```
Authorization: Bearer <superadmin-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "methodName": "Credit Card",
  "provider": "Visa"
}
```

**Expected Response (201 Created):**
```json
{
  "status": 201,
  "message": "Payment method created successfully",
  "timestamp": "2025-11-24T10:30:00.000+07:00",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "methodName": "Credit Card",
    "provider": "Visa",
    "status": "Active",
    "createdAt": "2025-11-24T10:30:00",
    "updatedAt": "2025-11-24T10:30:00",
    "deletedAt": null
  }
}
```

### ❌ Invalid Requests

#### 1. Empty Method Name

```json
{
  "methodName": "",
  "provider": "Visa"
}
```

**Expected:** `400 Bad Request` - "Method name is required"

#### 2. Empty Provider

```json
{
  "methodName": "Credit Card",
  "provider": ""
}
```

**Expected:** `400 Bad Request` - "Provider is required"

#### 3. Missing Fields

```json
{
  "methodName": "Credit Card"
}
```

**Expected:** `400 Bad Request` - Validation error

---

## 🧪 Testing Scenarios

### Scenario 1: Create Multiple Payment Methods

```bash
# 1. Credit Card - Visa
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"methodName": "Credit Card", "provider": "Visa"}'

# 2. Credit Card - Mastercard
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"methodName": "Credit Card", "provider": "Mastercard"}'

# 3. E-Wallet - GoPay
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"methodName": "E-Wallet", "provider": "GoPay"}'

# 4. E-Wallet - OVO
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"methodName": "E-Wallet", "provider": "OVO"}'

# 5. Bank Transfer - BCA
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"methodName": "Bank Transfer", "provider": "BCA"}'
```

### Scenario 2: Get All Payment Methods

```bash
# List all payment methods
curl http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"

# Filter by status
curl "http://localhost:8080/api/payment-methods?status=Active" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

### Scenario 3: Update Status

```bash
# Deactivate payment method
curl -X PUT http://localhost:8080/api/payment-methods/{id}/status \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "Inactive"}'

# Reactivate payment method
curl -X PUT http://localhost:8080/api/payment-methods/{id}/status \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "Active"}'
```

### Scenario 4: Delete Payment Method

```bash
# Soft delete
curl -X DELETE http://localhost:8080/api/payment-methods/{id} \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN"

# Verify deletion - should not appear in list
curl http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

---

## 🔐 Authorization Testing

### Test 1: Superadmin Can Create ✅

```bash
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $SUPERADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"methodName": "Test Method", "provider": "Test Provider"}'

# Expected: 201 Created
```

### Test 2: Customer Cannot Create ❌

```bash
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"methodName": "Test Method", "provider": "Test Provider"}'

# Expected: 403 Forbidden (after RBAC implementation)
# Currently: 201 Created (permitAll for testing)
```

---

## ✅ Validation Checklist

### POST Create Payment Method

- [x] `methodName` is required (not blank)
- [x] `provider` is required (not blank)
- [x] Default status is "Active"
- [x] UUID auto-generated
- [x] Timestamps auto-generated
- [x] Returns 201 Created on success
- [x] Returns 400 Bad Request on validation error
- [ ] Only Superadmin can access (TODO: Uncomment @PreAuthorize)

### GET All Payment Methods

- [x] Returns list of payment methods
- [x] Only shows non-deleted payment methods
- [x] Ordered by createdAt DESC
- [x] Can filter by status
- [x] All authenticated users can access

### Soft Delete

- [x] Sets deletedAt timestamp
- [x] Does not physically remove from database
- [x] Deleted items not shown in GET requests

---

## 📊 Expected Console Logs

When you create a payment method, you should see:

```
🎯 POST /payment-methods | Method: Credit Card | Provider: Visa
✅ Payment method created successfully | ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

---

## 🐛 Troubleshooting

### Issue: "401 Unauthorized"

**Solution:** Check your JWT token:
```bash
# Verify token is valid
curl https://acc-be.beel.my.id/api/auth/me \
  -H "Authorization: Bearer $YOUR_TOKEN"
```

### Issue: "400 Bad Request - Method name is required"

**Solution:** Make sure `methodName` is not empty:
```json
{
  "methodName": "Credit Card",  // ✅ Must have value
  "provider": "Visa"
}
```

### Issue: "404 Not Found - Payment method not found"

**Solution:** Check if payment method exists and is not deleted:
```bash
# List all payment methods
curl http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📚 Additional Resources

- [Payment Method Model](../../src/main/java/apap/ti/_5/tour_package_2306240156_be/model/PaymentMethod.java)
- [Payment Method Controller](../../src/main/java/apap/ti/_5/tour_package_2306240156_be/restcontroller/PaymentMethodRestController.java)
- [Payment Method Service](../../src/main/java/apap/ti/_5/tour_package_2306240156_be/restservice/paymentmethod/PaymentMethodRestServiceImpl.java)
- [Bruno Documentation](https://www.usebruno.com/docs)

---

## 🎓 Learning Objectives

After completing these tests, you should understand:

1. ✅ How to create payment methods via REST API
2. ✅ Input validation for required fields
3. ✅ Soft delete pattern (deletedAt field)
4. ✅ Role-based access control (Superadmin vs Customer)
5. ✅ Status management (Active/Inactive)
6. ✅ RESTful API design patterns
7. ✅ JWT authentication in Spring Boot

---

**Happy Testing! 🚀**
