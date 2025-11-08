package apap.ti._5.tour_package_2306240156_be.restcontroller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/location")
@CrossOrigin(origins = "http://localhost:5173")
public class LocationRestController {

    private final RestTemplate restTemplate = new RestTemplate();

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
