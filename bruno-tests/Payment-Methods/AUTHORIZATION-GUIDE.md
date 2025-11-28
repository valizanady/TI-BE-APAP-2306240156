# Payment Methods API - Authorization Guide

## Overview

Payment Methods API memiliki different authorization levels untuk different operations.

## Authorization Matrix

| Endpoint | Method | Customer | Superadmin | Purpose |
|----------|--------|----------|------------|---------|
| `/api/payment-methods` | GET | ✅ | ✅ | Customer needs to see payment methods for top-up dropdown |
| `/api/payment-methods/{id}` | GET | ✅ | ✅ | Customer might need payment method details |
| `/api/payment-methods` | POST | ❌ | ✅ | Only Superadmin can create payment methods |
| `/api/payment-methods/{id}/status` | PUT | ❌ | ✅ | Only Superadmin can update status |
| `/api/payment-methods/{id}` | DELETE | ❌ | ✅ | Only Superadmin can delete payment methods |

## Why Customer Needs Access to GET Endpoints?

### Use Case: Top-Up Transaction Form

Ketika Customer ingin melakukan top-up saldo, mereka perlu:

1. **Select Payment Method** dari dropdown
   - Frontend call: `GET /api/payment-methods?status=Active`
   - Response: List of active payment methods
   - Display format: "methodName - provider" (e.g., "QRIS - BCA")

2. **View Payment Method Details** (optional)
   - Frontend call: `GET /api/payment-methods/{id}`
   - Response: Full payment method details

3. **Create Top-Up Transaction**
   - Frontend call: `POST /api/top-up-transactions`
   - Body includes: `paymentMethodId`

### Example Flow

```
1. Customer opens Top-Up page
   ↓
2. Frontend fetches active payment methods:
   GET /api/payment-methods?status=Active
   Authorization: Bearer <CUSTOMER_JWT>
   ↓
3. Frontend populates dropdown:
   - QRIS - BCA
   - Bank Transfer - BCA
   - E-Wallet - GoPay
   ↓
4. Customer selects payment method and enters amount
   ↓
5. Customer clicks Submit
   ↓
6. Frontend creates transaction:
   POST /api/top-up-transactions
   {
     "customerId": "867bd2c3-588e-43d1-adc4-fa1b575403b1",
     "amount": 100000,
     "paymentMethodId": "384f4b6b-69db-411c-9c45-5744006e2a86"
   }
```

## Testing with Different Roles

### As Customer

```bash
# Login as Customer
# Get OTT from: https://acc-fe.beel.my.id/auth/login?redirect=http://localhost:5173/login-success
# (Use credentials: customer1 / password)

# Exchange OTT for JWT
curl -X POST http://localhost:8080/api/auth/exchange \
  -H "Content-Type: application/json" \
  -d '{"ott":"YOUR_CUSTOMER_OTT"}'

# Get active payment methods (for dropdown)
curl -X GET "http://localhost:8080/api/payment-methods?status=Active" \
  -H "Authorization: Bearer YOUR_CUSTOMER_JWT"

# ✅ Should return 200 with list of active payment methods

# Try to create payment method (should fail)
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer YOUR_CUSTOMER_JWT" \
  -H "Content-Type: application/json" \
  -d '{"methodName":"Test","provider":"Test"}'

# ❌ Should return 403 Forbidden
```

### As Superadmin

```bash
# Login as Superadmin
# Get OTT from: https://acc-fe.beel.my.id/auth/login?redirect=http://localhost:5173/login-success
# (Use Superadmin credentials)

# Exchange OTT for JWT
curl -X POST http://localhost:8080/api/auth/exchange \
  -H "Content-Type: application/json" \
  -d '{"ott":"YOUR_SUPERADMIN_OTT"}'

# Get all payment methods
curl -X GET http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer YOUR_SUPERADMIN_JWT"

# ✅ Should return 200 with all payment methods

# Create payment method
curl -X POST http://localhost:8080/api/payment-methods \
  -H "Authorization: Bearer YOUR_SUPERADMIN_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "methodName": "QRIS",
    "provider": "Mandiri"
  }'

# ✅ Should return 201 Created
```

## Common Errors

### 403 Forbidden - Customer trying to create/update/delete

```json
{
  "status": 403,
  "message": "Access Denied",
  "timestamp": "2025-11-27T12:00:00.000+07:00"
}
```

**Solution:** Use Superadmin JWT token for management operations.

### 401 Unauthorized - No token or invalid token

```json
{
  "status": 401,
  "message": "Unauthorized",
  "timestamp": "2025-11-27T12:00:00.000+07:00"
}
```

**Solution:** 
1. Check if `Authorization` header is present
2. Verify JWT token is not expired
3. Re-login and get fresh JWT token

## Best Practices

### For Frontend Developers

1. **Cache Payment Methods**: Fetch once when user opens top-up page, don't fetch on every keystroke
2. **Filter by Status**: Always use `?status=Active` for customer-facing dropdowns
3. **Handle 403 Gracefully**: If customer somehow tries management operations, show proper error message
4. **Token Refresh**: Implement token refresh logic when JWT expires

### For Backend Developers

1. **Keep GET endpoints accessible**: Customer needs to see payment methods for dropdown
2. **Restrict mutations**: Only Superadmin can create/update/delete
3. **Log authorization failures**: Help debug 403 errors
4. **Return proper status codes**: 401 for auth issues, 403 for permission issues

---

**Last Updated:** 2025-11-27

**Related Documentation:**
- Payment Methods API Tests: `./README.md`
- Top-Up Transactions API: `../Top-Up-Transactions/README.md`
- Authentication Guide: `../Auth/README.md`
