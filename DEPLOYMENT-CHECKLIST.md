# 🚀 Tour Package Deployment Checklist

Quick reference untuk deployment Tour Package Service ke production.

## ✅ Pre-Deployment Checklist

### 1. Environment Variables (WAJIB)

Pastikan semua variable ini sudah di-set di platform deployment:

```properties
# Database
DATABASE_URL_DEV=jdbc:postgresql://YOUR_HOST:5432/YOUR_DB
DEV_USERNAME=your_username
DEV_PASSWORD=your_password

# CORS
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com

# Bill Service Integration
BILL_SERVICE_URL=https://bill-service-url.com/api/bill/create
BILL_SERVICE_API_KEY=GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC
API_KEY_BILL_SERVICE=NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS
```

### 2. Service Dependencies

- ✅ PostgreSQL Database (ready & accessible)
- ✅ Bill Service (deployed & accessible)
- ✅ Profile Service JWT endpoint (https://acc-be.beel.my.id/api/auth/me)

### 3. Security

- ✅ API Keys configured correctly
- ✅ Database uses strong password
- ✅ CORS only allows trusted domains
- ✅ SSL/TLS enabled for production

---

## 📋 Variable Details

### DATABASE_URL_DEV
**Format**: `jdbc:postgresql://HOST:PORT/DATABASE_NAME`
**Example**: `jdbc:postgresql://db.railway.app:5432/tour-package-prod`
**Purpose**: PostgreSQL database connection

### DEV_USERNAME & DEV_PASSWORD
**Purpose**: Database authentication credentials
**Note**: Use strong password (min 16 chars)

### CORS_ALLOWED_ORIGINS
**Format**: Comma-separated URLs with protocol
**Example**: `https://tour-app.com,https://www.tour-app.com`
**Purpose**: Frontend access control

### BILL_SERVICE_URL
**Format**: Full URL with path
**Example**: `https://bill-service.railway.app/api/bill/create`
**Purpose**: Endpoint untuk create bill setelah package processed

### BILL_SERVICE_API_KEY
**Value**: `GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC`
**Purpose**: Tour Package mengirim key ini di header "API-KEY" saat call Bill Service
**Note**: Harus match dengan key yang di-expect Bill Service

### API_KEY_BILL_SERVICE
**Value**: `NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS`
**Purpose**: Bill Service mengirim key ini di header "API-KEY" saat payment callback
**Note**: Tour Package akan validate key ini di ApiKeyFilter

---

## 🔄 Integration Flow

### 1️⃣ Bill Creation Flow
```
Tour Package → [POST] BILL_SERVICE_URL
Header: "API-KEY: {BILL_SERVICE_API_KEY}"
Body: { packageId, userId, packageName, billDate, quantity, price, serviceName }
```

### 2️⃣ Payment Callback Flow
```
Bill Service → [POST] https://your-tour-package.com/api/package/payment/update
Header: "API-KEY: {API_KEY_BILL_SERVICE}"
Body: { packageId: "string", status: 0 or 1 }
```

**Status Codes**:
- `0` = UNPAID → Package tetap "Processed", throw error
- `1` = PAID → Package diupdate ke "Payment Confirmed"

---

## 🧪 Testing Steps

### 1. Health Check
```bash
curl https://your-tour-package-url.com/actuator/health
```

### 2. Test Bill Creation
1. Process a package via API
2. Check logs untuk "Bill successfully created"
3. Verify bill exists di Bill Service

### 3. Test Payment Callback
```bash
curl -X POST https://your-tour-package-url.com/api/package/payment/update \
  -H "API-KEY: NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS" \
  -H "Content-Type: application/json" \
  -d '{"packageId":"test-package-id","status":1}'
```

### 4. Test CORS
1. Open frontend di browser
2. Check Console untuk CORS errors
3. Verify requests dari frontend berhasil

---

## 🐛 Common Issues & Solutions

### ❌ "Cannot resolve placeholder 'DATABASE_URL_DEV'"
**Solution**: Set all required environment variables di deployment platform

### ❌ CORS Error: "No 'Access-Control-Allow-Origin' header"
**Solution**: Update `CORS_ALLOWED_ORIGINS` include frontend URL dengan protocol (https://)

### ❌ Bill Service returns 401 Unauthorized
**Solution**: Verify `BILL_SERVICE_API_KEY` matches dengan yang di-expect Bill Service

### ❌ Payment callback returns 403 Forbidden
**Solution**: Verify Bill Service mengirim correct `API-KEY` header (value: `API_KEY_BILL_SERVICE`)

### ❌ Package price is 0 in bill
**Solution**: Already fixed! Service calculate price dari OrderedQuantities if package.price = 0

---

## 📚 Documentation References

- **Full Deployment Guide**: [DEPLOYMENT-ENV-VARIABLES.md](./DEPLOYMENT-ENV-VARIABLES.md)
- **Bill Integration Details**: [BILL-INTEGRATION-COMPLETE-GUIDE.md](./BILL-INTEGRATION-COMPLETE-GUIDE.md)
- **Payment Callback API**: [PAYMENT-CALLBACK-GUIDE.md](./PAYMENT-CALLBACK-GUIDE.md)

---

## 📱 Quick Links

### Deployment Platforms Examples
- Railway: See [DEPLOYMENT-ENV-VARIABLES.md#option-1-railwayapp](./DEPLOYMENT-ENV-VARIABLES.md#option-1-railwayapp)
- Heroku: See [DEPLOYMENT-ENV-VARIABLES.md#option-2-heroku](./DEPLOYMENT-ENV-VARIABLES.md#option-2-heroku)
- Docker: See [DEPLOYMENT-ENV-VARIABLES.md#option-3-docker-compose](./DEPLOYMENT-ENV-VARIABLES.md#option-3-docker-compose)
- Kubernetes: See [DEPLOYMENT-ENV-VARIABLES.md#option-4-kubernetes](./DEPLOYMENT-ENV-VARIABLES.md#option-4-kubernetes)

---

## ✅ Final Checklist

Sebelum deploy, pastikan:

- [ ] Semua 7 environment variables sudah di-set
- [ ] Database accessible dari deployment environment
- [ ] Bill Service URL correct & accessible
- [ ] API Keys match dengan Bill Service configuration
- [ ] CORS includes production frontend URL
- [ ] SSL/TLS enabled (HTTPS)
- [ ] Tested di staging environment
- [ ] Backup database ready
- [ ] Monitoring & logging configured

---

**Status**: ✅ Ready for Production Deployment  
**Last Updated**: {{ current_date }}  
**Next Review**: After first production deployment

