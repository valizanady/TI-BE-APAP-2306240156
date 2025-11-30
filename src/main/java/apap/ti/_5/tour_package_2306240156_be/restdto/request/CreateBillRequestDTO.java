package apap.ti._5.tour_package_2306240156_be.restdto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk membuat Bill di Bill Service setelah Package diproses
 * 
 * Mapping dari Package ke Bill:
 * - packageId -> serviceReferenceId (reference ke package yang diproses)
 * - userId -> customerId (pembuat/pemilik package)
 * - amount -> total harga package
 * - serviceName -> "TOUR_PACKAGE" (fixed value)
 * - description -> deskripsi bill
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBillRequestDTO {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Service Name is required")
    private String serviceName;

    @NotBlank(message = "Service Reference ID is required")
    private String serviceReferenceId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Long amount;
}
