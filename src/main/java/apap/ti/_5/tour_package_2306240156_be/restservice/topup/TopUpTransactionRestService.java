package apap.ti._5.tour_package_2306240156_be.restservice.topup;

import apap.ti._5.tour_package_2306240156_be.model.TopUpTransaction;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.CreateTopUpTransactionRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.topup.UpdateTopUpStatusRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TopUpTransactionRestService {
    
    List<TopUpTransaction> getAllTransactions(String role, UUID userId);
    
    TopUpTransaction getTransactionById(UUID id);
    
    TopUpTransaction createTransaction(CreateTopUpTransactionRequestDTO requestDTO, String customerUsername);
    
    TopUpTransaction updateTransactionStatus(UUID id, UpdateTopUpStatusRequestDTO requestDTO, String token);
    
    void deleteTransaction(UUID id);
}