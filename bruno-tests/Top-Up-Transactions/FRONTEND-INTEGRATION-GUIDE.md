# Top-Up Transactions - Frontend Integration Guide

## 📋 Overview
This guide explains how to integrate Top-Up Transactions management into both Customer and Superadmin interfaces.

---

## 🎯 Requirements

### For **Customer** Role:

#### 1. **Create Top-Up Transaction**
**Route:** `/transactions/create` or `/top-up`

**Features:**
- Form with fields:
  - Customer ID (auto-filled from logged-in user)
  - Payment Method (dropdown, only Active methods)
  - Amount (number input, minimum validation)
  - Proof of Payment (file upload - image)
- Submit button
- Success/error messages

**API Endpoint:**
```
POST /api/transactions
```

---

#### 2. **View My Transactions**
**Route:** `/my-transactions`

**Features:**
- Display only customer's own transactions
- Table with columns:
  - Transaction ID
  - Payment Method
  - Amount
  - Status (Pending/Success/Rejected)
  - Proof URL
  - Created At
- Filter by status
- Cannot edit or delete

**API Endpoint:**
```
GET /api/transactions (automatically filtered by customer ID)
```

---

### For **Superadmin** Role:

#### 1. **View All Transactions**
**Route:** `/admin/transactions`

**Features:**
- Display all transactions from all customers
- Table with columns:
  - Transaction ID
  - Customer Username
  - Payment Method
  - Amount
  - Status
  - Proof URL
  - Created At
  - Actions (Approve/Reject/View Details/Delete)
- Filter by status
- Sortable columns

**API Endpoint:**
```
GET /api/transactions (shows all transactions for Superadmin)
```

---

#### 2. **Update Transaction Status (Approve/Reject)**
**Location:** Button in Actions column

**Features:**
- Approve button (changes status to "Success" and adds balance to customer)
- Reject button (changes status to "Rejected")
- Confirmation dialog before action
- Success/error messages
- **Important:** Balance only added once when status changes to "Success"

**API Endpoint:**
```
PUT /api/transactions/{id}/status
```

---

#### 3. **Delete Transaction**
**Location:** Button in Actions column

**Features:**
- Delete button
- Confirmation dialog
- Success/error messages

**API Endpoint:**
```
DELETE /api/transactions/{id}
```

---

## 🔌 API Documentation

### 1. Get All Transactions
```http
GET /api/transactions
Authorization: Bearer {JWT_TOKEN}
```

**For Customer:** Returns only their own transactions
**For Superadmin:** Returns all transactions

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transactions retrieved successfully",
  "data": [
    {
      "id": "uuid-here",
      "customerId": "customer-uuid",
      "customerUsername": "customer1",
      "paymentMethod": {
        "id": "method-uuid",
        "methodName": "Bank Transfer",
        "provider": "BCA"
      },
      "amount": 100000,
      "status": "Pending",
      "proofUrl": "https://example.com/proof.jpg",
      "createdAt": "2025-11-28T10:00:00.000+07:00"
    }
  ]
}
```

---

### 2. Create Transaction (Customer Only)
```http
POST /api/transactions
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "customerId": "uuid-from-jwt",
  "paymentMethodId": "payment-method-uuid",
  "amount": 100000,
  "proofUrl": "https://example.com/proof.jpg"
}
```

**Success Response (201 Created):**
```json
{
  "status": 201,
  "message": "Top-up transaction created successfully",
  "data": {
    "id": "new-uuid",
    "customerId": "customer-uuid",
    "customerUsername": "customer1",
    "paymentMethod": {
      "id": "method-uuid",
      "methodName": "Bank Transfer",
      "provider": "BCA"
    },
    "amount": 100000,
    "status": "Pending",
    "proofUrl": "https://example.com/proof.jpg",
    "createdAt": "2025-11-28T23:30:00.000+07:00"
  }
}
```

**Validation Errors:**
- Amount must be >= 10000
- Payment method must exist and be Active
- Proof URL is required

---

### 3. Update Transaction Status (Superadmin Only)
```http
PUT /api/transactions/{id}/status
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "status": "Success"  // or "Rejected"
}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transaction status updated successfully",
  "data": {
    "id": "uuid-here",
    "status": "Success",
    // ... other fields
  }
}
```

**Important Notes:**
- When status changes to "Success", the amount is automatically added to customer's balance
- Balance is only added ONCE (even if you call this endpoint multiple times with "Success")
- When status changes to "Rejected", no balance is added

---

### 4. Delete Transaction (Superadmin Only)
```http
DELETE /api/transactions/{id}
Authorization: Bearer {JWT_TOKEN}
```

**Success Response (200 OK):**
```json
{
  "status": 200,
  "message": "Transaction deleted successfully",
  "data": null
}
```

---

## 🎨 Frontend Implementation Example (Vue 3)

### 1. Customer - Create Transaction Page
```vue
<!-- pages/customer/CreateTransaction.vue -->
<template>
  <div class="create-transaction">
    <h1>Top-Up Balance</h1>
    
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>Customer ID</label>
        <input :value="currentUserId" disabled />
        <small>Auto-filled from your account</small>
      </div>

      <div class="form-group">
        <label for="paymentMethod">Payment Method *</label>
        <select 
          id="paymentMethod"
          v-model="form.paymentMethodId"
          required
        >
          <option value="">-- Select Payment Method --</option>
          <option 
            v-for="method in paymentMethods" 
            :key="method.id"
            :value="method.id"
          >
            {{ method.methodName }} - {{ method.provider }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="amount">Amount *</label>
        <input
          id="amount"
          v-model.number="form.amount"
          type="number"
          min="10000"
          required
          placeholder="Minimum: Rp 10,000"
        />
      </div>

      <div class="form-group">
        <label for="proofUrl">Proof of Payment (URL) *</label>
        <input
          id="proofUrl"
          v-model="form.proofUrl"
          type="url"
          required
          placeholder="https://example.com/proof.jpg"
        />
        <small>Upload your proof to an image hosting service and paste the URL here</small>
      </div>

      <button type="submit" class="btn-submit" :disabled="loading">
        {{ loading ? 'Creating...' : 'Submit Top-Up Request' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const paymentMethods = ref([])

const currentUserId = computed(() => authStore.user?.id)

const form = ref({
  customerId: '',
  paymentMethodId: '',
  amount: 10000,
  proofUrl: ''
})

const fetchPaymentMethods = async () => {
  try {
    const token = localStorage.getItem('jwt_token')
    const response = await axios.get('/api/payment-methods?status=Active', {
      headers: { Authorization: `Bearer ${token}` }
    })
    paymentMethods.value = response.data.data
  } catch (error) {
    console.error('Error fetching payment methods:', error)
  }
}

const handleSubmit = async () => {
  loading.value = true
  
  try {
    const token = localStorage.getItem('jwt_token')
    form.value.customerId = currentUserId.value
    
    await axios.post('/api/transactions', form.value, {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    alert('Top-up request submitted successfully! Please wait for admin approval.')
    router.push('/my-transactions')
  } catch (error) {
    console.error('Error creating transaction:', error)
    const message = error.response?.data?.message || 'Failed to create transaction'
    alert(message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPaymentMethods()
})
</script>
```

---

### 2. Customer - My Transactions Page
```vue
<!-- pages/customer/MyTransactions.vue -->
<template>
  <div class="my-transactions">
    <h1>My Top-Up Transactions</h1>
    
    <div class="filters">
      <select v-model="statusFilter" @change="fetchTransactions">
        <option value="">All Status</option>
        <option value="Pending">Pending</option>
        <option value="Success">Success</option>
        <option value="Rejected">Rejected</option>
      </select>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Payment Method</th>
          <th>Amount</th>
          <th>Status</th>
          <th>Proof</th>
          <th>Created At</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="tx in transactions" :key="tx.id">
          <td>{{ tx.id.substring(0, 8) }}...</td>
          <td>{{ tx.paymentMethod.methodName }} - {{ tx.paymentMethod.provider }}</td>
          <td>Rp {{ tx.amount.toLocaleString() }}</td>
          <td>
            <span :class="['badge', tx.status.toLowerCase()]">
              {{ tx.status }}
            </span>
          </td>
          <td>
            <a :href="tx.proofUrl" target="_blank">View</a>
          </td>
          <td>{{ formatDate(tx.createdAt) }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const transactions = ref([])
const statusFilter = ref('')

const fetchTransactions = async () => {
  try {
    const token = localStorage.getItem('jwt_token')
    const response = await axios.get('/api/transactions', {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    let data = response.data.data
    
    // Apply status filter
    if (statusFilter.value) {
      data = data.filter(tx => tx.status === statusFilter.value)
    }
    
    transactions.value = data
  } catch (error) {
    console.error('Error fetching transactions:', error)
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('id-ID')
}

onMounted(() => {
  fetchTransactions()
})
</script>

<style scoped>
.badge.pending {
  background: #ffc107;
  color: black;
}

.badge.success {
  background: #28a745;
  color: white;
}

.badge.rejected {
  background: #dc3545;
  color: white;
}
</style>
```

---

### 3. Superadmin - All Transactions Page
```vue
<!-- pages/admin/Transactions.vue -->
<template>
  <div class="all-transactions">
    <h1>All Top-Up Transactions</h1>
    
    <div class="filters">
      <select v-model="statusFilter" @change="fetchTransactions">
        <option value="">All Status</option>
        <option value="Pending">Pending</option>
        <option value="Success">Success</option>
        <option value="Rejected">Rejected</option>
      </select>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Customer</th>
          <th>Payment Method</th>
          <th>Amount</th>
          <th>Status</th>
          <th>Proof</th>
          <th>Created At</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="tx in transactions" :key="tx.id">
          <td>{{ tx.id.substring(0, 8) }}...</td>
          <td>{{ tx.customerUsername }}</td>
          <td>{{ tx.paymentMethod.methodName }} - {{ tx.paymentMethod.provider }}</td>
          <td>Rp {{ tx.amount.toLocaleString() }}</td>
          <td>
            <span :class="['badge', tx.status.toLowerCase()]">
              {{ tx.status }}
            </span>
          </td>
          <td>
            <a :href="tx.proofUrl" target="_blank">View</a>
          </td>
          <td>{{ formatDate(tx.createdAt) }}</td>
          <td class="actions">
            <button 
              v-if="tx.status === 'Pending'"
              @click="updateStatus(tx.id, 'Success')" 
              class="btn-approve"
            >
              ✓ Approve
            </button>
            <button 
              v-if="tx.status === 'Pending'"
              @click="updateStatus(tx.id, 'Rejected')" 
              class="btn-reject"
            >
              ✗ Reject
            </button>
            <button 
              @click="deleteTransaction(tx.id)" 
              class="btn-delete"
            >
              🗑️ Delete
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const transactions = ref([])
const statusFilter = ref('')

const fetchTransactions = async () => {
  try {
    const token = localStorage.getItem('jwt_token')
    const response = await axios.get('/api/transactions', {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    let data = response.data.data
    
    if (statusFilter.value) {
      data = data.filter(tx => tx.status === statusFilter.value)
    }
    
    transactions.value = data
  } catch (error) {
    console.error('Error fetching transactions:', error)
  }
}

const updateStatus = async (id, newStatus) => {
  const action = newStatus === 'Success' ? 'approve' : 'reject'
  
  if (!confirm(`Are you sure you want to ${action} this transaction?`)) {
    return
  }
  
  try {
    const token = localStorage.getItem('jwt_token')
    await axios.put(
      `/api/transactions/${id}/status`,
      { status: newStatus },
      { headers: { Authorization: `Bearer ${token}` } }
    )
    
    alert(`Transaction ${action}ed successfully!`)
    if (newStatus === 'Success') {
      alert('Customer balance has been updated.')
    }
    fetchTransactions()
  } catch (error) {
    console.error('Error updating status:', error)
    alert(`Failed to ${action} transaction`)
  }
}

const deleteTransaction = async (id) => {
  if (!confirm('Are you sure you want to delete this transaction?')) {
    return
  }
  
  try {
    const token = localStorage.getItem('jwt_token')
    await axios.delete(`/api/transactions/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    
    alert('Transaction deleted successfully!')
    fetchTransactions()
  } catch (error) {
    console.error('Error deleting transaction:', error)
    alert('Failed to delete transaction')
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('id-ID')
}

onMounted(() => {
  fetchTransactions()
})
</script>
```

---

## 🚀 Routes Configuration

```javascript
// router/index.js
const routes = [
  // Customer routes
  {
    path: '/top-up',
    name: 'CreateTransaction',
    component: () => import('@/pages/customer/CreateTransaction.vue'),
    meta: { requiresAuth: true, role: 'Customer' }
  },
  {
    path: '/my-transactions',
    name: 'MyTransactions',
    component: () => import('@/pages/customer/MyTransactions.vue'),
    meta: { requiresAuth: true, role: 'Customer' }
  },
  
  // Superadmin routes
  {
    path: '/admin/transactions',
    name: 'AllTransactions',
    component: () => import('@/pages/admin/Transactions.vue'),
    meta: { requiresAuth: true, role: 'Superadmin' }
  }
]
```

---

## ✅ Checklist

### Customer Interface:
- [ ] Add "Top-Up" link to Customer navbar
- [ ] Create Top-Up form page with payment method dropdown
- [ ] Add form validation (min amount 10000)
- [ ] Create "My Transactions" page to view own transactions
- [ ] Add status filter

### Superadmin Interface:
- [ ] Add "Transactions" link to Superadmin navbar
- [ ] Create All Transactions page
- [ ] Implement Approve/Reject buttons
- [ ] Add confirmation dialogs
- [ ] Implement Delete button
- [ ] Add status filter

### General:
- [ ] Test JWT authentication on all endpoints
- [ ] Add loading states
- [ ] Add error handling
- [ ] Test balance update on approval (should only happen once)

---

## 🔐 Authentication

All API calls require JWT token:
```javascript
headers: {
  'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
}
```

---

## 📝 Important Notes

1. **Balance Update Logic:**
   - Balance is ONLY added when status changes from non-Success to "Success"
   - If you approve the same transaction multiple times, balance is added only once
   - This is handled automatically by the backend

2. **Customer Restrictions:**
   - Customers can only see their own transactions
   - Customers cannot update status or delete transactions
   - Customers can only create transactions for themselves

3. **Superadmin Access:**
   - Can view all transactions from all customers
   - Can approve/reject pending transactions
   - Can delete any transaction

---

## 📞 Support

For API details:
- `TopUpTransactionRestController.java` - Controller
- `bruno-tests/Top-Up-Transactions/` - API tests
- `bruno-tests/Top-Up-Transactions/README.md` - Documentation
