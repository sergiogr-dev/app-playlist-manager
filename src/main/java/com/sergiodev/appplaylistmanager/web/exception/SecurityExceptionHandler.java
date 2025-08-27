package com.sergiodev.appplaylistmanager.web.exception;

import com.sergiodev.appplaylistmanager.domain.exception.InvalidTokenException;
import com.sergiodev.appplaylistmanager.domain.exception.TokenExpiredException;
import com.sergiodev.appplaylistmanager.domain.exception.UserNotActivatedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBadCredentials(
            BadCredentialsException ex, ServerWebExchange exchange) {
        
        log.error("Bad credentials: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUsernameNotFound(
            UsernameNotFoundException ex, ServerWebExchange exchange) {
        
        log.error("Username not found: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "User not found",
                "Invalid credentials", // Don't reveal that user doesn't exist
                exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleInvalidToken(
            InvalidTokenException ex, ServerWebExchange exchange) {
        
        log.error("Invalid token: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid token",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleTokenExpired(
            TokenExpiredException ex, ServerWebExchange exchange) {
        
        log.error("Token expired: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Token expired",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    @ExceptionHandler(UserNotActivatedException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUserNotActivated(
            UserNotActivatedException ex, ServerWebExchange exchange) {
        
        log.error("User not activated: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN,
                "User account not activated",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}
