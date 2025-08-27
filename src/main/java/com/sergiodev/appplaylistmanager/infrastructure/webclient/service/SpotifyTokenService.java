package com.sergiodev.appplaylistmanager.infrastructure.webclient.service;

import com.sergiodev.appplaylistmanager.domain.model.SpotifyTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpotifyTokenService {

    private final WebClient.Builder webClientBuilder;
    private final AtomicReference<SpotifyTokenResponse> cachedToken = new AtomicReference<>();

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String GRANT_TYPE = "client_credentials";

    public Mono<SpotifyTokenResponse> getAccessToken() {
        SpotifyTokenResponse currentToken = cachedToken.get();

        // Si hay un token válido en caché, lo devolvemos
        if (nonNull(currentToken) && !currentToken.isExpired()) {
            log.debug("Usando token en caché válido");
            return Mono.just(currentToken);
        }

        log.info("Token expirado o no existe, obteniendo nuevo token de Spotify");
        return requestNewToken()
            .doOnNext(newToken -> {
                cachedToken.set(newToken);
                log.info("Nuevo token obtenido y almacenado en caché, expira en {} segundos", newToken.expiresIn());
            });
    }

    private Mono<SpotifyTokenResponse> requestNewToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", GRANT_TYPE);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        return webClientBuilder.build()
            .post()
            .uri(TOKEN_URL)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(SpotifyTokenResponse.class)
            .map(response -> new SpotifyTokenResponse(
                response.accessToken(),
                response.tokenType(),
                response.expiresIn()
            ))
            .doOnError(error -> log.error("Error obteniendo nuevo token de Spotify: {}", error.getMessage()));
    }

    // Metodo para limpiar el caché manualmente si es necesario
    public void clearCache() {
        cachedToken.set(null);
        log.info("Caché de token limpiado");
    }
}