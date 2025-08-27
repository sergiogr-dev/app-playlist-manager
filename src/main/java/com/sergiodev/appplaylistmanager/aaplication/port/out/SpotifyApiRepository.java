package com.sergiodev.appplaylistmanager.aaplication.port.out;

import reactor.core.publisher.Flux;

public interface SpotifyApiRepository {
    Flux<String> getMarketAvailable();
}
