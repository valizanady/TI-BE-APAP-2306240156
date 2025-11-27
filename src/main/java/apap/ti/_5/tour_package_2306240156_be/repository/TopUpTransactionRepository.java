package apap.ti._5.tour_package_2306240156_be.repository;

import apap.ti._5.tour_package_2306240156_be.model.TopUpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopUpTransactionRepository extends JpaRepository<TopUpTransaction, UUID> {
    
    List<TopUpTransaction> findByDeletedAtIsNullOrderByCreatedAtDesc();
    
    List<TopUpTransaction> findByCustomerIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID customerId);
    
    Optional<TopUpTransaction> findByIdAndDeletedAtIsNull(UUID id);
}