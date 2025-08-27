package com.sergiodev.appplaylistmanager.infrastructure.persistance.playlist;

import com.sergiodev.appplaylistmanager.aaplication.port.out.PlaylistRepository;
import com.sergiodev.appplaylistmanager.domain.exception.type.CommonException;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

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

    @Override
    public Mono<Playlist> findByName(String name) {
        return Mono.fromCallable(() -> playListJpaRepository.findByName(name))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(playlistOptional -> validateIfExists(playlistOptional, name))
            .map(playlistDataMapper::toDomain);
    }

    private Mono<PlaylistEntity> validateIfExists(Optional<PlaylistEntity> playlistOptional, String name) {
        return playlistOptional.map(Mono::just)
            .orElseGet(() -> Mono.error(CommonException.Type.RESOURCE_NOT_FOUND.build(name)));
    }

    @Override
    public Flux<Playlist> findAll() {
        return Mono.fromCallable(playListJpaRepository::findAll)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(Flux::fromIterable)
            .map(playlistDataMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteByName(String name) {
        return Mono.fromRunnable(() -> playListJpaRepository.deleteByName(name))
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }
}
