package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Date;
import java.util.ArrayList;

@RestController
public class BaseController {

    @GetMapping("/")
    public Object baseResponse(@RequestParam(required = false) String token) {
        // If token is present, redirect to frontend with token (auth callback flow)
        if (token != null && !token.isEmpty()) {
            return new RedirectView("http://2306240156-fe.hafizmuh.site/?token=" + token);
        }
        
        // No token, return base response
        BaseResponseDTO<Object> response = new BaseResponseDTO<>(
                200,
                "Success",
                new Date(),
                new ArrayList<>()
        );
        return ResponseEntity.ok(response);
    }
}

