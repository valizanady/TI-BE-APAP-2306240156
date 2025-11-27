package apap.ti._5.tour_package_2306240156_be.restservice.paymentmethod;

import apap.ti._5.tour_package_2306240156_be.exception.BadRequestException;
import apap.ti._5.tour_package_2306240156_be.exception.NotFoundException;
import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.repository.PaymentMethodRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.paymentmethod.CreatePaymentMethodRequestDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentMethodRestServiceImpl implements PaymentMethodRestService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<PaymentMethod> getAllPaymentMethods() {
        System.out.println("🎯 GET /payment-methods | Fetching all payment methods");
        return paymentMethodRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Override
    public List<PaymentMethod> getPaymentMethodsByStatus(String status) {
        System.out.println("🎯 GET /payment-methods?status=" + status);
        return paymentMethodRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(status);
    }

    @Override
    public PaymentMethod getPaymentMethodById(UUID id) {
        System.out.println("🎯 GET /payment-methods/" + id);
        
        return paymentMethodRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Payment method not found"));
    }

    @Override
    public PaymentMethod createPaymentMethod(CreatePaymentMethodRequestDTO requestDTO) {
        System.out.println("🎯 POST /payment-methods | Method: " + requestDTO.getMethodName() + 
                          " | Provider: " + requestDTO.getProvider());
        
        // Validate input
        if (requestDTO.getMethodName() == null || requestDTO.getMethodName().trim().isEmpty()) {
            throw new BadRequestException("Method name is required");
        }
        
        if (requestDTO.getProvider() == null || requestDTO.getProvider().trim().isEmpty()) {
            throw new BadRequestException("Provider is required");
        }
        
        // Create new payment method
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setMethodName(requestDTO.getMethodName().trim());
        paymentMethod.setProvider(requestDTO.getProvider().trim());
        paymentMethod.setStatus("Active"); // Default status
        
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        
        System.out.println("✅ Payment method created successfully | ID: " + saved.getId());
        
        return saved;
    }

    @Override
    public PaymentMethod updatePaymentMethodStatus(UUID id, String status) {
        System.out.println("🎯 PUT /payment-methods/" + id + "/status | New status: " + status);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Payment method not found"));
        
        // Validate status
        if (!"Active".equalsIgnoreCase(status) && !"Inactive".equalsIgnoreCase(status)) {
            throw new BadRequestException("Invalid status. Must be 'Active' or 'Inactive'");
        }
        
        paymentMethod.setStatus(status);
        
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    public void deletePaymentMethod(UUID id) {
        System.out.println("🎯 DELETE /payment-methods/" + id + " | Soft delete");
        
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Payment method not found"));
        
        paymentMethod.setDeletedAt(LocalDateTime.now());
        paymentMethodRepository.save(paymentMethod);
        
        System.out.println("✅ Payment method deleted successfully");
    }
}
