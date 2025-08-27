package com.sergiodev.appplaylistmanager.infrastructure.security.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

@Slf4j
@UtilityClass
public class SecurityUtils {

    /**
     * Get the username of the current authenticated user.
     *
     * @return the username of the current user
     */
    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .map(UserDetails::getUsername)
                .doOnNext(username -> log.debug("Current authenticated user: {}", username))
                .doOnError(error -> log.error("Error getting current username", error));
    }

    /**
     * Get the current authenticated user details.
     *
     * @return the UserDetails of the current user
     */
    public static Mono<UserDetails> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .doOnNext(user -> log.debug("Current authenticated user details: {}", user.getUsername()))
                .doOnError(error -> log.error("Error getting current user details", error));
    }

    /**
     * Check if the current user is authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    public static Mono<Boolean> isAuthenticated() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::isAuthenticated)
                .defaultIfEmpty(false)
                .doOnNext(authenticated -> log.debug("User authentication status: {}", authenticated))
                .doOnError(error -> log.error("Error checking authentication status", error));
    }

    /**
     * Check if the current user has the specified role.
     *
     * @param role the role to check
     * @return true if user has the role, false otherwise
     */
    public static Mono<Boolean> hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix)))
                .defaultIfEmpty(false)
                .doOnNext(hasRole -> log.debug("User has role '{}': {}", role, hasRole))
                .doOnError(error -> log.error("Error checking role '{}'", role, error));
    }

    /**
     * Check if the current user has any of the specified roles.
     *
     * @param roles the roles to check
     * @return true if user has any of the roles, false otherwise
     */
    public static Mono<Boolean> hasAnyRole(String... roles) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    for (String role : roles) {
                        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        if (auth.getAuthorities().stream()
                                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix))) {
                            return true;
                        }
                    }
                    return false;
                })
                .defaultIfEmpty(false)
                .doOnNext(hasAnyRole -> log.debug("User has any of roles {}: {}", roles, hasAnyRole))
                .doOnError(error -> log.error("Error checking roles {}", roles, error));
    }
}
