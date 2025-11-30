package apap.ti._5.tour_package_2306240156_be.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API Key Filter for microservice-to-microservice authentication.
 * This filter validates the x-api-key header for specific endpoints.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);
    private static final String API_KEY_HEADER = "x-api-key";
    
    @Value("${API_KEY_BILL_SERVICE:default-secret-key}")
    private String apiKeyBillService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // Only apply API Key validation for /api/packages/payment/* endpoints
        if (requestURI.startsWith("/api/packages/payment/") || 
            requestURI.startsWith("/api/package/payment/")) {
            
            logger.info("🔑 API Key validation for: {} {}", request.getMethod(), requestURI);
            
            String providedApiKey = request.getHeader(API_KEY_HEADER);
            
            if (providedApiKey == null || providedApiKey.trim().isEmpty()) {
                logger.warn("❌ Missing API Key for {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"API Key is required\"}");
                return;
            }
            
            if (!apiKeyBillService.equals(providedApiKey)) {
                logger.warn("❌ Invalid API Key for {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid API Key\"}");
                return;
            }
            
            logger.info("✅ API Key validated successfully for {}", requestURI);
        }
        
        filterChain.doFilter(request, response);
    }
}
