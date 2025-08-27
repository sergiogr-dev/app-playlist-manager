package com.sergiodev.appplaylistmanager.web.controller;

import com.sergiodev.appplaylistmanager.aaplication.service.AuthenticationService;
import com.sergiodev.appplaylistmanager.infrastructure.security.util.JwtUtil;
import com.sergiodev.appplaylistmanager.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private AuthenticationResponse authenticationResponse;
    private TokenValidationResponse tokenValidationResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .username("user")
            .email("user@mail.com")
            .password("pass")
            .build();

        authenticationRequest = AuthenticationRequest.builder()
            .username("user")
            .password("pass")
            .build();

        refreshTokenRequest = RefreshTokenRequest.builder()
            .refreshToken("refreshToken")
            .build();

        authenticationResponse = AuthenticationResponse.builder()
            .accessToken("accessToken")
            .refreshToken("refreshToken")
            .build();

        tokenValidationResponse = TokenValidationResponse.builder()
            .valid(true)
            .username("user")
            .email("user@mail.com")
            .userId(1L)
            .expiresAt(123456789L)
            .message("Token is valid")
            .build();
    }

    @Test
    void register_ShouldReturnCreatedResponse() {
        when(authenticationService.register(registerRequest)).thenReturn(authenticationResponse);

        Mono<ResponseEntity<AuthenticationResponse>> result = authController.register(registerRequest);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(201, response.getStatusCodeValue());
                assertEquals(authenticationResponse, response.getBody());
            })
            .verifyComplete();

        verify(authenticationService).register(registerRequest);
    }

    @Test
    void register_ShouldHandleException() {
        when(authenticationService.register(registerRequest)).thenThrow(new RuntimeException("Error"));

        Mono<ResponseEntity<AuthenticationResponse>> result = authController.register(registerRequest);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(authenticationService).register(registerRequest);
    }

    @Test
    void authenticate_ShouldReturnOkResponse() {
        when(authenticationService.authenticate(authenticationRequest)).thenReturn(authenticationResponse);

        Mono<ResponseEntity<AuthenticationResponse>> result = authController.authenticate(authenticationRequest);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCodeValue());
                assertEquals(authenticationResponse, response.getBody());
            })
            .verifyComplete();

        verify(authenticationService).authenticate(authenticationRequest);
    }

    @Test
    void authenticate_ShouldHandleException() {
        when(authenticationService.authenticate(authenticationRequest)).thenThrow(new RuntimeException("Error"));

        Mono<ResponseEntity<AuthenticationResponse>> result = authController.authenticate(authenticationRequest);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(authenticationService).authenticate(authenticationRequest);
    }

    @Test
    void refreshToken_ShouldReturnOkResponse() {
        when(authenticationService.refreshToken(refreshTokenRequest)).thenReturn(authenticationResponse);

        Mono<ResponseEntity<AuthenticationResponse>> result = authController.refreshToken(refreshTokenRequest);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCodeValue());
                assertEquals(authenticationResponse, response.getBody());
            })
            .verifyComplete();

        verify(authenticationService).refreshToken(refreshTokenRequest);
    }

    @Test
    void refreshToken_ShouldHandleException() {
        when(authenticationService.refreshToken(refreshTokenRequest)).thenThrow(new RuntimeException("Error"));

        Mono<ResponseEntity<AuthenticationResponse>> result = authController.refreshToken(refreshTokenRequest);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(authenticationService).refreshToken(refreshTokenRequest);
    }

    @Test
    void logout_ShouldReturnOkResponse() {
        doNothing().when(authenticationService).logout("refreshToken");

        Mono<ResponseEntity<Void>> result = authController.logout(refreshTokenRequest);

        StepVerifier.create(result)
            .assertNext(response -> assertEquals(200, response.getStatusCodeValue()))
            .verifyComplete();

        verify(authenticationService).logout("refreshToken");
    }

    @Test
    void logout_ShouldHandleException() {
        doThrow(new RuntimeException("Error")).when(authenticationService).logout("refreshToken");

        Mono<ResponseEntity<Void>> result = authController.logout(refreshTokenRequest);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(authenticationService).logout("refreshToken");
    }

    @Test
    void verifyToken_ShouldReturnValidResponse() {
        String token = "token";
        when(authenticationService.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("user");
        when(jwtUtil.extractEmail(token)).thenReturn("user@mail.com");
        when(jwtUtil.extractUserId(token)).thenReturn(1L);
        when(jwtUtil.extractExpiration(token)).thenReturn(java.util.Date.from(java.time.Instant.ofEpochSecond(123456789L)));

        Mono<ResponseEntity<TokenValidationResponse>> result = authController.verifyToken(token);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertTrue(response.getBody().isValid());
                assertEquals("user", response.getBody().getUsername());
            })
            .verifyComplete();

        verify(authenticationService).validateToken(token);
        verify(jwtUtil).extractUsername(token);
        verify(jwtUtil).extractEmail(token);
        verify(jwtUtil).extractUserId(token);
        verify(jwtUtil).extractExpiration(token);
    }

    @Test
    void verifyToken_ShouldReturnInvalidResponse() {
        String token = "token";
        when(authenticationService.validateToken(token)).thenReturn(false);

        Mono<ResponseEntity<TokenValidationResponse>> result = authController.verifyToken(token);

        StepVerifier.create(result)
            .assertNext(response -> assertFalse(response.getBody().isValid()))
            .verifyComplete();

        verify(authenticationService).validateToken(token);
    }

    @Test
    void verifyToken_ShouldHandleException() {
        String token = "token";
        when(authenticationService.validateToken(token)).thenThrow(new RuntimeException("Error"));

        Mono<ResponseEntity<TokenValidationResponse>> result = authController.verifyToken(token);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertFalse(response.getBody().isValid());
                assertTrue(response.getBody().getMessage().contains("Token verification failed"));
            })
            .verifyComplete();

        verify(authenticationService).validateToken(token);
    }

    @Test
    void getCurrentUser_ShouldReturnValidResponse() {
        String authHeader = "Bearer token";
        String token = "token";
        when(authenticationService.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("user");
        when(jwtUtil.extractEmail(token)).thenReturn("user@mail.com");
        when(jwtUtil.extractUserId(token)).thenReturn(1L);
        when(jwtUtil.extractExpiration(token)).thenReturn(java.util.Date.from(java.time.Instant.ofEpochSecond(123456789L)));

        Mono<ResponseEntity<TokenValidationResponse>> result = authController.getCurrentUser(authHeader);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertTrue(response.getBody().isValid());
                assertEquals("user", response.getBody().getUsername());
            })
            .verifyComplete();

        verify(authenticationService).validateToken(token);
        verify(jwtUtil).extractUsername(token);
        verify(jwtUtil).extractEmail(token);
        verify(jwtUtil).extractUserId(token);
        verify(jwtUtil).extractExpiration(token);
    }

    @Test
    void getCurrentUser_ShouldReturnInvalidResponse() {
        String authHeader = "Bearer token";
        String token = "token";
        when(authenticationService.validateToken(token)).thenReturn(false);

        Mono<ResponseEntity<TokenValidationResponse>> result = authController.getCurrentUser(authHeader);

        StepVerifier.create(result)
            .assertNext(response -> assertFalse(response.getBody().isValid()))
            .verifyComplete();

        verify(authenticationService).validateToken(token);
    }

    @Test
    void getCurrentUser_ShouldHandleException() {
        String authHeader = "Bearer token";
        String token = "token";
        when(authenticationService.validateToken(token)).thenThrow(new RuntimeException("Error"));

        Mono<ResponseEntity<TokenValidationResponse>> result = authController.getCurrentUser(authHeader);

        StepVerifier.create(result)
            .assertNext(response -> {
                assertFalse(response.getBody().isValid());
                assertTrue(response.getBody().getMessage().contains("Failed to get user information"));
            })
            .verifyComplete();

        verify(authenticationService).validateToken(token);
    }
}