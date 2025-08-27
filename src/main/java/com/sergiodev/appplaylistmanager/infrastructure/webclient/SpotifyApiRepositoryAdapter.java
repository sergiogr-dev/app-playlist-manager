package com.sergiodev.appplaylistmanager.infrastructure.webclient;

import com.sergiodev.appplaylistmanager.aaplication.port.out.SpotifyApiRepository;
import com.sergiodev.appplaylistmanager.domain.exception.type.SpotifyException;
import com.sergiodev.appplaylistmanager.domain.model.SpotifyMarketsResponse;
import com.sergiodev.appplaylistmanager.infrastructure.webclient.service.SpotifyTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpotifyApiRepositoryAdapter implements SpotifyApiRepository {

    private final WebClient.Builder webClientBuilder;
    private final SpotifyTokenService spotifyTokenService;

    private static final String SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1";

    @Override
    public Flux<String> getMarketAvailable() {
        return spotifyTokenService.getAccessToken()
            .flatMap(tokenResponse ->
                webClientBuilder.baseUrl(SPOTIFY_API_BASE_URL)
                    .build()
                    .get()
                    .uri("/markets")
                    .header("Authorization", "Bearer " + tokenResponse.accessToken())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, handleClientErrorResponse())
                    .onStatus(HttpStatusCode::is5xxServerError, handleServerErrorResponse())
                    .bodyToMono(SpotifyMarketsResponse.class)
            )
            .flatMapMany(response -> Flux.fromIterable(response.markets()))
            .doOnError(error -> log.error("Error obteniendo mercados de Spotify: {}", error.getMessage()));
    }

    private Function<ClientResponse, Mono<? extends Throwable>> handleClientErrorResponse() {
        return clientResponse -> {
            log.error("Error de cliente: status={}", clientResponse.statusCode());
            if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                log.error("Acceso no autorizado al servicio de Spotify. Status: {}", clientResponse.statusCode());
                return Mono.error(SpotifyException.Type.SPOTIFY_SERVICE_CLIENT_EXCEPTION.build("Acceso no autorizado al servicio de Spotify"));
            }
            return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("Sin cuerpo de respuesta")
                .map(body -> {
                    log.error("Error de Cliente. Cuerpo de respuesta: {}", body);
                    return SpotifyException.Type.SPOTIFY_SERVICE_CLIENT_EXCEPTION.build(body);
                });
        };
    }

    private Function<ClientResponse, Mono<? extends Throwable>> handleServerErrorResponse() {
        return clientResponse -> {
            log.error("Error del servidor: status={}", clientResponse.statusCode());
            return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("Sin cuerpo de respuesta")
                .map(body -> {
                    log.error("Error del Servidor. Cuerpo de respuesta: {}", body);
                    return SpotifyException.Type.SPOTIFY_SERVICE_SERVER_EXCEPTION.build(body);
                });
        };
    }
}