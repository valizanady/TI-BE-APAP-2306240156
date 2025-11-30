# CORS Fix Guide - Token Exchange Error

## Problem
❌ Error: `Access to XMLHttpRequest at 'http://2306240156-be.hafizmuh.site/api/auth/exchange' from origin 'http://2306240156-fe.hafizmuh.site' has been blocked by CORS policy`

## Root Cause
Ada **2 CORS configurations yang konflik**:
1. **CorsConfig.java** - menggunakan env variable ✅
2. **WebSecurityConfig.java** - hardcoded origins ❌ (tidak include production URLs)

**WebSecurityConfig** CORS config di-override oleh **Spring Security**, jadi konfigurasi CorsConfig tidak terpakai.

## Solution Applied

### 1. Updated WebSecurityConfig.java ✅
Changed from hardcoded origins to use environment variable:

```java
@Value("${CORS_ALLOWED_ORIGINS}")
private String corsAllowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Parse origins dari environment variable (comma-separated)
    String[] origins = corsAllowedOrigins.split(",");
    configuration.setAllowedOrigins(Arrays.asList(origins));
    
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(List.of("Authorization"));
    
    // ... rest of config
}
```

## Required GitHub Secret

### Update `CORS_ALLOWED_ORIGINS` secret in GitHub:

**Value harus include semua origins** (comma-separated, **NO SPACES**):

```
http://localhost:5173,http://2306240156-fe.hafizmuh.site,https://2306240156-fe.hafizmuh.site,http://sso-ui.cs.ui.ac.id
```

**Include:**
- ✅ `http://localhost:5173` - local development
- ✅ `http://2306240156-fe.hafizmuh.site` - production FE
- ✅ `https://2306240156-fe.hafizmuh.site` - HTTPS version (jika ada)
- ✅ `http://sso-ui.cs.ui.ac.id` - SSO redirect origin

## Steps to Fix

### 1. Update GitHub Secret

Go to GitHub repository:
```
Settings → Secrets and variables → Actions → CORS_ALLOWED_ORIGINS
```

**Update value to:**
```
http://localhost:5173,http://2306240156-fe.hafizmuh.site,http://sso-ui.cs.ui.ac.id
```

⚠️ **IMPORTANT**: NO SPACES between origins!

### 2. Commit & Push Backend Changes

```bash
cd tour-package-2306240156-be
git add src/main/java/apap/ti/_5/tour_package_2306240156_be/security/WebSecurityConfig.java
git commit -m "fix: use environment variable for CORS origins in WebSecurityConfig"
git push origin backlog
```

### 3. Wait for CI/CD Pipeline

Pipeline akan:
1. Build JAR baru dengan CORS fix
2. Push Docker image
3. Deploy ke k3s dengan updated secret

Monitor di: https://github.com/your-username/tour-package-2306240156-be/actions

### 4. Verify Deployment

After deployment completes (~3-5 minutes):

```bash
# Check if new deployment is running
ssh ec2-user@your-ec2-host "sudo k3s kubectl get pods -n default"

# Check logs for CORS configuration
ssh ec2-user@your-ec2-host "sudo k3s kubectl logs deployment/tourpackage-be -n default | grep CORS"
```

### 5. Test CORS

Open browser console at `http://2306240156-fe.hafizmuh.site` and test:

```javascript
// Test OPTIONS preflight
fetch('http://2306240156-be.hafizmuh.site/api/auth/exchange', {
  method: 'OPTIONS',
  headers: {
    'Origin': 'http://2306240156-fe.hafizmuh.site',
    'Access-Control-Request-Method': 'POST',
    'Access-Control-Request-Headers': 'content-type'
  }
}).then(r => console.log('✅ CORS OK', r.headers.get('Access-Control-Allow-Origin')))
  .catch(e => console.error('❌ CORS failed', e));
```

Expected response headers:
```
Access-Control-Allow-Origin: http://2306240156-fe.hafizmuh.site
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

## Verification Checklist

- [ ] GitHub Secret `CORS_ALLOWED_ORIGINS` updated with correct value
- [ ] Backend code pushed to `backlog` branch
- [ ] CI/CD pipeline completed successfully
- [ ] New pods are running in k3s
- [ ] OPTIONS preflight request returns correct CORS headers
- [ ] POST request to `/api/auth/exchange` works without CORS error
- [ ] SSO login flow completes successfully

## Troubleshooting

### If CORS still fails after deployment:

1. **Check secret is actually applied:**
```bash
ssh ec2-user@your-ec2-host "sudo k3s kubectl get secret tourpackage-be-secret -n default -o yaml"
```

2. **Check pod environment variable:**
```bash
ssh ec2-user@your-ec2-host "sudo k3s kubectl exec deployment/tourpackage-be -n default -- env | grep CORS"
```

3. **Force pod restart:**
```bash
ssh ec2-user@your-ec2-host "sudo k3s kubectl rollout restart deployment/tourpackage-be -n default"
```

4. **Check application logs:**
```bash
ssh ec2-user@your-ec2-host "sudo k3s kubectl logs deployment/tourpackage-be -n default --tail=100"
```

### Common Issues

**Issue:** Secret not updated in k3s
**Fix:** Re-run deployment job in GitHub Actions

**Issue:** Old pods still running
**Fix:** Force restart pods manually

**Issue:** Browser caching old CORS response
**Fix:** Hard refresh (Cmd+Shift+R) or clear browser cache

## Why This Fix Works

1. **Single Source of Truth**: Both CorsConfig and WebSecurityConfig now use the same env variable
2. **Spring Security Priority**: WebSecurityConfig CORS takes priority over WebMvcConfigurer CORS
3. **Environment Variable**: CORS origins dapat di-update via GitHub Secret tanpa code change
4. **Proper CORS Headers**: Include `allowCredentials`, `exposedHeaders` untuk authentication

## Related Files

- ✅ `src/main/java/apap/ti/_5/tour_package_2306240156_be/security/WebSecurityConfig.java` - FIXED
- ✅ `src/main/java/apap/ti/_5/tour_package_2306240156_be/config/CorsConfig.java` - Original (redundant tapi harmless)
- ✅ `.github/workflows/ci.yml` - Generates secret with CORS_ALLOWED_ORIGINS
- ✅ `k8s/deployment.yaml` - References the secret

---

**Date Fixed:** 2025-11-30
**Fixed By:** GitHub Copilot
