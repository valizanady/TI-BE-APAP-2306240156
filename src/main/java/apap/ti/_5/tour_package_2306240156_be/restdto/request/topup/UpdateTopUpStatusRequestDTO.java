package apap.ti._5.tour_package_2306240156_be.restdto.request.topup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTopUpStatusRequestDTO {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "Success|Failed", message = "Status must be Success or Failed")
    private String status;
}