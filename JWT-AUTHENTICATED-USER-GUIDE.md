# 🔐 JWT Authenticated User Implementation Guide

## Overview

Backend telah dimodifikasi untuk menggunakan `AuthenticatedUser` yang otomatis di-inject dari JWT token.
**Frontend tidak perlu mengirim userId lagi** - semuanya otomatis dari token!

---

## 📋 What Changed?

### ✅ Before (Manual userId extraction)
```java
@PostMapping
public ResponseEntity<?> createPackage(@RequestBody CreatePackageRequestDTO request) {
    // ❌ Manual extraction dari SecurityContext
    String userId = getCurrentUserId();
    String role = getCurrentUserRole();
    
    // Logic...
}
```

### ✅ After (Automatic from token)
```java
@PostMapping
public ResponseEntity<?> createPackage(
    @AuthenticationPrincipal AuthenticatedUser user,  // ✅ Auto-injected!
    @RequestBody CreatePackageRequestDTO request) {
    
    // ✅ Direct access - no manual extraction needed!
    String userId = user.getId();
    String role = user.getRole();
    String email = user.getEmail();
    
    // Logic...
}
```

---

## 🏗️ Architecture

### 1. JWT Token Flow

```
┌─────────────┐         ┌──────────────┐         ┌─────────────────┐
│  Frontend   │  POST   │ JwtTokenFilter│ Validate│ SecurityContext │
│  (with JWT) │ ──────> │   (Filter)    │ ──────> │  (Spring Auth)  │
└─────────────┘         └──────────────┘         └─────────────────┘
                               │                          │
                               │ 1. Extract token         │
                               │ 2. Validate with         │
                               │    Profile Service       │
                               │ 3. Parse claims          │
                               │                          │
                               ▼                          ▼
                        ┌────────────────┐       ┌──────────────────┐
                        │ AuthenticatedUser │     │   Controller     │
                        ├────────────────┤       │  (receives user) │
                        │ - id           │       └──────────────────┘
                        │ - username     │                │
                        │ - email        │                │
                        │ - name         │                ▼
                        │ - role         │       ┌──────────────────┐
                        └────────────────┘       │    Service       │
                                                 │  (uses user.id)  │
                                                 └──────────────────┘
```

### 2. Component Breakdown

#### A. `AuthenticatedUser.java` (NEW)
```java
@Data
@Builder
public class AuthenticatedUser implements UserDetails {
    private String id;        // User ID from JWT
    private String username;  // Username from JWT
    private String email;     // Email from JWT
    private String name;      // Full name from JWT
    private String role;      // Role from JWT
    
    // Helper methods
    public boolean isCustomer() { ... }
    public boolean isSuperadmin() { ... }
    public boolean isVendor() { ... }
    public boolean hasAdminPrivileges() { ... }
    public String getCustomerId() { return id; }  // Semantic alias
    public String getVendorId() { return id; }    // Semantic alias
}
```

#### B. `JwtTokenFilter.java` (UPDATED)
```java
@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(...) {
        String token = parseJwt(request);
        
        if (token != null) {
            // 1. Validate token with Profile Service
            ResponseEntity response = validateTokenWithProfileService(token);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 2. Extract claims from token
                String userId = jwtUtils.getUserIdFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);
                String email = jwtUtils.getEmailFromToken(token);
                // ...
                
                // 3. Create AuthenticatedUser object
                AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .name(name)
                    .role(role)
                    .build();
                
                // 4. Set to SecurityContext (available via @AuthenticationPrincipal)
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        authenticatedUser,  // This becomes the principal
                        null,
                        authenticatedUser.getAuthorities()
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 📝 Usage Examples

### Example 1: Create Package (POST)

#### ❌ OLD WAY (Frontend sends userId)
```java
// Frontend
const createPackage = async (data) => {
  const payload = {
    packageName: "Bali Trip",
    userId: "f5dad850-118b-4bb5-a3c5-0c1e2fa84f16",  // ❌ Manual userId
    destination: "Bali"
  };
  await axios.post('/api/package', payload);
};

// Backend
@PostMapping
public ResponseEntity<?> createPackage(@RequestBody CreatePackageRequestDTO request) {
    String userId = request.getUserId();  // ❌ From request body
    // ...
}
```

#### ✅ NEW WAY (userId from token automatically)
```java
// Frontend
const createPackage = async (data) => {
  const payload = {
    packageName: "Bali Trip",
    // ✅ No userId needed!
    destination: "Bali"
  };
  await axios.post('/api/package', payload, {
    headers: { Authorization: `Bearer ${token}` }
  });
};

// Backend
@PostMapping
public ResponseEntity<?> createPackage(
    @AuthenticationPrincipal AuthenticatedUser user,  // ✅ Auto-injected
    @RequestBody CreatePackageRequestDTO request) {
    
    logger.info("Creating package for user: {}", user.getId());
    logger.info("User role: {}", user.getRole());
    logger.info("User email: {}", user.getEmail());
    
    // Pass user ID to service
    PackageResponseDTO result = service.createPackage(request, user.getId());
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new BaseResponseDTO<>(201, "Package created", new Date(), result));
}
```

### Example 2: Get User's Packages (GET with filtering)

```java
@GetMapping
public ResponseEntity<?> getAllPackages(
    @AuthenticationPrincipal AuthenticatedUser user) {
    
    List<PackageResponseDTO> packages;
    
    // Role-based filtering using helper methods
    if (user.hasAdminPrivileges()) {
        // Superadmin/Vendor: See all packages
        packages = service.getAll();
    } else {
        // Customer: See only their packages + admin/vendor packages
        packages = service.getPackagesForCustomer(user.getId());
    }
    
    return ResponseEntity.ok(
        new BaseResponseDTO<>(200, "Success", new Date(), packages)
    );
}
```

### Example 3: Update Package (Authorization check)

```java
@PutMapping("/{id}")
public ResponseEntity<?> updatePackage(
    @AuthenticationPrincipal AuthenticatedUser user,
    @PathVariable String id,
    @RequestBody UpdatePackageRequestDTO request) {
    
    // 1. Fetch existing package
    Package existingPackage = packageRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Package not found"));
    
    // 2. Authorization check
    if (!user.isSuperadmin() && !existingPackage.getUserId().equals(user.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new BaseResponseDTO<>(403, "You can only update your own packages", new Date(), null));
    }
    
    // 3. Update package
    PackageResponseDTO updated = service.updatePackage(id, request);
    
    return ResponseEntity.ok(
        new BaseResponseDTO<>(200, "Package updated", new Date(), updated)
    );
}
```

### Example 4: Create Activity (Vendor-only endpoint)

```java
@PostMapping("/api/activities")
public ResponseEntity<?> createActivity(
    @AuthenticationPrincipal AuthenticatedUser user,
    @RequestBody CreateActivityRequestDTO request) {
    
    // Check if user is vendor
    if (!user.isVendor() && !user.isSuperadmin()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of(
                "status", 403,
                "message", "Only vendors can create activities"
            ));
    }
    
    // Auto-set vendorId from token
    Activity activity = Activity.builder()
        .activityName(request.getActivityName())
        .vendorId(user.getVendorId())  // ✅ From token
        .price(request.getPrice())
        // ...
        .build();
    
    Activity saved = activityRepository.save(activity);
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of(
            "status", 201,
            "message", "Activity created",
            "data", saved
        ));
}
```

---

## 🔧 Service Layer Pattern

### Before
```java
public PackageResponseDTO createPackage(CreatePackageRequestDTO request) {
    // ❌ userId must be in request
    String userId = request.getUserId();
    
    Package pkg = Package.builder()
        .userId(userId)
        .packageName(request.getPackageName())
        .build();
    
    return save(pkg);
}
```

### After
```java
public PackageResponseDTO createPackage(CreatePackageRequestDTO request, String userId) {
    // ✅ userId passed from controller (from token)
    Package pkg = Package.builder()
        .userId(userId)  // From token
        .packageName(request.getPackageName())
        .build();
    
    return save(pkg);
}
```

---

## 🎯 Migration Checklist

### Step 1: Remove userId from DTOs
```java
// ❌ Before
public class CreatePackageRequestDTO {
    private String userId;  // Remove this!
    private String packageName;
    // ...
}

// ✅ After
public class CreatePackageRequestDTO {
    // No userId field!
    private String packageName;
    // ...
}
```

### Step 2: Update Controller Methods

#### Pattern A: Create/POST endpoints
```java
// Add @AuthenticationPrincipal parameter
// Pass user.getId() to service

@PostMapping
public ResponseEntity<?> create(
    @AuthenticationPrincipal AuthenticatedUser user,
    @RequestBody CreateDTO request) {
    
    ResultDTO result = service.create(request, user.getId());  // ✅
    return ResponseEntity.ok(result);
}
```

#### Pattern B: Update/PUT endpoints
```java
// Add authorization check before update

@PutMapping("/{id}")
public ResponseEntity<?> update(
    @AuthenticationPrincipal AuthenticatedUser user,
    @PathVariable String id,
    @RequestBody UpdateDTO request) {
    
    // Check ownership
    Entity existing = repository.findById(id).orElseThrow();
    
    if (!user.isSuperadmin() && !existing.getUserId().equals(user.getId())) {
        return ResponseEntity.status(403).body("Forbidden");
    }
    
    ResultDTO result = service.update(id, request);
    return ResponseEntity.ok(result);
}
```

#### Pattern C: Get/List endpoints
```java
// Use user.getId() for filtering

@GetMapping
public ResponseEntity<?> getAll(
    @AuthenticationPrincipal AuthenticatedUser user) {
    
    List<DTO> results;
    
    if (user.hasAdminPrivileges()) {
        results = service.getAll();
    } else {
        results = service.getAllByUserId(user.getId());  // ✅ From token
    }
    
    return ResponseEntity.ok(results);
}
```

### Step 3: Update Service Methods

```java
// Add userId parameter where needed

public interface PackageService {
    // ✅ Add userId parameter
    PackageDTO create(CreatePackageDTO request, String userId);
    
    // Keep existing methods
    PackageDTO getById(String id);
    List<PackageDTO> getAll();
    List<PackageDTO> getAllByUserId(String userId);  // ✅ Add this
}
```

---

## 🛡️ Security Best Practices

### 1. Always validate ownership
```java
if (!user.isSuperadmin() && !resource.getUserId().equals(user.getId())) {
    throw new ForbiddenException("You can only access your own resources");
}
```

### 2. Use helper methods for role checks
```java
// ✅ Good
if (user.hasAdminPrivileges()) { ... }
if (user.isCustomer()) { ... }
if (user.isVendor()) { ... }

// ❌ Avoid
if (user.getRole().equals("Superadmin") || user.getRole().equals("TourPackageVendor")) { ... }
```

### 3. Log user actions
```java
logger.info("User {} ({}) is creating package: {}", 
    user.getId(), user.getEmail(), request.getPackageName());
```

### 4. Handle missing authentication
```java
@GetMapping
public ResponseEntity<?> getAll(
    @AuthenticationPrincipal AuthenticatedUser user) {
    
    if (user == null) {
        return ResponseEntity.status(401)
            .body("Authentication required");
    }
    
    // Process request...
}
```

---

## 📱 Frontend Integration

### Store Token After Login
```javascript
// After successful login
const loginResponse = await axios.post('/api/auth/login', { username, password });

// Store token
localStorage.setItem('token', loginResponse.data.token);
localStorage.setItem('refreshToken', loginResponse.data.refreshToken);

// Store user info (optional - for UI display)
localStorage.setItem('user', JSON.stringify({
  id: loginResponse.data.id,
  role: loginResponse.data.role,
  name: loginResponse.data.name,
  email: loginResponse.data.email
}));
```

### Send Token with Every Request
```javascript
// Set up axios interceptor
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Now all requests automatically include token
await axios.get('/api/package');  // ✅ Token sent automatically
await axios.post('/api/package', { packageName: "Bali" });  // ✅ No userId needed
```

### Remove userId from Payloads
```javascript
// ❌ Before
const createPackage = async (data) => {
  await axios.post('/api/package', {
    userId: currentUser.id,  // Remove this!
    packageName: data.name,
    destination: data.destination
  });
};

// ✅ After
const createPackage = async (data) => {
  await axios.post('/api/package', {
    // No userId - backend gets it from token!
    packageName: data.name,
    destination: data.destination
  });
};
```

---

## 🧪 Testing

### Test with Bruno/Postman

```http
### 1. Login
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "customer1",
  "password": "password123"
}

### Response
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "...",
  "id": "f5dad850-118b-4bb5-a3c5-0c1e2fa84f16",
  "role": "Customer",
  "username": "customer1",
  "email": "customer1@example.com",
  "name": "John Customer"
}

### 2. Create Package (with token - NO userId in body)
POST http://localhost:8080/api/package
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "packageName": "Bali Adventure",
  "destination": "Bali",
  "price": 5000000
}

### Backend automatically gets userId from token!
```

---

## ✅ Summary

| Aspect | Before | After |
|--------|--------|-------|
| **User ID Source** | Request body/param | JWT token (automatic) |
| **Frontend sends** | `userId` in every request | Only JWT token in header |
| **Controller** | Manual SecurityContext extraction | `@AuthenticationPrincipal` injection |
| **Security** | Manual role checking | Helper methods on `AuthenticatedUser` |
| **Service Layer** | Gets userId from DTO | Gets userId from parameter |
| **Type Safety** | String from Map (unsafe) | Type-safe `AuthenticatedUser` object |

### Key Benefits
- ✅ **More Secure**: Frontend can't fake userId
- ✅ **Cleaner Code**: No manual SecurityContext extraction
- ✅ **Type Safe**: AuthenticatedUser is strongly typed
- ✅ **DRY**: Reusable helper methods (isCustomer(), hasAdminPrivileges())
- ✅ **Maintainable**: Single source of truth (JWT token)

---

## 🚀 Next Steps

1. **Remove userId from all Request DTOs**
2. **Update all controller methods** to use `@AuthenticationPrincipal AuthenticatedUser user`
3. **Update service methods** to accept `String userId` parameter
4. **Remove manual SecurityContext extraction** methods (getCurrentUserId(), getCurrentUserRole())
5. **Update frontend** to stop sending userId in request bodies
6. **Test thoroughly** with different roles

Happy coding! 🎉
