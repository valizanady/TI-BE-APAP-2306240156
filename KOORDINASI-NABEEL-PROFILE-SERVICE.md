# 📞 Koordinasi dengan Nabeel - Profile Service Integration

## 🎯 Tujuan

Tour Package Service perlu validasi JWT token ke Profile Service untuk autentikasi user.

---

## 🔗 Integration Details

**Tour Package → Profile Service**

- **Endpoint**: `GET https://acc-be.beel.my.id/api/auth/me`
- **Method**: GET
- **Authentication**: Bearer Token (JWT di header `Authorization`)
- **Purpose**: Validate JWT token dan get user info

---

## 📋 Yang Perlu Dikonfirmasi dengan Nabeel

### 1. ✅ Profile Service Status

- [ ] Apakah endpoint `/api/auth/me` masih aktif?
- [ ] Apakah service running stable di production?
- [ ] Ada scheduled maintenance atau downtime?

---

### 2. 🔐 JWT Token Configuration

**Yang perlu ditanya:**

```
- Algoritma JWT: HS256 atau RS256?
- JWT Secret: (sharing secret jika pakai HS256)
- Token Expiration: Berapa lama token valid? (misal: 24 jam, 7 hari)
- Refresh Token: Ada mekanisme refresh token?
```

**Claims yang ada di JWT:**
```json
{
  "id": "user-uuid",
  "username": "string",
  "email": "string@example.com",
  "name": "User Full Name",
  "role": "Customer|Admin|SuperAdmin",
  "exp": 1234567890,
  "iat": 1234567890
}
```

Confirm: Apakah structure claims di atas sudah benar?

---

### 3. 🌐 CORS Configuration

**Request dari Tour Package BE:**

```
Origin: https://your-tour-package-be-url.com
Headers: Authorization, Content-Type
Method: GET
```

**Yang perlu di-allow di Profile Service:**

```java
// Profile Service CORS Config (minta Nabeel tambahkan Tour Package URL)
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "https://tour-package-fe-url.com",  // Frontend
        "https://tour-package-be-url.com"   // ✅ Backend (Tour Package BE)
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // ✅ Include Authorization
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Confirm dengan Nabeel:**
- [ ] CORS config sudah include Tour Package BE URL?
- [ ] Header `Authorization` di-allow?

---

### 4. 📤 API Response Format

**Expected Response dari `/api/auth/me`:**

```json
{
  "id": "user-uuid-string",
  "username": "username",
  "email": "user@example.com",
  "name": "User Full Name",
  "role": "Customer"
}
```

**Confirm:**
- [ ] Response format sudah sesuai?
- [ ] Field `id`, `username`, `email`, `name`, `role` ada semua?
- [ ] Role values: "Customer", "Admin", "SuperAdmin"?

---

### 5. ⚡ Rate Limiting & Performance

**Questions:**

```
- Ada rate limiting di endpoint /api/auth/me?
  → Jika ada: berapa request per minute?
  
- Response time average: berapa ms?

- Kapasitas concurrent requests: berapa?

- Monitoring/alerting: Nabeel dapat notif jika service down?
```

---

## 🧪 Test Cases

**Yang perlu di-test bareng:**

### Test 1: Valid Token
```bash
curl -X GET https://acc-be.beel.my.id/api/auth/me \
  -H "Authorization: Bearer <valid-token>" \
  -v
```
Expected: 200 OK + User Info

---

### Test 2: Invalid Token
```bash
curl -X GET https://acc-be.beel.my.id/api/auth/me \
  -H "Authorization: Bearer invalid-token-12345" \
  -v
```
Expected: 401 Unauthorized

---

### Test 3: Expired Token
```bash
curl -X GET https://acc-be.beel.my.id/api/auth/me \
  -H "Authorization: Bearer <expired-token>" \
  -v
```
Expected: 401 Unauthorized dengan message "Token expired"

---

### Test 4: No Authorization Header
```bash
curl -X GET https://acc-be.beel.my.id/api/auth/me -v
```
Expected: 401 Unauthorized

---

### Test 5: CORS Preflight
```bash
curl -X OPTIONS https://acc-be.beel.my.id/api/auth/me \
  -H "Origin: https://tour-package-be-url.com" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v
```
Expected: 200 OK with CORS headers

---

## 🐛 Current Issue

**Symptom:**
```
⚠️  No JWT token found in request
```

**Possible Causes:**

1. **Frontend tidak kirim Authorization header**
   - Solution: Frontend team perlu tambah header `Authorization: Bearer <token>`

2. **CORS block Authorization header**
   - Solution: Nabeel perlu update CORS config di Profile Service

3. **Nginx/Ingress strip Authorization header**
   - Solution: Update k8s Ingress configuration

4. **Profile Service down/unreachable**
   - Solution: Nabeel check service health

---

## 📊 Monitoring & Logging

**Request Tour Package BE → Profile Service:**

```
Tour Package akan log setiap request ke Profile Service:

🔐 Validating JWT token with Profile Service
   Token (first 30 chars): eyJhbGciOiJIUzI1NiIsInR5cCI6...
✅ Token valid from Profile Service
👤 User Info:
   ID: user-123
   Email: user@example.com
   Username: username
   Role: Customer
```

**Request dari Nabeel untuk monitoring:**
- Bisa kirim log dari Profile Service saat Tour Package BE hit `/api/auth/me`?
- Format: timestamp, source IP, token (first 10 chars), response status

---

## 🚨 Error Handling

**Jika Profile Service down:**

Tour Package akan:
1. Log error: `❌ Token validation failed`
2. Request tetap di-forward (tidak block user)
3. User tidak ter-authenticate (tapi tidak dapat 401)

**Behavior:**
- Public endpoints: tetap accessible
- Protected endpoints: akan reject karena tidak ada authentication

**Recommendation:**
- Setup health check dari Tour Package ke Profile Service
- Alert jika Profile Service unreachable > 5 minutes

---

## 📝 Action Items

**Untuk Nabeel (Profile Service):**
- [ ] Confirm endpoint `/api/auth/me` active & stable
- [ ] Share JWT configuration (algorithm, expiration, claims structure)
- [ ] Update CORS config to allow Tour Package BE URL
- [ ] Verify `Authorization` header allowed in CORS
- [ ] Provide test token untuk testing (valid 24 hours)
- [ ] Setup monitoring/alerting untuk endpoint `/api/auth/me`

**Untuk Tour Package (Kamu):**
- [x] Document JWT integration flow
- [x] Create debug guide
- [ ] Test JWT validation dengan token dari Nabeel
- [ ] Verify CORS from Tour Package BE to Profile Service
- [ ] Setup health check monitoring
- [ ] Coordinate dengan Frontend team untuk send Authorization header

---

## 📞 Contact

**Profile Service Owner:** Nabeel  
**Tour Package Owner:** Valiza (kamu)

**Next Meeting Agenda:**
1. Review JWT token format & claims
2. Test end-to-end authentication flow
3. Discuss error handling & fallback mechanism
4. Setup monitoring & alerting

---

**Created**: November 30, 2025  
**Status**: 🟡 Waiting for coordination with Nabeel  
**Priority**: 🔴 High (blocking authentication)
