# 🗺️ Payment Methods & Transactions - System Architecture

## 📐 System Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (Vue 3)                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────┐    ┌──────────────────────┐          │
│  │   CUSTOMER PAGES     │    │   SUPERADMIN PAGES   │          │
│  ├──────────────────────┤    ├──────────────────────┤          │
│  │ • Top-Up Form        │    │ • Payment Methods    │          │
│  │ • My Transactions    │    │   - List All         │          │
│  │   (Read Only)        │    │   - Create New       │          │
│  │                      │    │   - Update Status    │          │
│  │                      │    │   - Delete           │          │
│  │                      │    │ • All Transactions   │          │
│  │                      │    │   - Approve/Reject   │          │
│  │                      │    │   - Delete           │          │
│  └──────────────────────┘    └──────────────────────┘          │
│                                                                   │
└───────────────────────┬───────────────────────────────────────┬─┘
                        │                                       │
                        │  JWT Authorization Header             │
                        │  Bearer eyJhbGciOiJIUzI1NiJ9...       │
                        │                                       │
┌───────────────────────▼───────────────────────────────────────▼─┐
│                    SPRING BOOT BACKEND                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │             JWT AUTHENTICATION FILTER                       │ │
│  │  • Validates JWT token with Profile Service                │ │
│  │  • Extracts user info (id, username, role)                 │ │
│  │  • Sets Spring Security Context                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              CONTROLLERS (@PreAuthorize)                    │ │
│  ├────────────────────────────────────────────────────────────┤ │
│  │                                                             │ │
│  │  PaymentMethodRestController     TopUpTransactionRestCont  │ │
│  │  (/api/payment-methods)          (/api/transactions)       │ │
│  │  ┌──────────────────────┐       ┌──────────────────────┐  │ │
│  │  │ GET    /             │       │ GET    /             │  │ │
│  │  │ GET    /{id}         │       │ POST   /             │  │ │
│  │  │ POST   /             │       │ PUT    /{id}/status  │  │ │
│  │  │ PUT    /{id}/status  │       │ DELETE /{id}         │  │ │
│  │  │ DELETE /{id}         │       │                      │  │ │
│  │  └──────────────────────┘       └──────────────────────┘  │ │
│  │                                                             │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                   SERVICES                                  │ │
│  │  • PaymentMethodRestService                                │ │
│  │  • TopUpTransactionRestService                             │ │
│  │  • ProfileServiceClient (calls external API)               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              DATABASE (PostgreSQL/MySQL)                    │ │
│  │  • payment_methods table                                   │ │
│  │  • top_up_transactions table                               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                   │
└───────────────────────────────────────────────────────────────┬─┘
                                                                │
                        External API Call                       │
                        (Update Balance)                        │
                                                                │
┌───────────────────────────────────────────────────────────────▼─┐
│               PROFILE SERVICE (External)                         │
│               https://acc-be.beel.my.id                          │
├─────────────────────────────────────────────────────────────────┤
│  • POST /api/auth/exchange       - Exchange OTT for JWT         │
│  • GET  /api/profile/validate    - Validate JWT                 │
│  • GET  /api/profile/{username}  - Get user by username         │
│  • PUT  /api/profile/update/saldo - Update user balance         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Transaction Approval Flow

```
┌─────────────┐                                              
│  CUSTOMER   │                                              
└──────┬──────┘                                              
       │                                                     
       │ 1. Create Top-Up Request                          
       │    POST /api/transactions                          
       │    {                                               
       │      paymentMethodId: "...",                       
       │      amount: 100000,                               
       │      proofUrl: "..."                               
       │    }                                               
       ▼                                                     
┌─────────────────────┐                                     
│   STATUS: Pending   │                                     
└─────────┬───────────┘                                     
          │                                                  
          │ 2. Superadmin Reviews                          
          │                                                  
          ▼                                                  
    ┌─────────┐                                             
    │ APPROVE │                                             
    └────┬────┘                                             
         │                                                   
         │ 3. PUT /api/transactions/{id}/status            
         │    { status: "Success" }                        
         │                                                   
         ▼                                                   
┌───────────────────────────────────────┐                  
│  Backend Process:                     │                  
│  ├─ Check current status              │                  
│  │  (if already Success, skip)        │                  
│  ├─ Update status to "Success"        │                  
│  ├─ Get customer profile               │                  
│  │  GET /api/profile/{username}       │                  
│  ├─ Calculate new balance              │                  
│  │  newBalance = current + amount     │                  
│  └─ Update balance in Profile Service │                  
│     PUT /api/profile/update/saldo     │                  
└───────────────────────────────────────┘                  
         │                                                   
         ▼                                                   
┌─────────────────────┐                                     
│  STATUS: Success    │                                     
│  Balance Updated!   │                                     
└─────────────────────┘                                     
```

---

## 🎭 Role-Based Access Matrix

| Endpoint | Customer | Superadmin | Description |
|----------|----------|------------|-------------|
| **Payment Methods** | | | |
| `GET /api/payment-methods` | ✅ (Active only) | ✅ (All) | View payment methods |
| `GET /api/payment-methods/{id}` | ✅ | ✅ | View single method |
| `POST /api/payment-methods` | ❌ | ✅ | Create new method |
| `PUT /api/payment-methods/{id}/status` | ❌ | ✅ | Update status |
| `DELETE /api/payment-methods/{id}` | ❌ | ✅ | Delete method |
| **Transactions** | | | |
| `GET /api/transactions` | ✅ (Own only) | ✅ (All) | View transactions |
| `POST /api/transactions` | ✅ | ❌ | Create transaction |
| `PUT /api/transactions/{id}/status` | ❌ | ✅ | Approve/Reject |
| `DELETE /api/transactions/{id}` | ❌ | ✅ | Delete transaction |

---

## 📦 Data Models

### PaymentMethod Model
```json
{
  "id": "uuid-string",
  "methodName": "Bank Transfer",
  "provider": "BCA",
  "status": "Active",          // or "Inactive"
  "createdAt": "2025-11-28T10:00:00.000+07:00",
  "updatedAt": null,
  "deletedAt": null            // Soft delete timestamp
}
```

### TopUpTransaction Model
```json
{
  "id": "uuid-string",
  "customerId": "customer-uuid",
  "customerUsername": "customer1",
  "paymentMethod": {
    "id": "payment-method-uuid",
    "methodName": "Bank Transfer",
    "provider": "BCA",
    "status": "Active"
  },
  "amount": 100000,
  "status": "Pending",         // or "Success", "Rejected"
  "proofUrl": "https://example.com/proof.jpg",
  "createdAt": "2025-11-28T10:00:00.000+07:00",
  "updatedAt": null,
  "deletedAt": null
}
```

---

## 🔐 Authentication Flow

```
┌──────────────┐
│   FRONTEND   │
└──────┬───────┘
       │
       │ 1. User logs in via SSO
       │    https://acc-fe.beel.my.id/auth/login?callback=...
       │
       ▼
┌──────────────────────┐
│  AUTH SERVICE (SSO)  │
└──────┬───────────────┘
       │
       │ 2. Redirect with OTT
       │    http://localhost:5173/login-success/auth?ott=2U9ZTI
       │
       ▼
┌──────────────┐
│   FRONTEND   │
└──────┬───────┘
       │
       │ 3. Exchange OTT for JWT
       │    POST /api/auth/exchange
       │    { ott: "2U9ZTI" }
       │
       ▼
┌──────────────────────┐
│  BACKEND (Your API)  │
└──────┬───────────────┘
       │
       │ 4. Call Profile Service
       │    POST https://acc-be.beel.my.id/api/auth/exchange
       │    { ott: "2U9ZTI" }
       │
       ▼
┌──────────────────────┐
│  PROFILE SERVICE     │
└──────┬───────────────┘
       │
       │ 5. Return JWT
       │    { jwt: "eyJhbGciOiJIUzI1NiJ9..." }
       │
       ▼
┌──────────────┐
│   FRONTEND   │
│ Store JWT in │
│ localStorage │
└──────┬───────┘
       │
       │ 6. All subsequent requests include JWT
       │    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
       │
       ▼
┌──────────────────────┐
│  BACKEND (Your API)  │
│  JwtTokenFilter      │
│  validates JWT       │
└──────────────────────┘
```

---

## 📁 File Structure

```
tour-package-2306240156-be/
│
├── src/main/java/.../
│   ├── restcontroller/
│   │   └── topup/
│   │       ├── PaymentMethodRestController.java
│   │       └── TopUpTransactionRestController.java
│   │
│   ├── restservice/
│   │   ├── paymentmethod/
│   │   │   └── PaymentMethodRestService.java
│   │   ├── topup/
│   │   │   └── TopUpTransactionRestService.java
│   │   └── ProfileServiceClient.java
│   │
│   ├── restdto/
│   │   ├── request/
│   │   │   ├── paymentmethod/
│   │   │   │   └── CreatePaymentMethodRequestDTO.java
│   │   │   └── topup/
│   │   │       ├── CreateTopUpTransactionRequestDTO.java
│   │   │       └── UpdateTopUpStatusRequestDTO.java
│   │   └── response/
│   │       ├── BaseResponseDTO.java
│   │       └── topup/
│   │           └── TopUpTransactionResponseDTO.java
│   │
│   ├── model/
│   │   ├── PaymentMethod.java
│   │   └── TopUpTransaction.java
│   │
│   ├── repository/
│   │   ├── PaymentMethodRepository.java
│   │   └── TopUpTransactionRepository.java
│   │
│   ├── security/
│   │   ├── WebSecurityConfig.java
│   │   └── jwt/
│   │       ├── JwtTokenFilter.java
│   │       └── JwtUtils.java
│   │
│   └── config/
│       └── RestTemplateConfig.java
│
├── bruno-tests/
│   ├── Payment-Methods/
│   │   ├── README.md
│   │   ├── FRONTEND-INTEGRATION-GUIDE.md
│   │   └── *.bru (API test files)
│   │
│   └── Top-Up-Transactions/
│       ├── README.md
│       ├── FRONTEND-INTEGRATION-GUIDE.md
│       └── *.bru (API test files)
│
├── NAVBAR-INTEGRATION.md (Quick navbar guide)
├── PAYMENT-METHODS-TRANSACTIONS-README.md (This file overview)
└── SYSTEM-ARCHITECTURE.md (This file)
```

---

## 🚀 Getting Started

### Backend Setup (Already Done)
```bash
# Run the Spring Boot application
./gradlew bootRun

# Application runs on http://localhost:8080
```

### Frontend Setup (Your Task)
1. Read `NAVBAR-INTEGRATION.md`
2. Implement navbar menu based on user role
3. Create pages using guides in:
   - `bruno-tests/Payment-Methods/FRONTEND-INTEGRATION-GUIDE.md`
   - `bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md`
4. Test with Bruno API tests

---

## 🧪 Testing Strategy

### 1. Unit Tests
- Service layer logic
- DTO validation
- Repository queries

### 2. Integration Tests
- Controller endpoints
- JWT authentication
- Role-based authorization

### 3. API Tests (Bruno)
- Full request/response cycle
- Authentication flow
- Edge cases

### 4. E2E Tests
- User flows
- Transaction approval workflow
- Balance update verification

---

## 🎯 Key Features

### ✨ Payment Methods
- ✅ CRUD operations
- ✅ Status management (Active/Inactive)
- ✅ Soft delete
- ✅ Superadmin-only access for management

### ✨ Transactions
- ✅ Customer can create top-up requests
- ✅ Superadmin can approve/reject
- ✅ Automatic balance update on approval
- ✅ **Prevents duplicate balance addition**
- ✅ Soft delete
- ✅ Role-based data filtering

### ✨ Security
- ✅ JWT authentication
- ✅ Role-based authorization (`@PreAuthorize`)
- ✅ Customer isolation (can only see own data)
- ✅ SSL/TLS support for external API calls

---

## 📊 Database Relationships

```
┌─────────────────┐
│  PaymentMethod  │
│  ─────────────  │
│  • id (PK)      │
│  • methodName   │
│  • provider     │
│  • status       │
└────────┬────────┘
         │
         │ 1:N
         │
         ▼
┌──────────────────────┐
│  TopUpTransaction    │
│  ──────────────────  │
│  • id (PK)           │
│  • customerId (FK)   │ ──────┐
│  • paymentMethodId   │       │
│  • amount            │       │
│  • status            │       │
│  • proofUrl          │       │ N:1
└──────────────────────┘       │
                               │
                               ▼
                     ┌─────────────────┐
                     │  User (Profile  │
                     │     Service)    │
                     │  ─────────────  │
                     │  • id (PK)      │
                     │  • username     │
                     │  • email        │
                     │  • role         │
                     │  • saldo        │
                     └─────────────────┘
```

---

## 🔄 Status Transitions

### Transaction Status Flow
```
   ┌─────────┐
   │ CREATED │
   └────┬────┘
        │
        ▼
   ┌─────────┐       ┌─────────┐
   │ PENDING │──────►│ SUCCESS │ (Balance added)
   └────┬────┘       └─────────┘
        │
        │
        ▼
   ┌──────────┐
   │ REJECTED │ (No balance change)
   └──────────┘

Note: Once Status = Success, balance cannot be added again
```

### Payment Method Status Flow
```
   ┌────────┐       ┌──────────┐
   │ ACTIVE │◄─────►│ INACTIVE │
   └────────┘       └──────────┘
     (Toggle via PUT /api/payment-methods/{id}/status)
```

---

## 💡 Best Practices

### Frontend
- Always check user role before showing menu items
- Validate forms before submission
- Show loading states during API calls
- Display confirmation dialogs for destructive actions
- Handle errors gracefully with user-friendly messages

### Backend
- Never trust client-side role checks (always validate on server)
- Use `@PreAuthorize` for all protected endpoints
- Log important actions (create, update, delete)
- Return consistent response format (`BaseResponseDTO`)
- Use soft delete for audit trail

### Security
- Always send JWT token in Authorization header
- Never store sensitive data in localStorage (only JWT)
- Logout should clear JWT from storage
- Validate JWT on every request (done by `JwtTokenFilter`)

---

**🎉 You're Ready to Implement!**

Start with `NAVBAR-INTEGRATION.md` for quick setup, then dive into the detailed FRONTEND-INTEGRATION-GUIDE files for each feature.
