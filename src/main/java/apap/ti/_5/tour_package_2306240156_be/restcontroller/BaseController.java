package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.ArrayList;

@RestController
public class BaseController {

    @GetMapping("/")
    public ResponseEntity<BaseResponseDTO<Object>> baseResponse() {
        BaseResponseDTO<Object> response = new BaseResponseDTO<>(
                200,
                "Success",
                new Date(),
                new ArrayList<>()
        );
        return ResponseEntity.ok(response);
    }
}

