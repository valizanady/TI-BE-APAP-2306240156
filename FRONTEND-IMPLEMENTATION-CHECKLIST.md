# ✅ Frontend Implementation Checklist

## 📋 Step-by-Step Implementation Guide

### Phase 1: Preparation (5-10 minutes)
- [ ] Read `NAVBAR-INTEGRATION.md` for overview
- [ ] Read `PAYMENT-METHODS-TRANSACTIONS-README.md` for complete understanding
- [ ] Review `SYSTEM-ARCHITECTURE.md` for system flow
- [ ] Test backend is running: `curl http://localhost:8080/api/auth/exchange`

---

### Phase 2: Navbar Implementation (15 minutes)

#### Customer Navbar:
- [ ] Add "My Transactions" link (`/my-transactions`)
- [ ] Add "Top-Up Balance" link (`/top-up`)
- [ ] Ensure links only show when `userRole === 'Customer'`

#### Superadmin Navbar:
- [ ] Add "Payment Methods" link (`/admin/payment-methods`)
- [ ] Add "Transactions" link (`/admin/transactions`)
- [ ] Ensure links only show when `userRole === 'Superadmin'`

**Code Reference:** `NAVBAR-INTEGRATION.md` section "Vue 3 Example"

---

### Phase 3: Payment Methods - Superadmin (1-2 hours)

#### Page 1: List All Payment Methods (`/admin/payment-methods`)
- [ ] Create Vue component: `pages/admin/PaymentMethods.vue`
- [ ] Implement data fetching: `GET /api/payment-methods`
- [ ] Add JWT token to request headers
- [ ] Display table with columns:
  - [ ] ID (shortened, first 8 chars)
  - [ ] Method Name
  - [ ] Provider
  - [ ] Status (with colored badge)
  - [ ] Actions (buttons)
- [ ] Implement sorting:
  - [ ] Click header to sort by Method Name
  - [ ] Click header to sort by Provider
  - [ ] Show arrow indicator (↑/↓)
- [ ] Implement status filter:
  - [ ] Dropdown: All / Active / Inactive
  - [ ] Filter data when changed
- [ ] Add "Create" button linking to create page
- [ ] Implement Update Status button:
  - [ ] Toggle between Active/Inactive
  - [ ] Show confirmation dialog
  - [ ] Call `PUT /api/payment-methods/{id}/status`
  - [ ] Refresh list after success
- [ ] Implement Delete button:
  - [ ] Show confirmation dialog
  - [ ] Call `DELETE /api/payment-methods/{id}`
  - [ ] Refresh list after success
- [ ] Add loading state (spinner)
- [ ] Add error handling (alerts/toasts)

**Code Reference:** `bruno-tests/Payment-Methods/FRONTEND-INTEGRATION-GUIDE.md` section "Payment Methods List Page"

#### Page 2: Create Payment Method (`/admin/payment-methods/create`)
- [ ] Create Vue component: `pages/admin/CreatePaymentMethod.vue`
- [ ] Create form with fields:
  - [ ] Method Name (text input, required, max 100)
  - [ ] Provider (text input, required, max 100)
- [ ] Add form validation
- [ ] Implement submit handler:
  - [ ] Call `POST /api/payment-methods`
  - [ ] Show success message
  - [ ] Redirect to list page
- [ ] Add Cancel button (go back to list)
- [ ] Add loading state during submission
- [ ] Add error handling

**Code Reference:** `bruno-tests/Payment-Methods/FRONTEND-INTEGRATION-GUIDE.md` section "Create Payment Method Page"

---

### Phase 4: Transactions - Customer (1-2 hours)

#### Page 1: Create Top-Up (`/top-up`)
- [ ] Create Vue component: `pages/customer/CreateTransaction.vue`
- [ ] Fetch active payment methods: `GET /api/payment-methods?status=Active`
- [ ] Create form with fields:
  - [ ] Customer ID (auto-filled, disabled)
  - [ ] Payment Method (dropdown, required)
  - [ ] Amount (number input, required, min 10000)
  - [ ] Proof URL (URL input, required)
- [ ] Add form validation (especially min amount)
- [ ] Implement submit handler:
  - [ ] Call `POST /api/transactions`
  - [ ] Show success message
  - [ ] Redirect to My Transactions page
- [ ] Add loading state during submission
- [ ] Add error handling with user-friendly messages

**Code Reference:** `bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md` section "Customer - Create Transaction Page"

#### Page 2: My Transactions (`/my-transactions`)
- [ ] Create Vue component: `pages/customer/MyTransactions.vue`
- [ ] Implement data fetching: `GET /api/transactions`
  - [ ] Automatically filtered to show only customer's own transactions
- [ ] Display table with columns:
  - [ ] ID (shortened)
  - [ ] Payment Method (name + provider)
  - [ ] Amount (formatted as currency)
  - [ ] Status (colored badge: Pending=Yellow, Success=Green, Rejected=Red)
  - [ ] Proof (link to view image)
  - [ ] Created At (formatted date)
- [ ] Implement status filter:
  - [ ] Dropdown: All / Pending / Success / Rejected
  - [ ] Filter data when changed
- [ ] Add loading state
- [ ] Add error handling
- [ ] No action buttons (read-only for customers)

**Code Reference:** `bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md` section "Customer - My Transactions Page"

---

### Phase 5: Transactions - Superadmin (1-2 hours)

#### Page: All Transactions (`/admin/transactions`)
- [ ] Create Vue component: `pages/admin/Transactions.vue`
- [ ] Implement data fetching: `GET /api/transactions`
  - [ ] Shows ALL transactions from all customers
- [ ] Display table with columns:
  - [ ] ID (shortened)
  - [ ] Customer Username
  - [ ] Payment Method (name + provider)
  - [ ] Amount (formatted as currency)
  - [ ] Status (colored badge)
  - [ ] Proof (link to view image)
  - [ ] Created At (formatted date)
  - [ ] Actions (buttons)
- [ ] Implement status filter:
  - [ ] Dropdown: All / Pending / Success / Rejected
  - [ ] Filter data when changed
- [ ] Implement Approve button:
  - [ ] Only show for Pending transactions
  - [ ] Show confirmation dialog
  - [ ] Call `PUT /api/transactions/{id}/status` with `{status: "Success"}`
  - [ ] Show success message: "Transaction approved. Customer balance updated."
  - [ ] Refresh list after success
- [ ] Implement Reject button:
  - [ ] Only show for Pending transactions
  - [ ] Show confirmation dialog
  - [ ] Call `PUT /api/transactions/{id}/status` with `{status: "Rejected"}`
  - [ ] Show success message
  - [ ] Refresh list after success
- [ ] Implement Delete button:
  - [ ] Show for all transactions
  - [ ] Show confirmation dialog
  - [ ] Call `DELETE /api/transactions/{id}`
  - [ ] Show success message
  - [ ] Refresh list after success
- [ ] Add loading state
- [ ] Add error handling

**Code Reference:** `bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md` section "Superadmin - All Transactions Page"

---

### Phase 6: Routes Configuration (15 minutes)

Add to your Vue Router (`router/index.js`):

```javascript
const routes = [
  // Customer routes
  {
    path: '/top-up',
    name: 'CreateTransaction',
    component: () => import('@/pages/customer/CreateTransaction.vue'),
    meta: { requiresAuth: true, role: 'Customer' }
  },
  {
    path: '/my-transactions',
    name: 'MyTransactions',
    component: () => import('@/pages/customer/MyTransactions.vue'),
    meta: { requiresAuth: true, role: 'Customer' }
  },
  
  // Superadmin routes
  {
    path: '/admin/payment-methods',
    name: 'PaymentMethods',
    component: () => import('@/pages/admin/PaymentMethods.vue'),
    meta: { requiresAuth: true, role: 'Superadmin' }
  },
  {
    path: '/admin/payment-methods/create',
    name: 'CreatePaymentMethod',
    component: () => import('@/pages/admin/CreatePaymentMethod.vue'),
    meta: { requiresAuth: true, role: 'Superadmin' }
  },
  {
    path: '/admin/transactions',
    name: 'AllTransactions',
    component: () => import('@/pages/admin/Transactions.vue'),
    meta: { requiresAuth: true, role: 'Superadmin' }
  }
]
```

**Checklist:**
- [ ] Add Customer routes
- [ ] Add Superadmin routes
- [ ] Add route guards (meta: requiresAuth, role)
- [ ] Implement navigation guard to check role before accessing

---

### Phase 7: Testing (30-60 minutes)

#### Authentication Testing:
- [ ] Test login flow
- [ ] Verify JWT token is stored in localStorage
- [ ] Verify JWT is sent in Authorization header
- [ ] Test logout (clears token)

#### Payment Methods Testing (Superadmin):
- [ ] Test viewing all payment methods
- [ ] Test sorting by Method Name (asc/desc)
- [ ] Test sorting by Provider (asc/desc)
- [ ] Test filter by status (All/Active/Inactive)
- [ ] Test creating new payment method
  - [ ] Valid data
  - [ ] Empty fields (should show error)
- [ ] Test updating status (Active ↔ Inactive)
  - [ ] Verify confirmation dialog appears
  - [ ] Verify status changes after confirm
- [ ] Test deleting payment method
  - [ ] Verify confirmation dialog appears
  - [ ] Verify item removed after confirm

#### Transactions Testing (Customer):
- [ ] Test creating top-up request
  - [ ] Valid amount (>= 10000)
  - [ ] Invalid amount (< 10000, should show error)
  - [ ] Valid proof URL
  - [ ] Empty fields (should show errors)
- [ ] Test viewing own transactions
  - [ ] Verify only own transactions shown
  - [ ] Test status filter (All/Pending/Success/Rejected)
  - [ ] Verify cannot see other customer's transactions

#### Transactions Testing (Superadmin):
- [ ] Test viewing all transactions
  - [ ] Verify can see transactions from ALL customers
  - [ ] Test status filter
- [ ] Test approving transaction
  - [ ] Verify confirmation dialog
  - [ ] Verify status changes to Success
  - [ ] **Important:** Verify customer balance increased (check profile)
  - [ ] Try approving same transaction again → balance should NOT increase again
- [ ] Test rejecting transaction
  - [ ] Verify confirmation dialog
  - [ ] Verify status changes to Rejected
  - [ ] Verify customer balance NOT changed
- [ ] Test deleting transaction
  - [ ] Verify confirmation dialog
  - [ ] Verify transaction removed

#### Role-Based Access Testing:
- [ ] Login as Customer
  - [ ] Verify can access: /top-up, /my-transactions
  - [ ] Verify CANNOT access: /admin/payment-methods, /admin/transactions
- [ ] Login as Superadmin
  - [ ] Verify can access: /admin/payment-methods, /admin/transactions
  - [ ] Verify CANNOT access: /top-up (Customer-only endpoint)

---

### Phase 8: Polish & UI/UX (30 minutes)

- [ ] Add loading spinners for all API calls
- [ ] Style confirmation dialogs (match design system)
- [ ] Add toast notifications for success/error messages
- [ ] Format currency properly (Rp 100,000)
- [ ] Format dates properly (locale-aware)
- [ ] Add empty states ("No transactions yet")
- [ ] Add error states ("Failed to load data. Retry?")
- [ ] Ensure responsive design (mobile-friendly)
- [ ] Add proper color coding for status badges
- [ ] Add hover effects on buttons
- [ ] Disable buttons while API calls are in progress

---

## 🧪 API Testing with Bruno (Optional but Recommended)

Before implementing frontend, test all APIs using Bruno:

1. [ ] Install Bruno: https://www.usebruno.com/
2. [ ] Open collection: `bruno-tests/`
3. [ ] Set up environment variables:
   ```
   BASE_URL = http://localhost:8080
   JWT_TOKEN = (get from /api/auth/exchange)
   ```
4. [ ] Run Auth tests to get JWT token
5. [ ] Run Payment Methods tests
6. [ ] Run Top-Up Transactions tests
7. [ ] Verify all responses match expected format

---

## 📚 Reference Documentation

Keep these open while implementing:

- **Quick Reference:** `NAVBAR-INTEGRATION.md`
- **Payment Methods Guide:** `bruno-tests/Payment-Methods/FRONTEND-INTEGRATION-GUIDE.md`
- **Transactions Guide:** `bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md`
- **System Architecture:** `SYSTEM-ARCHITECTURE.md`

---

## 🎯 Success Criteria

Your implementation is complete when:

✅ **Navigation:**
- Customer navbar shows: My Transactions, Top-Up Balance
- Superadmin navbar shows: Payment Methods, Transactions

✅ **Customer Features:**
- Can create top-up requests with proof
- Can view own transactions only
- Can filter transactions by status

✅ **Superadmin Features:**
- Can manage payment methods (CRUD + status toggle)
- Can view all transactions from all customers
- Can approve/reject transactions (with balance update)
- Can delete transactions

✅ **Security:**
- JWT authentication works on all endpoints
- Role-based access control enforced
- Customer cannot access admin pages
- Customer cannot see other customer's data

✅ **UX:**
- Loading states shown during API calls
- Success/error messages displayed
- Confirmation dialogs for destructive actions
- Forms validate before submission

---

## 🆘 Troubleshooting

### Issue: 403 Forbidden
**Check:**
- [ ] JWT token is valid (not expired)
- [ ] Authorization header is sent: `Bearer {token}`
- [ ] User has correct role for endpoint

### Issue: Cannot see data
**Check:**
- [ ] Backend is running (`http://localhost:8080`)
- [ ] Correct API endpoint URL
- [ ] Response format matches expected structure
- [ ] Console for error messages

### Issue: Balance not updating
**Check:**
- [ ] Transaction status changed to "Success"
- [ ] Check backend logs for balance update process
- [ ] Verify Profile Service is accessible
- [ ] Try approving a different transaction

---

## 🎉 Completion

When all checkboxes are complete, you have successfully implemented:
- ✅ Payment Methods Management (Superadmin)
- ✅ Top-Up Transactions (Customer & Superadmin)
- ✅ Role-Based Navigation
- ✅ Full CRUD Operations
- ✅ Secure JWT Authentication

**Congratulations! 🚀**

---

**Total Estimated Time:** 4-6 hours
**Difficulty:** Intermediate
**Prerequisites:** Vue 3, Axios, Vue Router, understanding of JWT authentication

