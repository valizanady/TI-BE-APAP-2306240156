package apap.ti._5.tour_package_2306240156_be.restservice.paymentmethod;

import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.restdto.request.paymentmethod.CreatePaymentMethodRequestDTO;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodRestService {
    
    /**
     * Get all payment methods (not deleted)
     */
    List<PaymentMethod> getAllPaymentMethods();
    
    /**
     * Get payment methods by status
     */
    List<PaymentMethod> getPaymentMethodsByStatus(String status);
    
    /**
     * Get payment method by id
     */
    PaymentMethod getPaymentMethodById(UUID id);
    
    /**
     * Create new payment method (Superadmin only)
     */
    PaymentMethod createPaymentMethod(CreatePaymentMethodRequestDTO requestDTO);
    
    /**
     * Update payment method status
     */
    PaymentMethod updatePaymentMethodStatus(UUID id, String status);
    
    /**
     * Delete payment method (soft delete)
     */
    void deletePaymentMethod(UUID id);
}
