package com.sergiodev.appplaylistmanager.web.controller;

import com.sergiodev.appplaylistmanager.aaplication.service.SpotifyService;
import com.sergiodev.appplaylistmanager.web.util.StandardizeApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyControllerTest {

    @Mock
    private SpotifyService spotifyService;

    @InjectMocks
    private SpotifyController spotifyController;

    private String traceId;
    private List<String> markets;

    @BeforeEach
    void setUp() {
        traceId = "test-trace-id";
        markets = List.of("US", "CA", "GB", "DE", "FR", "ES", "IT", "AU", "JP", "BR");
    }

    @Test
    void getMarkets_ShouldReturnSuccessfulResponse() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(spotifyService.getMarkets()).thenReturn(Flux.fromIterable(markets));

            Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> result =
                spotifyController.getMarkets();

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(200, response.getBody().httpCode());
                    assertEquals(markets, response.getBody().data());
                    assertEquals(traceId, response.getBody().traceId());
                })
                .verifyComplete();

            verify(spotifyService).getMarkets();
            mdcMock.verify(() -> MDC.get("traceId"));
        }
    }

    @Test
    void getMarkets_ShouldHandleEmptyMarketsResponse() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(spotifyService.getMarkets()).thenReturn(Flux.empty());

            Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> result =
                spotifyController.getMarkets();

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(200, response.getBody().httpCode());
                    assertTrue(response.getBody().data().isEmpty());
                    assertEquals(traceId, response.getBody().traceId());
                })
                .verifyComplete();

            verify(spotifyService).getMarkets();
            mdcMock.verify(() -> MDC.get("traceId"));
        }
    }

    @Test
    void getMarkets_ShouldHandleNullTraceId() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(null);
            when(spotifyService.getMarkets()).thenReturn(Flux.fromIterable(markets));

            Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> result =
                spotifyController.getMarkets();

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(200, response.getBody().httpCode());
                    assertEquals(markets, response.getBody().data());
                    assertNull(response.getBody().traceId());
                })
                .verifyComplete();

            verify(spotifyService).getMarkets();
            mdcMock.verify(() -> MDC.get("traceId"));
        }
    }

    @Test
    void getMarkets_ShouldHandleServiceException() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(spotifyService.getMarkets()).thenReturn(Flux.error(new RuntimeException("Spotify API error")));

            Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> result =
                spotifyController.getMarkets();

            StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

            verify(spotifyService).getMarkets();
            mdcMock.verify(() -> MDC.get("traceId"));
        }
    }

    @Test
    void getMarkets_ShouldHandleSingleMarket() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            List<String> singleMarket = List.of("US");
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(spotifyService.getMarkets()).thenReturn(Flux.fromIterable(singleMarket));

            Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> result =
                spotifyController.getMarkets();

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(1, response.getBody().data().size());
                    assertEquals("US", response.getBody().data().get(0));
                })
                .verifyComplete();

            verify(spotifyService).getMarkets();
        }
    }

    @Test
    void getMarkets_ShouldHandleServiceNetworkException() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(spotifyService.getMarkets()).thenReturn(Flux.error(new IllegalStateException("Network timeout")));

            Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> result =
                spotifyController.getMarkets();

            StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();

            verify(spotifyService).getMarkets();
        }
    }

    @Test
    void getMarkets_ShouldHandleNullPointerException() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(spotifyService.getMarkets()).thenReturn(Flux.error(new NullPointerException("Null response")));

            Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> result =
                spotifyController.getMarkets();

            StepVerifier.create(result)
                .expectError(NullPointerException.class)
                .verify();

            verify(spotifyService).getMarkets();
        }
    }

    @Test
    void getMarkets_ShouldVerifyCollectListBehavior() {
        try (MockedStatic<MDC> mdcMock = mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("traceId")).thenReturn(traceId);
            when(spotifyService.getMarkets()).thenReturn(Flux.fromIterable(markets));

            Mono<ResponseEntity<StandardizeApiResponse<List<String>>>> result =
                spotifyController.getMarkets();

            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(markets.size(), response.getBody().data().size());
                    assertTrue(response.getBody().data().containsAll(markets));
                })
                .verifyComplete();

            verify(spotifyService, times(1)).getMarkets();
        }
    }
}