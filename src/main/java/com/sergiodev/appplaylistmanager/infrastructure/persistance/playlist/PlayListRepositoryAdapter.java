package com.sergiodev.appplaylistmanager.infrastructure.persistance.playlist;

import com.sergiodev.appplaylistmanager.aaplication.port.out.PlaylistRepository;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class PlayListRepositoryAdapter implements PlaylistRepository {

    private final PlayListJpaRepository playListJpaRepository;
    private final PlaylistDataMapper playlistDataMapper;

    @Override
    @Transactional
    public Mono<Playlist> save(Playlist playlist) {
        return Mono.fromCallable(() -> playListJpaRepository.save(playlistDataMapper.toObject(playlist)))
            .subscribeOn(Schedulers.boundedElastic())
            .map(playlistDataMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNameIgnoreCase(String name) {
        return Mono.fromCallable(() -> playListJpaRepository.existsByNameIgnoreCase(name))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
