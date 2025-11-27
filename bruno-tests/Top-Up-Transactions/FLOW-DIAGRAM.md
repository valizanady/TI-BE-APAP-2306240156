# POST Create Top Up Transaction - Visual Flow

## 🔄 Request Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     POST /transactions Request                       │
│  Headers: Authorization: Bearer <JWT_TOKEN>                         │
│  Body: { customerId, amount, paymentMethodId }                      │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   JwtTokenFilter (Security)                          │
│  • Validate JWT token with Profile Service                          │
│  • Set SecurityContext with user details                            │
│  • Extract: id, role, username, email                               │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│              TopUpTransactionRestController                          │
│  @PostMapping                                                        │
│  • Receive CreateTopUpTransactionRequestDTO                          │
│  • Validate with @Valid annotation                                  │
│  • Check BindingResult for validation errors                        │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                │ Validation Failed?          │
                └──────────────┬──────────────┘
                      YES      │      NO
                      │        │
                      ▼        ▼
            ┌──────────────┐  │
            │ Return 400   │  │
            │ Bad Request  │  │
            └──────────────┘  │
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│            TopUpTransactionRestServiceImpl                           │
│  createTransaction(CreateTopUpTransactionRequestDTO)                 │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    VALIDATION STEP 1                                 │
│  Check: amount != null && amount > 0                                │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                │ Amount Invalid?             │
                └──────────────┬──────────────┘
                      YES      │      NO
                      │        │
                      ▼        ▼
        ┌────────────────────┐ │
        │ Throw              │ │
        │ BadRequestException│ │
        └────────────────────┘ │
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    VALIDATION STEP 2                                 │
│  paymentMethodRepository.findByIdAndDeletedAtIsNull(paymentMethodId)│
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                │ Payment Method Not Found?   │
                └──────────────┬──────────────┘
                      YES      │      NO
                      │        │
                      ▼        ▼
        ┌────────────────────┐ │
        │ Throw              │ │
        │ NotFoundException  │ │
        └────────────────────┘ │
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    VALIDATION STEP 3                                 │
│  Check: paymentMethod.status == "Active"                            │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                │ Status Not Active?          │
                └──────────────┬──────────────┘
                      YES      │      NO
                      │        │
                      ▼        ▼
        ┌────────────────────┐ │
        │ Throw              │ │
        │ BadRequestException│ │
        └────────────────────┘ │
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    CREATE TRANSACTION                                │
│  TopUpTransaction transaction = new TopUpTransaction()              │
│  • setCustomerId(requestDTO.getCustomerId())                        │
│  • setAmount(requestDTO.getAmount())                                │
│  • setPaymentMethod(paymentMethod)                                  │
│  • setStatus("Pending")                                             │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    @PrePersist Hook                                  │
│  • createdAt = LocalDateTime.now()                                  │
│  • updatedAt = LocalDateTime.now()                                  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    SAVE TO DATABASE                                  │
│  topUpTransactionRepository.save(transaction)                        │
│  • Generate UUID for id                                             │
│  • Insert into topup_transaction table                              │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    RETURN TO CONTROLLER                              │
│  Return: TopUpTransaction entity                                     │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    CONVERT TO DTO                                    │
│  TopUpTransactionResponseDTO.fromEntity(created)                     │
│  • Map all fields                                                   │
│  • Include payment method details (PaymentMethodResponseDTO)        │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    BUILD RESPONSE                                    │
│  BaseResponseDTO.<TopUpTransactionResponseDTO>builder()             │
│    .status(201)                                                     │
│    .message("Top-up transaction created successfully")              │
│    .data(responseDTO)                                               │
│    .build()                                                         │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    RETURN RESPONSE                                   │
│  ResponseEntity.status(HttpStatus.CREATED).body(response)           │
│  HTTP 201 Created                                                   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Validation Chain

```
Request Body
    │
    ├─→ Jakarta Bean Validation (@Valid)
    │   ├─ @NotNull(customerId) ✓
    │   ├─ @NotNull(amount) ✓
    │   ├─ @Min(1, amount) ✓
    │   └─ @NotNull(paymentMethodId) ✓
    │
    └─→ Business Logic Validation
        ├─ amount > 0 ✓
        ├─ Payment method exists ✓
        ├─ Payment method not deleted ✓
        └─ Payment method status = "Active" ✓
```

---

## 💾 Database Transaction

```sql
BEGIN TRANSACTION;

-- Step 1: Check payment method exists and is active
SELECT * FROM payment_method 
WHERE id = ? 
  AND deleted_at IS NULL 
  AND status = 'Active';

-- Step 2: Insert new transaction
INSERT INTO topup_transaction (
  id,
  customer_id,
  amount,
  payment_method_id,
  status,
  created_at,
  updated_at
) VALUES (
  gen_random_uuid(),
  ?,  -- customerId
  ?,  -- amount
  ?,  -- paymentMethodId
  'Pending',
  NOW(),
  NOW()
);

COMMIT;
```

---

## 📊 Response Structure

```
BaseResponseDTO
  ├─ status: 201
  ├─ message: "Top-up transaction created successfully"
  └─ data: TopUpTransactionResponseDTO
         ├─ id: UUID
         ├─ customerId: UUID
         ├─ amount: Long
         ├─ paymentMethod: PaymentMethodResponseDTO
         │    ├─ id: UUID
         │    ├─ methodName: String
         │    ├─ provider: String
         │    ├─ status: String
         │    ├─ createdAt: LocalDateTime
         │    └─ updatedAt: LocalDateTime
         ├─ status: "Pending"
         ├─ createdAt: LocalDateTime
         └─ updatedAt: LocalDateTime
```

---

## 🚨 Error Handling Flow

```
Exception Thrown
    │
    ├─→ BadRequestException
    │   └─ HTTP 400 Bad Request
    │      ├─ "Amount must be positive"
    │      └─ "Payment method is not active"
    │
    ├─→ NotFoundException
    │   └─ HTTP 404 Not Found
    │      └─ "Payment method not found"
    │
    ├─→ MethodArgumentNotValidException (Bean Validation)
    │   └─ HTTP 400 Bad Request
    │      ├─ "Customer ID is required"
    │      ├─ "Amount is required"
    │      └─ "Payment method ID is required"
    │
    └─→ Generic Exception
        └─ HTTP 500 Internal Server Error
           └─ "Error: <exception message>"
```

---

## 🔐 Security Flow

```
JWT Token in Header
    │
    ▼
JwtTokenFilter
    │
    ├─→ Extract token from "Authorization: Bearer <token>"
    │
    ├─→ Validate with Profile Service
    │   GET https://acc-be.beel.my.id/api/auth/me
    │   Headers: Authorization: Bearer <token>
    │
    ├─→ Profile Service responds with user data
    │   {
    │     "id": "uuid",
    │     "username": "customer",
    │     "email": "customer@example.com",
    │     "role": "Customer"
    │   }
    │
    ├─→ Parse JWT payload (Base64 decode)
    │   Extract: id, role, username, email
    │
    ├─→ Create Authentication object
    │   UsernamePasswordAuthenticationToken(
    │     principal: username,
    │     credentials: null,
    │     authorities: [role]
    │   )
    │
    └─→ Set SecurityContext
        SecurityContextHolder.getContext().setAuthentication(auth)
        
    ▼
Controller can access authenticated user
```

---

## 🔄 Complete End-to-End Flow

```
┌──────────────┐
│   Customer   │
└──────┬───────┘
       │ 1. Login to Profile Service
       │    POST /api/auth/login
       ▼
┌─────────────────────┐
│  Profile Service    │ → Returns JWT token
└──────┬──────────────┘
       │ 2. Use JWT token
       ▼
┌─────────────────────────────────────────┐
│  POST /transactions                     │
│  Headers: Authorization: Bearer <token> │
│  Body: { customerId, amount, ... }      │
└──────┬──────────────────────────────────┘
       │ 3. Validate token
       ▼
┌─────────────────────┐
│  JwtTokenFilter     │ → Validates with Profile Service
└──────┬──────────────┘
       │ 4. Process request
       ▼
┌─────────────────────┐
│  Controller         │ → Validate DTO
└──────┬──────────────┘
       │ 5. Business logic
       ▼
┌─────────────────────┐
│  Service            │ → Validate payment method
└──────┬──────────────┘
       │ 6. Save to DB
       ▼
┌─────────────────────┐
│  Repository         │ → Insert transaction
└──────┬──────────────┘
       │ 7. Return entity
       ▼
┌─────────────────────┐
│  Service            │ → Return to controller
└──────┬──────────────┘
       │ 8. Convert to DTO
       ▼
┌─────────────────────┐
│  Controller         │ → Build response
└──────┬──────────────┘
       │ 9. HTTP 201 Created
       ▼
┌─────────────────────────────────────────┐
│  Response                               │
│  {                                      │
│    "status": 201,                       │
│    "message": "Transaction created",    │
│    "data": { ... }                      │
│  }                                      │
└─────────────────────────────────────────┘
```

---

## 📈 Performance Considerations

### Database Queries
```
1 query - Check payment method exists (indexed)
1 query - Insert transaction (primary key index)
---
Total: 2 queries per request
```

### Response Time Estimate
```
Token validation:    ~200ms (external API call)
Database queries:    ~50ms
Business logic:      ~10ms
DTO conversion:      ~5ms
----------------------------
Total:              ~265ms average
```

### Optimization Opportunities
- Cache payment method data (reduce DB query)
- Async token validation (if security allows)
- Batch transaction creation (for bulk operations)

---

**Visual Flow Documentation Complete** ✅
