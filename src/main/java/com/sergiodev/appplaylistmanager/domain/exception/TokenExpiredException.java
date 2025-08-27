package com.sergiodev.appplaylistmanager.domain.exception;

import org.springframework.security.core.AuthenticationException;

public class TokenExpiredException extends AuthenticationException {
    
    public TokenExpiredException(String message) {
        super(message);
    }
    
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
