package apap.ti._5.tour_package_2306240156_be.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RestTemplate restTemplate;

    // Endpoint Profile Service untuk validasi token
    private final String AUTH_URL = "https://acc-be.beel.my.id/api/auth/me";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = parseJwt(request);

        if (token != null) {
            try {
                logger.info("🔐 Validating JWT token with Profile Service");
                
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // Hit API Profile Service untuk validasi token
                ResponseEntity<Map> responseFromExternal = restTemplate.exchange(
                        AUTH_URL,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

                if (responseFromExternal.getStatusCode() == HttpStatus.OK) {
                    logger.info("✅ Token valid");
                    
                    // Extract role, email, username, dan userId dari token
                    String role = jwtUtils.getRoleFromToken(token);
                    String email = jwtUtils.getEmailFromToken(token);
                    String username = jwtUtils.getUsernameFromToken(token);
                    String userId = jwtUtils.getUserIdFromToken(token);

                    // Fallback jika data null
                    if (role == null) role = "Customer";
                    if (email == null) email = username != null ? username : "User";

                    logger.info("User: {}, Role: {}, UserId: {}", email, role, userId);

                    // Set Spring Security Context (untuk @PreAuthorize)
                    // Tambahkan prefix "ROLE_" untuk kompatibilitas dengan hasRole()
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)
                    );
                    
                    // Gunakan email/username sebagai principal
                    UserDetails userDetails = new User(email, "", authorities);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    
                    // Set custom details dengan userId dan role
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", userId);
                    details.put("role", role);
                    details.put("email", email);
                    details.put("username", username);
                    
                    authentication.setDetails(details);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                // Token tidak valid (401 dari Profile Service)
                logger.error("❌ Token validation failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token dari Authorization header
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
