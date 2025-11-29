package apap.ti._5.tour_package_2306240156_be.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "package")
public class Package {

    @Id
    private String id;

    private String userId;
    
    // Role of the user who created this package (Customer, Superadmin, TourPackageVendor)
    // Used for authorization: Customer can see packages from Admin/Vendor
    private String creatorRole;
    
    private String packageName;
    private int quota;
    private Long price;
    private String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    // 1 package punya banyak plan
    @OneToMany(mappedBy = "tourPackage", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"tourPackage", "orderedQuantities"})
    private List<Plan> plans;
}
