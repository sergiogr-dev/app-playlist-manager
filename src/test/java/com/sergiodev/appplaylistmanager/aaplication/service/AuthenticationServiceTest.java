package com.sergiodev.appplaylistmanager.aaplication.service;

import com.sergiodev.appplaylistmanager.config.JwtProperties;
import com.sergiodev.appplaylistmanager.domain.exception.InvalidTokenException;
import com.sergiodev.appplaylistmanager.domain.exception.TokenExpiredException;
import com.sergiodev.appplaylistmanager.domain.exception.type.SecurityCustomException;
import com.sergiodev.appplaylistmanager.domain.model.RefreshToken;
import com.sergiodev.appplaylistmanager.domain.model.User;
import com.sergiodev.appplaylistmanager.infrastructure.persistance.repository.RefreshTokenRepository;
import com.sergiodev.appplaylistmanager.infrastructure.persistance.repository.UserRepository;
import com.sergiodev.appplaylistmanager.infrastructure.security.util.JwtUtil;
import com.sergiodev.appplaylistmanager.web.dto.AuthenticationRequest;
import com.sergiodev.appplaylistmanager.web.dto.AuthenticationResponse;
import com.sergiodev.appplaylistmanager.web.dto.RefreshTokenRequest;
import com.sergiodev.appplaylistmanager.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .username("testuser")
            .email("test@test.com")
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .build();

        authenticationRequest = AuthenticationRequest.builder()
            .username("testuser")
            .password("password123")
            .build();

        refreshTokenRequest = RefreshTokenRequest.builder()
            .refreshToken("refresh-token-123")
            .build();

        user = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@test.com")
            .password("encoded-password")
            .firstName("Test")
            .lastName("User")
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .roles(Set.of(User.Role.USER))
            .build();

        refreshToken = RefreshToken.builder()
            .id(1L)
            .token("refresh-token-123")
            .user(user)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .revoked(false)
            .build();
    }

    @Test
    void register_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        when(jwtProperties.getExpiration()).thenReturn(3600000L);
        when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L);

        // When
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@test.com");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThrows(SecurityCustomException.class,
            () -> authenticationService.register(registerRequest));

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(SecurityCustomException.class,
            () -> authenticationService.register(registerRequest));

        verify(userRepository).existsByEmail("test@test.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_Success() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        when(jwtProperties.getExpiration()).thenReturn(3600000L);
        when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L);

        // When
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder).matches("password123", "encoded-password");
    }

    @Test
    void authenticate_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
            () -> authenticationService.authenticate(authenticationRequest));

        verify(userRepository).findByUsernameOrEmail("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_InvalidPassword_ThrowsException() {
        // Given
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class,
            () -> authenticationService.authenticate(authenticationRequest));

        verify(passwordEncoder).matches("password123", "encoded-password");
    }

    @Test
    void authenticate_UserDisabled_ThrowsException() {
        // Given
        User disabledUser = user.toBuilder().enabled(false).build();
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(disabledUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When & Then
        assertThrows(BadCredentialsException.class,
            () -> authenticationService.authenticate(authenticationRequest));

        verify(passwordEncoder).matches("password123", "encoded-password");
    }

    @Test
    void refreshToken_Success() {
        // Given
        doNothing().when(jwtUtil).validateRefreshToken(anyString());
        when(jwtUtil.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("new-refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        doNothing().when(refreshTokenRepository).revokeToken(anyString());

        when(jwtProperties.getExpiration()).thenReturn(3600000L);
        when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L);

        // When
        AuthenticationResponse response = authenticationService.refreshToken(refreshTokenRequest);

        // Then
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());

        verify(jwtUtil).validateRefreshToken("refresh-token-123");
        verify(refreshTokenRepository).revokeToken("refresh-token-123");
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        // Given
        doThrow(new InvalidTokenException("Invalid token")).when(jwtUtil).validateRefreshToken(anyString());

        // When & Then
        assertThrows(BadCredentialsException.class,
            () -> authenticationService.refreshToken(refreshTokenRequest));

        verify(jwtUtil).validateRefreshToken("refresh-token-123");
    }

    @Test
    void refreshToken_ExpiredToken_ThrowsException() {
        // Given
        doThrow(new TokenExpiredException("Token expired")).when(jwtUtil).validateRefreshToken(anyString());

        // When & Then
        assertThrows(BadCredentialsException.class,
            () -> authenticationService.refreshToken(refreshTokenRequest));

        verify(jwtUtil).validateRefreshToken("refresh-token-123");
    }

    @Test
    void refreshToken_TokenNotFound_ThrowsException() {
        // Given
        doNothing().when(jwtUtil).validateRefreshToken(anyString());
        when(jwtUtil.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BadCredentialsException.class,
            () -> authenticationService.refreshToken(refreshTokenRequest));

        verify(refreshTokenRepository).findByToken("refresh-token-123");
    }

    @Test
    void refreshToken_ExpiredRefreshToken_ThrowsException() {
        // Given
        RefreshToken expiredToken = refreshToken.toBuilder()
            .expiresAt(LocalDateTime.now().minusDays(1))
            .build();

        doNothing().when(jwtUtil).validateRefreshToken(anyString());
        when(jwtUtil.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(expiredToken));

        // When & Then
        assertThrows(BadCredentialsException.class,
            () -> authenticationService.refreshToken(refreshTokenRequest));
    }

    @Test
    void logout_Success() {
        // Given
        when(jwtUtil.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenRepository).revokeAllTokensByUser(any(User.class));

        // When
        assertDoesNotThrow(() -> authenticationService.logout("refresh-token-123"));

        // Then
        verify(jwtUtil).extractUsername("refresh-token-123");
        verify(refreshTokenRepository).revokeAllTokensByUser(user);
    }

    @Test
    void logout_UserNotFound_ThrowsException() {
        // Given
        when(jwtUtil.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class,
            () -> authenticationService.logout("refresh-token-123"));

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void validateToken_Success() {
        // Given
        when(jwtUtil.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(true);

        // When
        boolean result = authenticationService.validateToken("valid-token");

        // Then
        assertTrue(result);
        verify(jwtUtil).validateToken("valid-token", "testuser");
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // Given
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("Invalid token"));

        // When
        boolean result = authenticationService.validateToken("invalid-token");

        // Then
        assertFalse(result);
        verify(jwtUtil).extractUsername("invalid-token");
    }

    @Test
    void validateToken_UserNotFound_ReturnsFalse() {
        // Given
        when(jwtUtil.extractUsername(anyString())).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When
        boolean result = authenticationService.validateToken("valid-token");

        // Then
        assertFalse(result);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void cleanupExpiredTokens_Success() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0);
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(now);

            // No es necesario stubear deleteExpiredAndRevokedTokens, solo verificar la llamada
            authenticationService.cleanupExpiredTokens();

            verify(refreshTokenRepository).deleteExpiredAndRevokedTokens(now);
        }
    }
}