package com.sergiodev.appplaylistmanager.integration;

import com.sergiodev.appplaylistmanager.web.dto.AuthenticationRequest;
import com.sergiodev.appplaylistmanager.web.dto.AuthenticationResponse;
import com.sergiodev.appplaylistmanager.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;
    
    private static final String TEST_PASSWORD = "password123";
    private static final String ADMIN_USERNAME = "admin";
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password(TEST_PASSWORD)
                .firstName("Test")
                .lastName("User")
                .build();

        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthenticationResponse.class)
                .value(response -> {
                    assertThat(response.getAccessToken()).isNotNull();
                    assertThat(response.getRefreshToken()).isNotNull();
                    assertThat(response.getTokenType()).isEqualTo("Bearer");
                    assertThat(response.getExpiresIn()).isPositive();
                });
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(ADMIN_USERNAME)
                .password(TEST_PASSWORD)
                .build();

        webTestClient.post()
                .uri(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationResponse.class)
                .value(response -> {
                    assertThat(response.getAccessToken()).isNotNull();
                    assertThat(response.getRefreshToken()).isNotNull();
                    assertThat(response.getTokenType()).isEqualTo("Bearer");
                    assertThat(response.getExpiresIn()).isPositive();
                });
    }

    @Test
    void shouldFailAuthenticationWithInvalidCredentials() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(ADMIN_USERNAME)
                .password("wrongpassword")
                .build();

        webTestClient.post()
                .uri(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldVerifyValidToken() {
        // First, authenticate to get a token
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .username(ADMIN_USERNAME)
                .password(TEST_PASSWORD)
                .build();

        AuthenticationResponse authResponse = webTestClient.post()
                .uri(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationResponse.class)
                .returnResult()
                .getResponseBody();

        // Then verify the token
        webTestClient.get()
                .uri("/api/v1/auth/verify-token?token=" + authResponse.getAccessToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.valid").isEqualTo(true)
                .jsonPath("$.username").isEqualTo(ADMIN_USERNAME);
    }

    @Test
    void shouldAccessProtectedEndpointWithValidToken() {
        // First, authenticate to get a token
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .username(ADMIN_USERNAME)
                .password(TEST_PASSWORD)
                .build();

        AuthenticationResponse authResponse = webTestClient.post()
                .uri(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationResponse.class)
                .returnResult()
                .getResponseBody();

        // Then access protected endpoint
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .header("Authorization", "Bearer " + authResponse.getAccessToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.valid").isEqualTo(true)
                .jsonPath("$.username").isEqualTo(ADMIN_USERNAME);
    }

    @Test
    void shouldDenyAccessToProtectedEndpointWithoutToken() {
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

}
