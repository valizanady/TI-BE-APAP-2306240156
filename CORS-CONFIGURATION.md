# 🌐 CORS Configuration Summary

## ✅ Current Setup

Backend menggunakan **GLOBAL CORS Configuration** di `WebSecurityConfig.java` yang bisa dikonfigurasi via **environment variable**.

---

## 📋 CORS Configuration Files

### 1. Main CORS Config (Global)
**File:** `src/main/java/.../security/WebSecurityConfig.java`

```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // ✅ Read from environment variable (comma-separated)
    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    configuration.setAllowedOrigins(origins);
    
    // Allow all HTTP methods
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    
    // Allow all headers
    configuration.setAllowedHeaders(List.of("*"));
    
    // Allow credentials (cookies, authorization headers)
    configuration.setAllowCredentials(true);
    ...
}
```

### 2. Environment Variable Configuration
**File:** `src/main/resources/application-prod.yaml`

```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:https://your-frontend-domain.com,http://localhost:5173,http://localhost:3000}
```

**Cara kerjanya:**
- Jika environment variable `CORS_ALLOWED_ORIGINS` di-set → pakai value tersebut
- Jika tidak di-set → pakai default value (untuk development)

### 3. Controller Level (❌ SUDAH DIHAPUS)
Sebelumnya ada `@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")` di setiap controller:
- ❌ `PackageRestController.java` - REMOVED
- ❌ `ActivityRestController.java` - REMOVED
- ❌ `StatisticsRestController.java` - REMOVED
- ❌ `LocationRestController.java` - REMOVED

**Kenapa dihapus?**
- Redundant (sudah ada global config)
- Environment variable `CORS_ALLOWED_ORIGINS` tidak terdefined sebelumnya
- Bisa bikin konflik dengan global config

---

## 🚀 How to Use

### Development (Local)

Tidak perlu set environment variable, akan pakai default value:
```
http://localhost:5173, http://localhost:3000
```

### Production (Server)

**Option 1: Set Environment Variable (Recommended)**

```bash
# Set via shell
export CORS_ALLOWED_ORIGINS="https://your-frontend.com,https://www.your-frontend.com"
java -jar tour-package-be.jar

# Atau via systemd service
Environment="CORS_ALLOWED_ORIGINS=https://your-frontend.com"

# Atau via Docker
docker run -e CORS_ALLOWED_ORIGINS="https://your-frontend.com" ...
```

**Option 2: Edit application-prod.yaml Directly**

```yaml
cors:
  allowed-origins: https://your-production-frontend.com
```

Lalu rebuild & redeploy.

---

## 📝 Example Values

### Single Frontend Domain
```bash
CORS_ALLOWED_ORIGINS="https://tour-package-fe.com"
```

### Multiple Domains (dengan www)
```bash
CORS_ALLOWED_ORIGINS="https://tour-package-fe.com,https://www.tour-package-fe.com"
```

### Multiple Environments (Dev + Prod)
```bash
CORS_ALLOWED_ORIGINS="https://tour-package-fe.com,http://localhost:5173,http://localhost:3000"
```

### Vercel Deployment
```bash
CORS_ALLOWED_ORIGINS="https://tour-package-fe.vercel.app,https://tour-package-fe-git-main.vercel.app"
```

### Netlify Deployment
```bash
CORS_ALLOWED_ORIGINS="https://tour-package-fe.netlify.app"
```

---

## ⚠️ Important Notes

### ✅ DO's

1. **Always include protocol** - `https://` or `http://`
   ```
   ✅ https://your-frontend.com
   ❌ your-frontend.com
   ```

2. **No trailing slash**
   ```
   ✅ https://your-frontend.com
   ❌ https://your-frontend.com/
   ```

3. **Include all subdomains if needed**
   ```
   CORS_ALLOWED_ORIGINS="https://app.example.com,https://www.example.com,https://admin.example.com"
   ```

4. **Separate multiple origins with comma (no spaces)**
   ```
   ✅ origin1,origin2,origin3
   ❌ origin1, origin2, origin3  (has spaces)
   ```

### ❌ DON'Ts

1. **Don't use wildcards in production** (security risk)
   ```
   ❌ CORS_ALLOWED_ORIGINS="*"
   ❌ CORS_ALLOWED_ORIGINS="https://*.example.com"
   ```

2. **Don't include paths**
   ```
   ❌ https://your-frontend.com/login
   ✅ https://your-frontend.com
   ```

3. **Don't mix http and https for same domain**
   ```
   ❌ http://example.com,https://example.com
   ✅ https://example.com  (only use HTTPS in production)
   ```

---

## 🧪 Testing CORS

### 1. Check Environment Variable
```bash
# SSH to server
ssh ubuntu@your-server-ip

# Check if variable is set
echo $CORS_ALLOWED_ORIGINS

# Check Java process environment
ps aux | grep java
cat /proc/$(pgrep -f 'java.*tour-package')/environ | tr '\0' '\n' | grep CORS
```

### 2. Test from Browser Console
```javascript
fetch('https://your-backend.com/api/activities', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
  }
})
.then(response => response.json())
.then(data => console.log('✅ CORS working!', data))
.catch(error => console.error('❌ CORS error:', error));
```

### 3. Test with cURL
```bash
# Test OPTIONS preflight
curl -X OPTIONS https://your-backend.com/api/activities \
  -H "Origin: https://your-frontend.com" \
  -H "Access-Control-Request-Method: GET" \
  -v

# Should see in response:
# Access-Control-Allow-Origin: https://your-frontend.com
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
# Access-Control-Allow-Credentials: true
```

---

## 🔍 Troubleshooting

### Problem: "No 'Access-Control-Allow-Origin' header"

**Check 1:** Environment variable di-set?
```bash
echo $CORS_ALLOWED_ORIGINS
```

**Check 2:** Backend sudah restart setelah set env variable?
```bash
sudo systemctl restart tour-package-be
```

**Check 3:** Frontend URL sudah benar (case-sensitive)?
```bash
# ❌ Wrong
CORS_ALLOWED_ORIGINS="https://Your-Frontend.Com"

# ✅ Correct
CORS_ALLOWED_ORIGINS="https://your-frontend.com"
```

### Problem: Still getting CORS error after setting variable

**Check 4:** Ada typo di URL?
```bash
# Print actual value
printenv | grep CORS

# Should match exactly with frontend URL
```

**Check 5:** Frontend pakai subdomain yang berbeda?
```bash
# Frontend di: https://www.example.com
# Backend CORS: https://example.com  ❌ Won't work

# Fix: Include both
CORS_ALLOWED_ORIGINS="https://example.com,https://www.example.com"
```

### Problem: Working locally but not in production

**Check 6:** Environment variable hanya di-set di shell session?
```bash
# Temporary (lost after reboot)
export CORS_ALLOWED_ORIGINS="..."

# Permanent - add to systemd service or docker-compose
```

---

## 📊 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      CORS Flow                               │
└─────────────────────────────────────────────────────────────┘

1. Frontend Request
   Origin: https://your-frontend.com
   ↓

2. Spring Security → WebSecurityConfig.java
   - corsConfigurationSource()
   - Read: ${cors.allowed-origins}
   ↓

3. application-prod.yaml
   - cors.allowed-origins: ${CORS_ALLOWED_ORIGINS:default}
   ↓

4. Environment Variable
   - CORS_ALLOWED_ORIGINS="https://your-frontend.com"
   ↓

5. If origin matches → Allow
   Response Headers:
   - Access-Control-Allow-Origin: https://your-frontend.com
   - Access-Control-Allow-Credentials: true
   - Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
```

---

## 🎯 Quick Reference

| Environment | CORS_ALLOWED_ORIGINS Value |
|-------------|---------------------------|
| **Local Development** | Default: `http://localhost:5173,http://localhost:3000` |
| **Production Single** | `https://your-frontend.com` |
| **Production Multi** | `https://your-frontend.com,https://www.your-frontend.com` |
| **Vercel** | `https://yourapp.vercel.app,https://yourapp-git-main.vercel.app` |
| **Netlify** | `https://yourapp.netlify.app` |
| **AWS Amplify** | `https://main.d1234abcd.amplifyapp.com` |

---

## 📚 Files Modified

- ✅ `WebSecurityConfig.java` - Global CORS with environment variable support
- ✅ `application-prod.yaml` - Added `cors.allowed-origins` property
- ✅ `PackageRestController.java` - Removed `@CrossOrigin` annotation
- ✅ `ActivityRestController.java` - Removed `@CrossOrigin` annotation
- ✅ `StatisticsRestController.java` - Removed `@CrossOrigin` annotation
- ✅ `LocationRestController.java` - Removed `@CrossOrigin` annotation

---

## ✅ Summary

**Single Source of Truth:** `WebSecurityConfig.java` (global CORS config)

**Configuration Method:** Environment variable `CORS_ALLOWED_ORIGINS`

**How to Change:** Set environment variable lalu restart backend

**No Code Changes Needed:** Bisa update CORS origins tanpa rebuild code!

---

## 🆘 Need Help?

1. Read: `CORS-SETUP-GUIDE.md` (detailed deployment instructions)
2. Check logs: `journalctl -u tour-package-be -f`
3. Test with cURL first before testing from frontend
4. Verify environment variable: `printenv | grep CORS`
