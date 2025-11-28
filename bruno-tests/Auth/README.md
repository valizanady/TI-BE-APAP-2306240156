# Authentication API Tests

This folder contains Bruno API tests for authentication-related endpoints.

## Overview

The authentication flow uses **Nabeel's authentication service** for login and token management:
- Auth Service Frontend: `https://acc-fe.beel.my.id`
- Auth Service Backend: `https://acc-be.beel.my.id/api`

## Authentication Flow (OTT Exchange Pattern)

### Complete Login Flow - UPDATED

```
1. User clicks Login in Frontend
   ↓
2. Frontend redirects to: https://acc-fe.beel.my.id/auth/login?redirect=http://localhost:5173/login-success
   (Note: redirect URL points directly to FRONTEND, NOT backend)
   ↓
3. User logs in at Nabeel's auth service
   ↓
4. Auth service redirects: http://localhost:5173/login-success?ott=2U9ZTI
   (SSO returns OTT directly to frontend)
   ↓
5. Frontend (LoginSuccessView.vue) receives OTT from URL
   ↓
6. Frontend calls: POST http://localhost:8080/api/auth/exchange
   Body: { "ott": "2U9ZTI" }
   ↓
7. Backend receives OTT and calls Nabeel's service:
   POST https://acc-be.beel.my.id/api/auth/exchange
   Body: { "ott": "2U9ZTI" }
   ↓
8. Nabeel's service validates OTT and returns JWT
   ↓
9. Backend returns JWT to frontend:
   Response: { "status": 200, "data": { "jwt": "eyJ..." } }
   ↓
10. Frontend stores JWT in localStorage (key: "token")
    ↓
11. Frontend parses JWT to extract user info (username, role, email, etc.)
    ↓
12. Frontend displays user info in navbar
    ↓
13. All subsequent API calls include: Authorization: Bearer <JWT>
```

### Why OTT Exchange Pattern?

**Security Benefits:**
- ✅ OTT is single-use and short-lived
- ✅ OTT never stored, only exchanged immediately
- ✅ JWT validation happens on backend
- ✅ Frontend never sees user credentials
- ✅ Prevents token replay attacks

**vs Old Pattern (Direct Token in URL):**
- ❌ Token exposed in browser history
- ❌ Token can be logged by proxies
- ❌ No server-side validation before use

## Endpoints

### 1. POST `/api/auth/exchange` - Exchange OTT for JWT

**Purpose:** Convert One-Time Token (OTT) from auth redirect into a JWT access token.

**Authentication:** None (public endpoint)

**Request:**
```json
{
  "ott": "2U9ZTI"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Token exchange successful",
  "timestamp": "2025-11-27T09:30:00.000+07:00",
  "data": {
    "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Error Responses:**
- `400` - OTT is missing or empty
- `401` - OTT is invalid or expired
- `500` - Internal server error

## How to Use JWT Token

After obtaining the JWT token, include it in the `Authorization` header for all API calls:

```javascript
fetch('http://localhost:8080/api/profile', {
  headers: {
    'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
  }
})
```

## Testing in Bruno

1. **Open Bruno** and load this workspace
2. **Get an OTT**: 
   - Go to `https://acc-fe.beel.my.id/auth/login?callback=http://localhost:8080/`
   - Login with your credentials
   - After redirect, extract the OTT from the URL: `http://localhost:5173/login-success/auth?ott=YOUR_OTT`
3. **Test Exchange**:
   - Open `POST-Exchange-OTT-for-JWT.bru`
   - Replace `"2U9ZTI"` with your actual OTT
   - Send the request
   - Copy the JWT from the response
4. **Use JWT**:
   - Go to `Local.bru` environment
   - Update `CUSTOMER_TOKEN` or `SUPERADMIN_TOKEN` with the JWT
   - Use in other API tests

## Security Notes

- **OTT is single-use**: Once exchanged, it cannot be used again
- **OTT expires quickly**: Usually valid for only a few minutes
- **JWT has longer lifetime**: Check token expiry in JWT payload
- **Store JWT securely**: Use httpOnly cookies or secure localStorage in production

## Related Files

**Backend:**
- Controller: `AuthController.java` - Handles `/api/auth/exchange` endpoint
- Service: `ProfileServiceClient.exchangeToken()` - Calls Nabeel's SSO
- Security Config: `WebSecurityConfig.java` - Allows `/api/auth/**` without authentication
- DTO: `TokenExchangeResponseDTO.java` - Response format with `jwt` field

**Frontend:**
- Auth Store: `src/stores/auth.ts` - Token exchange logic
- Login Success View: `src/views/LoginSuccessView.vue` - Receives OTT from URL
- App Layout: `src/layout/AppLayout.vue` - Displays user info in navbar
- Environment: `.env` - Must have `VITE_AUTH_BACKEND_URL=http://localhost:8080/api/auth`

**Documentation:**
- 📄 **Frontend Comprehensive Guide:** `../tour-package-2306240156-fe/AUTH-FLOW-SYNC-GUIDE.md`
- 📄 Frontend Test Guide: `../tour-package-2306240156-fe/LOGIN-TEST-GUIDE.md`
- 📄 Backend Auth README: This file

---

## Additional Information

### JWT Payload Structure

```json
{
  "username": "nadya.valiza",
  "role": "Customer",
  "email": "nadya.valiza@ui.ac.id",
  "userId": 21,
  "balance": 0.0,
  "iat": 1234567890,
  "exp": 1234571490
}
```

### Environment Configuration

**Backend `application.yaml`:**
```yaml
cors:
  allowed-origins: "http://localhost:5173"
```

**Frontend `.env`:**
```env
VITE_AUTH_BACKEND_URL=http://localhost:8080/api/auth
```

**⚠️ Common Mistake:** Forgetting `/api/auth` path causes 404 errors!

---

## Documentation Synchronization

✅ **This document is synchronized with frontend documentation**

Last synchronized: 2025-01-27

Changes from old documentation:
- ✅ Removed outdated BaseController redirect step
- ✅ Clarified SSO redirects directly to frontend, not backend first
- ✅ Updated redirect URL to point to frontend `/login-success`
- ✅ Added JWT payload structure
- ✅ Added environment configuration examples
- ✅ Added common troubleshooting issues
- ✅ Cross-referenced with frontend documentation

For complete visual flow diagrams and detailed explanations, see:
👉 **`../tour-package-2306240156-fe/AUTH-FLOW-SYNC-GUIDE.md`**

---

## Troubleshooting

### OTT Exchange Fails (401)
- **Cause**: OTT already used or expired
- **Solution**: Get a fresh OTT by logging in again

### Cannot Call Protected Endpoints
- **Cause**: JWT not included in Authorization header
- **Solution**: Add `Authorization: Bearer <JWT>` header

### JWT Validation Fails
- **Cause**: JWT expired or invalid
- **Solution**: Exchange a new OTT to get fresh JWT

### CORS Errors
- **Cause**: Frontend origin not allowed
- **Solution**: Check `WebSecurityConfig.corsConfigurationSource()` includes your frontend URL
