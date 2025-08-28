package com.sergiodev.appplaylistmanager.infrastructure.webclient.service;

import com.sergiodev.appplaylistmanager.domain.model.SpotifyTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyTokenServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private SpotifyTokenService spotifyTokenService;

    private SpotifyTokenResponse validToken;
    private SpotifyTokenResponse expiredToken;
    private String clientId;
    private String clientSecret;

    @BeforeEach
    void setUp() {
        clientId = "test-client-id";
        clientSecret = "test-client-secret";

        // Configurar propiedades @Value usando ReflectionTestUtils
        ReflectionTestUtils.setField(spotifyTokenService, "clientId", clientId);
        ReflectionTestUtils.setField(spotifyTokenService, "clientSecret", clientSecret);

        validToken = new SpotifyTokenResponse(
            "valid-access-token",
            "Bearer",
            3600
        );

        expiredToken = new SpotifyTokenResponse(
            "expired-access-token",
            "Bearer",
            -1
        );

        // Limpiar caché antes de cada test
        ReflectionTestUtils.setField(spotifyTokenService, "cachedToken", new AtomicReference<>());
    }

    @Test
    void getAccessToken_ShouldReturnCachedToken_WhenValidTokenExists() {
        // Configurar token válido en caché
        AtomicReference<SpotifyTokenResponse> cachedToken = new AtomicReference<>(validToken);
        ReflectionTestUtils.setField(spotifyTokenService, "cachedToken", cachedToken);

        Mono<SpotifyTokenResponse> result = spotifyTokenService.getAccessToken();

        StepVerifier.create(result)
            .assertNext(token -> {
                assertEquals(validToken.accessToken(), token.accessToken());
                assertEquals(validToken.tokenType(), token.tokenType());
                assertEquals(validToken.expiresIn(), token.expiresIn());
            })
            .verifyComplete();

        // Verificar que no se realizó llamada HTTP
        verifyNoInteractions(webClientBuilder);
    }

    @Test
    void getAccessToken_ShouldRequestNewToken_WhenNoTokenInCache() {
        setupWebClientMocks();
        when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.just(validToken));

        Mono<SpotifyTokenResponse> result = spotifyTokenService.getAccessToken();

        StepVerifier.create(result)
            .assertNext(token -> {
                assertEquals(validToken.accessToken(), token.accessToken());
                assertEquals(validToken.tokenType(), token.tokenType());
                assertEquals(validToken.expiresIn(), token.expiresIn());
            })
            .verifyComplete();

        verifyWebClientInteractions();
    }

    @Test
    void getAccessToken_ShouldRequestNewToken_WhenTokenIsExpired() {
        // Configurar token expirado en caché
        AtomicReference<SpotifyTokenResponse> cachedToken = new AtomicReference<>(expiredToken);
        ReflectionTestUtils.setField(spotifyTokenService, "cachedToken", cachedToken);

        setupWebClientMocks();
        when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.just(validToken));

        Mono<SpotifyTokenResponse> result = spotifyTokenService.getAccessToken();

        StepVerifier.create(result)
            .assertNext(token -> {
                assertEquals(validToken.accessToken(), token.accessToken());
                assertEquals(validToken.tokenType(), token.tokenType());
                assertEquals(validToken.expiresIn(), token.expiresIn());
            })
            .verifyComplete();

        verifyWebClientInteractions();
    }

    @Test
    void getAccessToken_ShouldHandleHttpError() {
        setupWebClientMocks();
        RuntimeException httpError = new RuntimeException("HTTP 401 Unauthorized");
        when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.error(httpError));

        Mono<SpotifyTokenResponse> result = spotifyTokenService.getAccessToken();

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verifyWebClientInteractions();
    }

    @Test
    void getAccessToken_ShouldHandleNetworkError() {
        setupWebClientMocks();
        RuntimeException networkError = new RuntimeException("Connection timeout");
        when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.error(networkError));

        Mono<SpotifyTokenResponse> result = spotifyTokenService.getAccessToken();

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verifyWebClientInteractions();
    }

    @Test
    void getAccessToken_ShouldCacheNewToken() {
        setupWebClientMocks();
        when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.just(validToken));

        Mono<SpotifyTokenResponse> result = spotifyTokenService.getAccessToken();

        StepVerifier.create(result)
            .assertNext(token -> assertEquals(validToken.accessToken(), token.accessToken()))
            .verifyComplete();

        // Verificar que el token se guardó en caché
        @SuppressWarnings("unchecked")
        AtomicReference<SpotifyTokenResponse> cachedToken =
            (AtomicReference<SpotifyTokenResponse>) ReflectionTestUtils.getField(spotifyTokenService, "cachedToken");

        assertNotNull(cachedToken.get());
        assertEquals(validToken.accessToken(), cachedToken.get().accessToken());
    }

    @Test
    void getAccessToken_ShouldUseCachedTokenOnSecondCall() {
        setupWebClientMocks();
        when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.just(validToken));

        // Primera llamada - debe hacer request HTTP
        Mono<SpotifyTokenResponse> firstResult = spotifyTokenService.getAccessToken();
        StepVerifier.create(firstResult)
            .assertNext(token -> {
                assertEquals(validToken.accessToken(), token.accessToken());
                assertEquals(validToken.tokenType(), token.tokenType());
                assertEquals(validToken.expiresIn(), token.expiresIn());
            })
            .verifyComplete();

        // Segunda llamada - debe usar caché
        Mono<SpotifyTokenResponse> secondResult = spotifyTokenService.getAccessToken();
        StepVerifier.create(secondResult)
            .assertNext(token -> {
                assertEquals(validToken.accessToken(), token.accessToken());
                assertEquals(validToken.tokenType(), token.tokenType());
                assertEquals(validToken.expiresIn(), token.expiresIn());
            })
            .verifyComplete();

        // Verificar que solo se hizo una llamada HTTP
        verify(webClientBuilder, times(1)).build();
    }

    @Test
    void clearCache_ShouldClearCachedToken() {
        // Configurar token en caché
        AtomicReference<SpotifyTokenResponse> cachedToken = new AtomicReference<>(validToken);
        ReflectionTestUtils.setField(spotifyTokenService, "cachedToken", cachedToken);

        // Verificar que hay token en caché
        assertNotNull(cachedToken.get());

        // Limpiar caché
        spotifyTokenService.clearCache();

        // Verificar que el caché está vacío
        assertNull(cachedToken.get());
    }

    @Test
    void clearCache_ShouldHandleEmptyCache() {
        // El caché ya está vacío por defecto
        @SuppressWarnings("unchecked")
        AtomicReference<SpotifyTokenResponse> cachedToken =
            (AtomicReference<SpotifyTokenResponse>) ReflectionTestUtils.getField(spotifyTokenService, "cachedToken");

        assertNull(cachedToken.get());

        // Limpiar caché no debería generar error
        assertDoesNotThrow(() -> spotifyTokenService.clearCache());

        // Verificar que sigue siendo null
        assertNull(cachedToken.get());
    }

    @Test
    void requestNewToken_ShouldCreateCorrectFormData() {
        try (MockedStatic<BodyInserters> bodyInsertersMock = mockStatic(BodyInserters.class)) {
            setupWebClientMocks();
            BodyInserters.FormInserter<String> mockFormInserter = mock(BodyInserters.FormInserter.class);

            bodyInsertersMock.when(() -> BodyInserters.fromFormData(any(MultiValueMap.class)))
                .thenReturn(mockFormInserter);

            when(requestBodySpec.body(mockFormInserter)).thenReturn(requestHeadersSpec);
            when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.just(validToken));

            Mono<SpotifyTokenResponse> result = spotifyTokenService.getAccessToken();

            StepVerifier.create(result)
                .assertNext(token -> {
                    assertEquals(validToken.accessToken(), token.accessToken());
                    assertEquals(validToken.tokenType(), token.tokenType());
                    assertEquals(validToken.expiresIn(), token.expiresIn());
                    // No comparar createdAt
                })
                .verifyComplete();

            // Verificar que se creó el form data con los datos correctos
            bodyInsertersMock.verify(() -> BodyInserters.fromFormData(argThat(formData -> {
                MultiValueMap<String, String> expectedData = new LinkedMultiValueMap<>();
                expectedData.add("grant_type", "client_credentials");
                expectedData.add("client_id", clientId);
                expectedData.add("client_secret", clientSecret);

                return formData.equals(expectedData);
            })));
        }
    }

    @Test
    void getAccessToken_ShouldHandleNullResponse() {
        setupWebClientMocks();
        when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.empty());

        Mono<SpotifyTokenResponse> result = spotifyTokenService.getAccessToken();

        StepVerifier.create(result)
            .verifyComplete();
    }

    private void setupWebClientMocks() {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("https://accounts.spotify.com/api/token")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE))
            .thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserters.FormInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(SpotifyTokenResponse.class)).thenReturn(Mono.just(validToken));
    }

    private void verifyWebClientInteractions() {
        verify(webClientBuilder).build();
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("https://accounts.spotify.com/api/token");
        verify(requestBodySpec).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        verify(requestBodySpec).body(any(BodyInserters.FormInserter.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(SpotifyTokenResponse.class);
    }
}