package com.sergiodev.appplaylistmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OpenApiConfigTest {

    @InjectMocks
    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openApiConfig, "applicationName", "app-playlist-manager");
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithCorrectTitle() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("app-playlist-manager API");
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithCorrectDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getInfo().getDescription())
            .isEqualTo("REST API for Playlist Manager with OAuth2 JWT Authentication");
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithCorrectVersion() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithCorrectContactInfo() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact.getName()).isEqualTo("Sergio García");
        assertThat(contact.getEmail()).isEqualTo("sergio@example.com");
        assertThat(contact.getUrl()).isEqualTo("https://github.com/sergiogr-dev");
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithCorrectLicense() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        License license = openAPI.getInfo().getLicense();
        assertThat(license.getName()).isEqualTo("MIT License");
        assertThat(license.getUrl()).isEqualTo("https://opensource.org/licenses/MIT");
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithLocalServer() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Server localServer = openAPI.getServers().get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:8080");
        assertThat(localServer.getDescription()).isEqualTo("Local development server");
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithProductionServer() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Server prodServer = openAPI.getServers().get(1);
        assertThat(prodServer.getUrl()).isEqualTo("https://api.example.com");
        assertThat(prodServer.getDescription()).isEqualTo("Production server");
    }

    @Test
    void customOpenAPI_ShouldCreateOpenAPIWithBearerAuthentication() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertThat(securityRequirement.get("Bearer Authentication")).isNotNull();
    }

    @Test
    void customOpenAPI_ShouldHandleCustomApplicationName() {
        ReflectionTestUtils.setField(openApiConfig, "applicationName", "custom-app-name");

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("custom-app-name API");
    }

    @Test
    void customOpenAPI_ShouldCreateTwoServers() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getServers()).hasSize(2);
    }

    @Test
    void customOpenAPI_ShouldHaveOneSecurityRequirement() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getSecurity()).hasSize(1);
    }
}