package apap.ti._5.tour_package_2306240156_be.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "ordered_quantity")
public class OrderedQuantity {
    @Id
    @GeneratedValue
    private UUID id;
    
    private int quota;
    
    private int orderedQuota;
    
    private Long price;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    
    // Soft delete flag
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    // Tambahkan field eksplisit untuk planId (sesuai class diagram)
    @Column(name = "plan_id", insertable = false, updatable = false)
    private UUID planId;
    
    // Banyak ordered quantity milik 1 plan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"orderedQuantities", "tourPackage"})
    private Plan plan;

    // Tambahkan field eksplisit untuk activityId (sesuai class diagram)
    @Column(name = "activity_id", insertable = false, updatable = false)
    private String activityId;
    
    // Banyak ordered quantity terkait 1 activity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"orderedQuantities"})
    private Activity activity;
}