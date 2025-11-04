// restservice/TourPackageRestServiceImpl.java
package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Package;
import apap.ti._5.tour_package_2306240156_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourPackageRestServiceImpl implements PackageRestService {
  private final PackageRepository repo;

  private PackageResponseDTO map(Package p) {
    return PackageResponseDTO.builder()
        .id(p.getId())
        .userId(p.getUserId())
        .packageName(p.getPackageName())
        .quota(p.getQuota())
        .price(p.getPrice())
        .status(p.getStatus())
        .startDate(p.getStartDate())
        .endDate(p.getEndDate())
        .build();
  }

  @Override
  public List<PackageResponseDTO> getAll() {
    return repo.findAll().stream().map(this::map).toList();
  }

  @Override
  public PackageResponseDTO getById(String id) {
    return repo.findById(id).map(this::map)
        .orElseThrow(() -> new RuntimeException("Package not found"));
  }
}

