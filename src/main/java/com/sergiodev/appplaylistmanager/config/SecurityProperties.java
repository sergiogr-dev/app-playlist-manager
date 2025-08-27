package com.sergiodev.appplaylistmanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    
    @Data
    public static class Jwt {
        private String privateKey;
        private String publicKey;
    }
    
    @Data
    public static class Cors {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private Boolean allowCredentials;
    }
}
