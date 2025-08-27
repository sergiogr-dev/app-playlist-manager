package com.sergiodev.appplaylistmanager.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.sql.SQLException;

@Configuration
@Profile("local")
public class H2ConsoleConfig {

    private Server webServer;
    private Server tcpServer;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws SQLException {
        Server.createWebServer("-webPort", "8082", "-webAllowOthers").start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        if (tcpServer != null) tcpServer.stop();
        if (webServer != null) webServer.stop();
    }
}
