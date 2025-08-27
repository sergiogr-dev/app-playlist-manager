package com.sergiodev.appplaylistmanager.config;

import com.sergiodev.appplaylistmanager.infrastructure.security.filter.JwtServerSecurityContextRepository;
import com.sergiodev.appplaylistmanager.infrastructure.security.handler.JwtAccessDeniedHandler;
import com.sergiodev.appplaylistmanager.infrastructure.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtServerSecurityContextRepository securityContextRepository;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final SecurityProperties securityProperties;
    
    // Constants for roles and paths
    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String API_V1_PLAYLISTS = "/api/v1/playlists/**";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/auth/verify-token").permitAll()
                        
                        // Actuator endpoints
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Swagger/OpenAPI endpoints
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        
                        // H2 Console (only for development)
                        .pathMatchers("/h2-console/**").permitAll()
                        
                        // Health check
                        .pathMatchers(HttpMethod.GET, "/health").permitAll()
                        
                        // OPTIONS requests for CORS preflight
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Admin endpoints
                        .pathMatchers("/api/v1/admin/**").hasRole(ROLE_ADMIN)
                        
                        // User endpoints
                        .pathMatchers("/api/v1/users/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        
                        // Playlist endpoints
                        .pathMatchers(HttpMethod.GET, API_V1_PLAYLISTS).hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        .pathMatchers(HttpMethod.POST, API_V1_PLAYLISTS).hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        .pathMatchers(HttpMethod.PUT, API_V1_PLAYLISTS).hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        .pathMatchers(HttpMethod.DELETE, API_V1_PLAYLISTS).hasAnyRole(ROLE_USER, ROLE_ADMIN)
                        
                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .securityContextRepository(securityContextRepository)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Use properties if available, otherwise use defaults
        List<String> allowedOrigins = securityProperties.getCors().getAllowedOrigins();
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(allowedOrigins);
        } else {
            configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        }
        
        List<String> allowedMethods = securityProperties.getCors().getAllowedMethods();
        if (allowedMethods != null && !allowedMethods.isEmpty()) {
            configuration.setAllowedMethods(allowedMethods);
        } else {
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }
        
        List<String> allowedHeaders = securityProperties.getCors().getAllowedHeaders();
        if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
            configuration.setAllowedHeaders(allowedHeaders);
        } else {
            configuration.setAllowedHeaders(Arrays.asList("*"));
        }
        
        Boolean allowCredentials = securityProperties.getCors().getAllowCredentials();
        configuration.setAllowCredentials(allowCredentials != null ? allowCredentials : Boolean.TRUE);
        
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS configured with origins: {}, methods: {}, headers: {}, allowCredentials: {}", 
                configuration.getAllowedOrigins(),
                configuration.getAllowedMethods(),
                configuration.getAllowedHeaders(),
                configuration.getAllowCredentials());
        
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
