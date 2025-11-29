# 📦 Package Create - Auto UserID from JWT

## ✅ What Changed

Backend sekarang **otomatis mengisi `userId`** saat create package dari JWT token yang login.

Frontend **TIDAK PERLU KIRIM `userId` lagi** di request body!

---

## 🔧 Backend Changes

### 1️⃣ Updated Controller
**File:** `PackageRestController.java`

```java
@PostMapping("/create")
public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> create(
    @Valid @RequestBody CreatePackageRequestDTO req,
    @AuthenticationPrincipal AuthenticatedUser user) {  // ✅ Auto-inject user from JWT
  
  // ✅ Pass userId from JWT token to service
  var data = service.create(req, user.getId());
  ...
}
```

### 2️⃣ Updated Service Interface
**File:** `PackageRestService.java`

```java
// ❌ OLD
PackageResponseDTO create(CreatePackageRequestDTO req);

// ✅ NEW - Added userId parameter
PackageResponseDTO create(CreatePackageRequestDTO req, String userId);
```

### 3️⃣ Updated Service Implementation
**File:** `TourPackageRestServiceImpl.java`

```java
@Override
public PackageResponseDTO create(CreatePackageRequestDTO req, String userId) {
  ...
  var entity = Package.builder()
      .id(id)
      .userId(userId)  // ✅ Use userId from JWT token (not from request body!)
      .packageName(req.getPackageName())
      ...
      .build();
  ...
}
```

### 4️⃣ Updated DTO - Removed `userId` field
**File:** `CreatePackageRequestDTO.java`

```java
@Data
public class CreatePackageRequestDTO {
    @NotBlank(message = "Package name is required")
    private String packageName;

    // ❌ REMOVED: @NotBlank private String userId;  
    // ✅ userId now comes from JWT token automatically!

    @Positive(message = "Quota must be > 0")
    private int quota;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
}
```

---

## 🎯 Frontend Integration Guide

### ❌ OLD Request (Don't use anymore!)

```javascript
// ❌ OLD - Frontend had to manually send userId
const createPackage = async (packageData) => {
  const response = await fetch('/api/package/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({
      userId: "customer-123",  // ❌ Don't send this anymore!
      packageName: "Yogyakarta Adventure",
      quota: 20,
      startDate: "2025-12-01T09:00",
      endDate: "2025-12-05T18:00"
    })
  });
  return response.json();
};
```

### ✅ NEW Request (Use this!)

```javascript
// ✅ NEW - userId automatically extracted from JWT token
const createPackage = async (packageData) => {
  const response = await fetch('/api/package/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`  // ✅ Backend extracts userId from this!
    },
    body: JSON.stringify({
      // ✅ NO userId field needed!
      packageName: "Yogyakarta Adventure",
      quota: 20,
      startDate: "2025-12-01T09:00",
      endDate: "2025-12-05T18:00"
    })
  });
  return response.json();
};
```

---

## 🔐 Security Benefits

### Before (Manual userId):
- ❌ Frontend could fake userId and create packages as other users
- ❌ Security risk: Any user could pretend to be another user
- ❌ Backend trusted frontend to send correct userId

### After (Auto userId from JWT):
- ✅ **Impossible to fake userId** - comes from tamper-proof JWT token
- ✅ Backend extracts userId from authenticated token
- ✅ User can only create packages under their own ID
- ✅ More secure and less error-prone

---

## 📝 Request/Response Examples

### Example Request Body (NEW)
```json
{
  "packageName": "Bali Beach Tour",
  "quota": 30,
  "startDate": "2025-12-10T08:00",
  "endDate": "2025-12-15T20:00"
}
```

### Example Response
```json
{
  "status": 201,
  "message": "Created",
  "timestamp": "2025-11-29T10:30:00.000Z",
  "data": {
    "id": "PKG-20251129-001",
    "userId": "customer-abc-123",  // ✅ Auto-filled from JWT token
    "packageName": "Bali Beach Tour",
    "quota": 30,
    "price": 0,
    "status": "Pending",
    "startDate": "2025-12-10T08:00:00",
    "endDate": "2025-12-15T20:00:00"
  }
}
```

---

## 🚀 Testing Guide

### 1. Get JWT Token (Login)
```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "password": "password123"
  }'

# Response will contain JWT token
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6ImN1c3RvbWVyLTEyMyIsInJvbGUiOiJDdXN0b21lciIsImlhdCI6MTczMjg2NzIwMCwiZXhwIjoxNzMyOTUzNjAwfQ..."
# }
```

### 2. Create Package (Using JWT Token)
```bash
# ✅ Create package WITHOUT sending userId in body
curl -X POST http://localhost:8080/api/package/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "packageName": "Test Package",
    "quota": 10,
    "startDate": "2025-12-01T09:00",
    "endDate": "2025-12-05T18:00"
  }'
```

### 3. Verify userId is Auto-filled
```bash
# Response will show userId automatically filled from JWT:
{
  "status": 201,
  "message": "Created",
  "data": {
    "id": "PKG-20251129-001",
    "userId": "customer-123",  # ✅ Matches the user ID in JWT token!
    "packageName": "Test Package",
    ...
  }
}
```

---

## ⚠️ Migration Checklist for Frontend

- [ ] **Remove `userId` field** from create package form/request body
- [ ] **Keep JWT token** in Authorization header (no changes needed here)
- [ ] **Update validation** - no longer validate/require userId input from user
- [ ] **Remove userId input field** from create package UI form
- [ ] **Test create package** - verify it works without userId in body
- [ ] **Check response** - verify userId is auto-filled in response data

---

## 🎨 UI Form Changes

### ❌ OLD Form (Remove userId field!)
```jsx
<form onSubmit={handleCreatePackage}>
  <input name="userId" value={currentUserId} required />  {/* ❌ REMOVE THIS */}
  <input name="packageName" required />
  <input name="quota" type="number" required />
  <input name="startDate" type="datetime-local" required />
  <input name="endDate" type="datetime-local" required />
  <button type="submit">Create Package</button>
</form>
```

### ✅ NEW Form (Cleaner!)
```jsx
<form onSubmit={handleCreatePackage}>
  {/* ✅ No userId field needed! */}
  <input name="packageName" required />
  <input name="quota" type="number" required />
  <input name="startDate" type="datetime-local" required />
  <input name="endDate" type="datetime-local" required />
  <button type="submit">Create Package</button>
</form>
```

---

## 📊 Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Request Body** | Includes `userId` field | ❌ No `userId` field |
| **UserId Source** | From request body (frontend) | ✅ From JWT token (backend) |
| **Security** | ⚠️ Can be faked | ✅ Tamper-proof |
| **Frontend Code** | More complex (needs userId) | ✅ Simpler (no userId) |
| **Backend** | Trusts frontend | ✅ Validates from token |

---

## 🔗 Related Endpoints

Same pattern applied to these endpoints:
- ✅ `POST /api/activities` - Auto-fill vendorId from JWT
- ✅ `POST /api/package/create` - Auto-fill userId from JWT (THIS ONE!)
- 🔄 `POST /api/plans/create` - Will apply same pattern
- 🔄 Other create endpoints - Will migrate gradually

---

## 💡 Key Takeaway

**Frontend:** Stop sending `userId` in request body. Just send JWT token in header!

**Backend:** Automatically extracts `userId` from JWT token and assigns it to the package.

**Result:** More secure, simpler, and less error-prone! 🎉
