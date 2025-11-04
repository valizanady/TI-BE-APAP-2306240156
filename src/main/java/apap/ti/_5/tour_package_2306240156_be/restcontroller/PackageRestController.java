// restcontroller/TourPackageRestController.java
package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.response.PackageResponseDTO;
import apap.ti._5.tour_package_2306240156_be.restservice.PackageRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/package")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PackageRestController {
  private final PackageRestService service;

  @GetMapping
  public ResponseEntity<BaseResponseDTO<List<PackageResponseDTO>>> getAll() {
    var body = new BaseResponseDTO<>(200, "Success", new Date(), service.getAll());
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseResponseDTO<PackageResponseDTO>> getById(@PathVariable String id) {
    var body = new BaseResponseDTO<>(200, "Success", new Date(), service.getById(id));
    return ResponseEntity.ok(body);
  }
}
