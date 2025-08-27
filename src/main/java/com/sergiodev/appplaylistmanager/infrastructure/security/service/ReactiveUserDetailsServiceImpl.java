package com.sergiodev.appplaylistmanager.infrastructure.security.service;

import com.sergiodev.appplaylistmanager.domain.exception.UserNotActivatedException;
import com.sergiodev.appplaylistmanager.domain.model.User;
import com.sergiodev.appplaylistmanager.infrastructure.persistance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        log.debug("Authenticating user: {}", username);
        
        return Mono.fromCallable(() -> userRepository.findByUsernameOrEmail(username)
                .map(this::createSpringSecurityUser)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' was not found in the database")))
                .doOnNext(userDetails -> log.debug("Found user: {}", userDetails.getUsername()))
                .doOnError(throwable -> log.error("Error finding user: {}", username, throwable))
            .subscribeOn(Schedulers.boundedElastic());
    }

    private UserDetails createSpringSecurityUser(User user) {
        if (!user.isEnabled()) {
            throw new UserNotActivatedException("User " + user.getUsername() + " was not activated");
        }

        log.debug("Creating UserDetails for user: {} with roles: {}", user.getUsername(), user.getRoles());
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .accountExpired(!user.isAccountNonExpired())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .authorities(user.getAuthorities())
                .build();
    }
}
