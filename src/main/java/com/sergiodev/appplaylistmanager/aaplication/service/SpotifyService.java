package com.sergiodev.appplaylistmanager.aaplication.service;

import com.sergiodev.appplaylistmanager.aaplication.port.out.SpotifyApiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class SpotifyService {
    private final SpotifyApiRepository spotifyApiRepository;

    public Flux<String> getMarkets() {
        return spotifyApiRepository.getMarketAvailable();
    }
}
