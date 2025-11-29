package apap.ti._5.tour_package_2306240156_be.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * AuthenticatedUser - Wrapper untuk user yang sudah terautentikasi
 * 
 * Objek ini di-inject ke SecurityContext oleh JwtTokenFilter setelah validasi token berhasil.
 * Bisa diakses di controller menggunakan @AuthenticationPrincipal AuthenticatedUser user
 * 
 * Properties:
 * - id: User ID dari token (UUID string)
 * - username: Username dari token
 * - email: Email dari token
 * - name: Full name dari token
 * - role: Role dari token (Customer, Superadmin, TourPackageVendor, etc.)
 * 
 * Usage in Controller:
 * ```
 * @PostMapping
 * public ResponseEntity<?> createPackage(
 *     @AuthenticationPrincipal AuthenticatedUser user,
 *     @RequestBody CreatePackageRequestDTO request) {
 *     
 *     String userId = user.getId();  // Get user ID from token
 *     String role = user.getRole();   // Get user role from token
 *     
 *     // ... your logic
 * }
 * ```
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticatedUser implements UserDetails {
    
    /**
     * User ID dari JWT token (UUID string)
     * Digunakan sebagai:
     * - customerId jika role = Customer
     * - adminId/vendorId jika role = Superadmin/Vendor
     */
    private String id;
    
    /**
     * Username dari JWT token
     */
    private String username;
    
    /**
     * Email dari JWT token
     */
    private String email;
    
    /**
     * Full name dari JWT token
     */
    private String name;
    
    /**
     * Role dari JWT token
     * Values: Customer, Superadmin, TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor
     */
    private String role;
    
    // ==================== UserDetails Implementation ====================
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return role dengan prefix "ROLE_" untuk kompatibilitas dengan Spring Security
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role)
        );
    }
    
    @Override
    public String getPassword() {
        // JWT token tidak butuh password
        return "";
    }
    
    @Override
    public String getUsername() {
        return this.username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Check if user is Customer
     */
    public boolean isCustomer() {
        return "Customer".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user is Superadmin
     */
    public boolean isSuperadmin() {
        return "Superadmin".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user is any kind of vendor
     * (TourPackageVendor, FlightAirline, AccommodationOwner, RentalVendor)
     */
    public boolean isVendor() {
        return "TourPackageVendor".equalsIgnoreCase(this.role) ||
               "FlightAirline".equalsIgnoreCase(this.role) ||
               "AccommodationOwner".equalsIgnoreCase(this.role) ||
               "RentalVendor".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user is TourPackageVendor
     */
    public boolean isTourPackageVendor() {
        return "TourPackageVendor".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user has admin privileges (Superadmin or any Vendor)
     */
    public boolean hasAdminPrivileges() {
        return isSuperadmin() || isVendor();
    }
    
    /**
     * Get customerId (alias for getId())
     * Semantic method for Customer role
     */
    public String getCustomerId() {
        return this.id;
    }
    
    /**
     * Get vendorId (alias for getId())
     * Semantic method for Vendor roles
     */
    public String getVendorId() {
        return this.id;
    }
}
