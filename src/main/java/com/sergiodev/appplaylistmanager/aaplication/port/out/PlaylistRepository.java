package com.sergiodev.appplaylistmanager.aaplication.port.out;

import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlaylistRepository {
    Mono<Playlist> save(Playlist playlist);
    Mono<Boolean> existsByNameIgnoreCase(String name);
    Mono<Playlist> findByName(String name);
    Flux<Playlist> findAll();
}
