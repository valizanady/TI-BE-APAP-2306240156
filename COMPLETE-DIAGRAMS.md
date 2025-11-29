# 📐 Complete System Diagrams

## 🗺️ Navigation Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│                        APPLICATION NAVBAR                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────────────────┐  ┌──────────────────────────────┐│
│  │   IF USER ROLE = Customer    │  │  IF USER ROLE = Superadmin   ││
│  ├──────────────────────────────┤  ├──────────────────────────────┤│
│  │                              │  │                              ││
│  │  🏠 Home                     │  │  📊 Dashboard                ││
│  │  📋 My Transactions          │  │  💳 Payment Methods          ││
│  │  💰 Top-Up Balance           │  │  📊 Transactions             ││
│  │  👤 My Profile               │  │  👥 Users                    ││
│  │  🚪 Logout                   │  │  🚪 Logout                   ││
│  │                              │  │                              ││
│  └──────────────────────────────┘  └──────────────────────────────┘│
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Payment Methods Management Flow (Superadmin)

```
┌───────────────────────────────────────────────────────────────┐
│  SUPERADMIN clicks "Payment Methods" in navbar                 │
└───────────────┬───────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────────────────────┐
│  PAGE: /admin/payment-methods                                  │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Payment Methods Management        [+ Create New]       │  │
│  ├─────────────────────────────────────────────────────────┤  │
│  │  Filter: [All ▼]                                        │  │
│  ├────────┬──────────────┬─────────┬────────┬─────────────┤  │
│  │ ID     │ Method Name ↕│ Provider│ Status │ Actions     │  │
│  ├────────┼──────────────┼─────────┼────────┼─────────────┤  │
│  │ abc... │ Bank Transfer│ BCA     │[Active]│[Deact] [🗑] │  │
│  │ def... │ E-Wallet     │ GoPay   │[Inact.]│[Act.] [🗑]  │  │
│  └────────┴──────────────┴─────────┴────────┴─────────────┘  │
└───────────────────────────────────────────────────────────────┘
                │
                ├─── Click [+ Create New] ───────────────┐
                │                                        │
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ PAGE: /admin/payment-     │
                │                        │       methods/create      │
                │                        ├───────────────────────────┤
                │                        │ Method Name: [_________]  │
                │                        │ Provider:    [_________]  │
                │                        │                           │
                │                        │    [Cancel]  [Create]     │
                │                        └───────────────────────────┘
                │                                        │
                │                                        │ Click [Create]
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ POST /api/payment-methods │
                │                        │ { methodName, provider }  │
                │                        └───────────────────────────┘
                │                                        │
                │                                        │ Success
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ Redirect to list page     │
                │                        │ Show success message      │
                │                        └───────────────────────────┘
                │
                ├─── Click [Deactivate] ─────────────────┐
                │                                        │
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ ⚠️  Confirm Status Change  │
                │                        │                           │
                │                        │ Change "Bank Transfer"    │
                │                        │ to Inactive?              │
                │                        │                           │
                │                        │  [Cancel]  [Confirm]      │
                │                        └───────────────────────────┘
                │                                        │
                │                                        │ Click [Confirm]
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ PUT /api/payment-methods/ │
                │                        │     {id}/status           │
                │                        │ { status: "Inactive" }    │
                │                        └───────────────────────────┘
                │                                        │
                │                                        │ Success
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ Refresh table             │
                │                        │ Show success message      │
                │                        │ Badge now shows [Inactive]│
                │                        └───────────────────────────┘
                │
                └─── Click [🗑] ──────────────────────────┐
                                                          │
                                                          ▼
                                          ┌───────────────────────────┐
                                          │ ⚠️  Confirm Deletion       │
                                          │                           │
                                          │ Delete "E-Wallet - GoPay"?│
                                          │ Cannot be undone.         │
                                          │                           │
                                          │  [Cancel]  [Delete]       │
                                          └───────────────────────────┘
                                                          │
                                                          │ Click [Delete]
                                                          ▼
                                          ┌───────────────────────────┐
                                          │ DELETE /api/payment-      │
                                          │        methods/{id}       │
                                          └───────────────────────────┘
                                                          │
                                                          │ Success
                                                          ▼
                                          ┌───────────────────────────┐
                                          │ Refresh table             │
                                          │ Show success message      │
                                          │ Item removed from table   │
                                          └───────────────────────────┘
```

---

## 💰 Top-Up Transaction Flow (Customer)

```
┌───────────────────────────────────────────────────────────────┐
│  CUSTOMER clicks "Top-Up Balance" in navbar                    │
└───────────────┬───────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────────────────────┐
│  PAGE: /top-up                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Top-Up Balance                                         │  │
│  ├─────────────────────────────────────────────────────────┤  │
│  │  Customer ID: [auto-filled, disabled]                  │  │
│  │                                                         │  │
│  │  Payment Method: [Bank Transfer - BCA ▼]               │  │
│  │                                                         │  │
│  │  Amount: [____________] (min: Rp 10,000)                │  │
│  │                                                         │  │
│  │  Proof URL: [________________________]                  │  │
│  │                                                         │  │
│  │                       [Submit Top-Up Request]           │  │
│  └─────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────┘
                │
                │ Click [Submit]
                ▼
┌───────────────────────────────────────────────────────────────┐
│  POST /api/transactions                                        │
│  {                                                             │
│    customerId: "uuid-from-jwt",                               │
│    paymentMethodId: "selected-method-id",                     │
│    amount: 100000,                                             │
│    proofUrl: "https://example.com/proof.jpg"                  │
│  }                                                             │
└───────────────┬───────────────────────────────────────────────┘
                │
                │ Success (201 Created)
                ▼
┌───────────────────────────────────────────────────────────────┐
│  Transaction Created!                                          │
│  Status: Pending                                               │
│  Message: "Please wait for admin approval"                     │
└───────────────┬───────────────────────────────────────────────┘
                │
                │ Redirect to
                ▼
┌───────────────────────────────────────────────────────────────┐
│  PAGE: /my-transactions                                        │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  My Transactions          Filter: [All ▼]               │  │
│  ├────────┬────────────┬──────────┬─────────┬────────────┤  │
│  │ ID     │ Method     │ Amount   │ Status  │ Proof      │  │
│  ├────────┼────────────┼──────────┼─────────┼────────────┤  │
│  │ xyz... │ BCA        │ 100,000  │[Pending]│ [View]     │  │
│  │ abc... │ GoPay      │ 50,000   │[Success]│ [View]     │  │
│  │ def... │ Visa       │ 200,000  │[Reject.]│ [View]     │  │
│  └────────┴────────────┴──────────┴─────────┴────────────┘  │
│                                                               │
│  Note: Customer can only VIEW, cannot edit or delete          │
└───────────────────────────────────────────────────────────────┘
```

---

## ✅ Transaction Approval Flow (Superadmin)

```
┌───────────────────────────────────────────────────────────────┐
│  SUPERADMIN clicks "Transactions" in navbar                    │
└───────────────┬───────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────────────────────┐
│  PAGE: /admin/transactions                                     │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  All Transactions          Filter: [Pending ▼]          │  │
│  ├────────┬─────────┬────────┬──────────┬─────────────────┤  │
│  │ ID     │Customer │ Method │ Amount   │ Status │ Actions│  │
│  ├────────┼─────────┼────────┼──────────┼────────┼────────┤  │
│  │ xyz... │customer1│ BCA    │ 100,000  │[Pend.] │[✓][✗]  │  │
│  │ abc... │customer2│ GoPay  │ 50,000   │[Succ.] │  [🗑]   │  │
│  └────────┴─────────┴────────┴──────────┴────────┴────────┘  │
└───────────────────────────────────────────────────────────────┘
                │
                ├─── Click [✓ Approve] ──────────────────┐
                │                                        │
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ ⚠️  Confirm Approval       │
                │                        │                           │
                │                        │ Approve transaction from  │
                │                        │ customer1 for Rp 100,000? │
                │                        │                           │
                │                        │ Customer balance will be  │
                │                        │ increased.                │
                │                        │                           │
                │                        │  [Cancel]  [Approve]      │
                │                        └───────────────────────────┘
                │                                        │
                │                                        │ Click [Approve]
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ PUT /api/transactions/    │
                │                        │     {id}/status           │
                │                        │ { status: "Success" }     │
                │                        └───────────────────────────┘
                │                                        │
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ BACKEND PROCESS:          │
                │                        │ 1. Check current status   │
                │                        │ 2. If not Success yet:    │
                │                        │    a. Get current balance │
                │                        │    b. Add transaction amt │
                │                        │    c. Update in Profile   │
                │                        │       Service             │
                │                        │ 3. Update status to       │
                │                        │    "Success"              │
                │                        └───────────────────────────┘
                │                                        │
                │                                        │ Success (200 OK)
                │                                        ▼
                │                        ┌───────────────────────────┐
                │                        │ ✅ Transaction approved!   │
                │                        │ Customer balance updated  │
                │                        │                           │
                │                        │ Refresh table             │
                │                        │ Status now: [Success]     │
                │                        │ Actions now: [🗑]          │
                │                        └───────────────────────────┘
                │
                └─── Click [✗ Reject] ───────────────────┐
                                                          │
                                                          ▼
                                          ┌───────────────────────────┐
                                          │ ⚠️  Confirm Rejection      │
                                          │                           │
                                          │ Reject transaction from   │
                                          │ customer1?                │
                                          │                           │
                                          │ No balance will be added. │
                                          │                           │
                                          │  [Cancel]  [Reject]       │
                                          └───────────────────────────┘
                                                          │
                                                          │ Click [Reject]
                                                          ▼
                                          ┌───────────────────────────┐
                                          │ PUT /api/transactions/    │
                                          │     {id}/status           │
                                          │ { status: "Rejected" }    │
                                          └───────────────────────────┘
                                                          │
                                                          │ Success (200 OK)
                                                          ▼
                                          ┌───────────────────────────┐
                                          │ ❌ Transaction rejected    │
                                          │ No balance change         │
                                          │                           │
                                          │ Refresh table             │
                                          │ Status now: [Rejected]    │
                                          │ Actions now: [🗑]          │
                                          └───────────────────────────┘
```

---

## 🔐 Authentication Flow

```
┌────────────────┐
│  USER LOGIN    │
│  (SSO Service) │
└───────┬────────┘
        │
        │ 1. Redirect with OTT
        │    ?ott=2U9ZTI
        ▼
┌────────────────┐
│  FRONTEND      │
│  /login-success│
└───────┬────────┘
        │
        │ 2. Extract OTT
        │    from URL params
        ▼
┌────────────────────────┐
│ POST /api/auth/exchange│
│ { ott: "2U9ZTI" }      │
└───────┬────────────────┘
        │
        │ 3. Backend calls
        │    Profile Service
        ▼
┌────────────────────────┐
│  BACKEND receives JWT  │
│  Returns to frontend   │
└───────┬────────────────┘
        │
        │ 4. Store JWT
        ▼
┌────────────────────────┐
│  localStorage          │
│  .setItem('jwt_token') │
└───────┬────────────────┘
        │
        │ 5. All API calls
        │    include JWT
        ▼
┌────────────────────────────────┐
│  Authorization:                 │
│  Bearer eyJhbGciOiJIUzI1NiJ9... │
└────────────────────────────────┘
```

---

## 📦 Data Flow - Complete Request/Response

```
FRONTEND                     BACKEND                      DATABASE
   │                            │                            │
   │ 1. User Action             │                            │
   │ (e.g., Click Create)       │                            │
   │                            │                            │
   │ 2. POST /api/payment-      │                            │
   │    methods                 │                            │
   │    + JWT Token             │                            │
   ├───────────────────────────►│                            │
   │                            │                            │
   │                            │ 3. JwtTokenFilter          │
   │                            │    validates token         │
   │                            │    with Profile Service    │
   │                            │                            │
   │                            │ 4. Extract user info       │
   │                            │    (id, role, username)    │
   │                            │                            │
   │                            │ 5. Check @PreAuthorize     │
   │                            │    (Superadmin required)   │
   │                            │                            │
   │                            │ 6. Controller receives     │
   │                            │    request                 │
   │                            │                            │
   │                            │ 7. Service layer           │
   │                            │    processes logic         │
   │                            │                            │
   │                            │ 8. Save to database        │
   │                            ├───────────────────────────►│
   │                            │                            │
   │                            │ 9. Return entity           │
   │                            │◄───────────────────────────┤
   │                            │                            │
   │                            │ 10. Build response DTO     │
   │                            │                            │
   │ 11. Receive response       │                            │
   │◄───────────────────────────┤                            │
   │ {                          │                            │
   │   status: 201,             │                            │
   │   message: "Success",      │                            │
   │   data: { ... }            │                            │
   │ }                          │                            │
   │                            │                            │
   │ 12. Update UI              │                            │
   │     Show success message   │                            │
   │     Refresh data           │                            │
   │                            │                            │
```

---

## 🎨 UI Component Hierarchy

```
App.vue
│
├── Navbar.vue
│   ├── CustomerNav (v-if="role === 'Customer'")
│   │   ├── Link: My Transactions
│   │   └── Link: Top-Up Balance
│   │
│   └── SuperadminNav (v-if="role === 'Superadmin'")
│       ├── Link: Payment Methods
│       └── Link: Transactions
│
├── Router View
│   │
│   ├── Customer Routes
│   │   ├── CreateTransaction.vue (/top-up)
│   │   │   ├── PaymentMethodDropdown
│   │   │   ├── AmountInput
│   │   │   ├── ProofUrlInput
│   │   │   └── SubmitButton
│   │   │
│   │   └── MyTransactions.vue (/my-transactions)
│   │       ├── StatusFilter
│   │       └── TransactionTable (read-only)
│   │
│   └── Superadmin Routes
│       ├── PaymentMethods.vue (/admin/payment-methods)
│       │   ├── StatusFilter
│       │   ├── CreateButton
│       │   └── PaymentMethodsTable
│       │       ├── SortableHeaders
│       │       ├── StatusBadge
│       │       └── ActionsColumn
│       │           ├── UpdateStatusButton
│       │           └── DeleteButton
│       │
│       ├── CreatePaymentMethod.vue (/admin/payment-methods/create)
│       │   ├── MethodNameInput
│       │   ├── ProviderInput
│       │   ├── CancelButton
│       │   └── SubmitButton
│       │
│       └── AllTransactions.vue (/admin/transactions)
│           ├── StatusFilter
│           └── TransactionsTable
│               ├── CustomerColumn
│               ├── AmountColumn
│               ├── StatusBadge
│               └── ActionsColumn
│                   ├── ApproveButton (if Pending)
│                   ├── RejectButton (if Pending)
│                   └── DeleteButton
│
└── Modals/Dialogs
    ├── ConfirmStatusChange.vue
    ├── ConfirmDelete.vue
    ├── ConfirmApproval.vue
    └── ConfirmRejection.vue
```

---

**Use these diagrams as reference while implementing! 🎨**
