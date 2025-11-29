# ✅ **CUSTOMER ACCESS CONTROL - IMPLEMENTATION GUIDE**

## 📋 **OVERVIEW**

Implementasi role-based access control (RBAC) untuk Customer role dalam Tour Package Management System.
Customer hanya dapat melihat package milik sendiri + package yang dibuat oleh Admin/Vendor, TIDAK bisa melihat package customer lain.

---

## 🎯 **BUSINESS RULES**

### **1. Customer dapat melihat package yang dibuat oleh:**
- ✅ **Dirinya sendiri** (`package.userId == currentUser.id`)
- ✅ **Admin/Vendor** (`package.creatorRole IN ["Superadmin", "TourPackageVendor"]`)
- ❌ **TIDAK dapat melihat** package customer lain

### **2. Customer dapat melihat DETAIL package:**
- ✅ Package milik sendiri
- ✅ Package dibuat Admin/Vendor
- ❌ Package customer lain → **403 Forbidden**

### **3. Customer dapat EDIT/DELETE package:**
- ✅ **Hanya package sendiri** (`package.userId == currentUser.id`)
- ✅ **Delete hanya jika status = Pending**
- ❌ Customer **TIDAK bisa** edit/delete package Admin/Vendor

### **4. Admin/Vendor:**
- ✅ Dapat melihat **semua package** (no filtering)
- ✅ Dapat edit/delete semua package

---

## 🔧 **BACKEND IMPLEMENTATION**

### **A. Database Schema Changes**

#### **1. Add `creatorRole` column to Package table**

```sql
-- File: src/main/resources/db/migration/V3__add_creator_role_to_package.sql

ALTER TABLE package ADD COLUMN IF NOT EXISTS creator_role VARCHAR(255);

UPDATE package SET creator_role = 'Superadmin' WHERE creator_role IS NULL;

COMMENT ON COLUMN package.creator_role IS 'Role of the user who created this package (Customer, Superadmin, TourPackageVendor). Used for authorization filtering.';
```

**Why?** 
- Menyimpan role pembuat package untuk efficient filtering
- Menghindari query ke Profile Service setiap kali filtering
- Backward compatibility: old packages default to 'Superadmin'

---

### **B. Model Changes**

#### **1. Package Entity** (`model/Package.java`)

```java
@Entity
@Table(name = "package")
public class Package {
    @Id
    private String id;
    
    private String userId;
    
    // ✅ NEW FIELD: Role of package creator
    private String creatorRole;  // Customer, Superadmin, TourPackageVendor
    
    private String packageName;
    private int quota;
    private Long price;
    private String status;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    
    @OneToMany(mappedBy = "tourPackage", fetch = FetchType.LAZY)
    private List<Plan> plans;
}
```

---

#### **2. PackageResponseDTO** (`restdto/response/PackageResponseDTO.java`)

```java
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PackageResponseDTO {
    private String id;
    private String userId;
    
    // ✅ NEW FIELD: Include in response
    private String creatorRole;
    
    private String packageName;
    private int quota;
    private Long price;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<PlanResponseDTO> plans;
}
```

---

### **C. Service Layer Changes**

#### **1. Update Interface** (`restservice/PackageRestService.java`)

```java
public interface PackageRestService {
    List<PackageResponseDTO> getAll();
    
    List<PackageResponseDTO> getPackagesForCustomer(String userId);
    
    PackageResponseDTO getById(String id);
    
    // ✅ UPDATED: Now accepts userRole parameter
    PackageResponseDTO create(CreatePackageRequestDTO req, String userId, String userRole);
    
    PackageResponseDTO deleteById(String id);
    PackageResponseDTO updatePackage(String id, UpdatePackageRequestDTO dto);
    PackageResponseDTO processPackage(String id);
}
```

---

#### **2. Implementation** (`restservice/TourPackageRestServiceImpl.java`)

##### **a. Update `create()` method to store creatorRole**

```java
@Override
public PackageResponseDTO create(CreatePackageRequestDTO req, String userId, String userRole) {
    // ... validation logic ...
    
    var entity = Package.builder()
        .id(id)
        .userId(userId)
        
        // ✅ NEW: Store creator's role
        .creatorRole(userRole)
        
        .packageName(req.getPackageName())
        .quota(req.getQuota())
        .price(0L)
        .status("Pending")
        .startDate(req.getStartDate())
        .endDate(req.getEndDate())
        .build();
    
    var saved = repo.save(entity);
    return map(saved);
}
```

---

##### **b. Update `getPackagesForCustomer()` method**

```java
@Override
public List<PackageResponseDTO> getPackagesForCustomer(String userId) {
    // Customer melihat:
    // 1. Package milik sendiri (semua status)
    // 2. Package yang dibuat oleh Superadmin atau TourPackageVendor
    
    return repo.findAllActive().stream()
        .filter(pkg -> {
            String pkgUserId = pkg.getUserId();
            String creatorRole = pkg.getCreatorRole();
            
            // Show own package
            boolean isOwnPackage = pkgUserId != null && pkgUserId.equals(userId);
            
            // Show packages created by Admin/Vendor
            // If creatorRole is null (old data), allow access for backward compatibility
            boolean isAdminVendorPackage = creatorRole == null 
                || "Superadmin".equals(creatorRole) 
                || "TourPackageVendor".equals(creatorRole);
            
            return isOwnPackage || isAdminVendorPackage;
        })
        .map(this::map)
        .toList();
}
```

**Logic:**
- ✅ `isOwnPackage`: Package dengan userId sama dengan current user
- ✅ `isAdminVendorPackage`: Package dibuat Admin/Vendor (based on creatorRole)
- ✅ `creatorRole == null`: Backward compatibility untuk old data

---

##### **c. Update `map()` method to include creatorRole**

```java
private PackageResponseDTO map(Package p) {
    // ... calculate price logic ...
    
    return PackageResponseDTO.builder()
        .id(p.getId())
        .userId(p.getUserId())
        
        // ✅ NEW: Include creator role
        .creatorRole(p.getCreatorRole())
        
        .packageName(p.getPackageName())
        .quota(p.getQuota())
        .price(totalPackagePrice)
        .status(p.getStatus())
        .startDate(p.getStartDate())
        .endDate(p.getEndDate())
        .build();
}
```

---

### **D. Controller Layer Changes**

#### **1. PackageRestController** (`restcontroller/PackageRestController.java`)

##### **a. Update GET /api/package (List packages)**

```java
@GetMapping
public ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> getAll(
    @AuthenticationPrincipal AuthenticatedUser user) {
    
    logger.info("🔍 GET /api/package - Fetching packages with role-based filtering");
    logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
    
    List<PackageResponseDTO> packages;
    
    if (user.hasAdminPrivileges()) {
        // Superadmin & TourPackageVendor: See all packages
        logger.info("✅ Admin/Vendor access: Fetching all packages");
        packages = service.getAll();
    } else {
        // Customer: See packages from admin/vendor + own packages
        logger.info("👥 Customer access: Fetching filtered packages");
        
        // ✅ Backend handles filtering, not frontend!
        packages = service.getPackagesForCustomer(user.getId());
    }
    
    logger.info("📦 Total packages returned: {}", packages.size());
    var body = new BaseResponseDTO<>(200, "Success", new Date(), packages);
    return ResponseEntity.ok(body);
}
```

**Key Points:**
- Admin/Vendor: `service.getAll()` - no filtering
- Customer: `service.getPackagesForCustomer(userId)` - filtered by ownership + creatorRole

---

##### **b. Update GET /api/package/{id} (Package detail with authorization)**

```java
@GetMapping("/{id}")
public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> getById(
    @PathVariable String id,
    @AuthenticationPrincipal AuthenticatedUser user) {
    
    logger.info("🔍 GET /package/{} - Fetching package detail", id);
    logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
    
    PackageResponseDTO packageData = service.getById(id);
    
    // ✅ Authorization check for Customer role
    if (!user.hasAdminPrivileges()) {
        boolean isOwnPackage = user.getId().equals(packageData.getUserId());
        
        // Check if package is created by Admin/Vendor (based on creatorRole)
        boolean isAdminVendorPackage = packageData.getCreatorRole() == null 
            || "Superadmin".equals(packageData.getCreatorRole())
            || "TourPackageVendor".equals(packageData.getCreatorRole());
        
        if (!isOwnPackage && !isAdminVendorPackage) {
            // ❌ Package is created by another Customer
            logger.warn("❌ Access denied: Customer {} cannot view package {} created by another customer", 
                        user.getId(), id);
            
            var body = new BaseResponseDTO<PackageResponseDTO>(
                403, 
                "Access denied: You can only view your own packages or packages created by admin/vendor", 
                new Date(), 
                null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }
    
    logger.info("✅ Access granted: Package detail retrieved successfully");
    var body = new BaseResponseDTO<>(200, "Success", new Date(), packageData);
    return ResponseEntity.ok(body);
}
```

**Authorization Logic:**
1. Admin/Vendor → Full access
2. Customer → Check `isOwnPackage` OR `isAdminVendorPackage`
3. If neither → **403 Forbidden**

---

##### **c. Update POST /api/package/create**

```java
@PostMapping("/create")
public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> create(
    @Valid @RequestBody CreatePackageRequestDTO req,
    @AuthenticationPrincipal AuthenticatedUser user) {
    
    try {
        logger.info("📦 POST /api/package/create - Creating package");
        logger.info("👤 User: ID={}, Role={}", user.getId(), user.getRole());
        
        // ✅ Pass both userId AND userRole from JWT token
        var data = service.create(req, user.getId(), user.getRole());
        
        logger.info("✅ Package created successfully: {}", data.getId());
        var body = new BaseResponseDTO<>(201, "Created", new Date(), data);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
        
    } catch (IllegalArgumentException e) {
        logger.error("❌ Validation error: {}", e.getMessage());
        var body = new BaseResponseDTO<PackageResponseDTO>(400, e.getMessage(), new Date(), null);
        return ResponseEntity.badRequest().body(body);
    }
}
```

**Key Change:**
- Pass `user.getRole()` to service for storing `creatorRole`

---

## 🎨 **FRONTEND IMPLEMENTATION**

### **A. PackageView.vue Changes**

#### **Remove frontend filtering (Backend handles it now)**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import { usePackageStore } from '@/stores/package'

const store = usePackageStore()

const filteredPackages = computed(() => {
  // ✅ Backend sudah melakukan role-based filtering berdasarkan creatorRole
  // Customer: Melihat package sendiri + package dari Admin/Vendor
  // Admin/Vendor: Melihat semua package
  
  // Frontend hanya perlu filter berdasarkan search query
  return store.items.filter((p) =>
    p.packageName.toLowerCase().includes(searchQuery.value.toLowerCase()),
  )
})
</script>
```

**Why?**
- Backend sudah filter packages berdasarkan role
- Frontend tidak perlu duplicate filtering logic
- Single source of truth: Backend

---

### **B. PackageDetailView.vue Changes**

#### **Keep frontend validation for UX (Backend enforces security)**

```vue
<script setup lang="ts">
// Check if Customer has access to view this package
function checkAccessPermission() {
  if (!pkg.value) return

  // ⚠️ This is for UX only - Backend enforces security
  // Customer hanya bisa akses:
  // 1. Package yang dibuat sendiri
  // 2. Package dari Admin/Vendor (based on creatorRole from backend)
  
  if (userRole.value === 'Customer') {
    const isOwnPackage = String(pkg.value.userId) === String(userId.value)
    
    // Backend returns creatorRole, check if it's Admin/Vendor
    const isAdminVendorPackage = 
      pkg.value.creatorRole === 'Superadmin' || 
      pkg.value.creatorRole === 'TourPackageVendor'

    if (!isOwnPackage && !isAdminVendorPackage) {
      alert('⚠️ You do not have permission to view this package')
      router.push('/package')
    }
  }
}
</script>
```

**Note:**
- Frontend check = UX improvement (instant feedback)
- Backend enforces actual security (returns 403 if unauthorized)

---

### **C. Add `creatorRole` to Package Interface**

```typescript
// File: src/interfaces/package.interface.ts

export interface Package {
  id: string
  userId: string
  
  // ✅ NEW FIELD: Creator role from backend
  creatorRole?: string  // 'Customer' | 'Superadmin' | 'TourPackageVendor'
  
  packageName: string
  quota: number
  price: number
  status: string
  startDate: string
  endDate: string
  plans?: Plan[]
}
```

---

## 📊 **AUTHORIZATION MATRIX**

| Action | Customer (Own Package) | Customer (Admin/Vendor Package) | Customer (Other Customer Package) | Admin/Vendor |
|--------|------------------------|--------------------------------|----------------------------------|--------------|
| **View List** | ✅ Yes | ✅ Yes | ❌ No (filtered by backend) | ✅ Yes (all) |
| **View Detail** | ✅ Yes | ✅ Yes | ❌ 403 Forbidden | ✅ Yes |
| **Create** | ✅ Yes | ❌ N/A | ❌ N/A | ✅ Yes |
| **Edit/Update** | ✅ Yes (if Pending) | ❌ No | ❌ No | ✅ Yes |
| **Delete** | ✅ Yes (if Pending) | ❌ No | ❌ No | ✅ Yes (if Pending) |
| **Process** | ❌ No | ❌ No | ❌ No | ✅ Yes |

---

## 🧪 **TESTING GUIDE**

### **Test Scenario 1: Customer Views Package List**

**Setup:**
1. Login as Admin → Create 3 packages (PKG-001, PKG-002, PKG-003)
2. Login as Customer1 → Create 2 packages (PKG-004, PKG-005)
3. Login as Customer2 → Create 1 package (PKG-006)

**Expected Results for Customer1:**
```
GET /api/package
Response: [PKG-001, PKG-002, PKG-003, PKG-004, PKG-005]
```
- ✅ Shows own packages (PKG-004, PKG-005)
- ✅ Shows Admin packages (PKG-001, PKG-002, PKG-003)
- ❌ Does NOT show Customer2's package (PKG-006)

---

### **Test Scenario 2: Customer Views Package Detail**

**Test Cases:**

| Endpoint | User | Package Owner | Expected |
|----------|------|--------------|----------|
| GET /package/PKG-001 | Customer1 | Admin | ✅ 200 OK |
| GET /package/PKG-004 | Customer1 | Customer1 | ✅ 200 OK |
| GET /package/PKG-006 | Customer1 | Customer2 | ❌ 403 Forbidden |

---

### **Test Scenario 3: Customer Edits Package**

**Test Cases:**

| Endpoint | User | Package Owner | Expected |
|----------|------|--------------|----------|
| PUT /package/PKG-004 | Customer1 | Customer1 | ✅ 200 OK |
| PUT /package/PKG-001 | Customer1 | Admin | ❌ 403 Forbidden |
| PUT /package/PKG-006 | Customer1 | Customer2 | ❌ 403 Forbidden |

---

## 🚀 **DEPLOYMENT STEPS**

### **Backend:**
1. ✅ Run database migration: `V3__add_creator_role_to_package.sql`
2. ✅ Restart backend application
3. ✅ Verify existing packages have `creatorRole = 'Superadmin'`
4. ✅ Test endpoints with Postman/Bruno

### **Frontend:**
1. ✅ Pull latest changes
2. ✅ Run `npm install` (if dependencies changed)
3. ✅ Test with different roles (Customer, Admin, Vendor)

---

## 📝 **MIGRATION NOTES**

### **Backward Compatibility:**

#### Old packages without `creatorRole`:
```sql
UPDATE package SET creator_role = 'Superadmin' WHERE creator_role IS NULL;
```

#### Service handles null gracefully:
```java
boolean isAdminVendorPackage = creatorRole == null  // Old data
    || "Superadmin".equals(creatorRole) 
    || "TourPackageVendor".equals(creatorRole);
```

---

## ⚠️ **IMPORTANT NOTES**

1. **Security is enforced at Backend level**
   - Frontend checks are for UX only
   - Never trust frontend validation alone

2. **creatorRole is immutable**
   - Set once during package creation
   - Cannot be changed via update endpoint

3. **Admin/Vendor packages are public to all Customers**
   - Customers can view but cannot edit
   - This is by design (catalog feature)

4. **Customer can only edit own packages**
   - Even if status is "Processed", only view access
   - Edit/Delete restricted to "Pending" status

---

## 📞 **SUPPORT & QUESTIONS**

If you encounter issues:
1. Check backend logs for authorization failures
2. Verify JWT token contains correct `userId` and `role`
3. Confirm database has `creator_role` column populated
4. Test with different user roles (Customer, Admin, Vendor)

---

**Last Updated:** November 29, 2025
**Author:** System Implementation Team
**Version:** 1.0.0
