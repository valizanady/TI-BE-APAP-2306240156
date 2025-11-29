# Payment Methods - Frontend Integration Guide

## 📋 Overview
This guide explains how to integrate Payment Methods management into the Superadmin navbar and create the required pages.

---

## 🎯 Requirements

### 1. **Read All Payment Methods** (Superadmin)
**Route:** `/payment-methods` or `/admin/payment-methods`

**Features:**
- Display all payment methods in a table with columns:
  - ID
  - Method Name
  - Provider
  - Status
  - Actions (Update Status, Delete)
- Sortable columns: `methodName`, `provider` (ascending/descending)
- Filter by status dropdown (Active/Inactive)
- Can use DataTables or similar library

**API Endpoint:**
```
GET /api/payment-methods
GET /api/payment-methods?status=Active (filter by status)
```

---

### 2. **Create Payment Method** (Superadmin)
**Route:** `/payment-methods/create` or `/admin/payment-methods/create`

**Features:**
- Form with fields:
  - Method Name (required, max 100 chars)
  - Provider (required, max 100 chars)
- Submit button
- Success/error messages

**API Endpoint:**
```
POST /api/payment-methods
Content-Type: application/json

Request Body:
{
  "methodName": "Bank Transfer",
  "provider": "BCA"
}
```

---

### 3. **Update Payment Method Status** (Superadmin)
**Location:** Button in Actions column on Read All page

**Features:**
- Toggle button or dropdown to change status between "Active" and "Inactive"
- Confirmation dialog before changing status
- Success/error messages

**API Endpoint:**
```
PUT /api/payment-methods/{id}/status
Content-Type: application/json

Request Body:
{
  "status": "Active"  // or "Inactive"
}
```

---

### 4. **Delete Payment Method** (Superadmin)
**Location:** Button in Actions column on Read All page

**Features:**
- Delete button (trash icon)
- Confirmation dialog before deletion
- Success/error messages

**API Endpoint:**
```
DELETE /api/payment-methods/{id}
```

---

## 🔌 API Documentation

### 1. Get All Payment Methods
```http
GET /api/payment-methods
Authorization: Bearer {JWT_TOKEN}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment methods retrieved successfully",
  "timestamp": "2025-11-28T23:30:00.000+07:00",
  "data": [
    {
      "id": "uuid-here",
      "methodName": "Bank Transfer",
      "provider": "BCA",
      "status": "Active",
      "createdAt": "2025-11-28T10:00:00.000+07:00",
      "updatedAt": null,
      "deletedAt": null
    },
    {
      "id": "uuid-here-2",
      "methodName": "E-Wallet",
      "provider": "GoPay",
      "status": "Inactive",
      "createdAt": "2025-11-28T11:00:00.000+07:00",
      "updatedAt": "2025-11-28T12:00:00.000+07:00",
      "deletedAt": null
    }
  ]
}
```

### 2. Get Payment Methods by Status (Filter)
```http
GET /api/payment-methods?status=Active
Authorization: Bearer {JWT_TOKEN}
```

**Response:** Same as above, but filtered

---

### 3. Create Payment Method
```http
POST /api/payment-methods
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "methodName": "Credit Card",
  "provider": "Visa"
}
```

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Payment method created successfully",
  "timestamp": "2025-11-28T23:30:00.000+07:00",
  "data": {
    "id": "new-uuid",
    "methodName": "Credit Card",
    "provider": "Visa",
    "status": "Active",
    "createdAt": "2025-11-28T23:30:00.000+07:00",
    "updatedAt": null,
    "deletedAt": null
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Method name is required",
  "timestamp": "2025-11-28T23:30:00.000+07:00",
  "data": null
}
```

---

### 4. Update Payment Method Status
```http
PUT /api/payment-methods/{id}/status
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "status": "Inactive"
}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment method status updated successfully",
  "timestamp": "2025-11-28T23:30:00.000+07:00",
  "data": {
    "id": "uuid-here",
    "methodName": "Bank Transfer",
    "provider": "BCA",
    "status": "Inactive",
    "createdAt": "2025-11-28T10:00:00.000+07:00",
    "updatedAt": "2025-11-28T23:30:00.000+07:00",
    "deletedAt": null
  }
}
```

---

### 5. Delete Payment Method
```http
DELETE /api/payment-methods/{id}
Authorization: Bearer {JWT_TOKEN}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Payment method deleted successfully",
  "timestamp": "2025-11-28T23:30:00.000+07:00",
  "data": null
}
```

---

## 🎨 Frontend Implementation Example (Vue 3)

### 1. Add to Navbar (for Superadmin)
```vue
<!-- components/Navbar.vue -->
<template>
  <nav v-if="userRole === 'Superadmin'">
    <router-link to="/admin/payment-methods">Payment Methods</router-link>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const userRole = computed(() => authStore.user?.role)
</script>
```

---

### 2. Payment Methods List Page
```vue
<!-- pages/admin/PaymentMethods.vue -->
<template>
  <div class="payment-methods">
    <h1>Payment Methods Management</h1>
    
    <!-- Filter -->
    <div class="filters">
      <select v-model="statusFilter" @change="fetchPaymentMethods">
        <option value="">All Status</option>
        <option value="Active">Active</option>
        <option value="Inactive">Inactive</option>
      </select>
      
      <router-link to="/admin/payment-methods/create" class="btn-create">
        + Create Payment Method
      </router-link>
    </div>

    <!-- Table -->
    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th @click="sort('methodName')">
            Method Name
            <span v-if="sortField === 'methodName'">
              {{ sortOrder === 'asc' ? '↑' : '↓' }}
            </span>
          </th>
          <th @click="sort('provider')">
            Provider
            <span v-if="sortField === 'provider'">
              {{ sortOrder === 'asc' ? '↑' : '↓' }}
            </span>
          </th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="method in sortedPaymentMethods" :key="method.id">
          <td>{{ method.id.substring(0, 8) }}...</td>
          <td>{{ method.methodName }}</td>
          <td>{{ method.provider }}</td>
          <td>
            <span :class="['badge', method.status.toLowerCase()]">
              {{ method.status }}
            </span>
          </td>
          <td class="actions">
            <button 
              @click="toggleStatus(method)" 
              class="btn-toggle"
              :class="method.status === 'Active' ? 'deactivate' : 'activate'"
            >
              {{ method.status === 'Active' ? 'Deactivate' : 'Activate' }}
            </button>
            <button @click="deleteMethod(method)" class="btn-delete">
              🗑️ Delete
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

const paymentMethods = ref([])
const statusFilter = ref('')
const sortField = ref('')
const sortOrder = ref('asc')

// Fetch payment methods
const fetchPaymentMethods = async () => {
  try {
    const token = localStorage.getItem('jwt_token')
    const url = statusFilter.value 
      ? `/api/payment-methods?status=${statusFilter.value}`
      : '/api/payment-methods'
    
    const response = await axios.get(url, {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    paymentMethods.value = response.data.data
  } catch (error) {
    console.error('Error fetching payment methods:', error)
    alert('Failed to fetch payment methods')
  }
}

// Sort functionality
const sort = (field) => {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = 'asc'
  }
}

const sortedPaymentMethods = computed(() => {
  if (!sortField.value) return paymentMethods.value
  
  return [...paymentMethods.value].sort((a, b) => {
    const aVal = a[sortField.value].toLowerCase()
    const bVal = b[sortField.value].toLowerCase()
    
    if (sortOrder.value === 'asc') {
      return aVal > bVal ? 1 : -1
    } else {
      return aVal < bVal ? 1 : -1
    }
  })
})

// Toggle status
const toggleStatus = async (method) => {
  const newStatus = method.status === 'Active' ? 'Inactive' : 'Active'
  
  if (!confirm(`Are you sure you want to change status to ${newStatus}?`)) {
    return
  }
  
  try {
    const token = localStorage.getItem('jwt_token')
    await axios.put(
      `/api/payment-methods/${method.id}/status`,
      { status: newStatus },
      { headers: { Authorization: `Bearer ${token}` } }
    )
    
    alert('Status updated successfully!')
    fetchPaymentMethods()
  } catch (error) {
    console.error('Error updating status:', error)
    alert('Failed to update status')
  }
}

// Delete payment method
const deleteMethod = async (method) => {
  if (!confirm(`Are you sure you want to delete ${method.methodName}?`)) {
    return
  }
  
  try {
    const token = localStorage.getItem('jwt_token')
    await axios.delete(`/api/payment-methods/${method.id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    alert('Payment method deleted successfully!')
    fetchPaymentMethods()
  } catch (error) {
    console.error('Error deleting payment method:', error)
    alert('Failed to delete payment method')
  }
}

onMounted(() => {
  fetchPaymentMethods()
})
</script>

<style scoped>
.payment-methods {
  padding: 20px;
}

.filters {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  cursor: pointer;
  background: #f5f5f5;
  padding: 12px;
  text-align: left;
}

.data-table td {
  padding: 12px;
  border-bottom: 1px solid #ddd;
}

.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.badge.active {
  background: #d4edda;
  color: #155724;
}

.badge.inactive {
  background: #f8d7da;
  color: #721c24;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-toggle, .btn-delete {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-toggle.activate {
  background: #28a745;
  color: white;
}

.btn-toggle.deactivate {
  background: #ffc107;
  color: black;
}

.btn-delete {
  background: #dc3545;
  color: white;
}
</style>
```

---

### 3. Create Payment Method Page
```vue
<!-- pages/admin/CreatePaymentMethod.vue -->
<template>
  <div class="create-payment-method">
    <h1>Create Payment Method</h1>
    
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="methodName">Method Name *</label>
        <input
          id="methodName"
          v-model="form.methodName"
          type="text"
          maxlength="100"
          required
          placeholder="e.g., Bank Transfer, Credit Card"
        />
      </div>

      <div class="form-group">
        <label for="provider">Provider *</label>
        <input
          id="provider"
          v-model="form.provider"
          type="text"
          maxlength="100"
          required
          placeholder="e.g., BCA, Visa, GoPay"
        />
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-submit">Create</button>
        <router-link to="/admin/payment-methods" class="btn-cancel">
          Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const form = ref({
  methodName: '',
  provider: ''
})

const handleSubmit = async () => {
  try {
    const token = localStorage.getItem('jwt_token')
    await axios.post('/api/payment-methods', form.value, {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    alert('Payment method created successfully!')
    router.push('/admin/payment-methods')
  } catch (error) {
    console.error('Error creating payment method:', error)
    const message = error.response?.data?.message || 'Failed to create payment method'
    alert(message)
  }
}
</script>

<style scoped>
.create-payment-method {
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn-submit {
  padding: 10px 24px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-cancel {
  padding: 10px 24px;
  background: #6c757d;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}
</style>
```

---

## 🚀 Routes Configuration

Add these routes to your Vue Router:

```javascript
// router/index.js
const routes = [
  // ... other routes
  {
    path: '/admin/payment-methods',
    name: 'PaymentMethods',
    component: () => import('@/pages/admin/PaymentMethods.vue'),
    meta: { requiresAuth: true, role: 'Superadmin' }
  },
  {
    path: '/admin/payment-methods/create',
    name: 'CreatePaymentMethod',
    component: () => import('@/pages/admin/CreatePaymentMethod.vue'),
    meta: { requiresAuth: true, role: 'Superadmin' }
  }
]
```

---

## ✅ Checklist

- [ ] Add "Payment Methods" link to Superadmin navbar
- [ ] Create Payment Methods list page with table
- [ ] Implement sorting for methodName and provider columns
- [ ] Add status filter dropdown
- [ ] Create "Create Payment Method" page with form
- [ ] Implement Update Status button with confirmation
- [ ] Implement Delete button with confirmation
- [ ] Test all endpoints with JWT authentication
- [ ] Add loading states and error handling
- [ ] Style pages to match your design system

---

## 🔐 Authentication

All API calls require JWT token in Authorization header:
```javascript
headers: {
  'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
}
```

---

## 📞 Support

For API issues or questions, check:
- `PaymentMethodRestController.java` - Controller implementation
- `bruno-tests/Payment-Methods/` - API test examples
- `bruno-tests/Payment-Methods/README.md` - Detailed API documentation
