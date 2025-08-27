package com.sergiodev.appplaylistmanager.config;

import com.sergiodev.appplaylistmanager.aaplication.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TokenCleanupScheduler tokenCleanupScheduler;

    @BeforeEach
    void setUp() {
        // No additional setup needed as all dependencies are mocked
    }

    @Test
    void cleanupExpiredTokens_ShouldCallAuthenticationService() {
        doNothing().when(authenticationService).cleanupExpiredTokens();

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(authenticationService, times(1)).cleanupExpiredTokens();
    }

    @Test
    void cleanupExpiredTokens_ShouldHandleSuccessfulExecution() {
        doNothing().when(authenticationService).cleanupExpiredTokens();

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(authenticationService).cleanupExpiredTokens();
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void cleanupExpiredTokens_ShouldHandleExceptionFromAuthenticationService() {
        RuntimeException exception = new RuntimeException("Database connection failed");
        doThrow(exception).when(authenticationService).cleanupExpiredTokens();

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(authenticationService).cleanupExpiredTokens();
    }

    @Test
    void cleanupExpiredTokens_ShouldNotThrowExceptionWhenAuthenticationServiceFails() {
        doThrow(new RuntimeException("Service error")).when(authenticationService).cleanupExpiredTokens();

        // Should not throw exception due to try-catch block
        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(authenticationService).cleanupExpiredTokens();
    }

    @Test
    void cleanupExpiredTokens_ShouldCallAuthenticationServiceOnlyOnce() {
        doNothing().when(authenticationService).cleanupExpiredTokens();

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(authenticationService, times(1)).cleanupExpiredTokens();
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void cleanupExpiredTokens_ShouldHandleNullPointerException() {
        doThrow(new NullPointerException("Null value encountered")).when(authenticationService).cleanupExpiredTokens();

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(authenticationService).cleanupExpiredTokens();
    }

    @Test
    void cleanupExpiredTokens_ShouldHandleGenericException() {
        doThrow(new RuntimeException("Generic error")).when(authenticationService).cleanupExpiredTokens();

        tokenCleanupScheduler.cleanupExpiredTokens();

        verify(authenticationService).cleanupExpiredTokens();
    }
}