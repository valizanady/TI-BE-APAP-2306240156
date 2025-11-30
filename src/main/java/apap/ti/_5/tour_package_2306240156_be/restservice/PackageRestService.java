// restservice/TourPackageRestService.java
package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePackageRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePackageRequestDTO;

import java.util.List;

public interface PackageRestService {
  List<PackageResponseDTO> getAll();
  List<PackageResponseDTO> getPackagesForCustomer(String userId);
  PackageResponseDTO getById(String id);
  PackageResponseDTO getById(String id, String userId, String userRole); // With authorization
  PackageResponseDTO create(CreatePackageRequestDTO req, String userId, String userRole);
  PackageResponseDTO deleteById(String id);
  PackageResponseDTO updatePackage(String id, UpdatePackageRequestDTO dto);
  PackageResponseDTO processPackage(String id);
  PackageResponseDTO updatePaymentStatus(String packageId, Integer status);
}
