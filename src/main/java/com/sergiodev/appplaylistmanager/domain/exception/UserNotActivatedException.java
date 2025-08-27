package com.sergiodev.appplaylistmanager.domain.exception;

import org.springframework.security.core.AuthenticationException;

public class UserNotActivatedException extends AuthenticationException {
    
    public UserNotActivatedException(String message) {
        super(message);
    }
    
    public UserNotActivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
