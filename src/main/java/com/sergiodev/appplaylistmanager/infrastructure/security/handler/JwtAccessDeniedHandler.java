package com.sergiodev.appplaylistmanager.infrastructure.security.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
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
public class JwtAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        log.error("Access denied: {} for request: {}", 
                denied.getMessage(), 
                exchange.getRequest().getPath().value());

        return exchange.getResponse().writeWith(
                Mono.fromCallable(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("timestamp", LocalDateTime.now().toString());
                    errorResponse.put("status", HttpStatus.FORBIDDEN.value());
                    errorResponse.put("error", "Forbidden");
                    errorResponse.put("message", "Access denied: " + denied.getMessage());
                    errorResponse.put("path", exchange.getRequest().getPath().value());

                    try {
                        String json = objectMapper.writeValueAsString(errorResponse);
                        return exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
                    } catch (JsonProcessingException e) {
                        log.error("Error creating JSON response", e);
                        String fallbackResponse = "{\"error\":\"Access denied\",\"status\":403}";
                        return exchange.getResponse().bufferFactory().wrap(fallbackResponse.getBytes(StandardCharsets.UTF_8));
                    }
                })
        );
    }
}
