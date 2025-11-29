# 📦 Payment Methods & Top-Up Transactions - Complete Documentation

## 📖 Overview

This repository contains the backend API for managing **Payment Methods** and **Top-Up Transactions** with role-based access control (Customer & Superadmin).

---

## 🗂️ Documentation Structure

### 📁 Quick Start
- **`NAVBAR-INTEGRATION.md`** - Quick reference for adding menu items to navbar

### 📁 Payment Methods
- **`bruno-tests/Payment-Methods/FRONTEND-INTEGRATION-GUIDE.md`**
  - Complete guide for implementing Payment Methods UI
  - API documentation
  - Code examples (Vue 3)
  - Superadmin features only

### 📁 Top-Up Transactions
- **`bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md`**
  - Complete guide for implementing Transactions UI
  - API documentation
  - Code examples (Vue 3)
  - Both Customer and Superadmin features

### 📁 API Testing
- **`bruno-tests/Payment-Methods/`** - Bruno API tests for Payment Methods
- **`bruno-tests/Top-Up-Transactions/`** - Bruno API tests for Transactions

---

## 🎯 Features by Role

### 👤 Customer Features

#### Top-Up Transactions:
1. **Create Top-Up Request**
   - Choose payment method
   - Enter amount (min Rp 10,000)
   - Upload proof of payment
   - Status starts as "Pending"

2. **View My Transactions**
   - See only own transactions
   - Filter by status (Pending/Success/Rejected)
   - Cannot edit or delete

#### Payment Methods:
- View active payment methods (for dropdown in top-up form)

---

### 👨‍💼 Superadmin Features

#### Payment Methods Management:
1. **View All Payment Methods**
   - Sortable table (methodName, provider)
   - Filter by status (Active/Inactive)
   - See all payment methods

2. **Create Payment Method**
   - Add new payment method
   - Fields: Method Name, Provider
   - Default status: Active

3. **Update Payment Method Status**
   - Toggle between Active/Inactive
   - Confirmation before change

4. **Delete Payment Method**
   - Soft delete
   - Confirmation before deletion

#### Transactions Management:
1. **View All Transactions**
   - See transactions from all customers
   - Filter by status
   - View proof of payment

2. **Approve/Reject Transactions**
   - Approve: Changes status to "Success" + adds balance to customer
   - Reject: Changes status to "Rejected" (no balance change)
   - Confirmation before action
   - **Important:** Balance is added only once per transaction

3. **Delete Transaction**
   - Soft delete
   - Confirmation before deletion

---

## 🔌 API Endpoints Summary

### Payment Methods
```
GET    /api/payment-methods              - Get all (with optional ?status=Active filter)
GET    /api/payment-methods/{id}         - Get by ID
POST   /api/payment-methods              - Create new (Superadmin only)
PUT    /api/payment-methods/{id}/status  - Update status (Superadmin only)
DELETE /api/payment-methods/{id}         - Delete (Superadmin only)
```

### Transactions
```
GET    /api/transactions                 - Get all (filtered by role automatically)
POST   /api/transactions                 - Create new (Customer only)
PUT    /api/transactions/{id}/status     - Update status (Superadmin only)
DELETE /api/transactions/{id}            - Delete (Superadmin only)
```

### Authentication
```
POST   /api/auth/exchange                - Exchange OTT for JWT token
```

---

## 🚀 Quick Start for Frontend Developers

### Step 1: Read Documentation
1. Start with **`NAVBAR-INTEGRATION.md`** for quick overview
2. Read **Payment Methods FRONTEND-INTEGRATION-GUIDE.md** for Payment Methods features
3. Read **Top-Up Transactions FRONTEND-INTEGRATION-GUIDE.md** for Transaction features

### Step 2: Implement Navbar
Add menu items based on user role:
- **Customer:** "My Transactions", "Top-Up Balance"
- **Superadmin:** "Payment Methods", "Transactions"

### Step 3: Create Pages
Follow the code examples in the FRONTEND-INTEGRATION-GUIDE files.

### Step 4: Test API Endpoints
Use Bruno tests in `bruno-tests/` directories to understand API behavior.

---

## 🔐 Authentication & Authorization

### JWT Token Required
All endpoints (except `/api/auth/exchange`) require JWT token in Authorization header:
```javascript
headers: {
  'Authorization': `Bearer ${jwt_token}`
}
```

### Role-Based Access Control

#### Customer Can Access:
- `GET /api/payment-methods?status=Active` (for dropdown)
- `GET /api/transactions` (only sees own transactions)
- `POST /api/transactions` (create own transaction)

#### Superadmin Can Access:
- All Payment Methods endpoints (GET, POST, PUT, DELETE)
- All Transactions endpoints (GET, PUT, DELETE)
- `GET /api/transactions` (sees ALL transactions)

---

## 📊 Database Schema

### PaymentMethod
```
- id: UUID (Primary Key)
- methodName: String (100 chars, required)
- provider: String (100 chars, required)
- status: String (Active/Inactive)
- createdAt: Timestamp
- updatedAt: Timestamp
- deletedAt: Timestamp (soft delete)
```

### TopUpTransaction
```
- id: UUID (Primary Key)
- customerId: UUID (Foreign Key to User)
- customerUsername: String
- paymentMethodId: UUID (Foreign Key to PaymentMethod)
- amount: Integer (min 10000)
- status: String (Pending/Success/Rejected)
- proofUrl: String (URL to proof image)
- createdAt: Timestamp
- updatedAt: Timestamp
- deletedAt: Timestamp (soft delete)
```

---

## 🎨 UI/UX Recommendations

### Payment Methods (Superadmin)
- Use DataTables or similar library for sorting/filtering
- Color code status badges:
  - 🟢 Active = Green
  - 🔴 Inactive = Red
- Confirmation modals for destructive actions

### Transactions (Customer)
- Show clear status indicators:
  - ⏳ Pending = Yellow/Orange
  - ✅ Success = Green
  - ❌ Rejected = Red
- Disable form while submitting
- Show success message after submission

### Transactions (Superadmin)
- Highlight pending transactions
- Show customer username clearly
- Provide quick approve/reject buttons
- Show proof image preview or link

---

## ✅ Implementation Checklist

### Backend (Already Done ✅)
- [x] Payment Methods CRUD endpoints
- [x] Top-Up Transactions CRUD endpoints
- [x] Role-based authorization with `@PreAuthorize`
- [x] JWT authentication with JwtTokenFilter
- [x] Balance update logic (prevents double addition)
- [x] API documentation
- [x] Bruno API tests

### Frontend (Your Task 📝)
- [ ] Add navbar links based on user role
- [ ] Create Payment Methods pages (Superadmin)
  - [ ] List page with sorting & filtering
  - [ ] Create form page
- [ ] Create Transactions pages
  - [ ] Customer: My Transactions (read-only)
  - [ ] Customer: Top-Up form
  - [ ] Superadmin: All Transactions with actions
- [ ] Implement confirmation dialogs
- [ ] Add error handling & loading states
- [ ] Test with real JWT tokens

---

## 🧪 Testing

### Using Bruno (Recommended)
1. Install Bruno: https://www.usebruno.com/
2. Open collection: `bruno-tests/`
3. Set environment variables in `environments/Local.bru`:
   ```
   BASE_URL = http://localhost:8080
   JWT_TOKEN = your-jwt-token-here
   ```
4. Run tests in order:
   - Auth tests first (to get JWT)
   - Payment Methods tests
   - Top-Up Transactions tests

### Manual Testing with curl
See examples in the FRONTEND-INTEGRATION-GUIDE files.

---

## 🐛 Troubleshooting

### Issue: 403 Forbidden
**Cause:** Missing or invalid JWT token, or wrong role
**Solution:** 
1. Verify JWT token is valid (call `/api/auth/exchange` first)
2. Check user role matches endpoint requirements
3. Ensure `Authorization: Bearer {token}` header is sent

### Issue: 401 Unauthorized
**Cause:** JWT token expired or invalid
**Solution:** Get a new token by logging in again

### Issue: Balance added multiple times
**Cause:** This should NOT happen anymore (fixed in backend)
**Solution:** The backend now checks transaction status before adding balance. Balance is only added once when status changes to "Success" for the first time.

---

## 📞 Support & Contact

For questions or issues:
1. Check the detailed FRONTEND-INTEGRATION-GUIDE files
2. Review Bruno API tests for examples
3. Check backend controller code:
   - `PaymentMethodRestController.java`
   - `TopUpTransactionRestController.java`
4. Review authentication flow in `JwtTokenFilter.java`

---

## 📝 Additional Notes

### Important Backend Logic

1. **Soft Delete:** 
   - When you delete a payment method or transaction, it's not removed from database
   - Instead, `deletedAt` field is set with current timestamp
   - Soft-deleted records are excluded from queries

2. **Balance Update:**
   - Only happens when transaction status changes to "Success"
   - Backend prevents duplicate balance additions
   - Balance update flow:
     1. Get current customer balance from Profile Service
     2. Add transaction amount
     3. Update balance in Profile Service

3. **Payment Method Validation:**
   - Customer can only see Active payment methods in dropdown
   - Superadmin can create/update/delete any payment method
   - Cannot delete a payment method that has associated transactions

4. **Security:**
   - Customer can only create transactions for themselves (enforced by backend)
   - Customer can only view their own transactions
   - All endpoints are protected with `@PreAuthorize` annotations

---

## 🎓 Learning Resources

- Spring Security: https://spring.io/guides/gs/securing-web/
- JWT Authentication: https://jwt.io/
- Vue 3 Documentation: https://vuejs.org/
- Axios HTTP Client: https://axios-http.com/

---

**Happy Coding! 🚀**
