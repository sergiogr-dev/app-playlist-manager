package com.sergiodev.appplaylistmanager.config;

import com.sergiodev.appplaylistmanager.aaplication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final AuthenticationService authenticationService;

    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired tokens");
        try {
            authenticationService.cleanupExpiredTokens();
            log.info("Scheduled cleanup of expired tokens completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup of expired tokens", e);
        }
    }
}
