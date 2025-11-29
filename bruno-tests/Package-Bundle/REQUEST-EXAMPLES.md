# Contoh Request Create Bundle

## 📋 Request Format

### Endpoint
```
POST http://localhost:8080/package/create-bundle
Content-Type: application/json
```

---

## ✅ Contoh 1: Simple Request (1 Plan, 1 Activity)

```json
{
  "packageName": "Paket Weekend Getaway",
  "userId": "user-001",
  "quota": 10,
  "startDate": "2025-12-10T08:00:00",
  "endDate": "2025-12-10T18:00:00",
  "plans": [
    {
      "planName": "Morning Beach Activity",
      "activityType": "Outdoor",
      "startDate": "2025-12-10T09:00:00",
      "endDate": "2025-12-10T12:00:00",
      "startLocation": "Jakarta",
      "endLocation": "Ancol",
      "orderedActivities": [
        {
          "activityId": "ACT-001",
          "orderedQuantity": 10
        }
      ]
    }
  ]
}
```

**Expected Response:**
```json
{
  "status": 201,
  "message": "Package bundle created successfully",
  "timestamp": "2025-11-28T...",
  "data": {
    "packageId": "PACK-user-001-001",
    "packageName": "Paket Weekend Getaway",
    "createdPlans": [
      {
        "planId": "uuid-generated",
        "planName": "Morning Beach Activity",
        "activityType": "Outdoor",
        "status": "Fulfilled",
        "planPrice": 500000,
        "activitiesCount": 1,
        "totalOrderedQuantity": 10
      }
    ],
    "totalPrice": 500000,
    "status": "Pending"
  }
}
```

---

## ✅ Contoh 2: Multiple Activities per Plan

```json
{
  "packageName": "Paket Full Day Adventure",
  "userId": "user-002",
  "quota": 20,
  "startDate": "2025-12-15T06:00:00",
  "endDate": "2025-12-15T22:00:00",
  "plans": [
    {
      "planName": "Adventure Day",
      "activityType": "Outdoor",
      "startDate": "2025-12-15T08:00:00",
      "endDate": "2025-12-15T20:00:00",
      "startLocation": "Bandung",
      "endLocation": "Bandung",
      "orderedActivities": [
        {
          "activityId": "ACT-RAFTING-001",
          "orderedQuantity": 10
        },
        {
          "activityId": "ACT-CAMPING-001",
          "orderedQuantity": 10
        }
      ]
    }
  ]
}
```

---

## ✅ Contoh 3: Multiple Plans (3 Hari)

```json
{
  "packageName": "Paket Tour Bali 3 Hari 2 Malam",
  "userId": "user-bali-123",
  "quota": 15,
  "startDate": "2025-12-20T08:00:00",
  "endDate": "2025-12-22T20:00:00",
  "plans": [
    {
      "planName": "Day 1 - Arrival & Beach",
      "activityType": "Outdoor",
      "startDate": "2025-12-20T14:00:00",
      "endDate": "2025-12-20T18:00:00",
      "startLocation": "Denpasar",
      "endLocation": "Kuta",
      "orderedActivities": [
        {
          "activityId": "ACT-BEACH-001",
          "orderedQuantity": 15
        }
      ]
    },
    {
      "planName": "Day 2 - Cultural Tour",
      "activityType": "Cultural",
      "startDate": "2025-12-21T09:00:00",
      "endDate": "2025-12-21T17:00:00",
      "startLocation": "Kuta",
      "endLocation": "Ubud",
      "orderedActivities": [
        {
          "activityId": "ACT-TEMPLE-001",
          "orderedQuantity": 8
        },
        {
          "activityId": "ACT-MUSEUM-001",
          "orderedQuantity": 7
        }
      ]
    },
    {
      "planName": "Day 3 - Water Sports",
      "activityType": "Outdoor",
      "startDate": "2025-12-22T08:00:00",
      "endDate": "2025-12-22T15:00:00",
      "startLocation": "Ubud",
      "endLocation": "Nusa Dua",
      "orderedActivities": [
        {
          "activityId": "ACT-DIVING-001",
          "orderedQuantity": 15
        }
      ]
    }
  ]
}
```

---

## ✅ Contoh 4: Unfulfilled Plan (Quota Tidak Penuh)

```json
{
  "packageName": "Paket Flexible",
  "userId": "user-flex-001",
  "quota": 20,
  "startDate": "2025-12-25T08:00:00",
  "endDate": "2025-12-25T18:00:00",
  "plans": [
    {
      "planName": "Morning Activity",
      "activityType": "Outdoor",
      "startDate": "2025-12-25T09:00:00",
      "endDate": "2025-12-25T12:00:00",
      "startLocation": "Jakarta",
      "endLocation": "Jakarta",
      "orderedActivities": [
        {
          "activityId": "ACT-GYM-001",
          "orderedQuantity": 8
        }
      ]
    }
  ]
}
```

**Note:** Plan akan memiliki status "Unfulfilled" karena ordered quantity (8) < package quota (20)

---

## 🚫 Contoh Error Scenarios

### Error 1: Ordered Quantity Melebihi Package Quota
```json
{
  "packageName": "Test Over Quota",
  "userId": "user-error",
  "quota": 10,
  "startDate": "2025-12-10T08:00:00",
  "endDate": "2025-12-10T18:00:00",
  "plans": [
    {
      "planName": "Plan Over Quota",
      "activityType": "Outdoor",
      "startDate": "2025-12-10T09:00:00",
      "endDate": "2025-12-10T12:00:00",
      "startLocation": "Jakarta",
      "endLocation": "Jakarta",
      "orderedActivities": [
        {
          "activityId": "ACT-001",
          "orderedQuantity": 15
        }
      ]
    }
  ]
}
```
**Expected Error:** `"Cannot add activity. Total ordered quantity in this plan (15) would exceed package quota (10)"`

### Error 2: End Date Sebelum Start Date
```json
{
  "packageName": "Test Invalid Date",
  "userId": "user-error",
  "quota": 10,
  "startDate": "2025-12-10T18:00:00",
  "endDate": "2025-12-10T08:00:00",
  "plans": []
}
```
**Expected Error:** `"End date cannot be earlier than start date"`

### Error 3: Activity Not Found
```json
{
  "packageName": "Test Activity Not Found",
  "userId": "user-error",
  "quota": 10,
  "startDate": "2025-12-10T08:00:00",
  "endDate": "2025-12-10T18:00:00",
  "plans": [
    {
      "planName": "Test Plan",
      "activityType": "Outdoor",
      "startDate": "2025-12-10T09:00:00",
      "endDate": "2025-12-10T12:00:00",
      "startLocation": "Jakarta",
      "endLocation": "Jakarta",
      "orderedActivities": [
        {
          "activityId": "ACT-NOTEXIST",
          "orderedQuantity": 5
        }
      ]
    }
  ]
}
```
**Expected Error:** `"Activity not found with id: ACT-NOTEXIST"`

---

## 📝 Using CURL

### Simple Request
```bash
curl -X POST http://localhost:8080/package/create-bundle \
  -H "Content-Type: application/json" \
  -d '{
    "packageName": "Paket Test CURL",
    "userId": "user-curl-001",
    "quota": 10,
    "startDate": "2025-12-10T08:00:00",
    "endDate": "2025-12-10T18:00:00",
    "plans": [
      {
        "planName": "Test Plan",
        "activityType": "Outdoor",
        "startDate": "2025-12-10T09:00:00",
        "endDate": "2025-12-10T12:00:00",
        "startLocation": "Jakarta",
        "endLocation": "Jakarta",
        "orderedActivities": [
          {
            "activityId": "ACT-001",
            "orderedQuantity": 10
          }
        ]
      }
    ]
  }'
```

### With Pretty Print
```bash
curl -X POST http://localhost:8080/package/create-bundle \
  -H "Content-Type: application/json" \
  -d @request.json | jq '.'
```

---

## 🎯 Tips

1. **Package Quota:** Total ordered quantity di SATU plan harus <= package quota untuk plan tersebut bisa Fulfilled
2. **Date Validation:** Activity dates harus dalam range plan dates, plan dates harus dalam range package dates
3. **Activity Type:** Activity type harus sama dengan plan activity type
4. **Locations:** Activity start/end locations harus sama dengan plan start/end locations
5. **Status:**
   - Package status selalu dimulai sebagai "Pending"
   - Plan status "Fulfilled" jika ordered quantity >= package quota
   - Plan status "Unfulfilled" jika ordered quantity < package quota

---

## 🔗 Related Endpoints

- `GET /package` - List all packages
- `GET /package/{id}` - Get package detail with plans
- `POST /package/create` - Create package only
- `POST /package/{id}/plans/create` - Add plan to existing package
- `POST /package/{id}/process` - Process package (reduce activity capacity)
