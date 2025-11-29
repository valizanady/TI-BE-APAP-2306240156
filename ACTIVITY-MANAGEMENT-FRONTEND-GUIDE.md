# 🏃 Activity Management - Frontend Implementation Guide

## 📖 Overview

This guide provides complete documentation for implementing the **Activity Management** feature in the frontend. Activities are the core building blocks for tour packages, representing flights, accommodations, vehicle rentals, and tour activities.

---

## 🎯 Features by Role

### 👨‍💼 Superadmin
- ✅ View all activities (active and inactive)
- ✅ Create new activities of any type
- ✅ Update all activities
- ✅ Delete (soft delete) all activities

### 🏢 Tour Package Vendor
- ✅ View all activities
- ✅ Create activities of any type
- ✅ Update only activities they created
- ✅ Delete only activities they created

### ✈️ Flight Airline Vendor
- ✅ View all activities
- ✅ Create only "Flight" type activities
- ✅ Update only their own Flight activities
- ✅ Delete only their own Flight activities

### 🏨 Accommodation Owner
- ✅ View all activities
- ✅ Create only "Accommodation" type activities
- ✅ Update only their own Accommodation activities
- ✅ Delete only their own Accommodation activities

### 🚗 Vehicle Rental Vendor
- ✅ View all activities
- ✅ Create only "Vehicle Rental" type activities
- ✅ Update only their own Vehicle Rental activities
- ✅ Delete only their own Vehicle Rental activities

---

## 🔌 API Endpoints

### Base URL
```
http://localhost:8080/api/activities
```

### Authentication
All endpoints require JWT token in Authorization header:
```javascript
headers: {
  'Authorization': `Bearer ${jwt_token}`,
  'Content-Type': 'application/json'
}
```

---

## 📋 API Reference

### 1. Get All Activities

**Endpoint:** `GET /activities`

**Query Parameters (all optional):**
- `isDeleted` (Boolean) - Filter by deletion status
  - `null` or omitted: Show ALL activities (active + inactive)
  - `false`: Show only active activities (recommended for most cases)
  - `true`: Show only deleted/inactive activities
- `activityType` (String) - Filter by activity type (Flight, Accommodation, Vehicle Rental, Tour Activity)
- `startLocation` (String) - Filter by start location
- `endLocation` (String) - Filter by end location
- `startDate` (DateTime) - Filter activities starting from this date
- `endDate` (DateTime) - Filter activities ending before this date
- `search` (String) - Search by activity name or activity item (case-insensitive)

**Response:**
```json
{
  "status": 200,
  "message": "Activities retrieved successfully",
  "count": 15,
  "data": [
    {
      "id": "ACT-20251129-001",
      "vendorId": "vendor-uuid-123",
      "activityName": "Jakarta to Bali Flight",
      "activityItem": "Boeing 737-800",
      "activityType": "Flight",
      "capacity": 180,
      "price": 1500000,
      "startDate": "2025-12-01T08:00:00",
      "endDate": "2025-12-01T10:30:00",
      "startLocation": "Jakarta",
      "endLocation": "Bali",
      "isDeleted": false
    },
    // ... more activities
  ]
}
```

**Example Requests:**
```javascript
// Get all active activities
GET /api/activities?isDeleted=false

// Get Flight activities only
GET /api/activities?activityType=Flight&isDeleted=false

// Search for "Bali" activities
GET /api/activities?search=Bali&isDeleted=false

// Get activities in December 2025
GET /api/activities?startDate=2025-12-01T00:00:00&endDate=2025-12-31T23:59:59&isDeleted=false
```

---

### 2. Get Activity by ID

**Endpoint:** `GET /api/activities/{id}`

**Note:** Only returns activities with `isDeleted = false`. Returns 404 for deleted activities.

**Response:**
```json
{
  "status": 200,
  "message": "Activity retrieved successfully",
  "data": {
    "id": "ACT-20251129-001",
    "vendorId": "vendor-uuid-123",
    "activityName": "Jakarta to Bali Flight",
    "activityItem": "Boeing 737-800",
    "activityType": "Flight",
    "capacity": 180,
    "price": 1500000,
    "startDate": "2025-12-01T08:00:00",
    "endDate": "2025-12-01T10:30:00",
    "startLocation": "Jakarta",
    "endLocation": "Bali",
    "isDeleted": false,
    "orderedQuantities": []
  }
}
```

**Error Response (404):**
```json
{
  "status": 404,
  "message": "Activity with ID ACT-20251129-001 not found"
}
```

---

### 3. Create Activity

**Endpoint:** `POST /api/activities`

**Request Body:**
```json
{
  "activityName": "Jakarta to Bali Flight",
  "activityItem": "Boeing 737-800",
  "activityType": "Flight",
  "capacity": 180,
  "price": 1500000,
  "startDate": "2025-12-01T08:00:00",
  "endDate": "2025-12-01T10:30:00",
  "startLocation": "Jakarta",
  "endLocation": "Bali"
}
```

**Validations:**
- ✅ All fields are required (not null/empty)
- ✅ `price` must be > 0
- ✅ `capacity` must be > 0
- ✅ `startDate` must be < `endDate`
- ✅ `startDate` must be >= now (cannot create activities in the past)

**Activity Types:**
- `Flight`
- `Accommodation`
- `Vehicle Rental`
- `Tour Activity`

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Activity created successfully",
  "data": {
    "id": "ACT-20251129-001",
    "vendorId": "vendor-uuid-123",
    "activityName": "Jakarta to Bali Flight",
    // ... all fields
    "isDeleted": false
  }
}
```

**Error Response (400 Validation Error):**
```json
{
  "status": 400,
  "message": "Start date must be before end date"
}
```

**Error Response (403 Forbidden - Vendor Type Mismatch):**
```json
{
  "status": 403,
  "message": "Flight vendor can only create Flight activities"
}
```

---

### 4. Update Activity

**Endpoint:** `PUT /api/activities/{id}`

**Request Body:**
```json
{
  "activityName": "Jakarta to Bali Premium Flight",
  "activityItem": "Boeing 787 Dreamliner",
  "capacity": 220,
  "price": 2000000,
  "startDate": "2025-12-01T09:00:00",
  "endDate": "2025-12-01T11:30:00",
  "startLocation": "Jakarta",
  "endLocation": "Bali"
}
```

**Important Notes:**
- ❌ **`activityType` cannot be changed** after creation (not included in update DTO)
- ✅ Only vendors can update activities
- ✅ Vendors can only update activities they created (except Superadmin)
- ❌ Cannot update activities with `isDeleted = true`
- ❌ Cannot update activities that have fulfilled orderedActivities

**Validations:**
- ✅ `price` must be > 0
- ✅ `capacity` must be > 0
- ✅ `startDate` must be < `endDate`
- ✅ `startDate` must be >= now

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Activity updated successfully",
  "data": {
    "id": "ACT-20251129-001",
    "activityName": "Jakarta to Bali Premium Flight",
    // ... updated fields
    "activityType": "Flight" // Unchanged
  }
}
```

**Error Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "Access denied: You can only update activities you created"
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Cannot update activity that has fulfilled orderedActivities"
}
```

---

### 5. Delete Activity (Soft Delete)

**Endpoint:** `DELETE /api/activities/{id}`

**Soft Delete Mechanism:**
- Sets `isDeleted = true` (does not remove from database)
- Deleted activities cannot be updated
- Deleted activities do not appear in `GET /activities/{id}` (returns 404)
- Deleted activities can be filtered in `GET /activities?isDeleted=true`

**Authorization:**
- ✅ Only vendors can delete activities
- ✅ Vendors can only delete activities they created (except Superadmin)

**Restrictions:**
- ❌ Cannot delete if activity has **unfulfilled** orderedActivities
  - Unfulfilled = Package status is NOT "Processed"
  - Status "Pending" or "Fulfilled" = still unfulfilled
- ✅ Can delete if:
  - Activity has NO orderedActivities, OR
  - Activity has ONLY orderedActivities with Package status = "Processed"

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Activity deleted successfully (soft delete)",
  "data": {
    "id": "ACT-20251129-001",
    "isDeleted": true,
    // ... other fields
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Cannot delete activity that has unfulfilled orderedActivities. Only activities with no orders or only fulfilled orders can be deleted."
}
```

**Error Response (403 Forbidden):**
```json
{
  "status": 403,
  "message": "Access denied: You can only delete activities you created"
}
```

---

## 🎨 UI/UX Recommendations

### Activity List Page

**Layout:**
```
┌────────────────────────────────────────────────────────┐
│  🏃 Activity Management                    [+ Create]  │
├────────────────────────────────────────────────────────┤
│  Filters:                                              │
│  [Type ▼] [Location ▼] [Search...        ] [Filter]   │
│  [ ] Show deleted activities                           │
├────────────────────────────────────────────────────────┤
│  Activity Name        | Type    | Price     | Actions │
│  ─────────────────────┼─────────┼──────────┼─────────│
│  Jakarta-Bali Flight  | Flight  | 1,500,000| [✏️] [🗑️]│
│  Grand Hotel          | Accom.  | 800,000  | [✏️] [🗑️]│
│  Toyota Avanza        | Rental  | 500,000  | [✏️] [🗑️]│
└────────────────────────────────────────────────────────┘
```

**Features:**
1. **DataTable** with sorting and filtering
2. **Search bar** for activity name/item
3. **Filter by:**
   - Activity Type (dropdown)
   - Start/End Location (dropdown)
   - Date Range (date picker)
   - Active/Inactive status (checkbox)
4. **Actions column:**
   - Edit button (only for own activities or Superadmin)
   - Delete button (only for own activities or Superadmin)
   - View details button
5. **Color coding:**
   - 🟢 Active = Green border
   - 🔴 Inactive/Deleted = Red border or grayed out
6. **Capacity indicator:** Show remaining capacity if activity is in use

---

### Create/Edit Activity Form

**Layout:**
```
┌────────────────────────────────────────────┐
│  Create New Activity                       │
├────────────────────────────────────────────┤
│  Activity Type: [Flight        ▼]         │
│  Activity Name: [________________]         │
│  Activity Item: [________________]         │
│                                            │
│  Price (Rp):    [________________]         │
│  Capacity:      [________________]         │
│                                            │
│  Start Date:    [📅 2025-12-01 08:00]     │
│  End Date:      [📅 2025-12-01 10:30]     │
│                                            │
│  Start Location:[________________]         │
│  End Location:  [________________]         │
│                                            │
│         [Cancel]  [Create Activity]        │
└────────────────────────────────────────────┘
```

**Validation Messages:**
- ⚠️ "Price must be greater than 0"
- ⚠️ "Capacity must be greater than 0"
- ⚠️ "Start date must be before end date"
- ⚠️ "Start date cannot be in the past"
- ⚠️ "All fields are required"

**Edit Form Notes:**
- 🔒 **Activity Type field should be disabled/read-only** (cannot be changed)
- Show message: "Activity type cannot be changed after creation"

---

### Delete Confirmation Modal

```
┌────────────────────────────────────────────┐
│  ⚠️ Delete Activity?                       │
├────────────────────────────────────────────┤
│                                            │
│  Are you sure you want to delete:         │
│  "Jakarta to Bali Flight"?                 │
│                                            │
│  This is a soft delete. The activity will │
│  be marked as inactive and cannot be used │
│  for new packages.                         │
│                                            │
│  Note: You cannot delete activities with  │
│  unfulfilled orders.                       │
│                                            │
│         [Cancel]  [Delete Activity]        │
└────────────────────────────────────────────┘
```

---

## 💻 Code Examples (Vue 3 + Axios)

### 1. Activity List Component

```vue
<template>
  <div class="activity-management">
    <div class="header">
      <h1>🏃 Activity Management</h1>
      <button @click="showCreateForm" class="btn-create" v-if="canCreate">
        + Create Activity
      </button>
    </div>

    <!-- Filters -->
    <div class="filters">
      <select v-model="filters.activityType" @change="fetchActivities">
        <option value="">All Types</option>
        <option value="Flight">Flight</option>
        <option value="Accommodation">Accommodation</option>
        <option value="Vehicle Rental">Vehicle Rental</option>
        <option value="Tour Activity">Tour Activity</option>
      </select>

      <input 
        v-model="filters.search" 
        @input="debouncedSearch"
        placeholder="Search activities..."
        type="text"
      />

      <label>
        <input type="checkbox" v-model="filters.showDeleted" @change="fetchActivities" />
        Show deleted activities
      </label>

      <button @click="clearFilters">Clear Filters</button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">Loading activities...</div>

    <!-- Error State -->
    <div v-if="error" class="error">{{ error }}</div>

    <!-- Activity Table -->
    <table v-if="!loading && activities.length > 0" class="activity-table">
      <thead>
        <tr>
          <th>Activity Name</th>
          <th>Type</th>
          <th>Location</th>
          <th>Price</th>
          <th>Capacity</th>
          <th>Dates</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr 
          v-for="activity in activities" 
          :key="activity.id"
          :class="{ 'deleted': activity.isDeleted }"
        >
          <td>
            <strong>{{ activity.activityName }}</strong><br>
            <small>{{ activity.activityItem }}</small>
          </td>
          <td>
            <span :class="`badge badge-${getTypeColor(activity.activityType)}`">
              {{ activity.activityType }}
            </span>
          </td>
          <td>
            {{ activity.startLocation }} → {{ activity.endLocation }}
          </td>
          <td>Rp {{ formatCurrency(activity.price) }}</td>
          <td>{{ activity.capacity }} pax</td>
          <td>
            {{ formatDate(activity.startDate) }}<br>
            <small>to {{ formatDate(activity.endDate) }}</small>
          </td>
          <td>
            <span :class="`status ${activity.isDeleted ? 'inactive' : 'active'}`">
              {{ activity.isDeleted ? 'Inactive' : 'Active' }}
            </span>
          </td>
          <td>
            <button 
              @click="viewActivity(activity.id)" 
              class="btn-icon"
              title="View Details"
            >
              👁️
            </button>
            <button 
              v-if="canEdit(activity)"
              @click="editActivity(activity.id)" 
              class="btn-icon"
              title="Edit"
            >
              ✏️
            </button>
            <button 
              v-if="canDelete(activity)"
              @click="confirmDelete(activity)" 
              class="btn-icon btn-danger"
              title="Delete"
            >
              🗑️
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Empty State -->
    <div v-if="!loading && activities.length === 0" class="empty-state">
      <p>No activities found. Create your first activity!</p>
    </div>

    <!-- Delete Confirmation Modal -->
    <ConfirmModal
      v-if="showDeleteModal"
      :activity="activityToDelete"
      @confirm="deleteActivity"
      @cancel="showDeleteModal = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const activities = ref([]);
const loading = ref(false);
const error = ref(null);
const showDeleteModal = ref(false);
const activityToDelete = ref(null);

const filters = ref({
  activityType: '',
  search: '',
  showDeleted: false
});

// Authorization checks
const userRole = computed(() => authStore.user?.role);
const userId = computed(() => authStore.user?.id);

const canCreate = computed(() => {
  return ['Superadmin', 'TourPackageVendor', 'FlightAirline', 
          'AccommodationOwner', 'RentalVendor'].includes(userRole.value);
});

const canEdit = (activity) => {
  if (activity.isDeleted) return false;
  if (userRole.value === 'Superadmin') return true;
  return activity.vendorId === userId.value;
};

const canDelete = (activity) => {
  if (activity.isDeleted) return false;
  if (userRole.value === 'Superadmin') return true;
  return activity.vendorId === userId.value;
};

// Fetch activities
const fetchActivities = async () => {
  loading.value = true;
  error.value = null;

  try {
    const params = new URLSearchParams();
    
    // Add isDeleted filter
    if (!filters.value.showDeleted) {
      params.append('isDeleted', 'false');
    }
    
    // Add activity type filter
    if (filters.value.activityType) {
      params.append('activityType', filters.value.activityType);
    }
    
    // Add search filter
    if (filters.value.search) {
      params.append('search', filters.value.search);
    }

    const response = await axios.get('/api/activities', {
      params,
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    });

    activities.value = response.data.data;
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to fetch activities';
    console.error('Error fetching activities:', err);
  } finally {
    loading.value = false;
  }
};

// Debounced search
let searchTimeout;
const debouncedSearch = () => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    fetchActivities();
  }, 500);
};

// Clear filters
const clearFilters = () => {
  filters.value = {
    activityType: '',
    search: '',
    showDeleted: false
  };
  fetchActivities();
};

// Delete activity
const confirmDelete = (activity) => {
  activityToDelete.value = activity;
  showDeleteModal.value = true;
};

const deleteActivity = async () => {
  try {
    await axios.delete(`/api/activities/${activityToDelete.value.id}`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    });

    // Refresh list
    await fetchActivities();
    showDeleteModal.value = false;
    activityToDelete.value = null;

    // Show success notification
    alert('Activity deleted successfully');
  } catch (err) {
    const errorMsg = err.response?.data?.message || 'Failed to delete activity';
    alert(errorMsg);
  }
};

// Utility functions
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('id-ID').format(amount);
};

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('id-ID', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getTypeColor = (type) => {
  const colors = {
    'Flight': 'blue',
    'Accommodation': 'green',
    'Vehicle Rental': 'orange',
    'Tour Activity': 'purple'
  };
  return colors[type] || 'gray';
};

// Navigation
const showCreateForm = () => {
  // Navigate to create form
  // router.push('/activities/create');
};

const viewActivity = (id) => {
  // Navigate to detail page
  // router.push(`/activities/${id}`);
};

const editActivity = (id) => {
  // Navigate to edit form
  // router.push(`/activities/${id}/edit`);
};

onMounted(() => {
  fetchActivities();
});
</script>

<style scoped>
.activity-management {
  padding: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filters select,
.filters input[type="text"] {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 200px;
}

.activity-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.activity-table th,
.activity-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.activity-table th {
  background: #f8f9fa;
  font-weight: 600;
}

.activity-table tr.deleted {
  opacity: 0.5;
  background: #f5f5f5;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.badge-blue { background: #e3f2fd; color: #1976d2; }
.badge-green { background: #e8f5e9; color: #388e3c; }
.badge-orange { background: #fff3e0; color: #f57c00; }
.badge-purple { background: #f3e5f5; color: #7b1fa2; }

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status.active {
  background: #e8f5e9;
  color: #388e3c;
}

.status.inactive {
  background: #ffebee;
  color: #c62828;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.25rem;
  padding: 0.25rem;
  margin: 0 0.25rem;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.btn-icon:hover {
  opacity: 1;
}

.btn-create {
  background: #1976d2;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.loading,
.error,
.empty-state {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.error {
  color: #c62828;
}
</style>
```

---

### 2. Create Activity Form Component

```vue
<template>
  <div class="create-activity-form">
    <h1>{{ isEdit ? 'Edit Activity' : 'Create New Activity' }}</h1>

    <form @submit.prevent="handleSubmit">
      <!-- Activity Type (disabled on edit) -->
      <div class="form-group">
        <label>Activity Type <span class="required">*</span></label>
        <select 
          v-model="form.activityType" 
          required
          :disabled="isEdit"
          :class="{ 'disabled': isEdit }"
        >
          <option value="">Select Type</option>
          <option 
            v-for="type in availableTypes" 
            :key="type" 
            :value="type"
          >
            {{ type }}
          </option>
        </select>
        <small v-if="isEdit" class="help-text">
          ⚠️ Activity type cannot be changed after creation
        </small>
      </div>

      <!-- Activity Name -->
      <div class="form-group">
        <label>Activity Name <span class="required">*</span></label>
        <input 
          v-model="form.activityName" 
          type="text" 
          required
          placeholder="e.g., Jakarta to Bali Flight"
        />
      </div>

      <!-- Activity Item -->
      <div class="form-group">
        <label>Activity Item <span class="required">*</span></label>
        <input 
          v-model="form.activityItem" 
          type="text" 
          required
          placeholder="e.g., Boeing 737-800"
        />
      </div>

      <!-- Price and Capacity -->
      <div class="form-row">
        <div class="form-group">
          <label>Price (Rp) <span class="required">*</span></label>
          <input 
            v-model.number="form.price" 
            type="number" 
            required
            min="1"
            placeholder="1500000"
          />
          <small class="help-text">Must be greater than 0</small>
        </div>

        <div class="form-group">
          <label>Capacity (pax) <span class="required">*</span></label>
          <input 
            v-model.number="form.capacity" 
            type="number" 
            required
            min="1"
            placeholder="180"
          />
          <small class="help-text">Must be greater than 0</small>
        </div>
      </div>

      <!-- Start and End Date -->
      <div class="form-row">
        <div class="form-group">
          <label>Start Date & Time <span class="required">*</span></label>
          <input 
            v-model="form.startDate" 
            type="datetime-local" 
            required
            :min="minDateTime"
          />
          <small class="help-text">Must be in the future</small>
        </div>

        <div class="form-group">
          <label>End Date & Time <span class="required">*</span></label>
          <input 
            v-model="form.endDate" 
            type="datetime-local" 
            required
            :min="form.startDate"
          />
          <small class="help-text">Must be after start date</small>
        </div>
      </div>

      <!-- Start and End Location -->
      <div class="form-row">
        <div class="form-group">
          <label>Start Location <span class="required">*</span></label>
          <input 
            v-model="form.startLocation" 
            type="text" 
            required
            placeholder="Jakarta"
          />
        </div>

        <div class="form-group">
          <label>End Location <span class="required">*</span></label>
          <input 
            v-model="form.endLocation" 
            type="text" 
            required
            placeholder="Bali"
          />
        </div>
      </div>

      <!-- Error message -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <!-- Buttons -->
      <div class="form-actions">
        <button type="button" @click="goBack" class="btn-secondary">
          Cancel
        </button>
        <button type="submit" class="btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : (isEdit ? 'Update Activity' : 'Create Activity') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const isEdit = computed(() => !!route.params.id);
const submitting = ref(false);
const error = ref(null);

// Form data
const form = ref({
  activityName: '',
  activityItem: '',
  activityType: '',
  capacity: null,
  price: null,
  startDate: '',
  endDate: '',
  startLocation: '',
  endLocation: ''
});

// Available activity types based on user role
const availableTypes = computed(() => {
  const role = authStore.user?.role;
  
  if (role === 'Superadmin' || role === 'TourPackageVendor') {
    return ['Flight', 'Accommodation', 'Vehicle Rental', 'Tour Activity'];
  }
  
  if (role === 'FlightAirline') return ['Flight'];
  if (role === 'AccommodationOwner') return ['Accommodation'];
  if (role === 'RentalVendor') return ['Vehicle Rental'];
  
  return [];
});

// Minimum datetime (now)
const minDateTime = computed(() => {
  const now = new Date();
  return now.toISOString().slice(0, 16);
});

// Fetch activity data if editing
onMounted(async () => {
  if (isEdit.value) {
    try {
      const response = await axios.get(`/api/activities/${route.params.id}`, {
        headers: {
          'Authorization': `Bearer ${authStore.token}`
        }
      });

      const activity = response.data.data;
      
      // Populate form
      form.value = {
        activityName: activity.activityName,
        activityItem: activity.activityItem,
        activityType: activity.activityType,
        capacity: activity.capacity,
        price: activity.price,
        startDate: formatDateTimeForInput(activity.startDate),
        endDate: formatDateTimeForInput(activity.endDate),
        startLocation: activity.startLocation,
        endLocation: activity.endLocation
      };
    } catch (err) {
      error.value = 'Failed to load activity data';
      console.error(err);
    }
  }
});

// Submit form
const handleSubmit = async () => {
  error.value = null;
  submitting.value = true;

  try {
    // Format dates for API
    const payload = {
      ...form.value,
      startDate: formatDateTimeForAPI(form.value.startDate),
      endDate: formatDateTimeForAPI(form.value.endDate)
    };

    if (isEdit.value) {
      // Update activity (remove activityType from payload)
      const { activityType, ...updatePayload } = payload;
      
      await axios.put(`/api/activities/${route.params.id}`, updatePayload, {
        headers: {
          'Authorization': `Bearer ${authStore.token}`
        }
      });

      alert('Activity updated successfully');
    } else {
      // Create activity
      await axios.post('/api/activities', payload, {
        headers: {
          'Authorization': `Bearer ${authStore.token}`
        }
      });

      alert('Activity created successfully');
    }

    // Navigate back to list
    router.push('/activities');
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to save activity';
    console.error(err);
  } finally {
    submitting.value = false;
  }
};

// Format datetime for input field (YYYY-MM-DDTHH:mm)
const formatDateTimeForInput = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toISOString().slice(0, 16);
};

// Format datetime for API (YYYY-MM-DDTHH:mm:ss)
const formatDateTimeForAPI = (dateString) => {
  if (!dateString) return null;
  return dateString + ':00';
};

const goBack = () => {
  router.push('/activities');
};
</script>

<style scoped>
.create-activity-form {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.required {
  color: #c62828;
}

input,
select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus,
select:focus {
  outline: none;
  border-color: #1976d2;
}

input.disabled,
select:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.help-text {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #666;
}

.error-message {
  padding: 1rem;
  background: #ffebee;
  color: #c62828;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn-primary,
.btn-secondary {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  font-size: 1rem;
}

.btn-primary {
  background: #1976d2;
  color: white;
}

.btn-primary:hover {
  background: #1565c0;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
}

.btn-secondary:hover {
  background: #e0e0e0;
}
</style>
```

---

### 3. Delete Confirmation Modal Component

```vue
<template>
  <div class="modal-overlay" @click.self="$emit('cancel')">
    <div class="modal-content">
      <h2>⚠️ Delete Activity?</h2>
      
      <p>Are you sure you want to delete:</p>
      <p class="activity-name"><strong>{{ activity.activityName }}</strong></p>
      
      <div class="info-box">
        <p>This is a <strong>soft delete</strong>. The activity will:</p>
        <ul>
          <li>Be marked as inactive (isDeleted = true)</li>
          <li>Not be available for new packages</li>
          <li>Cannot be updated anymore</li>
          <li>Still remain in the database</li>
        </ul>
      </div>
      
      <div class="warning-box" v-if="hasOrders">
        <p>⚠️ <strong>Note:</strong> This activity has associated orders.</p>
        <p>You can only delete if all orders are fulfilled (Package status = "Processed").</p>
      </div>
      
      <div class="modal-actions">
        <button @click="$emit('cancel')" class="btn-secondary">
          Cancel
        </button>
        <button @click="$emit('confirm')" class="btn-danger">
          Delete Activity
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  activity: {
    type: Object,
    required: true
  }
});

const hasOrders = computed(() => {
  return props.activity.orderedQuantities && 
         props.activity.orderedQuantities.length > 0;
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
}

.activity-name {
  text-align: center;
  font-size: 1.25rem;
  margin: 1rem 0;
  color: #c62828;
}

.info-box,
.warning-box {
  padding: 1rem;
  border-radius: 4px;
  margin: 1rem 0;
}

.info-box {
  background: #e3f2fd;
  border-left: 4px solid #1976d2;
}

.warning-box {
  background: #fff3e0;
  border-left: 4px solid #f57c00;
}

.info-box ul {
  margin: 0.5rem 0 0 1rem;
  padding: 0;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn-secondary,
.btn-danger {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
}

.btn-danger {
  background: #c62828;
  color: white;
}

.btn-danger:hover {
  background: #b71c1c;
}
</style>
```

---

## 🔧 Axios Configuration

```javascript
// src/services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add JWT token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle errors globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 🧪 Testing Checklist

### Create Activity
- [ ] Form validation works for all fields
- [ ] Cannot select activity type if vendor doesn't have permission
- [ ] Cannot set start date in the past
- [ ] Cannot set end date before start date
- [ ] Price and capacity must be > 0
- [ ] Success message shows after creation
- [ ] Activity appears in list after creation

### Update Activity
- [ ] Activity type field is disabled/read-only
- [ ] Only vendor who created can edit (except Superadmin)
- [ ] Cannot edit deleted activities
- [ ] Cannot edit activities with fulfilled orders
- [ ] Validations work same as create
- [ ] Success message shows after update
- [ ] Changes reflect in list

### Delete Activity
- [ ] Confirmation modal appears
- [ ] Only vendor who created can delete (except Superadmin)
- [ ] Cannot delete if has unfulfilled orders
- [ ] Can delete if has only fulfilled orders
- [ ] Can delete if has no orders
- [ ] Activity marked as inactive after delete
- [ ] Deleted activity doesn't appear in list (if filter active)

### List View
- [ ] Shows all active activities by default
- [ ] Filter by activity type works
- [ ] Search by name/item works
- [ ] Show deleted checkbox works
- [ ] Sorting works
- [ ] Edit/Delete buttons only show for authorized users
- [ ] Deleted activities are grayed out when shown

---

## 🚨 Error Handling

### Common Errors

**403 Forbidden - Wrong Vendor Type**
```json
{
  "status": 403,
  "message": "Flight vendor can only create Flight activities"
}
```
**Solution:** Check user role and only allow appropriate activity types.

**400 Bad Request - Validation Error**
```json
{
  "status": 400,
  "message": "Start date must be before end date"
}
```
**Solution:** Validate form data before submission.

**400 Bad Request - Cannot Update**
```json
{
  "status": 400,
  "message": "Cannot update activity that has fulfilled orderedActivities"
}
```
**Solution:** Show message and disable edit button for such activities.

**400 Bad Request - Cannot Delete**
```json
{
  "status": 400,
  "message": "Cannot delete activity that has unfulfilled orderedActivities"
}
```
**Solution:** Show warning in delete modal and prevent deletion.

---

## 📱 Responsive Design Tips

```css
/* Mobile-first approach */
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .activity-table {
    font-size: 0.875rem;
  }

  .activity-table th,
  .activity-table td {
    padding: 0.5rem;
  }

  .filters {
    flex-direction: column;
  }

  .filters select,
  .filters input {
    width: 100%;
  }
}
```

---

## 🎯 Next Steps

1. **Implement the Activity List Page** first
2. **Add Create/Edit Forms** with validation
3. **Test with different user roles** (use JWT tokens from Profile Service)
4. **Add Delete Confirmation Modal**
5. **Test error scenarios** (unfulfilled orders, unauthorized access, etc.)
6. **Add loading states and animations**
7. **Implement pagination** if you have many activities

---

## 📞 Support

For backend API issues or questions:
- Check the ActivityRestController.java source code
- Review API response format
- Test endpoints using Bruno or Postman
- Verify JWT token is valid

---

**Good luck with your implementation! 🚀**
