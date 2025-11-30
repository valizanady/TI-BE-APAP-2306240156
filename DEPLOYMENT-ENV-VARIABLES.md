# Environment Variables - Tour Package Service

Dokumentasi lengkap environment variables yang dibutuhkan untuk deployment Tour Package Service.

## 📋 Complete Environment Variables List

### 🗄️ Database Configuration

```properties
# PostgreSQL Database Connection
DATABASE_URL_DEV=jdbc:postgresql://localhost:35002/tour-package-dev
DEV_USERNAME=tour-package-dev
DEV_PASSWORD=CLI-825.2025X
```

**Production Settings:**
```properties
DATABASE_URL_DEV=jdbc:postgresql://YOUR_DB_HOST:5432/tour-package-prod
DEV_USERNAME=your_db_username
DEV_PASSWORD=your_secure_password
```

---

### 🌐 CORS Configuration

```properties
# Frontend URL untuk CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

**Production Settings:**
```properties
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com,https://www.your-frontend-domain.com
```

---

### 💳 Bill Service Integration

#### 1️⃣ Tour Package → Bill Service (CREATE BILL)

```properties
# URL endpoint Bill Service untuk create bill
BILL_SERVICE_URL=http://localhost:8081/api/bill/create

# API Key yang digunakan Tour Package saat call Bill Service
# Bill Service akan validate key ini di header "API-KEY"
BILL_SERVICE_API_KEY=GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC
```

**Production Settings:**
```properties
BILL_SERVICE_URL=https://bill-service-domain.com/api/bill/create
BILL_SERVICE_API_KEY=GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC
```

#### 2️⃣ Bill Service → Tour Package (PAYMENT CALLBACK)

```properties
# API Key yang Bill Service kirim ke Tour Package saat payment callback
# Tour Package akan validate key ini di header "API-KEY"
API_KEY_BILL_SERVICE=NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
```

**Production Settings:**
```properties
API_KEY_BILL_SERVICE=NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
```

---

## 🚀 Deployment Platforms

### Option 1: Railway.app

Create file `railway.toml` or set di Railway Dashboard:

```toml
[deploy]
startCommand = "./gradlew bootRun"
healthcheckPath = "/actuator/health"
restartPolicyType = "ON_FAILURE"

[build]
builder = "NIXPACKS"
```

**Environment Variables di Railway:**
```
DATABASE_URL_DEV=jdbc:postgresql://railway-db-host:5432/railway
DEV_USERNAME=postgres
DEV_PASSWORD=railway-generated-password
CORS_ALLOWED_ORIGINS=https://your-frontend.railway.app
BILL_SERVICE_URL=https://bill-service.railway.app/api/bill/create
BILL_SERVICE_API_KEY=GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC
API_KEY_BILL_SERVICE=NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
```

---

### Option 2: Heroku

Create `Procfile`:
```
web: java -Dserver.port=$PORT -jar build/libs/tour-package-2306240156-be.jar
```

**Set Environment Variables:**
```bash
heroku config:set DATABASE_URL_DEV=jdbc:postgresql://heroku-db:5432/dbname
heroku config:set DEV_USERNAME=username
heroku config:set DEV_PASSWORD=password
heroku config:set CORS_ALLOWED_ORIGINS=https://your-app.herokuapp.com
heroku config:set BILL_SERVICE_URL=https://bill-service.herokuapp.com/api/bill/create
heroku config:set BILL_SERVICE_API_KEY=GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC
heroku config:set API_KEY_BILL_SERVICE=NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
```

---

### Option 3: Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  tour-package-db:
    image: postgres:15
    environment:
      POSTGRES_DB: tour-package-prod
      POSTGRES_USER: tour_admin
      POSTGRES_PASSWORD: secure_password_123
    ports:
      - "5432:5432"
    volumes:
      - tour-package-data:/var/lib/postgresql/data

  tour-package-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL_DEV: jdbc:postgresql://tour-package-db:5432/tour-package-prod
      DEV_USERNAME: tour_admin
      DEV_PASSWORD: secure_password_123
      CORS_ALLOWED_ORIGINS: https://your-frontend.com
      BILL_SERVICE_URL: https://bill-service.com/api/bill/create
      BILL_SERVICE_API_KEY: GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC
      API_KEY_BILL_SERVICE: NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
    depends_on:
      - tour-package-db

volumes:
  tour-package-data:
```

---

### Option 4: Kubernetes

Create `deployment.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tour-package-config
data:
  CORS_ALLOWED_ORIGINS: "https://your-frontend.com"
  BILL_SERVICE_URL: "https://bill-service.com/api/bill/create"

---

apiVersion: v1
kind: Secret
metadata:
  name: tour-package-secrets
type: Opaque
stringData:
  DATABASE_URL_DEV: "jdbc:postgresql://postgres-service:5432/tour-package-prod"
  DEV_USERNAME: "tour_admin"
  DEV_PASSWORD: "secure_password_123"
  BILL_SERVICE_API_KEY: "GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC"
  API_KEY_BILL_SERVICE: "NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS"

---

apiVersion: apps/v1
kind: Deployment
metadata:
  name: tour-package-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: tour-package
  template:
    metadata:
      labels:
        app: tour-package
    spec:
      containers:
      - name: tour-package
        image: your-registry/tour-package:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: tour-package-config
        - secretRef:
            name: tour-package-secrets
```

---

## 🔒 Security Best Practices

### 1. Environment Variables Protection

✅ **DO:**
- Store secrets in secure vault (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
- Use different API keys for dev/staging/production
- Rotate API keys regularly (every 90 days)
- Use strong, randomly generated passwords (min 32 characters)
- Never commit `.env` file to git (add to `.gitignore`)

❌ **DON'T:**
- Hardcode secrets in source code
- Share API keys in public channels
- Use same credentials across environments
- Store secrets in version control

### 2. Recommended API Key Generation

```bash
# Generate secure random API key (32 characters)
openssl rand -base64 32

# Or use UUID
uuidgen
```

### 3. Database Security

- Use SSL/TLS connection for production database
- Restrict database access by IP whitelist
- Use strong passwords (min 16 characters, mixed case, numbers, symbols)
- Enable database audit logging

---

## 📝 Environment Variables Template

Copy this template untuk setup environment baru:

```properties
# ===========================================
# DATABASE CONFIGURATION
# ===========================================
DATABASE_URL_DEV=jdbc:postgresql://HOST:PORT/DATABASE_NAME
DEV_USERNAME=your_db_username
DEV_PASSWORD=your_secure_password

# ===========================================
# CORS CONFIGURATION
# ===========================================
CORS_ALLOWED_ORIGINS=https://your-frontend.com

# ===========================================
# BILL SERVICE INTEGRATION
# ===========================================

# Tour Package → Bill Service (CREATE BILL)
BILL_SERVICE_URL=https://bill-service.com/api/bill/create
BILL_SERVICE_API_KEY=your-api-key-here

# Bill Service → Tour Package (PAYMENT CALLBACK)
API_KEY_BILL_SERVICE=your-api-key-here
```

---

## 🧪 Testing Environment Variables

### Verify Variables are Loaded

Create test endpoint (development only):

```java
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Value("${CORS_ALLOWED_ORIGINS}")
    private String corsOrigins;
    
    @Value("${BILL_SERVICE_URL}")
    private String billServiceUrl;
    
    @GetMapping("/config")
    public Map<String, String> checkConfig() {
        return Map.of(
            "corsOrigins", corsOrigins,
            "billServiceUrl", billServiceUrl,
            "status", "OK"
        );
    }
}
```

Test:
```bash
curl http://localhost:8080/api/health/config
```

⚠️ **WARNING**: Remove this endpoint in production or add authentication!

---

## 📊 Environment Variables Summary

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DATABASE_URL_DEV` | ✅ | - | PostgreSQL JDBC connection URL |
| `DEV_USERNAME` | ✅ | - | Database username |
| `DEV_PASSWORD` | ✅ | - | Database password |
| `CORS_ALLOWED_ORIGINS` | ✅ | - | Comma-separated frontend URLs |
| `BILL_SERVICE_URL` | ✅ | `http://localhost:8081/api/bill/create` | Bill Service endpoint |
| `BILL_SERVICE_API_KEY` | ✅ | `NlfUxKNkXI...` | API Key to call Bill Service |
| `API_KEY_BILL_SERVICE` | ✅ | `NlfUxKNkXI...` | API Key for Bill Service callback |

---

## 🔧 Troubleshooting

### Issue: App fails to start with "Cannot resolve placeholder"

**Solution**: Ensure all required environment variables are set

```bash
# Check environment variables
env | grep -E "DATABASE|CORS|BILL|API_KEY"

# Or in Java code
System.getenv().forEach((k, v) -> System.out.println(k + " = " + v));
```

### Issue: CORS errors in browser

**Solution**: Update `CORS_ALLOWED_ORIGINS` to include frontend URL

```properties
# Must include protocol (http/https) and port
CORS_ALLOWED_ORIGINS=https://my-frontend.com,http://localhost:5173
```

### Issue: Bill Service returns 401 Unauthorized

**Solution**: Verify API Key matches between services

```bash
# Tour Package sends this in header "API-KEY"
echo $BILL_SERVICE_API_KEY

# Bill Service expects this value in its configuration
```

---

## 📱 Contact & Support

- **Documentation**: [BILL-INTEGRATION-COMPLETE-GUIDE.md](./BILL-INTEGRATION-COMPLETE-GUIDE.md)
- **API Reference**: [PAYMENT-CALLBACK-GUIDE.md](./PAYMENT-CALLBACK-GUIDE.md)
- **Issues**: Create issue in repository

---

**Last Updated**: November 30, 2025  
**Version**: 1.0  
**Deployment Status**: ✅ Production Ready
