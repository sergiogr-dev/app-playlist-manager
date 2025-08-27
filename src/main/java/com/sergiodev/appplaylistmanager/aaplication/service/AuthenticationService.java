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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    
    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";

    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw SecurityCustomException.Type.USERNAME_ALREADY_EXISTS.build(request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw SecurityCustomException.Type.EMAIL_ALREADY_EXISTS.build(request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(Set.of(User.Role.USER))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return generateTokens(savedUser);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE + request.getUsername()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Invalid password for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            log.error("User account is disabled: {}", request.getUsername());
            throw new BadCredentialsException("User account is disabled");
        }

        log.info("User authenticated successfully: {}", user.getUsername());
        return generateTokens(user);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        try {
            jwtUtil.validateRefreshToken(request.getRefreshToken());
        } catch (InvalidTokenException | TokenExpiredException e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE + username));

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));

        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            log.error("Refresh token is expired or revoked for user: {}", username);
            throw new BadCredentialsException("Refresh token is expired or revoked");
        }

        // Revoke old refresh token
        refreshTokenRepository.revokeToken(request.getRefreshToken());

        log.info("Token refreshed successfully for user: {}", username);
        return generateTokens(user);
    }

    public void logout(String refreshToken) {
        log.info("Logging out user");

        try {
            String username = jwtUtil.extractUsername(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE + username));

            refreshTokenRepository.revokeAllTokensByUser(user);
            log.info("User logged out successfully: {}", username);
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            throw new RuntimeException("Error during logout");
        }
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE + username));

            return jwtUtil.validateToken(token, user.getUsername());
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private AuthenticationResponse generateTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenValue = jwtUtil.generateRefreshToken(user);

        // Save refresh token to database
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpiration() / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration() / 1000) // Convert to seconds
                .build();
    }

    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired refresh tokens");
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
    }
}
