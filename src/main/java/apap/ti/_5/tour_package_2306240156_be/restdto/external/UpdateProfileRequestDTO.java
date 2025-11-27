package apap.ti._5.tour_package_2306240156_be.restdto.external;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequestDTO {
    private String id;
    private String username;
    private String name;
    private String email;
    private String password; // Optional - only if changing password
    private Boolean gender;
    private Long saldo;
}
