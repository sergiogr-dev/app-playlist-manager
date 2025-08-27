package com.sergiodev.appplaylistmanager.infrastructure.security.util;

import com.sergiodev.appplaylistmanager.config.JwtProperties;
import com.sergiodev.appplaylistmanager.domain.exception.InvalidTokenException;
import com.sergiodev.appplaylistmanager.domain.exception.TokenExpiredException;
import com.sergiodev.appplaylistmanager.domain.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    
    // Constants
    private static final String USER_ID_CLAIM = "userId";
    private static final String EMAIL_CLAIM = "email";
    private static final String FIRST_NAME_CLAIM = "firstName";
    private static final String LAST_NAME_CLAIM = "lastName";
    private static final String ROLES_CLAIM = "roles";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, user.getId());
        claims.put(EMAIL_CLAIM, user.getEmail());
        claims.put(FIRST_NAME_CLAIM, user.getFirstName());
        claims.put(LAST_NAME_CLAIM, user.getLastName());
        claims.put(ROLES_CLAIM, user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return createToken(claims, user.getUsername(), jwtProperties.getExpiration());
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, user.getId());
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        
        return createToken(claims, user.getUsername(), jwtProperties.getRefreshExpiration());
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusMillis(expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation failed for user: {}", username, e);
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(USER_ID_CLAIM, Long.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get(EMAIL_CLAIM, String.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get(ROLES_CLAIM));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            throw new TokenExpiredException("JWT token has expired");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new InvalidTokenException("JWT token is unsupported");
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw new InvalidTokenException("JWT token is malformed");
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw new InvalidTokenException("JWT signature validation failed");
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw new InvalidTokenException("JWT token compact of handler are invalid");
        }
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    public Boolean isRefreshToken(String token) {
        String tokenType = extractTokenType(token);
        return REFRESH_TOKEN_TYPE.equals(tokenType);
    }

    public LocalDateTime getExpirationDateFromToken(String token) {
        Date expiration = extractExpiration(token);
        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public void validateRefreshToken(String token) {
        if (Boolean.FALSE.equals(isRefreshToken(token))) {
            throw new InvalidTokenException("Token is not a refresh token");
        }
        
        if (Boolean.TRUE.equals(isTokenExpired(token))) {
            throw new TokenExpiredException("Refresh token has expired");
        }
    }

    public Long getTokenExpirationTime() {
        return jwtProperties.getExpiration();
    }

    public Long getRefreshTokenExpirationTime() {
        return jwtProperties.getRefreshExpiration();
    }
}
