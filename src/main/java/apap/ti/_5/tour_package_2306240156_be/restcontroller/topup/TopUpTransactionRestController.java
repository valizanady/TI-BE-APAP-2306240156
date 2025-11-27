package apap.ti._5.tour_package_2306240156_be.restcontroller.topup;

import apap.ti._5.tour_package_2306240156_be.exception.ForbiddenException;
import apap.ti._5.tour_package_2306240156_be.model.TopUpTransaction;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.CreateTopUpTransactionRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.UpdateTopUpStatusRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.topup.TopUpTransactionResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.topup.TopUpTransactionRestService;
import apap.ti._5.tour_package_2306240156_be.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
public class TopUpTransactionRestController {

    @Autowired
    private TopUpTransactionRestService topUpTransactionRestService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Get current user role from SecurityContext or JWT token
     */
    private String getCurrentUserRole(HttpServletRequest request) {
        // Try from SecurityContext first (set by JwtTokenFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
            String role = (String) details.get("role");
            if (role != null) {
                return role;
            }
        }

        // Fallback: parse from token directly
        String token = extractTokenFromRequest(request);
        if (token != null) {
            return jwtUtils.getRoleFromToken(token);
        }

        return null;
    }

    /**
     * Get current user ID from SecurityContext or JWT token
     */
    private UUID getCurrentUserId(HttpServletRequest request) {
        // Try from SecurityContext first
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
            String userId = (String) details.get("id");
            if (userId != null) {
                return UUID.fromString(userId);
            }
        }

        // Fallback: parse from token directly
        String token = extractTokenFromRequest(request);
        if (token != null) {
            String userId = jwtUtils.getUserIdFromToken(token);
            if (userId != null) {
                return UUID.fromString(userId);
            }
        }

        return null;
    }

    /**
     * Get current username from SecurityContext or JWT token
     */
    private String getCurrentUsername(HttpServletRequest request) {
        // Try from SecurityContext first
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
            String username = (String) details.get("username");
            if (username != null) {
                return username;
            }
        }

        // Fallback: parse from token directly
        String token = extractTokenFromRequest(request);
        if (token != null) {
            return jwtUtils.getUsernameFromToken(token);
        }

        return null;
    }

    /**
     * GET /transactions
     * Get all transactions (filtered by role)
     * Access: Customer (own transactions only) or Superadmin (all transactions)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('Customer', 'Superadmin')")
    public ResponseEntity<BaseResponseDTO<List<TopUpTransactionResponseDTO>>> getAllTransactions(HttpServletRequest request) {
        try {
            String role = getCurrentUserRole(request);
            UUID userId = getCurrentUserId(request);
            
            System.out.println("🔍 GET /transactions - Controller");
            System.out.println("   Role from controller: " + role);
            System.out.println("   UserId from controller: " + userId);
            
            List<TopUpTransaction> transactions = topUpTransactionRestService.getAllTransactions(role, userId);
            
            System.out.println("   Transactions found: " + transactions.size());
            
            List<TopUpTransactionResponseDTO> response = transactions.stream()
                    .map(TopUpTransactionResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                    BaseResponseDTO.<List<TopUpTransactionResponseDTO>>builder()
                            .status(HttpStatus.OK.value())
                            .message("Transactions retrieved successfully")
                            .data(response)
                            .build()
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDTO.<List<TopUpTransactionResponseDTO>>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * GET /transactions/{id}
     * Get transaction by ID
     * Access: Superadmin only
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Superadmin')")
    public ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> getTransactionById(
            @PathVariable UUID id,
            HttpServletRequest request) {
        try {
            String role = getCurrentUserRole(request);
            
            if (!"Superadmin".equalsIgnoreCase(role)) {
                throw new ForbiddenException("Unauthorized access");
            }
            
            TopUpTransaction transaction = topUpTransactionRestService.getTransactionById(id);
            
            return ResponseEntity.ok(
                    BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Transaction retrieved successfully")
                            .data(TopUpTransactionResponseDTO.fromEntity(transaction))
                            .build()
            );
            
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * POST /transactions
     * Create new top-up transaction
     * Access: Customer only
     */
    @PostMapping
    @PreAuthorize("hasRole('Customer')")
    public ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> createTransaction(
            @Valid @RequestBody CreateTopUpTransactionRequestDTO requestDTO,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
                return ResponseEntity.badRequest().body(
                        BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(errorMessage)
                                .data(null)
                                .build()
                );
            }
            
            // Security: Customer hanya bisa create transaction untuk diri sendiri
            UUID currentUserId = getCurrentUserId(request);
            if (currentUserId != null && !currentUserId.equals(requestDTO.getCustomerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message("You can only create transactions for yourself")
                                .data(null)
                                .build()
                );
            }
            
            // Get customer username from JWT token
            String customerUsername = getCurrentUsername(request);
            
            System.out.println("🔍 DEBUG Create Transaction:");
            System.out.println("   Current User ID: " + currentUserId);
            System.out.println("   Current Username: " + customerUsername);
            
            // Validate username
            if (customerUsername == null || customerUsername.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Unable to extract username from token. Please login again.")
                                .data(null)
                                .build()
                );
            }
            
            TopUpTransaction created = topUpTransactionRestService.createTransaction(requestDTO, customerUsername);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                            .status(HttpStatus.CREATED.value())
                            .message("Top-up transaction created successfully")
                            .data(TopUpTransactionResponseDTO.fromEntity(created))
                            .build()
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * PUT /transactions/{id}/status
     * Update transaction status (Approve/Reject)
     * Access: Superadmin only
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('Superadmin')")
    public ResponseEntity<BaseResponseDTO<TopUpTransactionResponseDTO>> updateTransactionStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTopUpStatusRequestDTO requestDTO,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        try {
            String role = getCurrentUserRole(request);
            
            if (!"Superadmin".equalsIgnoreCase(role)) {
                throw new ForbiddenException("Unauthorized access");
            }
            
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
                return ResponseEntity.badRequest().body(
                        BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(errorMessage)
                                .data(null)
                                .build()
                );
            }
            
            // Extract JWT token untuk digunakan saat update balance di Profile Service
            String token = extractTokenFromRequest(request);
            
            TopUpTransaction updated = topUpTransactionRestService.updateTransactionStatus(id, requestDTO, token);
            
            return ResponseEntity.ok(
                    BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Transaction status updated successfully")
                            .data(TopUpTransactionResponseDTO.fromEntity(updated))
                            .build()
            );
            
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDTO.<TopUpTransactionResponseDTO>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * DELETE /transactions/{id}
     * Delete transaction (soft delete)
     * Access: Superadmin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Superadmin')")
    public ResponseEntity<BaseResponseDTO<String>> deleteTransaction(
            @PathVariable UUID id,
            HttpServletRequest request) {
        try {
            String role = getCurrentUserRole(request);
            
            if (!"Superadmin".equalsIgnoreCase(role)) {
                throw new ForbiddenException("Unauthorized access");
            }
            
            topUpTransactionRestService.deleteTransaction(id);
            
            return ResponseEntity.ok(
                    BaseResponseDTO.<String>builder()
                            .status(HttpStatus.OK.value())
                            .message("Transaction deleted successfully")
                            .data(null)
                            .build()
            );
            
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    BaseResponseDTO.<String>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDTO.<String>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}
