package com.sergiodev.appplaylistmanager.infrastructure.security.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        log.error("Authentication error: {} for request: {}", 
                ex.getMessage(), 
                exchange.getRequest().getPath().value());

        return exchange.getResponse().writeWith(
                Mono.fromCallable(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("timestamp", LocalDateTime.now().toString());
                    errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                    errorResponse.put("error", "Unauthorized");
                    errorResponse.put("message", "Authentication failed: " + ex.getMessage());
                    errorResponse.put("path", exchange.getRequest().getPath().value());

                    try {
                        String json = objectMapper.writeValueAsString(errorResponse);
                        return exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
                    } catch (JsonProcessingException e) {
                        log.error("Error creating JSON response", e);
                        String fallbackResponse = "{\"error\":\"Authentication failed\",\"status\":401}";
                        return exchange.getResponse().bufferFactory().wrap(fallbackResponse.getBytes(StandardCharsets.UTF_8));
                    }
                })
        );
    }
}
