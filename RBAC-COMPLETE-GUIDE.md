# 🔐 **COMPLETE RBAC GUIDE - Tour Package Management System**

## 📋 **OVERVIEW**

Dokumentasi lengkap Role-Based Access Control (RBAC) untuk semua fitur dalam Tour Package Management System.

### **Roles:**
1. **Customer** - End user yang membuat dan mengelola package sendiri
2. **Superadmin** - Full access ke semua fitur
3. **TourPackageVendor** - Vendor yang menyediakan tour packages

---

## 🎯 **AUTHORIZATION MATRIX**

| Feature | Customer | Superadmin | TourPackageVendor | Notes |
|---------|----------|------------|-------------------|-------|
| **ACTIVITIES** |
| Filter Activities | ✅ Yes | ✅ Yes | ✅ Yes | All roles can view activities |
| **PACKAGES** |
| Get All Packages | ✅ Yes (filtered) | ✅ Yes (all) | ✅ Yes (all) | Customer: own + admin/vendor packages |
| Detail Package | ✅ Yes (own + admin/vendor) | ✅ Yes | ✅ Yes | Customer: 403 for other customer's packages |
| Create Package | ✅ Yes | ✅ Yes | ✅ Yes | All roles can create |
| Update Package | ✅ Yes (own only) | ✅ Yes (all) | ✅ Yes (all) | Customer: own packages only |
| Delete Package | ✅ Yes (own + Pending) | ✅ Yes (Pending) | ✅ Yes (Pending) | Only Pending packages |
| Process Package | ✅ Yes (own only) | ❌ No | ❌ No | **Customer ONLY** |
| **PLANS** |
| Get All Plans by Package | ✅ Yes | ✅ Yes | ✅ Yes | Inherited from package access |
| Detail Plan | ✅ Yes | ✅ Yes | ✅ Yes | Inherited from package access |
| Create Plan | ✅ Yes (own pkg) | ✅ Yes | ✅ Yes | Must own package (Customer) |
| Update Plan | ✅ Yes (own pkg) | ✅ Yes (all) | ✅ Yes (all) | **NEW: Customer own only** |
| Delete Plan | ✅ Yes (own pkg) | ✅ Yes | ✅ Yes | Must own package (Customer) |
| **ORDERED ACTIVITIES** |
| Get All OrderedActivities by Plan | ✅ Yes | ✅ Yes | ✅ Yes | Inherited from plan access |
| Create OrderedActivities | ✅ Yes (own plan) | ✅ Yes | ✅ Yes | Must own plan (Customer) |
| Update OrderedActivities Quota | ✅ Yes (own plan) | ✅ Yes | ✅ Yes | Must own plan (Customer) |
| Delete OrderedActivities | ✅ Yes (own plan) | ✅ Yes | ✅ Yes | Must own plan (Customer) |
| **REVENUE STATISTICS** |
| Get Revenue Statistics | ❌ No | ✅ Yes | ✅ Yes | Admin/Vendor only |
| Get Yearly Revenue | ❌ No | ✅ Yes | ✅ Yes | Admin/Vendor only |
| Get Monthly Revenue | ❌ No | ✅ Yes | ✅ Yes | Admin/Vendor only |

---

## 🔧 **BACKEND IMPLEMENTATION STATUS**

### **✅ Already Implemented**

#### **1. PackageRestController**
- ✅ GET `/api/package` - Role-based filtering
- ✅ GET `/api/package/{id}` - Authorization check for Customer
- ✅ POST `/api/package/create` - Stores `creatorRole`
- ✅ PUT `/api/package/{id}/update` - Customer: own only
- ✅ DELETE `/api/package/{id}` - Customer: own only
- ✅ POST `/api/package/{id}/process` - Customer ONLY

#### **2. PlanRestController**
- ✅ GET `/api/plans/{id}` - Authorization check
- ✅ PUT `/api/plans/{id}/edit` - **Customer: own package only, Admin/Vendor: all**
- ✅ DELETE `/api/plans/{id}` - Authorization check

#### **3. OrderedActivityRestController**
- ✅ GET `/api/ordered-activities/eligible` - Authorization check
- ✅ POST `/api/ordered-activities/create` - Authorization check
- ✅ PUT `/api/ordered-activities/{id}` - Authorization check
- ✅ DELETE `/api/ordered-activities/{id}` - Authorization check

---

## 📝 **UPDATE PLAN - DETAILED GUIDE**

### **🎯 Business Rules**

#### **Access Control:**
| Role | Can Update | Restriction |
|------|-----------|-------------|
| **Customer** | ✅ Own packages only | ❌ Cannot update plans from other customer's packages |
| **Superadmin** | ✅ All plans | No restriction |
| **TourPackageVendor** | ✅ All plans | No restriction |

#### **Update Conditions:**
1. ✅ Package status must be **"Pending"**
2. ✅ Plan must not have active OrderedQuantities
3. ✅ `startDate` < `endDate`
4. ✅ `price` > 0

#### **Editable Fields:**
- ✅ `planName`
- ✅ `startDate`
- ✅ `endDate`
- ✅ `startLocation`
- ✅ `endLocation`
- ✅ `price`

#### **Readonly Fields:**
- ❌ `packageId` (cannot move plan to another package)
- ❌ `activityType` (fixed after creation)

---

### **🔧 Backend Implementation**

#### **Endpoint:** `PUT /api/plans/{id}/edit`

**Authorization Flow:**
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
        // Customer: Check ownership
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

**Service Layer Validation:**
```java
@Override
public Plan updatePlan(UUID id, UpdatePlanRequestDTO request) {
    Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
    
    // Validate: Package must be Pending
    if (!"Pending".equals(plan.getTourPackage().getStatus())) {
        throw new RuntimeException("Cannot update plan. Package status must be 'Pending'");
    }
    
    // Validate: No active ordered quantities
    long activeOQ = plan.getOrderedQuantities().stream()
            .filter(oq -> !Boolean.TRUE.equals(oq.getIsDeleted()))
            .count();
    
    if (activeOQ > 0) {
        throw new RuntimeException("Cannot update plan. Plan has active ordered activities");
    }
    
    // Validate: price > 0
    if (request.getPrice() <= 0) {
        throw new IllegalArgumentException("Price must be greater than 0");
    }
    
    // Validate: startDate < endDate
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

---

### **💻 Frontend Implementation**

#### **A. PlanEditView.vue - Complete Component**

```vue
<template>
  <div class="plan-edit-container">
    <!-- Header -->
    <div class="page-header">
      <h1>Edit Plan</h1>
      <button @click="router.back()" class="btn btn-secondary">
        ← Back
      </button>
    </div>

    <!-- Error State -->
    <div v-if="error" class="alert alert-error">
      {{ error }}
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      Loading plan data...
    </div>

    <!-- Edit Form -->
    <form v-if="plan && !loading" @submit.prevent="handleSubmit" class="plan-form">
      
      <!-- Readonly Fields -->
      <div class="form-section">
        <h3>Package Information (Cannot be changed)</h3>
        
        <div class="form-group">
          <label>Package Name</label>
          <input 
            type="text" 
            :value="plan.packageName" 
            disabled 
            class="form-input readonly"
          />
        </div>

        <div class="form-group">
          <label>Activity Type</label>
          <input 
            type="text" 
            :value="plan.activityType" 
            disabled 
            class="form-input readonly"
          />
        </div>

        <div class="form-group">
          <label>Package Status</label>
          <input 
            type="text" 
            :value="plan.packageStatus" 
            disabled 
            class="form-input readonly"
          />
        </div>
      </div>

      <!-- Editable Fields -->
      <div class="form-section">
        <h3>Plan Details</h3>
        
        <div class="form-group" :class="{ 'has-error': errors.planName }">
          <label>Plan Name <span class="required">*</span></label>
          <input 
            v-model="formData.planName"
            type="text"
            class="form-input"
            required
            @blur="validateField('planName')"
          />
          <span v-if="errors.planName" class="error-text">{{ errors.planName }}</span>
        </div>

        <div class="form-group" :class="{ 'has-error': errors.startDate }">
          <label>Start Date <span class="required">*</span></label>
          <input 
            v-model="formData.startDate"
            type="datetime-local"
            class="form-input"
            required
            @blur="validateField('startDate')"
          />
          <span v-if="errors.startDate" class="error-text">{{ errors.startDate }}</span>
        </div>

        <div class="form-group" :class="{ 'has-error': errors.endDate }">
          <label>End Date <span class="required">*</span></label>
          <input 
            v-model="formData.endDate"
            type="datetime-local"
            class="form-input"
            required
            @blur="validateField('endDate')"
          />
          <span v-if="errors.endDate" class="error-text">{{ errors.endDate }}</span>
        </div>

        <div class="form-group">
          <label>Start Location</label>
          <input 
            v-model="formData.startLocation"
            type="text"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label>End Location</label>
          <input 
            v-model="formData.endLocation"
            type="text"
            class="form-input"
          />
        </div>

        <div class="form-group" :class="{ 'has-error': errors.price }">
          <label>Price <span class="required">*</span></label>
          <input 
            v-model.number="formData.price"
            type="number"
            class="form-input"
            required
            min="1"
            @blur="validateField('price')"
          />
          <span v-if="errors.price" class="error-text">{{ errors.price }}</span>
        </div>
      </div>

      <!-- Actions -->
      <div class="form-actions">
        <button type="button" @click="router.back()" class="btn btn-secondary" :disabled="submitting">
          Cancel
        </button>
        <button 
          type="submit" 
          class="btn btn-primary"
          :disabled="!canSubmit || submitting"
        >
          {{ submitting ? 'Updating...' : 'Update Plan' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePlanStore } from '@/stores/plan'
import { useAuthStore } from '@/stores/auth'
import type { Plan } from '@/interfaces/plan.interface'

const route = useRoute()
const router = useRouter()
const planStore = usePlanStore()
const authStore = useAuthStore()

// State
const plan = ref<Plan | null>(null)
const loading = ref(true)
const error = ref('')
const submitting = ref(false)

// Form Data
const formData = ref({
  planName: '',
  startDate: '',
  endDate: '',
  startLocation: '',
  endLocation: '',
  price: 0,
})

// Validation Errors
const errors = ref<Record<string, string>>({})

// Computed
const canSubmit = computed(() => {
  if (!plan.value) return false
  
  // Package must be Pending
  if (plan.value.packageStatus !== 'Pending') return false
  
  // No validation errors
  if (Object.keys(errors.value).length > 0) return false
  
  // All required fields filled
  return formData.value.planName && 
         formData.value.startDate && 
         formData.value.endDate && 
         formData.value.price > 0
})

// Methods
async function loadPlan() {
  try {
    loading.value = true
    error.value = ''
    
    const planId = route.params.id as string
    const fetchedPlan = await planStore.getPlanById(planId)
    
    if (!fetchedPlan) {
      throw new Error('Plan not found')
    }
    
    // Authorization check
    const userId = authStore.user?.id
    const userRole = authStore.user?.role
    
    // Customer can only edit own package's plans
    if (userRole === 'Customer' && fetchedPlan.packageUserId !== userId) {
      error.value = 'Access denied: You can only edit plans from your own packages'
      return
    }
    
    // Check if package is Pending
    if (fetchedPlan.packageStatus !== 'Pending') {
      error.value = `Cannot edit plan: Package status is ${fetchedPlan.packageStatus}. Only plans in Pending packages can be edited.`
      return
    }
    
    plan.value = fetchedPlan
    
    // Populate form
    formData.value = {
      planName: fetchedPlan.planName,
      startDate: formatDateForInput(fetchedPlan.startDate),
      endDate: formatDateForInput(fetchedPlan.endDate),
      startLocation: fetchedPlan.startLocation || '',
      endLocation: fetchedPlan.endLocation || '',
      price: fetchedPlan.price || 0,
    }
    
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Failed to load plan'
    console.error('Error loading plan:', err)
  } finally {
    loading.value = false
  }
}

function formatDateForInput(dateString: string): string {
  return dateString.substring(0, 16)
}

function validateField(fieldName: string) {
  delete errors.value[fieldName]
  
  switch (fieldName) {
    case 'planName':
      if (!formData.value.planName.trim()) {
        errors.value.planName = 'Plan name is required'
      }
      break
      
    case 'startDate':
      if (!formData.value.startDate) {
        errors.value.startDate = 'Start date is required'
      } else if (formData.value.endDate && 
                 new Date(formData.value.startDate) >= new Date(formData.value.endDate)) {
        errors.value.startDate = 'Start date must be before end date'
      } else {
        delete errors.value.endDate
      }
      break
      
    case 'endDate':
      if (!formData.value.endDate) {
        errors.value.endDate = 'End date is required'
      } else if (formData.value.startDate && 
                 new Date(formData.value.endDate) <= new Date(formData.value.startDate)) {
        errors.value.endDate = 'End date must be after start date'
      }
      break
      
    case 'price':
      if (formData.value.price <= 0) {
        errors.value.price = 'Price must be greater than 0'
      }
      break
  }
}

function validateAllFields(): boolean {
  errors.value = {}
  
  validateField('planName')
  validateField('startDate')
  validateField('endDate')
  validateField('price')
  
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validateAllFields()) {
    alert('Please fix validation errors')
    return
  }
  
  try {
    submitting.value = true
    
    const planId = route.params.id as string
    
    await planStore.updatePlan(planId, {
      planName: formData.value.planName,
      startDate: formData.value.startDate,
      endDate: formData.value.endDate,
      startLocation: formData.value.startLocation,
      endLocation: formData.value.endLocation,
      price: formData.value.price,
    })
    
    alert('✅ Plan updated successfully!')
    router.push(`/plans/${planId}`)
    
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to update plan'
    alert(`❌ ${errorMessage}`)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadPlan()
})
</script>

<style scoped>
.plan-edit-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.alert {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.alert-error {
  background-color: #fee;
  border: 1px solid #fcc;
  color: #c33;
}

.loading {
  text-align: center;
  padding: 48px;
  color: #666;
}

.plan-form {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.form-section {
  margin-bottom: 32px;
}

.form-section h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e0e0e0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
}

.required {
  color: #e53e3e;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #4a90e2;
}

.form-input.readonly {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.has-error .form-input {
  border-color: #e53e3e;
}

.error-text {
  display: block;
  color: #e53e3e;
  font-size: 12px;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 24px;
  border-top: 1px solid #e0e0e0;
}

.btn {
  padding: 10px 24px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-primary {
  background-color: #4a90e2;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #357abd;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #666;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
```

#### **B. Update Pinia Store**

```typescript
// src/stores/plan.ts

async function updatePlan(planId: string, updateData: any) {
  try {
    const response = await api.put(`/plans/${planId}/edit`, updateData)
    
    if (response.data.status === 200) {
      // Update local state
      const index = items.value.findIndex(p => p.id === planId)
      if (index !== -1) {
        items.value[index] = response.data.data
      }
      
      return response.data.data
    } else {
      throw new Error(response.data.message || 'Failed to update plan')
    }
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.message || 'Failed to update plan'
    throw new Error(errorMessage)
  }
}
```

#### **C. Add Router Route**

```typescript
// src/router/index.ts

{
  path: '/plans/:id/edit',
  name: 'plan-edit',
  component: () => import('@/views/PlanEditView.vue'),
  meta: { requiresAuth: true }
}
```

---

## 🧪 **TESTING GUIDE**

### **Test Case 1: Customer Updates Own Plan**
- Login as Customer
- Navigate to own package's plan
- Click Edit → Should load form
- Modify fields → Submit → Success

### **Test Case 2: Customer Tries to Edit Other's Plan**
- Login as Customer1
- Try to access `/plans/{planId}/edit` (plan from Customer2)
- Should show "Access denied" error

### **Test Case 3: Admin/Vendor Edit Any Plan**
- Login as Superadmin or Vendor
- Navigate to any plan → Click Edit
- Should load form successfully
- Submit → Success

### **Test Case 4: Validation Tests**
- startDate >= endDate → Error
- price <= 0 → Error
- Empty required fields → Error

---

**Last Updated:** November 29, 2025  
**Version:** 1.0.0
