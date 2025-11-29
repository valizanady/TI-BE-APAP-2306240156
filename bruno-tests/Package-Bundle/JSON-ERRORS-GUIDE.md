# Common JSON Errors dan Solusinya

## ❌ Error 1: Trailing Comma

### SALAH ❌
```json
{
  "orderedActivities": [
    {
      "activityId": "57a5ea63-def5-4c6b-8240-aaa0eb0b422b",
      "orderedQuantity": 5
    },  ⬅️ JANGAN tambahkan comma di sini!
  ]
}
```

### BENAR ✅
```json
{
  "orderedActivities": [
    {
      "activityId": "57a5ea63-def5-4c6b-8240-aaa0eb0b422b",
      "orderedQuantity": 5
    }
  ]
}
```

---

## ❌ Error 2: Empty Object dalam Array

### SALAH ❌
```json
{
  "plans": [
    {
      "planName": "Hotel",
      "activityType": "Accommodation",
      ...
    },
    {  ⬅️ JANGAN tambahkan empty object!
      
    }
  ]
}
```

### BENAR ✅
```json
{
  "plans": [
    {
      "planName": "Hotel",
      "activityType": "Accommodation",
      ...
    }
  ]
}
```

---

## ❌ Error 3: Missing Comma

### SALAH ❌
```json
{
  "orderedActivities": [
    {
      "activityId": "ACT-001",
      "orderedQuantity": 5
    }  ⬅️ HARUS ada comma kalau ada object lain setelahnya!
    {
      "activityId": "ACT-002",
      "orderedQuantity": 5
    }
  ]
}
```

### BENAR ✅
```json
{
  "orderedActivities": [
    {
      "activityId": "ACT-001",
      "orderedQuantity": 5
    },
    {
      "activityId": "ACT-002",
      "orderedQuantity": 5
    }
  ]
}
```

---

## ✅ Template Request yang Benar

### Single Plan dengan Single Activity
```json
{
  "packageName": "Paket Wisata Jogja",
  "userId": "user-001",
  "quota": 10,
  "startDate": "2025-12-01T08:00:00",
  "endDate": "2025-12-15T18:00:00",
  "plans": [
    {
      "planName": "Hotel",
      "activityType": "Accommodation",
      "startDate": "2025-12-11T09:00:00",
      "endDate": "2025-12-12T17:00:00",
      "startLocation": "KABUPATEN SIMEULUE",
      "endLocation": "KABUPATEN SIMEULUE",
      "orderedActivities": [
        {
          "activityId": "57a5ea63-def5-4c6b-8240-aaa0eb0b422b",
          "orderedQuantity": 10
        }
      ]
    }
  ]
}
```

### Single Plan dengan Multiple Activities
```json
{
  "packageName": "Paket Wisata Jogja",
  "userId": "user-001",
  "quota": 10,
  "startDate": "2025-12-01T08:00:00",
  "endDate": "2025-12-15T18:00:00",
  "plans": [
    {
      "planName": "Hotel & Transport",
      "activityType": "Accommodation",
      "startDate": "2025-12-11T09:00:00",
      "endDate": "2025-12-12T17:00:00",
      "startLocation": "KABUPATEN SIMEULUE",
      "endLocation": "KABUPATEN SIMEULUE",
      "orderedActivities": [
        {
          "activityId": "57a5ea63-def5-4c6b-8240-aaa0eb0b422b",
          "orderedQuantity": 5
        },
        {
          "activityId": "another-activity-id",
          "orderedQuantity": 5
        }
      ]
    }
  ]
}
```

### Multiple Plans dengan Multiple Activities
```json
{
  "packageName": "Paket Wisata Jogja 3 Hari",
  "userId": "user-001",
  "quota": 10,
  "startDate": "2025-12-01T08:00:00",
  "endDate": "2025-12-15T18:00:00",
  "plans": [
    {
      "planName": "Day 1 - Hotel",
      "activityType": "Accommodation",
      "startDate": "2025-12-11T09:00:00",
      "endDate": "2025-12-12T17:00:00",
      "startLocation": "KABUPATEN SIMEULUE",
      "endLocation": "KABUPATEN SIMEULUE",
      "orderedActivities": [
        {
          "activityId": "57a5ea63-def5-4c6b-8240-aaa0eb0b422b",
          "orderedQuantity": 10
        }
      ]
    },
    {
      "planName": "Day 2 - Sightseeing",
      "activityType": "Cultural",
      "startDate": "2025-12-12T09:00:00",
      "endDate": "2025-12-13T17:00:00",
      "startLocation": "Jogja",
      "endLocation": "Jogja",
      "orderedActivities": [
        {
          "activityId": "cultural-activity-1",
          "orderedQuantity": 5
        },
        {
          "activityId": "cultural-activity-2",
          "orderedQuantity": 5
        }
      ]
    }
  ]
}
```

---

## 🔍 Tips Validasi JSON

### 1. Gunakan Online JSON Validator
- https://jsonlint.com/
- https://jsonformatter.org/

### 2. Cek di VS Code
- Install extension: "JSON Validate"
- Paste JSON dan lihat error highlight

### 3. Rules Penting:
✅ **DO:**
- Gunakan double quotes (`"`) untuk keys dan strings
- Tambahkan comma antar items kecuali item terakhir
- Pastikan semua brackets `{}` dan `[]` berpasangan

❌ **DON'T:**
- Jangan tambahkan comma setelah item terakhir (trailing comma)
- Jangan tambahkan empty objects `{}`
- Jangan lupa comma antara items (kecuali item terakhir)
- Jangan gunakan single quotes (`'`)

---

## 📝 Checklist Sebelum Send Request

- [ ] Tidak ada trailing comma
- [ ] Tidak ada empty objects
- [ ] Semua brackets berpasangan
- [ ] Gunakan double quotes
- [ ] Field names sesuai dengan DTO:
  - `packageName` ✅
  - `userId` ✅
  - `quota` ✅
  - `startDate` ✅
  - `endDate` ✅
  - `plans` ✅
    - `planName` ✅
    - `activityType` ✅
    - `startDate` ✅
    - `endDate` ✅
    - `startLocation` ✅
    - `endLocation` ✅
    - `orderedActivities` ✅
      - `activityId` ✅
      - `orderedQuantity` ✅
