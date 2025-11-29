# ✅ **UPDATE PLAN - IMPLEMENTATION SUMMARY**

## 📋 **OVERVIEW**

Implementasi lengkap fitur **Update Plan** dengan Role-Based Access Control (RBAC).

---

## 🎯 **BUSINESS RULES IMPLEMENTED**

### **Authorization Matrix:**

| Role | Can Update Plan? | Restriction |
|------|-----------------|-------------|
| **Customer** | ✅ Own packages only | ❌ Cannot update plans from other customer's packages |
| **Superadmin** | ✅ All plans | No restriction |
| **TourPackageVendor** | ✅ All plans | No restriction |

### **Update Conditions:**
1. ✅ Package status must be **"Pending"**
2. ✅ Plan must not have active OrderedQuantities
3. ✅ `startDate` < `endDate`
4. ✅ `price` > 0

### **Editable Fields:**
- ✅ `planName`
- ✅ `startDate`
- ✅ `endDate`
- ✅ `startLocation`
- ✅ `endLocation`
- ✅ `price`

### **Readonly Fields:**
- ❌ `packageId` (cannot move plan to another package)
- ❌ `activityType` (fixed after creation)

---

## 🔧 **FILES MODIFIED/CREATED**

### **Backend Changes:**

#### **1. PlanResponseDTO.java** ✅
**Location:** `src/main/java/.../restdto/response/PlanResponseDTO.java`

**Changes:**
```java
// Added fields for frontend RBAC
private String packageName;
private String packageStatus;
private String packageUserId; // ✅ For Customer authorization check
private String creatorRole; // ✅ For tracking

// Updated fromEntity() method
public static PlanResponseDTO fromEntity(Plan plan) {
    // ... existing code ...
    
    // Get package info if available
    String packageUserId = null;
    String creatorRole = null;
    
    if (plan.getTourPackage() != null) {
        packageUserId = plan.getTourPackage().getUserId();
        creatorRole = plan.getTourPackage().getCreatorRole();
    }
    
    return PlanResponseDTO.builder()
        // ... existing fields ...
        .packageName(packageName)
        .packageStatus(packageStatus)
        .packageUserId(packageUserId) // ✅ NEW
        .creatorRole(creatorRole) // ✅ NEW
        .build();
}
```

**Impact:**
- Frontend can now perform client-side authorization checks
- No additional API calls needed to check ownership

---

#### **2. PlanRestController.java** ✅ (Already Implemented)
**Location:** `src/main/java/.../restcontroller/PlanRestController.java`

**Endpoint:** `PUT /api/plans/{id}/edit`

**Authorization Logic:**
```java
@PutMapping("/{id}/edit")
public ResponseEntity<BaseResponseDTO<PlanResponseDTO>> updatePlan(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePlanRequestDTO request,
        @AuthenticationPrincipal AuthenticatedUser user,
        BindingResult bindingResult) {
    
    // 1. Get existing plan
    Plan existingPlan = planRestService.getPlanById(id);
    
    // 2. Authorization check for Customer
    if (!user.hasAdminPrivileges()) {
        String packageUserId = existingPlan.getTourPackage().getUserId();
        
        if (!packageUserId.equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(BaseResponseDTO.<PlanResponseDTO>builder()
                            .status(403)
                            .message("Access denied: You can only update plans from your own packages")
                            .build());
        }
    }
    
    // 3. Update plan
    Plan plan = planRestService.updatePlan(id, request);
    
    return ResponseEntity.ok()
            .body(BaseResponseDTO.<PlanResponseDTO>builder()
                    .status(200)
                    .message("Plan updated successfully")
                    .data(PlanResponseDTO.fromEntity(plan))
                    .build());
}
```

**Status:** ✅ Already implemented in previous session

---

#### **3. PlanRestServiceImpl.java** ✅ (Already Implemented)
**Location:** `src/main/java/.../restservice/PlanRestServiceImpl.java`

**Validations:**
```java
@Override
public Plan updatePlan(UUID id, UpdatePlanRequestDTO request) {
    Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
    
    // ✅ Validate: Package must be Pending
    if (!"Pending".equals(plan.getTourPackage().getStatus())) {
        throw new RuntimeException(
            "Cannot update plan. Package status must be 'Pending', current status: " + 
            plan.getTourPackage().getStatus()
        );
    }
    
    // ✅ Validate: No active ordered quantities
    long activeOQ = plan.getOrderedQuantities().stream()
            .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
            .count();
    
    if (activeOQ > 0) {
        throw new RuntimeException(
            "Cannot update plan. Plan has active ordered activities"
        );
    }
    
    // ✅ Validate: price > 0
    if (request.getPrice() <= 0) {
        throw new IllegalArgumentException("Price must be greater than 0");
    }
    
    // ✅ Validate: startDate < endDate
    if (!request.getEndDate().isAfter(request.getStartDate())) {
        throw new IllegalArgumentException("End date must be after start date");
    }
    
    // Update fields
    plan.setPlanName(request.getPlanName());
    plan.setStartDate(request.getStartDate());
    plan.setEndDate(request.getEndDate());
    plan.setStartLocation(request.getStartLocation());
    plan.setEndLocation(request.getEndLocation());
    plan.setPrice(request.getPrice());
    
    return planRepository.save(plan);
}
```

**Status:** ✅ Already implemented in previous session

---

### **Frontend Changes:**

#### **1. PlanEditView.vue** ✅ NEW
**Location:** `src/views/PlanEditView.vue`

**Features:**
- ✅ Readonly fields: `packageName`, `activityType`, `packageStatus`
- ✅ Editable fields: `planName`, `startDate`, `endDate`, `startLocation`, `endLocation`, `price`
- ✅ Client-side validation:
  - Required fields check
  - `startDate` < `endDate`
  - `price` > 0
- ✅ Real-time validation on blur
- ✅ Authorization check (Customer: own only)
- ✅ Package status check (Pending only)
- ✅ Loading states & error handling
- ✅ Success notification → Redirect to plan detail

**Key Components:**
```vue
<template>
  <div class="plan-edit-container">
    <!-- Readonly Section -->
    <div class="form-section">
      <h3>📦 Package Information (Cannot be changed)</h3>
      <input :value="plan.packageName" disabled />
      <input :value="plan.activityType" disabled />
    </div>

    <!-- Editable Section -->
    <div class="form-section">
      <h3>✏️ Plan Details</h3>
      <input v-model="formData.planName" @blur="validateField('planName')" />
      <input v-model="formData.startDate" type="datetime-local" />
      <!-- ... other fields ... -->
    </div>

    <!-- Actions -->
    <button type="submit" :disabled="!canSubmit || submitting">
      Update Plan
    </button>
  </div>
</template>
```

**Validation Logic:**
```typescript
function validateField(fieldName: string) {
  switch (fieldName) {
    case 'startDate':
      if (new Date(formData.value.startDate) >= new Date(formData.value.endDate)) {
        errors.value.startDate = 'Start date must be before end date'
      }
      break
    case 'price':
      if (formData.value.price <= 0) {
        errors.value.price = 'Price must be greater than 0'
      }
      break
  }
}
```

**Authorization:**
```typescript
async function loadPlan() {
  const fetchedPlan = await planStore.getPlanDetail(planId)
  
  // Customer: Check ownership
  if (userRole === 'Customer' && 
      fetchedPlan.packageUserId && 
      fetchedPlan.packageUserId !== userId) {
    error.value = '🚫 Access denied: You can only edit plans from your own packages'
    return
  }
  
  // Check status
  if (fetchedPlan.packageStatus !== 'Pending') {
    error.value = `⚠️ Cannot edit plan: Package status is "${fetchedPlan.packageStatus}"`
    return
  }
}
```

---

#### **2. ViewPlanView.vue** ✅ Modified
**Location:** `src/views/ViewPlanView.vue`

**Changes:**
```vue
<!-- Before -->
<button @click="$router.push(`/plans/${plan.id}/edit`)">
  Edit Plan
</button>

<!-- After -->
<button 
  v-if="canEditPlan"
  @click="$router.push(`/plans/${plan.id}/edit`)"
  :disabled="plan.packageStatus !== 'Pending'"
>
  Edit Plan
</button>
```

**Added Computed:**
```typescript
const canEditPlan = computed(() => {
  if (!plan.value) return false
  
  // Superadmin & TourPackageVendor can edit all plans
  if (userRole.value === 'Superadmin' || userRole.value === 'TourPackageVendor') {
    return true
  }
  
  // Customer: Show button, auth check happens in EditPlanView
  if (userRole.value === 'Customer') {
    return true
  }
  
  return false
})
```

---

#### **3. plan.interface.ts** ✅ Modified
**Location:** `src/interfaces/plan.interface.ts`

**Changes:**
```typescript
// Added field to UpdatePlanRequest
export interface UpdatePlanRequest {
  planName: string
  startDate: string
  endDate: string
  startLocation: string
  endLocation: string
  price: number // ✅ Added
}

// Added field to PlanDetail
export interface PlanDetail {
  // ... existing fields ...
  packageUserId?: string // ✅ Added for RBAC
}
```

---

#### **4. plan.ts (Store)** ✅ Already Implemented
**Location:** `src/stores/plan.ts`

**Method:**
```typescript
async updatePlan(planId: string, data: UpdatePlanRequest) {
  try {
    const requestData = {
      ...data,
      startDate: toLocalDateTimeString(data.startDate),
      endDate: toLocalDateTimeString(data.endDate),
    }

    const res = await axios.put(`${BASE_URL}plans/${planId}/edit`, requestData)
    return res.data.data
  } catch (e: any) {
    this.error = e.response?.data?.message || e.message
    throw e
  }
}
```

**Status:** ✅ Already implemented

---

#### **5. Router (index.ts)** ✅ Already Configured
**Location:** `src/router/index.ts`

**Route:**
```typescript
{
  path: '/plans/:id/edit',
  name: 'EditPlan',
  component: () => import('@/views/PlanEditView.vue'),
}
```

**Status:** ✅ Already configured

---

## 🧪 **TESTING GUIDE**

### **Test Case 1: Customer Updates Own Plan**
**Steps:**
1. Login as Customer (e.g., `customer@example.com`)
2. Navigate to Package List → Click own package
3. Click on a Plan → Click "Edit Plan" button
4. Modify fields:
   - Plan Name: "Updated Plan Name"
   - Price: 5000000
5. Click "Update Plan"

**Expected:**
- ✅ Form loads successfully
- ✅ Readonly fields are disabled (packageName, activityType)
- ✅ Submit succeeds
- ✅ Success notification appears
- ✅ Redirects to plan detail page
- ✅ Updated data displayed

---

### **Test Case 2: Customer Tries to Edit Other Customer's Plan**
**Steps:**
1. Login as Customer1
2. Get plan ID from Customer2's package (e.g., via database/logs)
3. Navigate to `/plans/{customer2-plan-id}/edit`

**Expected:**
- ❌ Error message: "🚫 Access denied: You can only edit plans from your own packages"
- ❌ Form not displayed
- ✅ Can navigate back

---

### **Test Case 3: Admin/Vendor Edits Any Plan**
**Steps:**
1. Login as Superadmin or TourPackageVendor
2. Navigate to any plan (including customer's)
3. Click "Edit Plan"
4. Modify fields → Submit

**Expected:**
- ✅ Form loads successfully
- ✅ Can edit any plan regardless of creator
- ✅ Submit succeeds

---

### **Test Case 4: Validation - Invalid Dates**
**Steps:**
1. Login as any user
2. Edit a plan
3. Set `startDate` = 2025-12-31
4. Set `endDate` = 2025-01-01 (before start)
5. Try to submit

**Expected:**
- ❌ Error message: "End date must be after start date"
- ❌ Submit button disabled
- ✅ Can fix and retry

---

### **Test Case 5: Validation - Invalid Price**
**Steps:**
1. Edit a plan
2. Set `price` = 0
3. Try to submit

**Expected:**
- ❌ Error message: "Price must be greater than 0"
- ❌ Submit button disabled

---

### **Test Case 6: Cannot Edit Non-Pending Package Plan**
**Steps:**
1. Login as Customer
2. Process a package (status → Accepted/Processed)
3. Try to navigate to `/plans/{id}/edit`

**Expected:**
- ❌ Error message: "⚠️ Cannot edit plan: Package status is 'Accepted'"
- ❌ Form not displayed
- ✅ Edit button disabled in ViewPlanView

---

### **Test Case 7: Readonly Fields Cannot Be Modified**
**Steps:**
1. Open Edit Plan form
2. Try to modify:
   - Package Name
   - Activity Type

**Expected:**
- ✅ Fields are disabled (gray background)
- ✅ Cannot type or modify
- ✅ Submit only updates editable fields

---

## 📊 **AUTHORIZATION FLOW DIAGRAM**

```
User clicks "Edit Plan" in ViewPlanView
                |
                v
        Navigate to /plans/{id}/edit
                |
                v
    PlanEditView → loadPlan()
                |
                v
    Fetch plan detail from API
                |
                v
        +-------+-------+
        |               |
    Customer?       Admin/Vendor?
        |               |
        v               v
Check packageUserId   No check needed
 == user.id?          (can edit all)
        |               |
        v               v
   Match?             Success
    /   \               |
  Yes    No             |
   |      |             |
   v      v             v
Success  403 Error   Load Form
```

---

## 🚀 **DEPLOYMENT CHECKLIST**

### **Backend:**
- [x] PlanResponseDTO updated with packageUserId
- [x] PlanRestController authorization implemented
- [x] PlanRestServiceImpl validations complete
- [ ] Test API endpoints with Postman/Bruno
- [ ] Deploy to staging

### **Frontend:**
- [x] PlanEditView.vue created
- [x] ViewPlanView.vue updated with canEditPlan
- [x] plan.interface.ts updated
- [x] Router configured
- [ ] Test all validation scenarios
- [ ] Test RBAC for all roles
- [ ] Deploy to staging

---

## 📝 **RELATED DOCUMENTATION**

- **RBAC Complete Guide:** `RBAC-COMPLETE-GUIDE.md`
- **Customer Access Control:** `CUSTOMER-ACCESS-CONTROL-IMPLEMENTATION.md`
- **JWT Implementation:** `JWT-IMPLEMENTATION-COMPLETE.md`

---

**Implementation Date:** November 29, 2025  
**Status:** ✅ Complete  
**Version:** 1.0.0
