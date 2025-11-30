package apap.ti._5.tour_package_2306240156_be.restcontroller;

import apap.ti._5.tour_package_2306240156_be.restdto.response.BaseResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BaseControllerUnitTest {

    @InjectMocks
    private BaseController baseController;

    @Test
    void testBaseResponse_WithValidToken_ReturnsRedirectView() {
        // Arrange
        String token = "valid-jwt-token-abc123";

        // Act
        Object response = baseController.baseResponse(token);

        // Assert
        assertNotNull(response);
        assertTrue(response instanceof RedirectView);
        
        RedirectView redirectView = (RedirectView) response;
        assertEquals("http://2306240156-fe.hafizmuh.site/?token=valid-jwt-token-abc123", 
                redirectView.getUrl());
    }

    @Test
    void testBaseResponse_WithLongToken_ReturnsRedirectView() {
        // Arrange
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";

        // Act
        Object response = baseController.baseResponse(token);

        // Assert
        assertNotNull(response);
        assertTrue(response instanceof RedirectView);
        
        RedirectView redirectView = (RedirectView) response;
        assertTrue(redirectView.getUrl().startsWith("http://2306240156-fe.hafizmuh.site/?token="));
        assertTrue(redirectView.getUrl().contains(token));
    }

    @Test
    void testBaseResponse_WithSpecialCharactersInToken_ReturnsRedirectView() {
        // Arrange
        String token = "token-with-special-chars_123.456";

        // Act
        Object response = baseController.baseResponse(token);

        // Assert
        assertNotNull(response);
        assertTrue(response instanceof RedirectView);
        
        RedirectView redirectView = (RedirectView) response;
        assertEquals("http://2306240156-fe.hafizmuh.site/?token=" + token, 
                redirectView.getUrl());
    }

    @Test
    void testBaseResponse_NoToken_ReturnsBaseResponseDTO() {
        // Act
        Object response = baseController.baseResponse(null);

        // Assert
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        
        @SuppressWarnings("unchecked")
        ResponseEntity<BaseResponseDTO<Object>> responseEntity = 
                (ResponseEntity<BaseResponseDTO<Object>>) response;
        
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(200, responseEntity.getBody().getStatus());
        assertEquals("Success", responseEntity.getBody().getMessage());
        assertNotNull(responseEntity.getBody().getTimestamp());
        assertNotNull(responseEntity.getBody().getData());
        assertTrue(responseEntity.getBody().getData() instanceof java.util.List);
        assertTrue(((java.util.List<?>) responseEntity.getBody().getData()).isEmpty());
    }

    @Test
    void testBaseResponse_EmptyToken_ReturnsBaseResponseDTO() {
        // Act
        Object response = baseController.baseResponse("");

        // Assert
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        
        @SuppressWarnings("unchecked")
        ResponseEntity<BaseResponseDTO<Object>> responseEntity = 
                (ResponseEntity<BaseResponseDTO<Object>>) response;
        
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(200, responseEntity.getBody().getStatus());
        assertEquals("Success", responseEntity.getBody().getMessage());
        assertNotNull(responseEntity.getBody().getTimestamp());
        assertNotNull(responseEntity.getBody().getData());
        assertTrue(responseEntity.getBody().getData() instanceof java.util.List);
    }

    @Test
    void testBaseResponse_BlankToken_ReturnsBaseResponseDTO() {
        // Act
        Object response = baseController.baseResponse("   ");

        // Assert
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        
        @SuppressWarnings("unchecked")
        ResponseEntity<BaseResponseDTO<Object>> responseEntity = 
                (ResponseEntity<BaseResponseDTO<Object>>) response;
        
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(200, responseEntity.getBody().getStatus());
        assertEquals("Success", responseEntity.getBody().getMessage());
    }

    @Test
    void testBaseResponse_NullToken_ResponseHasCorrectStructure() {
        // Act
        Object response = baseController.baseResponse(null);

        // Assert
        assertNotNull(response);
        assertTrue(response instanceof ResponseEntity);
        
        @SuppressWarnings("unchecked")
        ResponseEntity<BaseResponseDTO<Object>> responseEntity = 
                (ResponseEntity<BaseResponseDTO<Object>>) response;
        
        BaseResponseDTO<Object> body = responseEntity.getBody();
        assertNotNull(body);
        
        // Verify all fields are populated
        assertNotNull(body.getStatus());
        assertNotNull(body.getMessage());
        assertNotNull(body.getTimestamp());
        assertNotNull(body.getData());
        
        // Verify data is an empty list
        assertTrue(body.getData() instanceof java.util.List);
        assertEquals(0, ((java.util.List<?>) body.getData()).size());
    }

    @Test
    void testBaseResponse_WithToken_VerifyRedirectUrl() {
        // Arrange
        String token = "test-token-123";
        String expectedBaseUrl = "http://2306240156-fe.hafizmuh.site/";

        // Act
        Object response = baseController.baseResponse(token);

        // Assert
        assertNotNull(response);
        assertTrue(response instanceof RedirectView);
        
        RedirectView redirectView = (RedirectView) response;
        String actualUrl = redirectView.getUrl();
        
        assertNotNull(actualUrl);
        assertTrue(actualUrl.startsWith(expectedBaseUrl));
        assertTrue(actualUrl.contains("?token="));
        assertTrue(actualUrl.endsWith(token));
    }

    @Test
    void testBaseResponse_NullToken_VerifyResponseEntityType() {
        // Act
        Object response = baseController.baseResponse(null);

        // Assert
        assertNotNull(response);
        assertFalse(response instanceof RedirectView);
        assertTrue(response instanceof ResponseEntity);
    }

    @Test
    void testBaseResponse_WithToken_VerifyNotResponseEntity() {
        // Arrange
        String token = "some-token";

        // Act
        Object response = baseController.baseResponse(token);

        // Assert
        assertNotNull(response);
        assertFalse(response instanceof ResponseEntity);
        assertTrue(response instanceof RedirectView);
    }
}
