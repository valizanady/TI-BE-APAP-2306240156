package apap.ti._5.tour_package_2306240156_be.restcontroller.topup;

import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.restdto.request.paymentmethod.CreatePaymentMethodRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.paymentmethod.PaymentMethodRestService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodRestController {

    @Autowired
    private PaymentMethodRestService paymentMethodRestService;

    /**
     * GET /api/payment-methods
     * Get all payment methods
     * Access: Customer & Superadmin (Customer needs this for top-up dropdown)
     * 
     * Usage:
     * - Customer: GET /api/payment-methods?status=Active (for dropdown in top-up form)
     * - Superadmin: GET /api/payment-methods (all payment methods for management)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('Customer', 'Superadmin')")
    public ResponseEntity<BaseResponseDTO<List<PaymentMethod>>> getAllPaymentMethods(
            @RequestParam(required = false) String status
    ) {
        List<PaymentMethod> paymentMethods;
        
        if (status != null && !status.isEmpty()) {
            paymentMethods = paymentMethodRestService.getPaymentMethodsByStatus(status);
        } else {
            paymentMethods = paymentMethodRestService.getAllPaymentMethods();
        }
        
        BaseResponseDTO<List<PaymentMethod>> response = new BaseResponseDTO<>();
        response.setStatus(HttpStatus.OK.value());
        response.setData(paymentMethods);
        response.setMessage("Payment methods retrieved successfully");
        response.setTimestamp(new Date());
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/payment-methods/{id}
     * Get payment method by ID
     * Access: Customer & Superadmin (Customer might need payment method details)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Customer', 'Superadmin')")
    public ResponseEntity<BaseResponseDTO<PaymentMethod>> getPaymentMethodById(@PathVariable UUID id) {
        PaymentMethod paymentMethod = paymentMethodRestService.getPaymentMethodById(id);
        
        BaseResponseDTO<PaymentMethod> response = new BaseResponseDTO<>();
        response.setStatus(HttpStatus.OK.value());
        response.setData(paymentMethod);
        response.setMessage("Payment method retrieved successfully");
        response.setTimestamp(new Date());
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/payment-methods
     * Create new payment method
     * Access: Superadmin only
     */
    @PostMapping
    @PreAuthorize("hasRole('Superadmin')")
    public ResponseEntity<BaseResponseDTO<PaymentMethod>> createPaymentMethod(
            @Valid @RequestBody CreatePaymentMethodRequestDTO requestDTO
    ) {
        PaymentMethod paymentMethod = paymentMethodRestService.createPaymentMethod(requestDTO);
        
        BaseResponseDTO<PaymentMethod> response = new BaseResponseDTO<>();
        response.setStatus(HttpStatus.CREATED.value());
        response.setData(paymentMethod);
        response.setMessage("Payment method created successfully");
        response.setTimestamp(new Date());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/payment-methods/{id}/status
     * Update payment method status
     * Access: Superadmin only
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('Superadmin')")
    public ResponseEntity<BaseResponseDTO<PaymentMethod>> updatePaymentMethodStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request
    ) {
        String status = request.get("status");
        PaymentMethod paymentMethod = paymentMethodRestService.updatePaymentMethodStatus(id, status);
        
        BaseResponseDTO<PaymentMethod> response = new BaseResponseDTO<>();
        response.setStatus(HttpStatus.OK.value());
        response.setData(paymentMethod);
        response.setMessage("Payment method status updated successfully");
        response.setTimestamp(new Date());
        
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/payment-methods/{id}
     * Delete payment method (soft delete)
     * Access: Superadmin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Superadmin')")
    public ResponseEntity<BaseResponseDTO<Void>> deletePaymentMethod(@PathVariable UUID id) {
        paymentMethodRestService.deletePaymentMethod(id);
        
        BaseResponseDTO<Void> response = new BaseResponseDTO<>();
        response.setStatus(HttpStatus.OK.value());
        response.setData(null);
        response.setMessage("Payment method deleted successfully");
        response.setTimestamp(new Date());
        
        return ResponseEntity.ok(response);
    }
}
