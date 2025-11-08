package apap.ti._5.tour_package_2306240156_be.repository;

import apap.ti._5.tour_package_2306240156_be.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {
    
    @Query("""
        SELECT a FROM Activity a 
        WHERE UPPER(a.activityType) = UPPER(:activityType)
        AND a.startDate >= :planStartDate
        AND a.endDate <= :planEndDate
        AND UPPER(a.startLocation) = UPPER(:startLocation)
        AND UPPER(a.endLocation) = UPPER(:endLocation)
    """)
    List<Activity> findEligibleActivitiesForPlan(
            @Param("activityType") String activityType,
            @Param("planStartDate") LocalDateTime planStartDate,
            @Param("planEndDate") LocalDateTime planEndDate,
            @Param("startLocation") String startLocation,
            @Param("endLocation") String endLocation
    );
}