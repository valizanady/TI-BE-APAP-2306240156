# 🚀 Quick Start: Bill Integration After Package Processed

## Setup dalam 3 Langkah

### 1️⃣ Setup Bill Service (Port 8081)

```bash
cd /Users/valizanadya/Documents/SMT\ 5/APAP/tugas\ individu/ti-2306165931

# TIDAK PERLU perubahan - gunakan .env yang sudah ada
# Pastikan flight.api.key sudah di-set
# Contoh: flight.api.key=my-secret-key-123

# Run
./gradlew bootRun
```

**TIDAK ADA PERUBAHAN di Bill Service!** ✅

### 2️⃣ Setup Tour Package Service (Port 8080)

```bash
cd /Users/valizanadya/Documents/SMT\ 5/APAP/tugas\ individu/tour-package-2306240156-be

# Create .env - COPY nilai flight.api.key dari Bill Service
cat > .env << EOF
BILL_SERVICE_URL=http://localhost:8081/api/bill/create
BILL_SERVICE_API_KEY=<SAMA-dengan-flight.api.key-di-bill-service>
EOF

# Run
./gradlew bootRun
```

### 3️⃣ Test Integration

```bash
# Process a package (akan otomatis create bill)
curl -X PUT http://localhost:8080/api/package/<PACKAGE_ID>/process \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## ✅ Verification

**Check logs Tour Package Service:**
```
✅ Package processed successfully!
📄 Creating Bill for processed package...
✅ Bill created successfully in Bill Service!
```

**Check logs Bill Service:**
```
✅ API Key validated for path: /api/bill/create
```

**Check bills created:**
```bash
curl http://localhost:8081/api/bill
```

## 🎯 What Happens

```
User clicks "Process Package"
         ↓
Package validated & status → "Processed"
         ↓
Tour Package calls Bill Service
   POST /api/bill/create
   Header: API-KEY: <sama-dengan-flight.api.key>
         ↓
Bill Service validates API Key (EXISTING ApiKeyFilter)
         ↓
Bill created (status: Unpaid)
         ↓
Success response to user
```

## 📁 Files Modified

**Tour Package Service (ONLY):**
- ✅ `CreateBillRequestDTO.java` - Request DTO
- ✅ `BillResponseDTO.java` - Response DTO
- ✅ `BillIntegrationService.java` - HTTP client to Bill Service (uses `API-KEY` header)
- ✅ `TourPackageRestServiceImpl.java` - Call Bill after process

**Bill Service:**
- ✅ **TIDAK ADA PERUBAHAN** - Menggunakan `ApiKeyFilter` yang sudah ada

## 🔑 Important

**API Key HARUS SAMA dengan Bill Service!**

Tour Package `.env`:
```
BILL_SERVICE_API_KEY=<nilai-dari-flight.api.key>
```

Bill Service `.env` (EXISTING):
```
flight.api.key=your-api-key-here
```

**Header yang digunakan:** `API-KEY` (bukan `x-api-key`)

---

## 🐛 Common Issues

**Connection refused:**
```bash
# Check Bill Service running
curl http://localhost:8081/api/bill
```

**401 Unauthorized:**
```bash
# Verify API Keys match
# Restart both services after .env changes
```

**Package processed but no bill:**
```bash
# Check Tour Package logs for errors
# Verify BILL_SERVICE_URL is correct
```

## 📚 Full Documentation

See: `CREATE-BILL-AFTER-PACKAGE-PROCESSED-GUIDE.md`

---

**Status:** ✅ Implementation Complete  
**Ready for Testing:** Yes  
**Integration Type:** Microservice API Key Auth
