package apap.ti._5.tour_package_2306240156_be.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "plan")
public class Plan {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false)
    private String planName;
    
    private Long price;
    
    private String activityType;
    
    private String status;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    
    private String startLocation;
    
    private String endLocation;
    
    // Soft delete flag
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    // Banyak plan milik 1 package
    @Column(name = "package_id", insertable = false, updatable = false)
    private String packageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"plans"})
    private Package tourPackage;

    // 1 plan punya banyak ordered quantity
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"plan", "activity"})
    private List<OrderedQuantity> orderedQuantities;
}