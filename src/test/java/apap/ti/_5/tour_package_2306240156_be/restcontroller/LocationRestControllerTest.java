package apap.ti._5.tour_package_2306240156_be.restcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void getProvinces_success() throws Exception {
        String mockResponse = "[{\"id\":\"11\",\"name\":\"ACEH\"}]";
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/api/location/provinces"))
                .andExpect(status().isOk());
    }

    @Test
    void getRegencies_success() throws Exception {
        String mockResponse = "[{\"id\":\"1101\",\"province_id\":\"11\",\"name\":\"KABUPATEN SIMEULUE\"}]";
        when(restTemplate.getForEntity(anyString(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/api/location/regencies/11"))
                .andExpect(status().isOk());
    }
}
