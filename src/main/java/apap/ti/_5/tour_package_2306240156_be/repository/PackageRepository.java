package apap.ti._5.tour_package_2306240156_be.repository;

import apap.ti._5.tour_package_2306240156_be.model.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRepository extends JpaRepository<Package, String> {
}
