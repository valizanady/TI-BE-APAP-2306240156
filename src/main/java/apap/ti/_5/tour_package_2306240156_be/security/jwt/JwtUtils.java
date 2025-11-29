package apap.ti._5.tour_package_2306240156_be.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse JWT token payload (decode Base64)
     * 
     * @param token JWT token
     * @return Payload as Map or null if invalid
     */
    public Map<String, Object> parseTokenPayload(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                logger.error("Invalid token format");
                return null;
            }
            
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            logger.error("Failed to parse token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract role from JWT token
     * 
     * @param token JWT token
     * @return Role string or null
     */
    public String getRoleFromToken(String token) {
        Map<String, Object> payload = parseTokenPayload(token);
        return payload != null ? (String) payload.get("role") : null;
    }

    /**
     * Extract email from JWT token
     * 
     * @param token JWT token
     * @return Email string or null
     */
    public String getEmailFromToken(String token) {
        Map<String, Object> payload = parseTokenPayload(token);
        return payload != null ? (String) payload.get("email") : null;
    }

    /**
     * Extract username from JWT token
     * JWT from Profile Service uses "sub" field for username
     * 
     * @param token JWT token
     * @return Username string or null
     */
    public String getUsernameFromToken(String token) {
        Map<String, Object> payload = parseTokenPayload(token);
        if (payload != null) {
            // Try "sub" field first (standard JWT claim for username/subject)
            String username = (String) payload.get("sub");
            if (username != null) {
                return username;
            }
            // Fallback to "username" field
            return (String) payload.get("username");
        }
        return null;
    }

    /**
     * Extract user ID from JWT token
     * 
     * @param token JWT token
     * @return User ID string or null
     */
    public String getUserIdFromToken(String token) {
        Map<String, Object> payload = parseTokenPayload(token);
        return payload != null ? (String) payload.get("id") : null;
    }

    /**
     * Extract name from JWT token
     * 
     * @param token JWT token
     * @return Name string or null
     */
    public String getNameFromToken(String token) {
        Map<String, Object> payload = parseTokenPayload(token);
        return payload != null ? (String) payload.get("name") : null;
    }
}
