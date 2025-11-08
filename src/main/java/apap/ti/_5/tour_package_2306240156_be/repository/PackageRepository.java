package apap.ti._5.tour_package_2306240156_be.repository;

import apap.ti._5.tour_package_2306240156_be.model.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {
    long countByUserId(String userId);

    @Query("SELECT p FROM Package p WHERE p.status IS NULL OR p.status <> 'DELETED'")
    List<Package> findAllActive();

}
