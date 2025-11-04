// restservice/TourPackageRestService.java
package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
import java.util.List;

public interface PackageRestService {
  List<PackageResponseDTO> getAll();
  PackageResponseDTO getById(String id);
}
