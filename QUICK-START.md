# 🚀 Quick Start Guide - Payment Methods & Transactions

## 📖 Documentation Files (Read in Order)

1. **[README.md](README.md)** ⭐ - Main entry point with overview
2. **[NAVBAR-INTEGRATION.md](NAVBAR-INTEGRATION.md)** ⭐ - Quick navbar setup (5 min read)
3. **[FRONTEND-IMPLEMENTATION-CHECKLIST.md](FRONTEND-IMPLEMENTATION-CHECKLIST.md)** ⭐ - Step-by-step checklist
4. **[bruno-tests/Payment-Methods/FRONTEND-INTEGRATION-GUIDE.md](bruno-tests/Payment-Methods/FRONTEND-INTEGRATION-GUIDE.md)** - Payment Methods details
5. **[bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md](bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md)** - Transactions details

---

## 🎯 What to Build

### For **Superadmin**:
```
Navbar:
├── Payment Methods → /admin/payment-methods
│   ├── List (table with sort & filter)
│   ├── Create form
│   ├── Update Status (toggle Active/Inactive)
│   └── Delete (with confirmation)
│
└── Transactions → /admin/transactions
    ├── View All (from all customers)
    ├── Approve (changes status + adds balance)
    ├── Reject (changes status only)
    └── Delete (with confirmation)
```

### For **Customer**:
```
Navbar:
├── Top-Up Balance → /top-up
│   └── Form (payment method, amount, proof)
│
└── My Transactions → /my-transactions
    └── View Own (read-only, with filter)
```

---

## 🔌 API Endpoints

```bash
# Authentication
POST /api/auth/exchange         # Exchange OTT for JWT

# Payment Methods
GET    /api/payment-methods              # Get all
GET    /api/payment-methods?status=Active # Filter
POST   /api/payment-methods              # Create (Superadmin)
PUT    /api/payment-methods/{id}/status  # Update status (Superadmin)
DELETE /api/payment-methods/{id}         # Delete (Superadmin)

# Transactions
GET    /api/transactions                 # Get (role-filtered)
POST   /api/transactions                 # Create (Customer)
PUT    /api/transactions/{id}/status     # Approve/Reject (Superadmin)
DELETE /api/transactions/{id}            # Delete (Superadmin)
```

---

## 🔐 Authentication

Every API request needs:
```javascript
headers: {
  'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
}
```

---

## 📝 Implementation Steps (Quick)

1. **Read** `NAVBAR-INTEGRATION.md` (5 min)
2. **Add** navbar links (15 min)
3. **Create** Payment Methods pages (1-2 hours)
   - List page with table
   - Create form page
4. **Create** Transaction pages (1-2 hours)
   - Customer: Create form + My Transactions
   - Superadmin: All Transactions with actions
5. **Add** routes to Vue Router (15 min)
6. **Test** everything (30-60 min)

**Total Time:** 4-6 hours

---

## ✅ Quick Checklist

### Phase 1: Navbar
- [ ] Add "Payment Methods" link (Superadmin)
- [ ] Add "Transactions" link (Superadmin)
- [ ] Add "Top-Up" link (Customer)
- [ ] Add "My Transactions" link (Customer)

### Phase 2: Payment Methods (Superadmin)
- [ ] List page with table (sortable, filterable)
- [ ] Create form page
- [ ] Update Status button (with confirmation)
- [ ] Delete button (with confirmation)

### Phase 3: Transactions (Customer)
- [ ] Create Top-Up form
- [ ] My Transactions list (read-only)

### Phase 4: Transactions (Superadmin)
- [ ] All Transactions list
- [ ] Approve button (with confirmation)
- [ ] Reject button (with confirmation)
- [ ] Delete button (with confirmation)

### Phase 5: Routes & Guards
- [ ] Add routes to Vue Router
- [ ] Add role-based guards

### Phase 6: Testing
- [ ] Test with Customer account
- [ ] Test with Superadmin account
- [ ] Verify balance updates on approval
- [ ] Verify role-based access control

---

## 🎨 Code Template (Copy-Paste Ready)

### Navbar (Vue 3)
```vue
<nav>
  <!-- Customer -->
  <template v-if="userRole === 'Customer'">
    <router-link to="/my-transactions">My Transactions</router-link>
    <router-link to="/top-up">Top-Up</router-link>
  </template>
  
  <!-- Superadmin -->
  <template v-if="userRole === 'Superadmin'">
    <router-link to="/admin/payment-methods">Payment Methods</router-link>
    <router-link to="/admin/transactions">Transactions</router-link>
  </template>
</nav>
```

### API Call Template
```javascript
const fetchData = async () => {
  try {
    const token = localStorage.getItem('jwt_token')
    const response = await axios.get('/api/payment-methods', {
      headers: { Authorization: `Bearer ${token}` }
    })
    data.value = response.data.data
  } catch (error) {
    console.error('Error:', error)
    alert('Failed to fetch data')
  }
}
```

---

## 🐛 Common Issues

| Issue | Solution |
|-------|----------|
| 403 Forbidden | Check JWT token and user role |
| 401 Unauthorized | JWT expired, login again |
| Data not showing | Check backend is running on localhost:8080 |
| Balance not updating | This is fixed in backend, should work automatically |

---

## 📚 Full Documentation

For detailed guides with complete code examples:
- **Payment Methods:** `bruno-tests/Payment-Methods/FRONTEND-INTEGRATION-GUIDE.md`
- **Transactions:** `bruno-tests/Top-Up-Transactions/FRONTEND-INTEGRATION-GUIDE.md`
- **Step-by-step:** `FRONTEND-IMPLEMENTATION-CHECKLIST.md`

---

## 🎓 What You'll Learn

- ✅ Role-based UI rendering
- ✅ JWT authentication with Axios
- ✅ CRUD operations with RESTful API
- ✅ Form validation
- ✅ Confirmation dialogs
- ✅ Table sorting & filtering
- ✅ Error handling
- ✅ Loading states

---

**Ready to Start? Open [NAVBAR-INTEGRATION.md](NAVBAR-INTEGRATION.md) 🚀**
