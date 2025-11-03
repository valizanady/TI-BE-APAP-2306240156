package apap.ti._5.tour_package_2306240156_be.model;

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
@Table(name = "activity")
public class Activity {

    @Id
    private String id;

    private String activityName;
    private String activityItem;
    private int capacity;
    private Long price;
    private String activityType;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    private String startLocation;
    private String endLocation;

    // ✅ 1 activity bisa muncul di banyak ordered quantity
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderedQuantity> orderedQuantities;
}
