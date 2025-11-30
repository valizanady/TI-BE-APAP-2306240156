package apap.ti._5.tour_package_2306240156_be.restcontroller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class LocationRestController {

    private final RestTemplate restTemplate;

    public LocationRestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() {
        String url = "https://wilayah.id/api/provinces.json";
        var response = restTemplate.getForEntity(url, Object.class);
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/regencies/{provinceId}")
    public ResponseEntity<?> getRegencies(@PathVariable String provinceId) {
        String url = "https://wilayah.id/api/regencies/" + provinceId + ".json";
        var response = restTemplate.getForEntity(url, Object.class);
        return ResponseEntity.ok(response.getBody());
    }
}
