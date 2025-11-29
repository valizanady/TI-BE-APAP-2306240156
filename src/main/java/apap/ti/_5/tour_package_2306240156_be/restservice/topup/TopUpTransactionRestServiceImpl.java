package apap.ti._5.tour_package_2306240156_be.restservice.topup;

import apap.ti._5.tour_package_2306240156_be.exception.BadRequestException;
import apap.ti._5.tour_package_2306240156_be.exception.NotFoundException;
import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import apap.ti._5.tour_package_2306240156_be.model.TopUpTransaction;
import apap.ti._5.tour_package_2306240156_be.repository.PaymentMethodRepository;
import apap.ti._5.tour_package_2306240156_be.repository.TopUpTransactionRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.CreateTopUpTransactionRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.UpdateTopUpStatusRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.ProfileServiceClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TopUpTransactionRestServiceImpl implements TopUpTransactionRestService {

    @Autowired
    private TopUpTransactionRepository topUpTransactionRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private ProfileServiceClient profileServiceClient;

    @Override
    public List<TopUpTransaction> getAllTransactions(String role, UUID userId) {
        System.out.println("🎯 GET /transactions | Role: " + role + " | UserId: " + userId);
        
        if ("Superadmin".equalsIgnoreCase(role)) {
            return topUpTransactionRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
        } else {
            // Customer → only their transactions
            return topUpTransactionRepository.findByCustomerIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        }
    }

    @Override
    public TopUpTransaction getTransactionById(UUID id) {
        System.out.println("🎯 GET /transactions/" + id);
        
        return topUpTransactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
    }

    @Override
    public TopUpTransaction createTransaction(CreateTopUpTransactionRequestDTO requestDTO, String customerUsername) {
        System.out.println("🎯 POST /transactions | Amount: " + requestDTO.getAmount() + " | Customer: " + customerUsername);
        
        // Validate amount
        if (requestDTO.getAmount() == null || requestDTO.getAmount() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        
        // Validate payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndDeletedAtIsNull(requestDTO.getPaymentMethodId())
                .orElseThrow(() -> new NotFoundException("Payment method not found"));
        
        if (!"Active".equalsIgnoreCase(paymentMethod.getStatus())) {
            throw new BadRequestException("Payment method is not active");
        }
        
        TopUpTransaction transaction = new TopUpTransaction();
        transaction.setCustomerId(requestDTO.getCustomerId());
        transaction.setCustomerUsername(customerUsername);  // Save username for later use
        transaction.setAmount(requestDTO.getAmount());
        transaction.setPaymentMethod(paymentMethod);
        transaction.setStatus("Pending");
        
        return topUpTransactionRepository.save(transaction);
    }

    @Override
    public TopUpTransaction updateTransactionStatus(UUID id, UpdateTopUpStatusRequestDTO requestDTO, String token) {
        System.out.println("🎯 PUT /transactions/" + id + "/status | New status: " + requestDTO.getStatus());
        
        TopUpTransaction transaction = topUpTransactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        
        String currentStatus = transaction.getStatus();
        String newStatus = requestDTO.getStatus();
        
        System.out.println("   Current status: " + currentStatus + " → New status: " + newStatus);
        
        // Prevent duplicate balance addition if transaction is already approved
        if ("Success".equalsIgnoreCase(newStatus)) {
            if ("Success".equalsIgnoreCase(currentStatus)) {
                System.out.println("⚠️  Transaction already approved. Skipping balance update to prevent duplicate addition.");
                System.out.println("   Transaction ID: " + id);
                System.out.println("   Customer: " + transaction.getCustomerUsername());
                System.out.println("   Amount: " + transaction.getAmount());
                // Don't add balance again, just return the transaction as-is
                return transaction;
            }
            
            // Transaction is being approved for the first time - add balance
            System.out.println("✅ Approving transaction for the first time. Adding balance...");
            boolean success = profileServiceClient.addBalanceToProfile(
                    transaction.getCustomerUsername(),  // Pass username instead of ID
                    transaction.getAmount(),
                    token
            );
            
            if (!success) {
                throw new BadRequestException("Failed to update balance in Profile Service");
            }
            
            System.out.println("✅ Balance updated successfully for customer: " + transaction.getCustomerUsername());
        }
        
        transaction.setStatus(newStatus);
        
        return topUpTransactionRepository.save(transaction);
    }

    @Override
    public void deleteTransaction(UUID id) {
        System.out.println("🎯 DELETE /transactions/" + id + " | Soft delete");
        
        TopUpTransaction transaction = topUpTransactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        
        transaction.setDeletedAt(LocalDateTime.now());
        topUpTransactionRepository.save(transaction);
    }
}