# 🌐 CORS Setup Guide - Production Deployment

## ❌ Masalah

Backend tidak bisa diakses dari frontend production karena CORS configuration masih hardcoded ke `localhost`.

**Error yang mungkin muncul di Frontend:**
```
Access to fetch at 'http://backend-url/api/...' from origin 'https://your-frontend.com' 
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present
```

---

## ✅ Solusi

Backend sekarang menggunakan **environment variable** untuk CORS allowed origins, sehingga bisa dikonfigurasi tanpa rebuild code!

---

## 🔧 Changes Made

### 1️⃣ Updated `WebSecurityConfig.java`

**Before (Hardcoded):**
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:5173",
    "https://your-frontend-domain.com"  // ❌ Hardcoded
));
```

**After (Environment Variable):**
```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // ✅ Read from environment variable (comma-separated)
    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    configuration.setAllowedOrigins(origins);
    ...
}
```

### 2️⃣ Updated `application-prod.yaml`

```yaml
# CORS Configuration for Production
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:https://your-frontend-domain.com,http://localhost:5173,http://localhost:3000}
```

**Cara kerjanya:**
- Jika environment variable `CORS_ALLOWED_ORIGINS` di-set → gunakan nilai tersebut
- Jika tidak di-set → gunakan default value (localhost untuk development)

---

## 🚀 Deployment Instructions

### Option 1: Set Environment Variable di Server

#### A. Jika deploy dengan Docker

**Update `docker run` command:**
```bash
docker run -d \
  -p 8080:8080 \
  -e CORS_ALLOWED_ORIGINS="https://your-frontend.com,https://www.your-frontend.com" \
  --name tour-package-be \
  your-docker-image:latest
```

**Atau update `docker-compose.yml`:**
```yaml
services:
  backend:
    image: your-docker-image:latest
    ports:
      - "8080:8080"
    environment:
      - CORS_ALLOWED_ORIGINS=https://your-frontend.com,https://www.your-frontend.com
```

#### B. Jika deploy langsung di Ubuntu/Linux

**Set environment variable sebelum run aplikasi:**
```bash
export CORS_ALLOWED_ORIGINS="https://your-frontend.com,https://www.your-frontend.com"
java -jar tour-package-be.jar
```

**Atau tambahkan ke systemd service file** (`/etc/systemd/system/tour-package-be.service`):
```ini
[Unit]
Description=Tour Package Backend
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/app
Environment="CORS_ALLOWED_ORIGINS=https://your-frontend.com,https://www.your-frontend.com"
ExecStart=/usr/bin/java -jar /home/ubuntu/app/tour-package-be.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

Lalu reload dan restart:
```bash
sudo systemctl daemon-reload
sudo systemctl restart tour-package-be
```

#### C. Jika deploy di AWS Elastic Beanstalk

Tambahkan environment variable di **Configuration → Software → Environment properties**:
- Key: `CORS_ALLOWED_ORIGINS`
- Value: `https://your-frontend.com,https://www.your-frontend.com`

#### D. Jika deploy di Heroku

```bash
heroku config:set CORS_ALLOWED_ORIGINS="https://your-frontend.com,https://www.your-frontend.com"
```

---

### Option 2: Update `application-prod.yaml` Directly

Jika tidak ingin pakai environment variable, bisa langsung edit `application-prod.yaml`:

```yaml
cors:
  allowed-origins: https://your-frontend.com,https://www.your-frontend.com,http://localhost:5173
```

Lalu rebuild dan redeploy aplikasi.

---

## 📝 Example Values

### Development (Local)
```bash
CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000"
```

### Production (Single Domain)
```bash
CORS_ALLOWED_ORIGINS="https://tour-package-frontend.com"
```

### Production (Multiple Domains)
```bash
CORS_ALLOWED_ORIGINS="https://tour-package-frontend.com,https://www.tour-package-frontend.com,https://admin.tour-package-frontend.com"
```

### Both Development + Production
```bash
CORS_ALLOWED_ORIGINS="https://tour-package-frontend.com,http://localhost:5173,http://localhost:3000"
```

---

## 🧪 Testing CORS Configuration

### 1. Check if environment variable is loaded
```bash
# SSH ke server
ssh ubuntu@your-server-ip

# Check environment variable
echo $CORS_ALLOWED_ORIGINS

# Check Java process environment
ps aux | grep java
cat /proc/$(pgrep -f 'java.*tour-package')/environ | tr '\0' '\n' | grep CORS
```

### 2. Test CORS dari Frontend
```javascript
// Test fetch dari frontend
fetch('https://your-backend.com/api/activities', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer YOUR_JWT_TOKEN',
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log('✅ CORS working!', data))
.catch(error => console.error('❌ CORS error:', error));
```

### 3. Test dengan cURL
```bash
# Test OPTIONS preflight request
curl -X OPTIONS https://your-backend.com/api/activities \
  -H "Origin: https://your-frontend.com" \
  -H "Access-Control-Request-Method: GET" \
  -v

# Should return:
# Access-Control-Allow-Origin: https://your-frontend.com
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
# Access-Control-Allow-Credentials: true
```

---

## 🔍 Troubleshooting

### Problem: "No 'Access-Control-Allow-Origin' header is present"

**Cause:** Frontend origin tidak ada di `CORS_ALLOWED_ORIGINS`

**Solution:**
```bash
# Tambahkan frontend URL ke environment variable
export CORS_ALLOWED_ORIGINS="https://your-frontend.com,$CORS_ALLOWED_ORIGINS"

# Restart aplikasi
sudo systemctl restart tour-package-be
```

### Problem: "The 'Access-Control-Allow-Origin' header contains multiple values"

**Cause:** CORS configuration mungkin duplikat di multiple places (WebSecurityConfig + @CrossOrigin annotation)

**Solution:**
- Hapus semua `@CrossOrigin` annotation di RestControllers (sudah tidak perlu)
- Hanya pakai global CORS configuration di `WebSecurityConfig`

### Problem: Environment variable tidak terbaca

**Check:**
```bash
# Cek apakah variable di-set
printenv | grep CORS

# Cek log aplikasi saat startup
journalctl -u tour-package-be -f | grep CORS
```

**Solution:**
```bash
# Set variable di shell profile
echo 'export CORS_ALLOWED_ORIGINS="https://your-frontend.com"' >> ~/.bashrc
source ~/.bashrc

# Atau set di systemd service file (see Option 1B above)
```

---

## 📊 Verification Checklist

- [ ] Environment variable `CORS_ALLOWED_ORIGINS` sudah di-set di server
- [ ] Backend sudah di-restart setelah set environment variable
- [ ] Frontend URL (dengan protocol: `https://` atau `http://`) sudah benar
- [ ] Test API call dari frontend console - tidak ada CORS error
- [ ] Test OPTIONS preflight request dengan cURL - response OK
- [ ] Check browser DevTools Network tab - response headers include `Access-Control-Allow-Origin`

---

## 🎯 Quick Fix Commands

### Jika backend berjalan langsung di Ubuntu:

```bash
# 1. Stop backend
pkill -f 'java.*tour-package'

# 2. Set CORS environment variable
export CORS_ALLOWED_ORIGINS="https://your-frontend.com,http://localhost:5173"

# 3. Run backend dengan environment variable
java -jar tour-package-be.jar

# Atau jika pakai nohup untuk background:
nohup java -jar tour-package-be.jar > backend.log 2>&1 &
```

### Jika backend berjalan dengan Docker:

```bash
# 1. Stop & remove container
docker stop tour-package-be
docker rm tour-package-be

# 2. Run dengan environment variable
docker run -d \
  -p 8080:8080 \
  -e CORS_ALLOWED_ORIGINS="https://your-frontend.com,http://localhost:5173" \
  --name tour-package-be \
  --restart unless-stopped \
  your-docker-image:latest

# 3. Check logs
docker logs -f tour-package-be
```

---

## 💡 Best Practices

1. **Always use HTTPS in production** - `https://your-frontend.com` (not `http://`)
2. **Don't use wildcards** - `configuration.setAllowedOrigins(List.of("*"))` is insecure
3. **Remove unused origins** - Jangan include localhost di production CORS config
4. **Test before deploy** - Test CORS locally before deploying to production
5. **Use environment variables** - Easier to manage different environments (dev/staging/prod)

---

## 📚 Related Files Modified

- ✅ `WebSecurityConfig.java` - Added `@Value("${cors.allowed-origins}")`
- ✅ `application-prod.yaml` - Added `cors.allowed-origins` property
- 📝 All `@CrossOrigin` annotations in RestControllers can be removed (handled globally now)

---

## 🆘 Still Having Issues?

**Check Application Logs:**
```bash
# If using systemd
journalctl -u tour-package-be -f

# If using Docker
docker logs -f tour-package-be

# If running directly
tail -f backend.log
```

**Look for:**
- ✅ Application started successfully
- ✅ CORS configuration loaded
- ❌ Any startup errors or exceptions

**Contact me if:**
- CORS still not working after setting environment variable
- Backend returns 403 Forbidden from frontend
- OPTIONS preflight request failing
