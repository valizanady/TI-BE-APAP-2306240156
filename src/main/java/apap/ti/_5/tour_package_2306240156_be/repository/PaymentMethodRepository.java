package apap.ti._5.tour_package_2306240156_be.repository;

import apap.ti._5.tour_package_2306240156_be.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    
    List<PaymentMethod> findByDeletedAtIsNullOrderByCreatedAtDesc();
    
    List<PaymentMethod> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(String status);
    
    Optional<PaymentMethod> findByIdAndDeletedAtIsNull(UUID id);
}