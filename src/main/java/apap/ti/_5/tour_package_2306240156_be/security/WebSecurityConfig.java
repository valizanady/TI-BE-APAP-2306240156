package apap.ti._5.tour_package_2306240156_be.security;

import apap.ti._5.tour_package_2306240156_be.security.jwt.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private JwtTokenFilter jwtTokenFilter;
    
    @Autowired
    private ApiKeyFilter apiKeyFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (tanpa autentikasi)
                .requestMatchers("/").permitAll()  // Root endpoint
                .requestMatchers("/actuator/**").permitAll()  // Health checks
                .requestMatchers("/error").permitAll()  // Error handling
                .requestMatchers("/api/auth/**").permitAll()
                // Payment endpoints - no JWT required, only API Key (handled by ApiKeyFilter)
                .requestMatchers("/api/package/payment/**").permitAll()
                .requestMatchers("/api/packages/payment/**").permitAll()
                .requestMatchers("/api/**").permitAll()  // TODO: Change to authenticated() setelah testing
                
                // Semua request lain perlu autentikasi
                .anyRequest().authenticated()
            );

        // Add API Key filter first (for microservice endpoints)
        http.addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Add JWT filter sebelum UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration
     * Allow requests dari frontend domain
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (update dengan frontend URL kamu)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React/Vue local dev
            "http://localhost:5173",  // Vite local dev
            "http://localhost:8080",  // Backend local
            "https://your-frontend-domain.com"  // Production frontend
        ));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
