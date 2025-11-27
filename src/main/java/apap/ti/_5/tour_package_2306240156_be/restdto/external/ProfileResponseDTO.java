package apap.ti._5.tour_package_2306240156_be.restdto.external;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDTO {
    private String id;
    private String username;
    private String name;
    private String email;
    private Boolean gender;
    private String role;
    
    // Profile Service returns these as String, not LocalDateTime
    private String createdAt;
    private String updatedAt;
    
    private Long saldo;
}
