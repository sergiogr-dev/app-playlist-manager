package com.sergiodev.appplaylistmanager.config;

import org.h2.tools.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class H2ConsoleConfigTest {

    @Mock
    private Server mockServer;

    private H2ConsoleConfig h2ConsoleConfig;

    @BeforeEach
    void setUp() {
        h2ConsoleConfig = new H2ConsoleConfig();
    }

    @Test
    void start_ShouldStartWebServer() throws SQLException {
        try (var mockedServer = mockStatic(Server.class)) {
            mockedServer.when(() -> Server.createWebServer("-webPort", "8082", "-webAllowOthers"))
                .thenReturn(mockServer);

            h2ConsoleConfig.start();

            mockedServer.verify(() -> Server.createWebServer("-webPort", "8082", "-webAllowOthers"));
            verify(mockServer).start();
        }
    }

    @Test
    void stop_ShouldDoNothing_WhenServerIsNull() {
        h2ConsoleConfig.stop();

        verifyNoInteractions(mockServer);
    }
}