# ✅ JWT Integration untuk RBAC - SELESAI!

## 🎯 Yang Sudah Dikerjakan

Backend Spring Boot Anda sudah **fully integrated** dengan JWT token untuk mengambil ID user yang login secara otomatis!

**Tidak ada logic yang diubah** - hanya cara mengambil user ID yang berubah dari manual extraction menjadi otomatis dari JWT token.

---

## 📦 Files yang Dibuat/Diupdate

### ✅ File Baru
1. **`/security/AuthenticatedUser.java`** 
   - Class untuk user yang sudah login (dari JWT)
   - Bisa di-inject ke controller dengan `@AuthenticationPrincipal`
   - Punya helper methods: `isCustomer()`, `isVendor()`, `isSuperadmin()`, dll

### ✅ File yang Diupdate
1. **`/security/jwt/JwtTokenFilter.java`**
   - Sekarang membuat `AuthenticatedUser` object
   - Set ke SecurityContext sebagai principal
   
2. **`/security/jwt/JwtUtils.java`**
   - Tambah method `getNameFromToken()`

3. **`/restcontroller/ActivityRestController.java`**
   - ✅ POST: vendorId otomatis dari token
   - ✅ PUT: authorization check pakai `user.getId()`
   - ✅ DELETE: authorization check pakai `user.getId()`
   - ✅ Hapus manual extraction methods

4. **`/restdto/request/CreateActivityRequestDTO.java`**
   - ✅ Fix datetime format untuk terima `2025-12-11T17:00:00.000Z`

5. **`/restdto/request/UpdateActivityRequestDTO.java`**
   - ✅ Fix datetime format untuk terima `2025-12-11T17:00:00.000Z`

---

## 🔥 Cara Kerja Sekarang

### Before (Manual - ❌ Error prone)
```java
@PostMapping
public ResponseEntity<?> create(@RequestBody CreateDTO request) {
    // Manual extraction - ribet dan error prone
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Map details = (Map) auth.getDetails();
    String userId = (String) details.get("id");  // ❌ Unsafe
    
    service.create(request, userId);
}
```

### After (Otomatis - ✅ Type safe)
```java
@PostMapping
public ResponseEntity<?> create(
    @AuthenticationPrincipal AuthenticatedUser user,  // ✅ Auto-inject!
    @RequestBody CreateDTO request) {
    
    String userId = user.getId();  // ✅ Type safe, clean
    String role = user.getRole();
    
    service.create(request, userId);
}
```

---

## 🚀 Untuk Frontend Developer

### Yang TIDAK BERUBAH
- ✅ API endpoints tetap sama
- ✅ Response format tetap sama
- ✅ Logic RBAC tetap sama
- ✅ Authorization flow tetap sama

### Yang BERUBAH
- ✅ **Frontend TIDAK perlu kirim `userId` di request body lagi**
- ✅ **Backend otomatis ambil dari JWT token**

### Contoh: Create Activity

#### ❌ OLD (Kirim userId manual)
```javascript
const createActivity = async (data) => {
  await axios.post('/api/activities', {
    activityName: data.name,
    userId: currentUser.id,  // ❌ Jangan kirim ini lagi!
    price: data.price,
    // ...
  });
};
```

#### ✅ NEW (Hanya kirim JWT token)
```javascript
const createActivity = async (data) => {
  await axios.post('/api/activities', {
    activityName: data.name,
    // ✅ TIDAK ada userId - backend ambil dari token!
    price: data.price,
    // ...
  }, {
    headers: {
      Authorization: `Bearer ${token}`  // ✅ Hanya token
    }
  });
};
```

### Setup Axios Interceptor (Recommended)
```javascript
// Di main.js atau axios config
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Sekarang semua request otomatis kirim token
await axios.post('/api/activities', { activityName: "Bali Trip" });
await axios.get('/api/package');
await axios.put('/api/activities/123', { price: 500000 });
```

---

## 🔒 RBAC untuk Frontend

Backend sekarang return data dengan `userId`/`vendorId` yang bisa dipake untuk:

### 1. Show/Hide Buttons
```javascript
// Example: Show Edit button hanya untuk owner atau admin
<button 
  v-if="user.role === 'Superadmin' || activity.vendorId === user.id"
  @click="editActivity(activity)">
  Edit
</button>

<button 
  v-if="user.role === 'Superadmin' || activity.vendorId === user.id"
  @click="deleteActivity(activity)">
  Delete
</button>
```

### 2. Filter Data di Frontend
```javascript
// Example: Filter packages
const myPackages = packages.filter(pkg => {
  if (user.role === 'Superadmin') return true;  // Admin lihat semua
  if (user.role === 'Customer') {
    // Customer lihat yang dia buat + yang dari admin/vendor
    return pkg.userId === user.id || isVendorPackage(pkg);
  }
  return false;
});
```

### 3. Conditional Rendering
```vue
<template>
  <div v-if="canEdit(activity)">
    <!-- Edit form -->
  </div>
  <div v-else>
    <p>You cannot edit this activity</p>
  </div>
</template>

<script>
export default {
  methods: {
    canEdit(activity) {
      if (this.user.role === 'Superadmin') return true;
      return activity.vendorId === this.user.id;
    }
  }
}
</script>
```

---

## 📋 API Response Format (TIDAK BERUBAH)

### GET /api/activities
```json
{
  "status": 200,
  "message": "Activities retrieved successfully",
  "data": [
    {
      "id": "ACT-20251129-001",
      "activityName": "Bali Beach Tour",
      "vendorId": "48df8e8a-bcb1-4476-82ac-64b8bec7674a",  // ✅ Untuk RBAC
      "price": 500000,
      "isDeleted": false,
      // ...
    }
  ]
}
```

### POST /api/activities (Create)
```json
// REQUEST (NO userId!)
{
  "activityName": "Bali Beach Tour",
  "activityItem": "Tour Package",
  "activityType": "Tour",
  "capacity": 20,
  "price": 500000,
  "startDate": "2025-12-15T08:00:00.000Z",
  "endDate": "2025-12-15T17:00:00.000Z",
  "startLocation": "Kuta",
  "endLocation": "Uluwatu"
}

// RESPONSE
{
  "status": 201,
  "message": "Activity created successfully",
  "data": {
    "id": "ACT-20251129-001",
    "vendorId": "48df8e8a-bcb1-4476-82ac-64b8bec7674a",  // ✅ Otomatis dari token!
    "activityName": "Bali Beach Tour",
    // ...
  }
}
```

---

## 🧪 Testing

### 1. Login untuk dapat token
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin1",
  "password": "password123"
}

### Response
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": "48df8e8a-bcb1-4476-82ac-64b8bec7674a",
  "role": "Superadmin",
  "email": "admin1@accommodation.com"
}
```

### 2. Create Activity (dengan token, NO userId)
```http
POST http://localhost:8080/api/activities
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "activityName": "Bali Beach Tour",
  "activityItem": "Tour Package",
  "activityType": "Tour",
  "capacity": 20,
  "price": 500000,
  "startDate": "2025-12-15T08:00:00.000Z",
  "endDate": "2025-12-15T17:00:00.000Z",
  "startLocation": "Kuta Beach",
  "endLocation": "Uluwatu"
}

### Backend otomatis set vendorId dari token! ✅
```

### 3. Update Activity (hanya owner/admin)
```http
PUT http://localhost:8080/api/activities/ACT-20251129-001
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "activityName": "Bali Beach Tour - Updated",
  "activityItem": "Tour Package",
  "capacity": 25,
  "price": 550000,
  "startDate": "2025-12-15T08:00:00.000Z",
  "endDate": "2025-12-15T17:00:00.000Z",
  "startLocation": "Kuta Beach",
  "endLocation": "Uluwatu"
}

### Backend check: user.getId() == activity.vendorId ✅
```

---

## ✅ Status Kompilasi

**Semua file berhasil compile!** ✅

Error yang tersisa hanya **warning type safety** dari static analysis:
- `Null type safety: The expression of type...` 
- `Type safety: The expression of type Map...`

**Ini BUKAN blocking error** - aplikasi akan jalan dengan baik! 🚀

---

## 🎯 Next Steps untuk Frontend

### 1. Setup Axios Interceptor
```javascript
// di main.js atau axios-config.js
import axios from 'axios';

axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default axios;
```

### 2. Hapus userId dari semua Request Payloads
```javascript
// ❌ Before
const payload = {
  activityName: data.name,
  userId: currentUser.id,  // Hapus ini!
  price: data.price
};

// ✅ After
const payload = {
  activityName: data.name,
  // No userId!
  price: data.price
};
```

### 3. Implement RBAC di Frontend
```javascript
// Store user info dari login response
const user = {
  id: loginResponse.data.id,
  role: loginResponse.data.role,
  email: loginResponse.data.email
};
localStorage.setItem('user', JSON.stringify(user));

// Use untuk RBAC
const canEdit = (activity) => {
  if (user.role === 'Superadmin') return true;
  return activity.vendorId === user.id;
};

const canDelete = (activity) => {
  if (user.role === 'Superadmin') return true;
  return activity.vendorId === user.id;
};
```

---

## 🎉 Summary

| Fitur | Status | Catatan |
|-------|--------|---------|
| JWT Token Integration | ✅ DONE | AuthenticatedUser otomatis di-inject |
| ActivityRestController | ✅ DONE | POST/PUT/DELETE pakai `user.getId()` |
| DateTime Format Fix | ✅ DONE | Terima `2025-12-15T08:00:00.000Z` |
| RBAC Helper Methods | ✅ DONE | `isCustomer()`, `isVendor()`, `isSuperadmin()` |
| Type Safety | ✅ DONE | Hanya warning, tidak blocking |
| Kompilasi | ✅ SUCCESS | Ready untuk `./gradlew bootRun` |

---

## 🚀 Run Backend

```bash
cd /Users/valizanadya/Documents/SMT\ 5/APAP/tugas\ individu/tour-package-2306240156-be
./gradlew bootRun
```

Backend siap dipakai! Frontend tinggal:
1. ✅ Kirim JWT token di header
2. ✅ Jangan kirim userId di body
3. ✅ Pakai response data untuk RBAC (show/hide button, filter data)

**SELESAI!** 🎉✨
