package com.sergiodev.appplaylistmanager.aaplication.service;

import com.sergiodev.appplaylistmanager.aaplication.port.out.SpotifyApiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyServiceTest {

    @Mock
    private SpotifyApiRepository spotifyApiRepository;

    @InjectMocks
    private SpotifyService spotifyService;

    private Flux<String> marketsFlux;

    @BeforeEach
    void setUp() {
        marketsFlux = Flux.just("US", "ES", "FR");
    }

    @Test
    void getMarkets_Success() {
        when(spotifyApiRepository.getMarketAvailable()).thenReturn(marketsFlux);

        StepVerifier.create(spotifyService.getMarkets())
            .expectNext("US", "ES", "FR")
            .verifyComplete();

        verify(spotifyApiRepository).getMarketAvailable();
    }

    @Test
    void getMarkets_Empty() {
        when(spotifyApiRepository.getMarketAvailable()).thenReturn(Flux.empty());

        StepVerifier.create(spotifyService.getMarkets())
            .expectComplete()
            .verify();

        verify(spotifyApiRepository).getMarketAvailable();
    }

    @Test
    void getMarkets_Error() {
        when(spotifyApiRepository.getMarketAvailable()).thenReturn(Flux.error(new RuntimeException("API error")));

        StepVerifier.create(spotifyService.getMarkets())
            .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("API error"))
            .verify();

        verify(spotifyApiRepository).getMarketAvailable();
    }
}