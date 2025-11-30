# 🚀 Quick Deploy Guide - Set CORS for Production

## Frontend URL Kamu
```
https://2306240156-fe.hafizmuh.site
```

## Backend URL Kamu
```
https://2306240156-be.hafizmuh.site
```

---

## ✅ Step-by-Step Deployment

### 1. Set Environment Variable di Server

SSH ke server lalu set CORS:

```bash
# SSH ke server
ssh ubuntu@your-server-ip

# Set CORS allowed origins (ganti dengan URL frontend kamu)
export CORS_ALLOWED_ORIGINS="https://2306240156-fe.hafizmuh.site"

# Optional: Tambahkan localhost untuk testing
# export CORS_ALLOWED_ORIGINS="https://2306240156-fe.hafizmuh.site,http://localhost:5173"
```

### 2. Update di Kubernetes (K3s)

Jika deploy dengan Kubernetes, update environment variable di deployment:

```bash
# Edit deployment
kubectl edit deployment tour-package-be

# Tambahkan di section env:
env:
  - name: CORS_ALLOWED_ORIGINS
    value: "https://2306240156-fe.hafizmuh.site"
```

Atau update via kubectl set:

```bash
kubectl set env deployment/tour-package-be \
  CORS_ALLOWED_ORIGINS="https://2306240156-fe.hafizmuh.site"
```

### 3. Update GitLab CI/CD Variables (Recommended)

Di GitLab project → Settings → CI/CD → Variables:

**Add Variable:**
- Key: `CORS_ALLOWED_ORIGINS`
- Value: `https://2306240156-fe.hafizmuh.site`
- Type: Variable
- Protected: ✅ (jika pakai protected branch)
- Masked: ❌ (URL tidak sensitif)

Lalu update `.gitlab-ci.yml` untuk inject variable ke secret.yaml:

```yaml
deploy:
  script:
    - |
      cat <<EOF | kubectl apply -f -
      apiVersion: v1
      kind: Secret
      metadata:
        name: app-secrets
      stringData:
        CORS_ALLOWED_ORIGINS: "${CORS_ALLOWED_ORIGINS}"
      EOF
```

---

## 🧪 Test CORS

### Test dari Browser Console (F12)

```javascript
fetch('https://2306240156-be.hafizmuh.site/api/activities', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
  }
})
.then(r => r.json())
.then(data => console.log('✅ CORS working!', data))
.catch(e => console.error('❌ CORS error:', e));
```

### Test dengan cURL

```bash
curl -X OPTIONS https://2306240156-be.hafizmuh.site/api/activities \
  -H "Origin: https://2306240156-fe.hafizmuh.site" \
  -H "Access-Control-Request-Method: GET" \
  -v

# Should see in response headers:
# Access-Control-Allow-Origin: https://2306240156-fe.hafizmuh.site
# Access-Control-Allow-Credentials: true
```

---

## ✅ Changes Summary

### Fixed Issues:

1. **❌ DateTime jadi 00:00:00** → ✅ Fixed
   - Changed `@DateTimeFormat` to `@JsonFormat` in:
     - `CreatePackageRequestDTO.java`
     - `UpdatePackageRequestDTO.java`
   - Now properly parses `"2025-12-06T15:30"` format from frontend

2. **❌ CORS hardcoded localhost** → ✅ Fixed
   - WebSecurityConfig now uses `${cors.allowed-origins}` from environment variable
   - Removed all `@CrossOrigin` annotations from controllers
   - Single source of truth for CORS configuration

3. **❌ userId manual from frontend** → ✅ Fixed (previous commit)
   - Backend automatically extracts userId from JWT token
   - Frontend no longer sends userId in request body

---

## 📝 Request/Response Example

### Create Package Request (NEW - No userId)

```json
POST https://2306240156-be.hafizmuh.site/api/package/create
Authorization: Bearer YOUR_JWT_TOKEN

{
  "packageName": "Jakarta - Malay Travel Package",
  "quota": 60,
  "startDate": "2025-12-06T15:30",
  "endDate": "2025-12-07T18:00"
}
```

### Response (userId auto-filled, datetime preserved)

```json
{
  "status": 201,
  "message": "Created",
  "timestamp": "2025-11-30T16:45:00.000+07:00",
  "data": {
    "id": "PKG-20251130-001",
    "userId": "user003",
    "packageName": "Jakarta - Malay Travel Package",
    "quota": 60,
    "price": 0,
    "status": "Pending",
    "startDate": "2025-12-06T15:30:00",
    "endDate": "2025-12-07T18:00:00"
  }
}
```

✅ **Note:** Sekarang jam tidak hilang lagi!

---

## 🔧 Deployment Checklist

- [ ] Set `CORS_ALLOWED_ORIGINS` environment variable di server
- [ ] Restart backend service
- [ ] Test API call dari frontend (check browser console - no CORS error)
- [ ] Test create package (check datetime tidak jadi 00:00:00)
- [ ] Verify userId auto-filled from JWT token

---

## 📚 Full Documentation

- **CORS-CONFIGURATION.md** - Complete CORS setup reference
- **CORS-SETUP-GUIDE.md** - Detailed deployment guide
- **PACKAGE-CREATE-AUTO-USERID.md** - Auto userId from JWT guide
- **.env.example** - Environment variables template

---

**Status:** ✅ Ready to deploy!
