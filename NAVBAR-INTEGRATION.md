# Navbar Integration - Quick Reference

## 🎯 Navbar Menu Structure

### For **Superadmin** Role:

```
Navbar:
├── Dashboard
├── Payment Methods  ← NEW
│   ├── View All Payment Methods (/admin/payment-methods)
│   └── Create Payment Method (/admin/payment-methods/create)
├── Transactions  ← NEW
│   └── View All Transactions (/admin/transactions)
├── Users (if applicable)
└── Logout
```

### For **Customer** Role:

```
Navbar:
├── Home
├── My Transactions  ← NEW (/my-transactions)
├── Top-Up Balance  ← NEW (/top-up)
├── My Profile
└── Logout
```

---

## 🔨 Quick Implementation

### Vue 3 Example - Navbar Component

```vue
<template>
  <nav class="navbar">
    <!-- Customer Menu -->
    <template v-if="userRole === 'Customer'">
      <router-link to="/">Home</router-link>
      <router-link to="/my-transactions">My Transactions</router-link>
      <router-link to="/top-up">Top-Up Balance</router-link>
      <router-link to="/profile">My Profile</router-link>
      <button @click="logout">Logout</button>
    </template>

    <!-- Superadmin Menu -->
    <template v-if="userRole === 'Superadmin'">
      <router-link to="/admin/dashboard">Dashboard</router-link>
      <router-link to="/admin/payment-methods">Payment Methods</router-link>
      <router-link to="/admin/transactions">Transactions</router-link>
      <router-link to="/admin/users">Users</router-link>
      <button @click="logout">Logout</button>
    </template>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const userRole = computed(() => authStore.user?.role)

const logout = () => {
  localStorage.removeItem('jwt_token')
  authStore.clearUser()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  display: flex;
  gap: 20px;
  padding: 15px;
  background: #333;
  color: white;
}

.navbar a {
  color: white;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 4px;
}

.navbar a:hover,
.navbar a.router-link-active {
  background: #555;
}

.navbar button {
  margin-left: auto;
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
```

---

## 📋 Pages to Create

### Superadmin Pages:

1. **`/admin/payment-methods`**
   - List all payment methods
   - Sortable table (methodName, provider)
   - Filter by status
   - Actions: Update Status, Delete

2. **`/admin/payment-methods/create`**
   - Form to create new payment method
   - Fields: methodName, provider

3. **`/admin/transactions`**
   - List all top-up transactions
   - Filter by status
   - Actions: Approve, Reject, Delete

### Customer Pages:

1. **`/my-transactions`**
   - View own transactions only
   - Filter by status
   - Read-only (no actions)

2. **`/top-up`**
   - Form to create top-up request
   - Fields: Payment Method (dropdown), Amount, Proof URL

---

## 🔗 API Endpoints Summary

### Payment Methods:
```
GET    /api/payment-methods              - Get all (with optional ?status=Active)
GET    /api/payment-methods/{id}         - Get by ID
POST   /api/payment-methods              - Create (Superadmin)
PUT    /api/payment-methods/{id}/status  - Update status (Superadmin)
DELETE /api/payment-methods/{id}         - Delete (Superadmin)
```

### Transactions:
```
GET    /api/transactions                 - Get all (filtered by role)
POST   /api/transactions                 - Create (Customer)
PUT    /api/transactions/{id}/status     - Update status (Superadmin)
DELETE /api/transactions/{id}            - Delete (Superadmin)
```

---

## 🔐 Authentication

All requests need JWT token:
```javascript
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
```

Or per request:
```javascript
await axios.get('/api/payment-methods', {
  headers: { Authorization: `Bearer ${token}` }
})
```

---

## ✅ Implementation Checklist

- [ ] Update Navbar component with new links based on role
- [ ] Create Payment Methods pages (Superadmin)
  - [ ] List page with table
  - [ ] Create page with form
- [ ] Create Transactions pages
  - [ ] Customer: My Transactions (read-only)
  - [ ] Customer: Top-Up form
  - [ ] Superadmin: All Transactions with actions
- [ ] Add route guards for role-based access
- [ ] Test all pages with proper JWT authentication
- [ ] Add loading states and error handling
- [ ] Add confirmation dialogs for destructive actions

---

## 📚 Full Documentation

For detailed implementation guides:
- `FRONTEND-INTEGRATION-GUIDE.md` in `bruno-tests/Payment-Methods/`
- `FRONTEND-INTEGRATION-GUIDE.md` in `bruno-tests/Top-Up-Transactions/`

For API testing:
- Use Bruno tests in `bruno-tests/Payment-Methods/`
- Use Bruno tests in `bruno-tests/Top-Up-Transactions/`
