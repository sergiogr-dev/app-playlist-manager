package com.sergiodev.appplaylistmanager.web.controller;

import com.sergiodev.appplaylistmanager.aaplication.service.AuthenticationService;
import com.sergiodev.appplaylistmanager.infrastructure.security.util.JwtUtil;
import com.sergiodev.appplaylistmanager.web.dto.AuthenticationRequest;
import com.sergiodev.appplaylistmanager.web.dto.AuthenticationResponse;
import com.sergiodev.appplaylistmanager.web.dto.RefreshTokenRequest;
import com.sergiodev.appplaylistmanager.web.dto.RegisterRequest;
import com.sergiodev.appplaylistmanager.web.dto.TokenValidationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public Mono<ResponseEntity<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info("Registration request received for username: {}", request.getUsername());
        
        return Mono.fromCallable(() -> authenticationService.register(request))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnSuccess(response -> log.info("User registered successfully: {}", request.getUsername()))
                .doOnError(error -> log.error("Registration failed for username: {}", request.getUsername(), error));
    }

    @Operation(summary = "Authenticate user and get access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        
        log.info("Authentication request received for username: {}", request.getUsername());
        
        return Mono.fromCallable(() -> authenticationService.authenticate(request))
                .map(response -> ResponseEntity.ok().body(response))
                .doOnSuccess(response -> log.info("User authenticated successfully: {}", request.getUsername()))
                .doOnError(error -> log.error("Authentication failed for username: {}", request.getUsername(), error));
    }

    @Operation(summary = "Refresh access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        log.info("Token refresh request received");
        
        return Mono.fromCallable(() -> authenticationService.refreshToken(request))
                .map(response -> ResponseEntity.ok().body(response))
                .doOnSuccess(response -> log.info("Token refreshed successfully"))
                .doOnError(error -> log.error("Token refresh failed", error));
    }

    @Operation(summary = "Logout user and revoke refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        log.info("Logout request received");
        
        return Mono.fromRunnable(() -> authenticationService.logout(request.getRefreshToken()))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .doOnSuccess(response -> log.info("User logged out successfully"))
                .doOnError(error -> log.error("Logout failed", error));
    }

    @Operation(summary = "Validate access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token validation result"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping("/verify-token")
    public Mono<ResponseEntity<TokenValidationResponse>> verifyToken(
            @RequestParam String token) {
        
        log.info("Token verification request received");
        
        return Mono.fromCallable(() -> {
            try {
                boolean isValid = authenticationService.validateToken(token);
                
                if (isValid) {
                    String username = jwtUtil.extractUsername(token);
                    String email = jwtUtil.extractEmail(token);
                    Long userId = jwtUtil.extractUserId(token);
                    Long expiresAt = jwtUtil.extractExpiration(token).toInstant().getEpochSecond();

                    return TokenValidationResponse.builder()
                            .valid(true)
                            .username(username)
                            .email(email)
                            .userId(userId)
                            .expiresAt(expiresAt)
                            .message("Token is valid")
                            .build();
                } else {
                    return TokenValidationResponse.builder()
                            .valid(false)
                            .message("Token is invalid or expired")
                            .build();
                }
            } catch (Exception e) {
                log.error("Token verification failed", e);
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token verification failed: " + e.getMessage())
                        .build();
            }
        })
        .map(response -> ResponseEntity.ok().body(response))
        .doOnSuccess(response -> log.info("Token verification completed: valid={}", response.getBody().isValid()))
        .doOnError(error -> log.error("Token verification failed", error));
    }

    @Operation(summary = "Get current user information from token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    @GetMapping("/me")
    public Mono<ResponseEntity<TokenValidationResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Current user information request received");
        
        return Mono.fromCallable(() -> {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            try {
                boolean isValid = authenticationService.validateToken(token);
                
                if (isValid) {
                    String username = jwtUtil.extractUsername(token);
                    String email = jwtUtil.extractEmail(token);
                    Long userId = jwtUtil.extractUserId(token);
                    Long expiresAt = jwtUtil.extractExpiration(token).toInstant().getEpochSecond();

                    return TokenValidationResponse.builder()
                            .valid(true)
                            .username(username)
                            .email(email)
                            .userId(userId)
                            .expiresAt(expiresAt)
                            .message("User information retrieved successfully")
                            .build();
                } else {
                    return TokenValidationResponse.builder()
                            .valid(false)
                            .message("Token is invalid or expired")
                            .build();
                }
            } catch (Exception e) {
                log.error("Failed to get current user information", e);
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Failed to get user information: " + e.getMessage())
                        .build();
            }
        })
        .map(response -> ResponseEntity.ok().body(response))
        .doOnSuccess(response -> log.info("Current user information retrieved: username={}", 
                response.getBody().getUsername()))
        .doOnError(error -> log.error("Failed to get current user information", error));
    }
}
