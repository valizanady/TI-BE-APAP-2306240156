package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.restdto.external.ProfileResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.external.UpdateProfileRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ProfileServiceClient {

    private final RestTemplate restTemplate;

    private final String PROFILE_BASE_URL = "https://acc-be.beel.my.id/api";

    /**
     * Get user profile dari Profile Service menggunakan JWT token
     * Endpoint: GET /api/auth/me
     * 
     * @param token JWT access token (without "Bearer " prefix)
     * @return ProfileResponseDTO or null if invalid
     */
    public ProfileResponseDTO getUserProfile(String token) {
        try {
            System.out.println("📞 Getting user profile from Profile Service");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<BaseResponseDTO<ProfileResponseDTO>> response = restTemplate.exchange(
                    PROFILE_BASE_URL + "/auth/me",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<BaseResponseDTO<ProfileResponseDTO>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("✅ User profile retrieved successfully");
                return response.getBody().getData();
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to get user profile: " + e.getMessage());
            throw new RuntimeException("Gagal terhubung ke layanan Profile untuk cek data user.");
        }
        return null;
    }

    /**
     * Update user balance di Profile Service
     * Endpoint: PUT /api/profile/update
     * 
     * @param token JWT access token
     * @param userId User ID
     * @param newBalance New balance amount
     * @param currentProfile Current profile data
     */
    public void updateUserBalance(String token, String userId, Long newBalance, ProfileResponseDTO currentProfile) {
        try {
            System.out.println("📞 Updating user balance | UserId: " + userId + " | New Balance: " + newBalance);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                    .id(userId)
                    .username(currentProfile.getUsername())
                    .name(currentProfile.getName())
                    .email(currentProfile.getEmail())
                    .saldo(newBalance)
                    .gender(currentProfile.getGender())
                    .build();

            HttpEntity<UpdateProfileRequestDTO> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(
                    PROFILE_BASE_URL + "/profile/update",
                    HttpMethod.PUT,
                    entity,
                    Object.class
            );
            
            System.out.println("✅ User balance updated successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to update user balance: " + e.getMessage());
            throw new RuntimeException("Gagal update saldo user: " + e.getMessage());
        }
    }

    /**
     * Add balance to user (helper method)
     * 
     * @param token JWT token
     * @param amount Amount to add
     */
    public void addBalance(String token, Long amount) {
        ProfileResponseDTO profile = getUserProfile(token);
        if (profile != null) {
            Long newBalance = profile.getSaldo() + amount;
            updateUserBalance(token, profile.getId(), newBalance, profile);
        }
    }

    /**
     * Deduct balance from user (helper method)
     * 
     * @param token JWT token
     * @param amount Amount to deduct
     */
    public void deductBalance(String token, Long amount) {
        ProfileResponseDTO profile = getUserProfile(token);
        if (profile != null) {
            Long currentBalance = profile.getSaldo();
            if (currentBalance < amount) {
                throw new RuntimeException("Saldo tidak mencukupi. Saldo saat ini: " + currentBalance);
            }
            Long newBalance = currentBalance - amount;
            updateUserBalance(token, profile.getId(), newBalance, profile);
        }
    }

    /**
     * Get customer profile by username using Superadmin token
     * Endpoint: GET /api/profile/{username}
     * 
     * @param username Customer username
     * @param token Superadmin JWT token
     * @return ProfileResponseDTO or null if not found
     */
    public ProfileResponseDTO getCustomerProfileByUsername(String username, String token) {
        try {
            System.out.println("📞 Getting customer profile by username from Profile Service | Username: " + username);
            
            String url = PROFILE_BASE_URL + "/profile/" + username;
            
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // First, get raw response as String to debug
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            System.out.println("📥 Raw Response from Profile Service:");
            System.out.println(rawResponse.getBody());
            
            // Now parse properly
            ResponseEntity<BaseResponseDTO<ProfileResponseDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<BaseResponseDTO<ProfileResponseDTO>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("✅ Customer profile retrieved successfully");
                return response.getBody().getData();
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ Failed to get customer profile by username: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Failed to get customer profile: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get customer profile by ID
     * Since Profile Service doesn't have direct endpoint for GET by ID,
     * we fetch all customers and find the matching one
     * 
     * Endpoint: GET /api/profile/customer (returns list of all customers)
     * 
     * @param customerId Customer UUID
     * @param token Superadmin JWT token
     * @return ProfileResponseDTO or null if not found
     */
    public ProfileResponseDTO getCustomerProfileById(String customerId, String token) {
        try {
            System.out.println("📞 Getting customer profile by ID from Profile Service | ID: " + customerId);
            System.out.println("   Note: Profile Service doesn't have GET /api/profile/id/{id} endpoint");
            System.out.println("   Fetching all customers and filtering by ID...");
            
            String url = PROFILE_BASE_URL + "/profile/customer";
            
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Response is BaseResponseDTO<List<ProfileResponseDTO>>
            ResponseEntity<BaseResponseDTO<java.util.List<ProfileResponseDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<BaseResponseDTO<java.util.List<ProfileResponseDTO>>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                java.util.List<ProfileResponseDTO> customers = response.getBody().getData();
                
                // Find customer with matching ID
                ProfileResponseDTO matchedCustomer = customers.stream()
                        .filter(customer -> customer.getId().equals(customerId))
                        .findFirst()
                        .orElse(null);
                
                if (matchedCustomer != null) {
                    System.out.println("✅ Customer profile found | Username: " + matchedCustomer.getUsername());
                    return matchedCustomer;
                } else {
                    System.err.println("❌ Customer with ID " + customerId + " not found in customer list");
                }
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ Failed to get customer list: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Failed to get customer profile: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Add balance to user profile by customer username (for Top Up approval)
     * 
     * Endpoint: PUT /api/profile/update/saldo
     * Behavior: REPLACES total balance (not incremental!)
     * Process:
     *   1. Fetch current balance via GET /api/profile/{username}
     *   2. Calculate new balance = current + amount
     *   3. Send new total to PUT /api/profile/update/saldo
     * 
     * Uses Superadmin JWT token for authentication
     * 
     * @param customerUsername Customer username
     * @param amount Amount to ADD to current balance
     * @param token JWT token (from Superadmin who approves the transaction)
     * @return true if successful, false otherwise
     */
    public boolean addBalanceToProfile(String customerUsername, Long amount, String token) {
        try {
            System.out.println("📞 Adding balance to profile | Customer: " + customerUsername + " | Amount to add: " + amount);
            
            // Step 1: Get current customer profile to fetch current balance
            System.out.println("   Step 1: Fetching customer profile to get current balance...");
            
            ProfileResponseDTO customerProfile = getCustomerProfileByUsername(customerUsername, token);
            
            if (customerProfile == null) {
                System.err.println("❌ Cannot fetch customer profile. Unable to update balance.");
                System.err.println("   Customer username: " + customerUsername);
                System.err.println("   Check if username exists and Superadmin has access to /api/profile/{username}");
                return false;
            }
            
            Long currentBalance = customerProfile.getSaldo();
            Long newBalance = currentBalance + amount;
            
            System.out.println("   ✅ Current balance: " + currentBalance);
            System.out.println("   ➕ Amount to add: " + amount);
            System.out.println("   💰 New balance will be: " + newBalance);
            
            // Step 2: Update balance with new total
            System.out.println("   Step 2: Updating balance to new total...");
            String updateUrl = PROFILE_BASE_URL + "/profile/update/saldo";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (token != null && !token.isEmpty()) {
                headers.setBearerAuth(token);
            }
            
            // Request body with NEW TOTAL balance (not increment)
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("id", customerProfile.getId());  // Use ID from profile
            requestBody.put("saldo", newBalance);  // Send NEW TOTAL, not the increment amount
            
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            System.out.println("📤 Request to Profile Service:");
            System.out.println("   URL: " + updateUrl);
            System.out.println("   Body: {id: " + customerProfile.getId() + ", saldo: " + newBalance + "}");
            
            // Call Profile Service to update balance
            ResponseEntity<BaseResponseDTO<Object>> response = restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<BaseResponseDTO<Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Balance updated successfully!");
                System.out.println("   👤 Customer: " + customerUsername + " (ID: " + customerProfile.getId() + ")");
                System.out.println("   📊 " + currentBalance + " + " + amount + " = " + newBalance);
                return true;
            } else {
                System.err.println("❌ Failed to update balance. Status: " + response.getStatusCode());
                return false;
            }
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                System.err.println("   🔒 Authentication/Authorization failed. Check if:");
                System.err.println("      1. Token is valid");
                System.err.println("      2. Superadmin has permission to access /api/profile/{username} and /api/profile/update/saldo");
            }
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Failed to add balance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}