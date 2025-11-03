package apap.ti._5.tour_package_2306240156_be.repository;

import apap.ti._5.tour_package_2306240156_be.model.OrderedQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderedQuantityRepository extends JpaRepository<OrderedQuantity, UUID> {
    List<OrderedQuantity> findByPlanId(UUID planId);
    List<OrderedQuantity> findByActivityId(String activityId);
}

