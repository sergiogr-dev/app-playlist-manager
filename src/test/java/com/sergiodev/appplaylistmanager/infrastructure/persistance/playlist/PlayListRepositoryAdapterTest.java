package com.sergiodev.appplaylistmanager.infrastructure.persistance.playlist;

import com.sergiodev.appplaylistmanager.domain.exception.type.CommonException;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayListRepositoryAdapterTest {

    @Mock
    private PlayListJpaRepository playListJpaRepository;
    @Mock
    private PlaylistDataMapper playlistDataMapper;

    @InjectMocks
    private PlayListRepositoryAdapter adapter;

    private Playlist playlist;
    private PlaylistEntity playlistEntity;

    @BeforeEach
    void setUp() {
        playlist = Playlist.builder()
            .id(1L)
            .name("Rock")
            .build();
        playlistEntity = PlaylistEntity.builder()
            .id(1L)
            .name("Rock")
            .build();
    }

    @Test
    void save_ShouldReturnSavedPlaylist() {
        when(playlistDataMapper.toObject(playlist)).thenReturn(playlistEntity);
        when(playListJpaRepository.save(playlistEntity)).thenReturn(playlistEntity);
        when(playlistDataMapper.toDomain(playlistEntity)).thenReturn(playlist);

        Mono<Playlist> result = adapter.save(playlist);

        StepVerifier.create(result)
            .expectNext(playlist)
            .verifyComplete();

        verify(playListJpaRepository).save(playlistEntity);
        verify(playlistDataMapper).toDomain(playlistEntity);
    }

    @Test
    void existsByNameIgnoreCase_ShouldReturnTrue() {
        when(playListJpaRepository.existsByNameIgnoreCase("Rock")).thenReturn(true);

        Mono<Boolean> result = adapter.existsByNameIgnoreCase("Rock");

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete();

        verify(playListJpaRepository).existsByNameIgnoreCase("Rock");
    }

    @Test
    void findByName_ShouldReturnPlaylist_WhenExists() {
        when(playListJpaRepository.findByName("Rock")).thenReturn(Optional.of(playlistEntity));
        when(playlistDataMapper.toDomain(playlistEntity)).thenReturn(playlist);

        Mono<Playlist> result = adapter.findByName("Rock");

        StepVerifier.create(result)
            .expectNext(playlist)
            .verifyComplete();

        verify(playListJpaRepository).findByName("Rock");
        verify(playlistDataMapper).toDomain(playlistEntity);
    }

    @Test
    void findByName_ShouldReturnError_WhenNotExists() {
        when(playListJpaRepository.findByName("Pop")).thenReturn(Optional.empty());

        Mono<Playlist> result = adapter.findByName("Pop");

        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof CommonException cEx &&
                                     cEx.getErrorType() == CommonException.Type.RESOURCE_NOT_FOUND)
            .verify();

        verify(playListJpaRepository).findByName("Pop");
    }

    @Test
    void findAll_ShouldReturnAllPlaylists() {
        List<PlaylistEntity> entities = List.of(playlistEntity);
        when(playListJpaRepository.findAll()).thenReturn(entities);
        when(playlistDataMapper.toDomain(playlistEntity)).thenReturn(playlist);

        Flux<Playlist> result = adapter.findAll();

        StepVerifier.create(result)
            .expectNext(playlist)
            .verifyComplete();

        verify(playListJpaRepository).findAll();
        verify(playlistDataMapper).toDomain(playlistEntity);
    }

    @Test
    void deleteByName_ShouldCompleteWithoutError() {
        doNothing().when(playListJpaRepository).deleteByName("Rock");

        Mono<Void> result = adapter.deleteByName("Rock");

        StepVerifier.create(result)
            .verifyComplete();

        verify(playListJpaRepository).deleteByName("Rock");
    }
}