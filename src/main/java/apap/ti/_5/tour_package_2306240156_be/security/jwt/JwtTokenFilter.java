package apap.ti._5.tour_package_2306240156_be.security.jwt;

import apap.ti._5.tour_package_2306240156_be.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RestTemplate restTemplate;

    // Endpoint Profile Service untuk validasi token
    private final String AUTH_URL = "http://2306275166-be.hafizmuh.site/api/auth/me";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        logger.info("📍 Request: {} {}", request.getMethod(), requestPath);

        // Skip JWT validation for public endpoints
        if (requestPath.equals("/") || 
            requestPath.startsWith("/actuator") ||
            requestPath.startsWith("/error")) {
            logger.info("✅ Public endpoint, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        String token = parseJwt(request);

        if (token != null) {
            try {
                logger.info("🔐 Validating JWT token with Profile Service");
                logger.info("   Token (first 30 chars): {}...", token.substring(0, Math.min(30, token.length())));
                
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // Hit API Profile Service untuk validasi token
                @SuppressWarnings("rawtypes")
                ResponseEntity<Map> responseFromExternal = restTemplate.exchange(
                        AUTH_URL,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

                if (responseFromExternal.getStatusCode() == HttpStatus.OK) {
                    logger.info("✅ Token valid from Profile Service");
                    
                    // Extract role, email, username, name, dan userId dari token
                    String role = jwtUtils.getRoleFromToken(token);
                    String email = jwtUtils.getEmailFromToken(token);
                    String username = jwtUtils.getUsernameFromToken(token);
                    String userId = jwtUtils.getUserIdFromToken(token);
                    String name = jwtUtils.getNameFromToken(token);

                    // Fallback jika data null
                    if (role == null) role = "Customer";
                    if (email == null) email = username != null ? username : "User";
                    if (name == null) name = email;

                    logger.info("👤 User Info:");
                    logger.info("   ID: {}", userId);
                    logger.info("   Email: {}", email);
                    logger.info("   Username: {}", username);
                    logger.info("   Name: {}", name);
                    logger.info("   Role: {}", role);

                    // Create AuthenticatedUser object (dapat di-inject ke controller)
                    AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                            .id(userId)
                            .username(username)
                            .email(email)
                            .name(name)
                            .role(role)
                            .build();
                    
                    logger.info("🔑 Granted Authorities: {}", authenticatedUser.getAuthorities());
                    
                    // Set AuthenticatedUser sebagai principal di SecurityContext
                    // AuthenticatedUser implements UserDetails, jadi bisa langsung dipakai
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                authenticatedUser,  // principal (bisa diakses via @AuthenticationPrincipal)
                                null,               // credentials (tidak perlu untuk JWT)
                                authenticatedUser.getAuthorities()  // authorities
                            );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.info("✅ Security context set successfully with AuthenticatedUser");
                }

            } catch (Exception e) {
                // Token tidak valid (401 dari Profile Service)
                logger.error("❌ Token validation failed: " + e.getMessage());
                logger.error("   Error class: " + e.getClass().getName());
            }
        } else {
            logger.info("⚠️  No JWT token found in request");
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
