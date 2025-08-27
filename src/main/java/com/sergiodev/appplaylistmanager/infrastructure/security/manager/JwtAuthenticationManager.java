package com.sergiodev.appplaylistmanager.infrastructure.security.manager;

import com.sergiodev.appplaylistmanager.domain.exception.InvalidTokenException;
import com.sergiodev.appplaylistmanager.domain.exception.TokenExpiredException;
import com.sergiodev.appplaylistmanager.infrastructure.security.service.ReactiveUserDetailsServiceImpl;
import com.sergiodev.appplaylistmanager.infrastructure.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsServiceImpl userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
        String authToken = authentication.getCredentials().toString();
        String username;

        try {
            username = jwtUtil.extractUsername(authToken);
        } catch (InvalidTokenException | TokenExpiredException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return Mono.error(e);
        }

        if (username != null) {
            // Java
            return userDetailsService.findByUsername(username)
                .cast(UserDetails.class)
                .flatMap(userDetails -> {
                    if (Boolean.TRUE.equals(jwtUtil.validateToken(authToken, userDetails.getUsername()))) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        log.debug("User {} authenticated successfully", username);
                        return Mono.just((Authentication) authenticationToken); // Cast explícito
                    } else {
                        log.error("Token validation failed for user: {}", username);
                        return Mono.error(new InvalidTokenException("Token validation failed"));
                    }
                })
                .doOnError(throwable -> log.error("Authentication failed for user: {}", username, throwable));
        } else {
            log.error("Username extracted from token is null");
            return Mono.error(new InvalidTokenException("Username cannot be extracted from token"));
        }
    }
}
