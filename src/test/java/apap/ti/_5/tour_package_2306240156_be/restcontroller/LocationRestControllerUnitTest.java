package apap.ti._5.tour_package_2306240156_be.restcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationRestControllerUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LocationRestController controller;

    @Test
    void testGetProvinces_Success() {
        String mockResponse = "{\"data\": []}";
        when(restTemplate.getForEntity(eq("https://wilayah.id/api/provinces.json"), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        ResponseEntity<?> response = controller.getProvinces();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void testGetRegencies_Success() {
        String mockResponse = "{\"data\": []}";
        when(restTemplate.getForEntity(eq("https://wilayah.id/api/regencies/12.json"), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        ResponseEntity<?> response = controller.getRegencies("12");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }
}
