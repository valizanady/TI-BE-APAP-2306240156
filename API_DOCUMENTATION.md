# Tour Package API Documentation
**Base URL:** `/api`  
**Last Updated:** December 1, 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Common Response Format](#common-response-format)
4. [Endpoints](#endpoints)
   - [Authentication Endpoints](#authentication-endpoints)
   - [Activity Endpoints](#activity-endpoints)
   - [Package Endpoints](#package-endpoints)
   - [Plan Endpoints](#plan-endpoints)
   - [Ordered Activity Endpoints](#ordered-activity-endpoints)
   - [Statistics Endpoints](#statistics-endpoints)
   - [Top-Up Transaction Endpoints](#top-up-transaction-endpoints)
   - [Payment Method Endpoints](#payment-method-endpoints)
5. [Error Codes](#error-codes)

---

## Overview

The Tour Package API is a RESTful service for managing tour packages, activities, plans, and transactions. It supports role-based access control (RBAC) with different permissions for:

- **Superadmin**: Full access to all endpoints
- **Customer**: Can create/manage packages and top-up transactions
- **TourPackageVendor**: Can create/manage activities and packages
- **Other Vendors** (FlightAirline, AccommodationOwner, RentalVendor): Can create/manage activities

---

## Authentication

### Authentication Methods

1. **JWT Bearer Token**: Include in Authorization header
   ```
   Authorization: Bearer <jwt_token>
   ```

2. **OTT Exchange**: First-time authentication requires exchanging a One-Time Token (OTT) for a JWT

### Roles and Permissions

| Role | Description |
|------|-------------|
| `Superadmin` | Full system access |
| `Customer` | Can book packages, manage personal transactions |
| `TourPackageVendor` | Can create packages and activities |
| `FlightAirline` | Can create flight activities |
| `AccommodationOwner` | Can create accommodation activities |
| `RentalVendor` | Can create rental activities |

---

## Common Response Format

All API responses follow this structure:

```json
{
  "status": 200,
  "message": "Success message",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    // Response payload
  }
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `status` | integer | HTTP status code |
| `message` | string | Human-readable message |
| `timestamp` | string | Server timestamp (Asia/Jakarta timezone) |
| `data` | object/array | Response payload (null for errors) |

---

## Endpoints

## Authentication Endpoints

### 1. Exchange OTT for JWT

**Endpoint:** `POST /api/auth/exchange`

**Description:** Exchange a One-Time Token (OTT) from the profile service for a JWT access token.

**Authentication:** None required

**Request Body:**
```json
{
  "ott": "string"
}
```

**Request Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `ott` | string | Yes | One-Time Token from profile service |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Token exchange successful",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Error Responses:**

- **400 Bad Request:** OTT is missing or empty
- **401 Unauthorized:** OTT is invalid or expired
- **500 Internal Server Error:** Server error during token exchange

---

## Activity Endpoints

### 1. Get All Activities

**Endpoint:** `GET /api/activities`

**Description:** Retrieve all activities with optional filters and search capabilities.

**Authentication:** JWT required

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `isDeleted` | boolean | No | Filter by deletion status (default: shows all) |
| `activityType` | string | No | Filter by activity type (Flight, Accommodation, Rental, TourPackage) |
| `startDate` | datetime | No | Filter activities starting after this date (ISO 8601) |
| `endDate` | datetime | No | Filter activities ending before this date (ISO 8601) |
| `search` | string | No | Search in activityName, activityItem, startLocation, endLocation |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Activities retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": [
    {
      "activityId": "uuid-string",
      "activityName": "Bali Beach Tour",
      "activityItem": "Beach Visit",
      "activityType": "TourPackage",
      "capacity": 50,
      "price": 500000,
      "startDate": "2025-12-15T08:00:00Z",
      "endDate": "2025-12-15T17:00:00Z",
      "startLocation": "Denpasar",
      "endLocation": "Kuta Beach",
      "vendorId": "vendor-uuid",
      "isDeleted": false,
      "createdAt": "2025-11-01T10:00:00+07:00",
      "updatedAt": "2025-11-01T10:00:00+07:00"
    }
  ]
}
```

### 2. Get Activity by ID

**Endpoint:** `GET /api/activities/{id}`

**Description:** Retrieve detailed information about a specific activity.

**Authentication:** JWT required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Activity ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Activity retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "activityId": "uuid-string",
    "activityName": "Bali Beach Tour",
    "activityItem": "Beach Visit",
    "activityType": "TourPackage",
    "capacity": 50,
    "price": 500000,
    "startDate": "2025-12-15T08:00:00Z",
    "endDate": "2025-12-15T17:00:00Z",
    "startLocation": "Denpasar",
    "endLocation": "Kuta Beach",
    "vendorId": "vendor-uuid",
    "isDeleted": false
  }
}
```

**Error Responses:**

- **404 Not Found:** Activity not found or is deleted

### 3. Create Activity

**Endpoint:** `POST /api/activities`

**Description:** Create a new activity. Only vendors can create activities.

**Authentication:** JWT required

**Access:** Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor

**Request Body:**
```json
{
  "activityName": "Bali Beach Tour",
  "activityItem": "Beach Visit",
  "activityType": "TourPackage",
  "capacity": 50,
  "price": 500000,
  "startDate": "2025-12-15T08:00:00.000Z",
  "endDate": "2025-12-15T17:00:00.000Z",
  "startLocation": "Denpasar",
  "endLocation": "Kuta Beach"
}
```

**Request Fields:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `activityName` | string | Yes | Not blank | Name of the activity |
| `activityItem` | string | Yes | Not blank | Specific item/service |
| `activityType` | string | Yes | Not blank | Flight, Accommodation, Rental, TourPackage |
| `capacity` | integer | Yes | > 0 | Maximum number of participants |
| `price` | number | Yes | >= 1 | Price per person |
| `startDate` | datetime | Yes | ISO 8601 | Activity start date and time |
| `endDate` | datetime | Yes | ISO 8601 | Activity end date and time |
| `startLocation` | string | Yes | Not blank | Starting location |
| `endLocation` | string | Yes | Not blank | Ending location |

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Activity created successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "activityId": "uuid-string",
    "activityName": "Bali Beach Tour",
    "activityItem": "Beach Visit",
    "activityType": "TourPackage",
    "capacity": 50,
    "price": 500000,
    "startDate": "2025-12-15T08:00:00Z",
    "endDate": "2025-12-15T17:00:00Z",
    "startLocation": "Denpasar",
    "endLocation": "Kuta Beach",
    "vendorId": "vendor-uuid",
    "isDeleted": false
  }
}
```

**Error Responses:**

- **400 Bad Request:** Validation errors
- **403 Forbidden:** User doesn't have vendor role
- **500 Internal Server Error:** Server error

### 4. Update Activity

**Endpoint:** `PUT /api/activities/{id}`

**Description:** Update an existing activity. Only the owner vendor can update.

**Authentication:** JWT required

**Access:** Activity owner or Superadmin

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Activity ID |

**Request Body:**
```json
{
  "activityName": "Updated Bali Beach Tour",
  "activityItem": "Beach Visit + Snorkeling",
  "activityType": "TourPackage",
  "capacity": 60,
  "price": 600000,
  "startDate": "2025-12-15T08:00:00.000Z",
  "endDate": "2025-12-15T18:00:00.000Z",
  "startLocation": "Denpasar",
  "endLocation": "Kuta Beach"
}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Activity updated successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "activityId": "uuid-string",
    "activityName": "Updated Bali Beach Tour",
    "capacity": 60,
    "price": 600000
    // ... other fields
  }
}
```

**Error Responses:**

- **404 Not Found:** Activity not found
- **403 Forbidden:** User is not the activity owner
- **400 Bad Request:** Validation errors

### 5. Delete Activity (Soft Delete)

**Endpoint:** `DELETE /api/activities/{id}`

**Description:** Soft delete an activity (sets `isDeleted = true`).

**Authentication:** JWT required

**Access:** Activity owner or Superadmin

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Activity ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Activity deleted successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": null
}
```

**Error Responses:**

- **404 Not Found:** Activity not found
- **403 Forbidden:** User is not the activity owner
- **400 Bad Request:** Activity is already in use by plans

---

## Package Endpoints

### 1. Get All Packages

**Endpoint:** `GET /api/package`

**Description:** Retrieve packages with role-based filtering.

**Authentication:** JWT required

**Access:** Superadmin, Customer, TourPackageVendor

**Filtering Logic:**
- **Customer**: Only packages created by them
- **TourPackageVendor**: Only packages created by them
- **Superadmin**: All packages

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Packages retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": [
    {
      "packageId": "uuid-string",
      "packageName": "Bali Adventure Package",
      "quota": 10,
      "startDate": "2025-12-20T08:00:00",
      "endDate": "2025-12-25T18:00:00",
      "userId": "user-uuid",
      "userRole": "Customer",
      "status": "Pending",
      "totalPrice": 5000000,
      "createdAt": "2025-11-01T10:00:00+07:00",
      "plans": []
    }
  ]
}
```

### 2. Get Package by ID

**Endpoint:** `GET /api/package/{id}`

**Description:** Get detailed package information with authorization check.

**Authentication:** JWT required

**Access:** Package owner or Superadmin

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Package ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Package retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "packageId": "uuid-string",
    "packageName": "Bali Adventure Package",
    "quota": 10,
    "startDate": "2025-12-20T08:00:00",
    "endDate": "2025-12-25T18:00:00",
    "userId": "user-uuid",
    "userRole": "Customer",
    "status": "Pending",
    "totalPrice": 5000000,
    "plans": [
      {
        "planId": "plan-uuid",
        "planName": "Day 1 - Beach Tour",
        "startDate": "2025-12-20T08:00:00",
        "endDate": "2025-12-20T18:00:00",
        "orderedActivities": []
      }
    ]
  }
}
```

**Error Responses:**

- **404 Not Found:** Package not found
- **403 Forbidden:** User doesn't have access to this package

### 3. Create Package

**Endpoint:** `POST /api/package/create`

**Description:** Create a new tour package. User ID and role are automatically set from JWT token.

**Authentication:** JWT required

**Access:** Superadmin, Customer, TourPackageVendor

**Request Body:**
```json
{
  "packageName": "Bali Adventure Package",
  "quota": 10,
  "startDate": "2025-12-20T08:00",
  "endDate": "2025-12-25T18:00"
}
```

**Request Fields:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `packageName` | string | Yes | Not blank | Package name |
| `quota` | integer | Yes | > 0 | Number of available slots |
| `startDate` | datetime | Yes | ISO 8601 | Package start date |
| `endDate` | datetime | Yes | ISO 8601 | Package end date |

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Package created successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "packageId": "uuid-string",
    "packageName": "Bali Adventure Package",
    "quota": 10,
    "startDate": "2025-12-20T08:00:00",
    "endDate": "2025-12-25T18:00:00",
    "userId": "user-uuid",
    "userRole": "Customer",
    "status": "Pending",
    "totalPrice": 0
  }
}
```

### 4. Update Package

**Endpoint:** `PUT /api/package/{id}/edit`

**Description:** Update an existing package with authorization check.

**Authentication:** JWT required

**Access:** Package owner or Superadmin

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Package ID |

**Request Body:**
```json
{
  "packageName": "Updated Bali Package",
  "quota": 15,
  "startDate": "2025-12-20T08:00",
  "endDate": "2025-12-26T18:00"
}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Package updated successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "packageId": "uuid-string",
    "packageName": "Updated Bali Package",
    "quota": 15
    // ... other fields
  }
}
```

### 5. Process Package

**Endpoint:** `PUT /api/package/{id}/process`

**Description:** Change package status from "Pending" to "Processed".

**Authentication:** JWT required

**Access:** Package owner or Superadmin

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Package ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Package processed successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "packageId": "uuid-string",
    "status": "Processed"
    // ... other fields
  }
}
```

### 6. Delete Package

**Endpoint:** `DELETE /api/package/{id}`

**Description:** Delete a package (soft delete).

**Authentication:** JWT required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Package ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Package deleted successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": null
}
```

### 7. Update Payment Status

**Endpoint:** `POST /api/package/payment/update`

**Description:** Update package payment status (Called by Bill Service via API Key).

**Authentication:** API Key required

**Request Body:**
```json
{
  "billId": "bill-uuid",
  "status": "Paid"
}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment status updated successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": null
}
```

---

## Plan Endpoints

### 1. Create Plan

**Endpoint:** `POST /api/package/{id}/plan/create`

**Description:** Create a new plan for a package.

**Authentication:** JWT required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Package ID |

**Request Body:**
```json
{
  "planName": "Day 1 - Beach Tour",
  "startDate": "2025-12-20T08:00",
  "endDate": "2025-12-20T18:00"
}
```

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Plan created successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "planId": "plan-uuid",
    "planName": "Day 1 - Beach Tour",
    "startDate": "2025-12-20T08:00:00",
    "endDate": "2025-12-20T18:00:00",
    "orderedActivities": []
  }
}
```

### 2. Get Plan Detail

**Endpoint:** `GET /api/plan/{id}`

**Description:** Get detailed plan information with ordered activities.

**Authentication:** JWT required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Plan ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Plan retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "planId": "plan-uuid",
    "planName": "Day 1 - Beach Tour",
    "startDate": "2025-12-20T08:00:00",
    "endDate": "2025-12-20T18:00:00",
    "orderedActivities": [
      {
        "orderedActivityId": "ordered-uuid",
        "activity": {
          "activityId": "activity-uuid",
          "activityName": "Bali Beach Tour",
          "price": 500000
        },
        "quantity": 2,
        "subtotal": 1000000
      }
    ],
    "totalPrice": 1000000
  }
}
```

### 3. Update Plan

**Endpoint:** `PUT /api/plan/{id}/edit`

**Description:** Update an existing plan.

**Authentication:** JWT required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Plan ID |

**Request Body:**
```json
{
  "planName": "Updated Day 1 - Beach + Snorkeling",
  "startDate": "2025-12-20T08:00",
  "endDate": "2025-12-20T19:00"
}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Plan updated successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "planId": "plan-uuid",
    "planName": "Updated Day 1 - Beach + Snorkeling"
    // ... other fields
  }
}
```

### 4. Delete Plan

**Endpoint:** `DELETE /api/plan/{id}`

**Description:** Delete a plan and all its ordered activities.

**Authentication:** JWT required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Plan ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Plan deleted successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": null
}
```

---

## Ordered Activity Endpoints

### 1. Get Eligible Activities

**Endpoint:** `GET /api/ordered-activities/eligible`

**Description:** Get activities that can be added to a plan (filtered by plan date range and capacity).

**Authentication:** JWT required

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `planId` | string (UUID) | Yes | Plan ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Eligible activities retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": [
    {
      "activityId": "activity-uuid",
      "activityName": "Bali Beach Tour",
      "activityType": "TourPackage",
      "capacity": 50,
      "price": 500000,
      "startDate": "2025-12-15T08:00:00Z",
      "endDate": "2025-12-15T17:00:00Z",
      "startLocation": "Denpasar",
      "endLocation": "Kuta Beach"
    }
  ]
}
```

### 2. Create Ordered Activity

**Endpoint:** `POST /api/ordered-activities`

**Description:** Add an activity to a plan with specified quantity.

**Authentication:** JWT required

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `planId` | string (UUID) | Yes | Plan ID |

**Request Body:**
```json
{
  "activityId": "activity-uuid",
  "quantity": 2
}
```

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Ordered activity created successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "orderedActivityId": "ordered-uuid",
    "activity": {
      "activityId": "activity-uuid",
      "activityName": "Bali Beach Tour",
      "price": 500000
    },
    "quantity": 2,
    "subtotal": 1000000
  }
}
```

### 3. Update Ordered Activity Quantity

**Endpoint:** `PUT /api/ordered-activities/{orderedActivityId}`

**Description:** Update the quantity of an ordered activity.

**Authentication:** JWT required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `orderedActivityId` | string (UUID) | Yes | Ordered Activity ID |

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `quantity` | integer | Yes | New quantity |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Ordered activity quantity updated successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "orderedActivityId": "ordered-uuid",
    "quantity": 3,
    "subtotal": 1500000
  }
}
```

### 4. Delete Ordered Activity

**Endpoint:** `DELETE /api/ordered-activities/{orderedActivityId}`

**Description:** Remove an activity from a plan.

**Authentication:** JWT required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `orderedActivityId` | string (UUID) | Yes | Ordered Activity ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Ordered activity deleted successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": null
}
```

---

## Statistics Endpoints

### 1. Get Revenue Statistics

**Endpoint:** `GET /api/statistics/revenue`

**Description:** Calculate revenue statistics by activity type for a specific period.

**Authentication:** JWT required

**Access:** Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `year` | integer | Yes | Year (e.g., 2025) |
| `month` | integer | No | Month (1-12). If omitted, shows yearly data |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Revenue statistics retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "totalRevenue": 50000000,
    "breakdown": [
      {
        "activityType": "TourPackage",
        "revenue": 25000000,
        "count": 50
      },
      {
        "activityType": "Flight",
        "revenue": 15000000,
        "count": 30
      },
      {
        "activityType": "Accommodation",
        "revenue": 10000000,
        "count": 20
      }
    ]
  }
}
```

### 2. Get Yearly Revenue

**Endpoint:** `GET /api/statistics/revenue/yearly/{year}`

**Description:** Get monthly revenue breakdown for an entire year.

**Authentication:** JWT required

**Access:** Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `year` | integer | Yes | Year (e.g., 2025) |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Yearly revenue retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "year": 2025,
    "totalRevenue": 600000000,
    "monthlyData": [
      {
        "month": 1,
        "monthName": "January",
        "revenue": 50000000
      },
      {
        "month": 2,
        "monthName": "February",
        "revenue": 45000000
      }
      // ... months 3-12
    ]
  }
}
```

### 3. Get Monthly Revenue

**Endpoint:** `GET /api/statistics/revenue/monthly/{year}/{month}`

**Description:** Get detailed revenue statistics for a specific month, including breakdown by activity type.

**Authentication:** JWT required

**Access:** Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `year` | integer | Yes | Year (e.g., 2025) |
| `month` | integer | Yes | Month (1-12) |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Monthly revenue retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "year": 2025,
    "month": 12,
    "monthName": "December",
    "totalRevenue": 50000000,
    "breakdown": [
      {
        "activityType": "TourPackage",
        "revenue": 25000000,
        "count": 50,
        "percentage": 50.0
      },
      {
        "activityType": "Flight",
        "revenue": 15000000,
        "count": 30,
        "percentage": 30.0
      }
    ]
  }
}
```

---

## Top-Up Transaction Endpoints

### 1. Get All Transactions

**Endpoint:** `GET /api/transactions`

**Description:** Get all top-up transactions with role-based filtering.

**Authentication:** JWT required

**Access:** Customer (own transactions), Superadmin (all transactions)

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transactions retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": [
    {
      "transactionId": "transaction-uuid",
      "userId": "user-uuid",
      "username": "john_doe",
      "paymentMethod": {
        "paymentMethodId": "payment-uuid",
        "methodName": "Bank Transfer BCA",
        "status": "Active"
      },
      "amount": 1000000,
      "proofImageUrl": "https://example.com/proof.jpg",
      "status": "Pending",
      "isDeleted": false,
      "createdAt": "2025-12-01T10:00:00+07:00",
      "updatedAt": "2025-12-01T10:00:00+07:00"
    }
  ]
}
```

### 2. Get Transaction by ID

**Endpoint:** `GET /api/transactions/{id}`

**Description:** Get transaction details by ID.

**Authentication:** JWT required

**Access:** Superadmin only

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Transaction ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transaction retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "transactionId": "transaction-uuid",
    "userId": "user-uuid",
    "username": "john_doe",
    "paymentMethod": {
      "paymentMethodId": "payment-uuid",
      "methodName": "Bank Transfer BCA"
    },
    "amount": 1000000,
    "proofImageUrl": "https://example.com/proof.jpg",
    "status": "Pending"
  }
}
```

### 3. Create Transaction

**Endpoint:** `POST /api/transactions`

**Description:** Create a new top-up transaction.

**Authentication:** JWT required

**Access:** Customer only

**Request Body:**
```json
{
  "paymentMethodId": "payment-uuid",
  "amount": 1000000,
  "proofImageUrl": "https://example.com/proof.jpg"
}
```

**Request Fields:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `paymentMethodId` | string (UUID) | Yes | Valid UUID | Payment method to use |
| `amount` | number | Yes | > 0 | Top-up amount |
| `proofImageUrl` | string | Yes | Valid URL | URL to payment proof image |

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Top-up transaction created successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "transactionId": "transaction-uuid",
    "userId": "user-uuid",
    "amount": 1000000,
    "status": "Pending",
    "createdAt": "2025-12-01T10:00:00+07:00"
  }
}
```

**Error Responses:**

- **403 Forbidden:** User is not a Customer
- **404 Not Found:** Payment method not found or inactive
- **400 Bad Request:** Validation errors

### 4. Update Transaction Status

**Endpoint:** `PUT /api/transactions/{id}/status`

**Description:** Update transaction status to Approved or Rejected.

**Authentication:** JWT required

**Access:** Superadmin only

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Transaction ID |

**Request Body:**
```json
{
  "status": "Approved"
}
```

**Request Fields:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `status` | string | Yes | "Approved" or "Rejected" | New transaction status |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transaction status updated to Approved",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "transactionId": "transaction-uuid",
    "status": "Approved",
    "updatedAt": "2025-12-01T11:00:00+07:00"
  }
}
```

**Error Responses:**

- **403 Forbidden:** User is not a Superadmin
- **404 Not Found:** Transaction not found
- **400 Bad Request:** Invalid status value

### 5. Delete Transaction

**Endpoint:** `DELETE /api/transactions/{id}`

**Description:** Soft delete a transaction (sets `isDeleted = true`).

**Authentication:** JWT required

**Access:** Superadmin only

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Transaction ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transaction deleted successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": null
}
```

---

## Payment Method Endpoints

### 1. Get All Payment Methods

**Endpoint:** `GET /api/payment-methods`

**Description:** Get all payment methods, optionally filtered by status.

**Authentication:** JWT required

**Access:** Customer, Superadmin

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | string | No | Filter by status ("Active" or "Inactive") |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment methods retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": [
    {
      "paymentMethodId": "payment-uuid",
      "methodName": "Bank Transfer BCA",
      "accountNumber": "1234567890",
      "accountHolder": "PT Tour Package",
      "status": "Active",
      "isDeleted": false,
      "createdAt": "2025-11-01T10:00:00+07:00"
    }
  ]
}
```

### 2. Get Payment Method by ID

**Endpoint:** `GET /api/payment-methods/{id}`

**Description:** Get payment method details by ID.

**Authentication:** JWT required

**Access:** Customer, Superadmin

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Payment Method ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment method retrieved successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "paymentMethodId": "payment-uuid",
    "methodName": "Bank Transfer BCA",
    "accountNumber": "1234567890",
    "accountHolder": "PT Tour Package",
    "status": "Active"
  }
}
```

### 3. Create Payment Method

**Endpoint:** `POST /api/payment-methods`

**Description:** Create a new payment method.

**Authentication:** JWT required

**Access:** Superadmin only

**Request Body:**
```json
{
  "methodName": "Bank Transfer BCA",
  "accountNumber": "1234567890",
  "accountHolder": "PT Tour Package"
}
```

**Request Fields:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `methodName` | string | Yes | Not blank | Payment method name |
| `accountNumber` | string | Yes | Not blank | Account/card number |
| `accountHolder` | string | Yes | Not blank | Account holder name |

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Payment method created successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "paymentMethodId": "payment-uuid",
    "methodName": "Bank Transfer BCA",
    "accountNumber": "1234567890",
    "accountHolder": "PT Tour Package",
    "status": "Active"
  }
}
```

### 4. Update Payment Method Status

**Endpoint:** `PUT /api/payment-methods/{id}/status`

**Description:** Update payment method status (Active/Inactive).

**Authentication:** JWT required

**Access:** Superadmin only

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Payment Method ID |

**Request Body:**
```json
{
  "status": "Inactive"
}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment method status updated successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": {
    "paymentMethodId": "payment-uuid",
    "status": "Inactive"
  }
}
```

### 5. Delete Payment Method

**Endpoint:** `DELETE /api/payment-methods/{id}`

**Description:** Soft delete a payment method.

**Authentication:** JWT required

**Access:** Superadmin only

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Payment Method ID |

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment method deleted successfully",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": null
}
```

---

## Error Codes

### Common Error Response Format

```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2025-12-01T04:55:00+07:00",
  "data": null
}
```

### HTTP Status Codes

| Code | Description | Common Causes |
|------|-------------|---------------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST |
| 400 | Bad Request | Validation errors, invalid input |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server-side error |

### Common Error Messages

| Error | Description | Solution |
|-------|-------------|----------|
| "OTT is required" | OTT not provided | Include OTT in request body |
| "Token exchange failed" | Invalid or expired OTT | Request new OTT from profile service |
| "Access denied" | User doesn't have required role | Check user permissions |
| "Activity not found" | Activity ID doesn't exist | Verify activity ID |
| "Unauthorized access to package" | User doesn't own package | Request own packages only |
| "Payment method not found or inactive" | Invalid payment method | Use active payment method |
| "Insufficient capacity" | Not enough slots available | Reduce quantity or choose different activity |

---

## Additional Notes

### Date/Time Formats

- **Request DateTime**: ISO 8601 format with timezone
  - Activity dates: `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
  - Package/Plan dates: `yyyy-MM-dd'T'HH:mm`
  
- **Response DateTime**: ISO 8601 with Asia/Jakarta timezone
  - Example: `2025-12-01T04:55:00+07:00`

