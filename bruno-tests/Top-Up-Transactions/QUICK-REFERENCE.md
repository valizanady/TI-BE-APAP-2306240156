# 🚀 Quick Reference: POST Create Top Up Transaction

## Endpoint
```
POST http://localhost:8080/transactions
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json
```

## Request Body
```json
{
  "customerId": "uuid-customer",
  "amount": 100000,
  "paymentMethodId": "uuid-payment-method"
}
```

## ✅ Success Response (201)
```json
{
  "status": 201,
  "message": "Top-up transaction created successfully",
  "data": {
    "id": "uuid-transaction",
    "customerId": "uuid-customer",
    "amount": 100000,
    "paymentMethod": { ... },
    "status": "Pending",
    "createdAt": "2025-11-24T15:30:00",
    "updatedAt": "2025-11-24T15:30:00"
  }
}
```

## ❌ Error Responses

| Status | Condition | Message |
|--------|-----------|---------|
| 400 | amount < 1 | "Amount must be positive" |
| 400 | amount = 0 | "Amount must be positive" |
| 400 | null amount | "Amount is required" |
| 400 | null customerId | "Customer ID is required" |
| 400 | null paymentMethodId | "Payment method ID is required" |
| 400 | Payment method inactive | "Payment method is not active" |
| 404 | Payment method not found | "Payment method not found" |

## 🧪 Quick Test (cURL)

### 1. Get Prerequisites
```bash
# Get Payment Method ID
curl http://localhost:8080/payment-methods \
  -H "Authorization: Bearer YOUR_TOKEN" | jq '.data[0].id'

# Get Customer ID from JWT
echo "YOUR_JWT_TOKEN" | cut -d. -f2 | base64 -d | jq '.id'
```

### 2. Create Transaction
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Authorization: Bearer YOUR_CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "YOUR_CUSTOMER_ID",
    "amount": 100000,
    "paymentMethodId": "YOUR_PAYMENT_METHOD_ID"
  }' | jq
```

### 3. Verify in Database
```sql
SELECT * FROM topup_transaction 
WHERE customer_id = 'YOUR_CUSTOMER_ID' 
ORDER BY created_at DESC LIMIT 1;
```

## 📋 Validation Checklist

- [ ] Amount > 0
- [ ] customerId is valid UUID
- [ ] paymentMethodId is valid UUID
- [ ] Payment method exists in database
- [ ] Payment method status is "Active"
- [ ] Payment method is not deleted (deleted_at IS NULL)
- [ ] JWT token is valid

## 🎯 Transaction Status Flow

```
Created → "Pending" → [Superadmin Approval] → "Success" or "Failed"
```

## 📊 Test Scenarios

| Scenario | Amount | Expected Result |
|----------|--------|-----------------|
| Valid | 100000 | 201 Created, status "Pending" |
| Negative | -100000 | 400 Bad Request |
| Zero | 0 | 400 Bad Request |
| Null | null | 400 Bad Request |
| Very Large | 999999999 | 201 Created |

## 💡 Tips

1. **Get Active Payment Method**:
   ```bash
   curl http://localhost:8080/payment-methods | jq '.data[] | select(.status=="Active")'
   ```

2. **Create Multiple Transactions** (loop):
   ```bash
   for i in {1..5}; do
     curl -X POST http://localhost:8080/transactions \
       -H "Authorization: Bearer $TOKEN" \
       -H "Content-Type: application/json" \
       -d "{\"customerId\":\"$CUSTOMER_ID\",\"amount\":$((i*50000)),\"paymentMethodId\":\"$PAYMENT_METHOD_ID\"}"
   done
   ```

3. **Check Recent Transactions**:
   ```sql
   SELECT 
     id, amount, status, created_at
   FROM topup_transaction
   WHERE customer_id = 'YOUR_ID'
   ORDER BY created_at DESC
   LIMIT 10;
   ```

## 🔧 Troubleshooting

**Problem**: 404 Payment method not found
**Solution**: 
```sql
SELECT id, method_name, status FROM payment_method WHERE deleted_at IS NULL;
```

**Problem**: 400 Payment method is not active
**Solution**:
```sql
UPDATE payment_method SET status = 'Active' WHERE id = 'YOUR_ID';
```

**Problem**: 401 Unauthorized
**Solution**: Get new JWT token from Profile Service
```bash
curl -X POST https://acc-be.beel.my.id/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","password":"password"}'
```

---

**Happy Testing! 🎉**
