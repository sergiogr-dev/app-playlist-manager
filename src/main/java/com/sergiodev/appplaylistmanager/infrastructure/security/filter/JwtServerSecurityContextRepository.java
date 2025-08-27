package com.sergiodev.appplaylistmanager.infrastructure.security.filter;

import com.sergiodev.appplaylistmanager.infrastructure.security.manager.JwtAuthenticationManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtServerSecurityContextRepository implements ServerSecurityContextRepository {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_PREFIX_LENGTH = 7;
    
    private final JwtAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // No implementation needed for stateless JWT authentication
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            String authToken = authHeader.substring(TOKEN_PREFIX_LENGTH);
            Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
            
            return authenticationManager.authenticate(auth)
                .map(authResult -> (SecurityContext) new SecurityContextImpl(authResult))
                .doOnNext(securityContext -> log.debug("Security context created for request: {}",
                    request.getPath().value()))
                .doOnError(throwable -> log.debug("Failed to authenticate token for request: {}",
                    request.getPath().value(), throwable));
        } else {
            log.debug("No authorization header found for request: {}", request.getPath().value());
        }
        
        return Mono.empty();
    }
}
